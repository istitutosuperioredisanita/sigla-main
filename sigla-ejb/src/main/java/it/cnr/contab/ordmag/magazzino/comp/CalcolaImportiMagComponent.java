package it.cnr.contab.ordmag.magazzino.comp;

import it.cnr.contab.docamm00.tabrif.bulk.Voce_ivaBulk;


import it.cnr.contab.ordmag.ordini.dto.ImportoOrdine;

import it.cnr.contab.ordmag.ordini.dto.ParametriCalcoloImportoOrdine;
import it.cnr.contab.util.Utility;
import it.cnr.jada.comp.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;

public class CalcolaImportiMagComponent extends CRUDComponent {


    public ImportoOrdine calcoloImportoPerMagazzino(ParametriCalcoloImportoOrdine parametri) throws ApplicationException {
        BigDecimal imponibile = calcoloImponibile(parametri);
        imponibile = imponibile.divide(parametri.getQtaOrd());
        BigDecimal arrotondamento = Utility.nvl(parametri.getArrAliIva()).divide(parametri.getQtaOrd());
        BigDecimal arrAliIva = arrotondamento;

        Voce_ivaBulk voceIva = null;
        if (parametri.getVoceIvaRet() != null && parametri.getVoceIvaRet().getPercentuale() != null) {
            voceIva = parametri.getVoceIvaRet();
        } else {
            voceIva = parametri.getVoceIva();
        }

        BigDecimal importoIva = Utility.divide(imponibile.multiply(voceIva.getPercentuale()), Utility.CENTO, 6);
        BigDecimal ivaNonDetraibile = importoIva.multiply((Utility.CENTO.subtract(voceIva.getPercentuale_detraibilita())));

        BigDecimal ivaPerCalcoloProrata = importoIva.subtract(ivaNonDetraibile);

        BigDecimal ivaDetraibile = ivaPerCalcoloProrata.multiply(Utility.nvl(parametri.getPercProrata()));

        ivaNonDetraibile = ivaNonDetraibile.add((ivaPerCalcoloProrata.subtract(ivaDetraibile)));

        if (ivaDetraibile.compareTo(BigDecimal.ZERO) == 0 || ivaNonDetraibile.compareTo(BigDecimal.ZERO) > 0) {
            ivaNonDetraibile = ivaNonDetraibile.add(Utility.nvl(parametri.getArrAliIva()));
        } else {
            ivaDetraibile = ivaDetraibile.add(Utility.nvl(parametri.getArrAliIva()));
        }
        importoIva = importoIva.add(ivaDetraibile);
        ImportoOrdine importoOrdine = new ImportoOrdine();
        importoOrdine.setImponibile(Utility.round6Decimali(imponibile));
        importoOrdine.setImportoIva(Utility.round6Decimali(importoIva));
        importoOrdine.setImportoIvaInd(Utility.round6Decimali(ivaNonDetraibile));
        importoOrdine.setImportoIvaDetraibile(Utility.round6Decimali(ivaDetraibile));
        importoOrdine.setArrAliIva(arrAliIva);
        return importoOrdine;
    }
    public ImportoOrdine calcoloImporto(ParametriCalcoloImportoOrdine parametri) throws ComponentException{
        BigDecimal imponibile = calcoloImponibile(parametri);
        Voce_ivaBulk voceIva = null;
        if (parametri.getVoceIvaRet() != null && parametri.getVoceIvaRet().getPercentuale() != null) {
            voceIva = parametri.getVoceIvaRet();
        } else {
            voceIva = parametri.getVoceIva();
        }
        ImportoOrdine importo = new ImportoOrdine();
        importo = calcoloDettagliImporti(importo, parametri, imponibile, voceIva, false);
        if (parametri.getImponibileErratoPerNotaCredito() != null) {
            importo = calcoloDettagliImporti(importo, parametri, parametri.getImponibileErratoPerNotaCredito(), voceIva, true);
        }
        return importo;
    }
    public BigDecimal calcoloImponibile(ParametriCalcoloImportoOrdine parametri) throws ApplicationException {
        BigDecimal prezzo = Utility.nvl(parametri.getPrezzoRet(), parametri.getPrezzo());
        BigDecimal cambio = Utility.nvl(parametri.getCambioRet(), parametri.getCambio());
        if (parametri.getDivisa() == null || parametri.getDivisaRisultato() == null ||
                parametri.getDivisa().getCd_divisa() == null || parametri.getDivisaRisultato().getCd_divisa() == null) {
            throw new it.cnr.jada.comp.ApplicationException("E' necessario indicare le divise.");
        }
        if (!parametri.getDivisa().getCd_divisa().equals(parametri.getDivisaRisultato().getCd_divisa())) {
            if (parametri.getDivisaRisultato().getFl_calcola_con_diviso().booleanValue())
                prezzo = Utility.divide(prezzo, cambio, 6);
            else
                prezzo = prezzo.multiply(cambio).setScale(2, RoundingMode.HALF_UP);

        }
        BigDecimal sconto1 = Utility.nvl(Utility.nvl(parametri.getSconto1Ret(), parametri.getSconto1()));
        BigDecimal sconto2 = Utility.nvl(Utility.nvl(parametri.getSconto2Ret(), parametri.getSconto2()));
        BigDecimal sconto3 = Utility.nvl(Utility.nvl(parametri.getSconto3Ret(), parametri.getSconto3()));
        BigDecimal prezzoScontato = prezzo.
                multiply(BigDecimal.ONE.subtract(sconto1.divide(Utility.CENTO))).
                multiply(BigDecimal.ONE.subtract(sconto2.divide(Utility.CENTO))).
                multiply(BigDecimal.ONE.subtract(sconto3.divide(Utility.CENTO)));
        BigDecimal imponibile = prezzoScontato.multiply(parametri.getQtaOrd());
        return imponibile;
    }

    private ImportoOrdine calcoloDettagliImporti(ImportoOrdine importo, ParametriCalcoloImportoOrdine parametri, BigDecimal imponibile, Voce_ivaBulk voceIva, Boolean calcoloTotaliPerNotaCredito) {
        BigDecimal importoIva = Utility.round6Decimali((Utility.divide(imponibile, Utility.CENTO, 6)).multiply(voceIva.getPercentuale()));
        BigDecimal ivaNonDetraibile = Utility.round6Decimali(importoIva.multiply((Utility.CENTO.subtract(voceIva.getPercentuale_detraibilita()))));
        BigDecimal ivaPerCalcoloProrata = importoIva.subtract(ivaNonDetraibile);
        BigDecimal ivaDetraibile = Utility.round6Decimali(ivaPerCalcoloProrata.multiply(Utility.nvl(parametri.getPercProrata())));
        ivaNonDetraibile = ivaNonDetraibile.add((ivaPerCalcoloProrata.subtract(ivaDetraibile)));

        if (ivaDetraibile.compareTo(BigDecimal.ZERO) == 0 || ivaNonDetraibile.compareTo(BigDecimal.ZERO) > 0) {
            ivaNonDetraibile = ivaNonDetraibile.add(Utility.nvl(parametri.getArrAliIva()));
        } else {
            ivaDetraibile = ivaDetraibile.add(Utility.nvl(parametri.getArrAliIva()));
        }
        importoIva = importoIva.add(ivaDetraibile);
        if (calcoloTotaliPerNotaCredito) {
            importo.setImponibilePerNotaCredito(Utility.round2Decimali(imponibile));
            importo.setImportoIvaPerNotaCredito(Utility.round2Decimali(importoIva));
            importo.setImportoIvaIndPerNotaCredito(Utility.round2Decimali(ivaNonDetraibile));
            importo.setImportoIvaDetraibilePerNotaCredito(Utility.round2Decimali(ivaDetraibile));
        } else {
            importo.setImponibile(Utility.round2Decimali(imponibile));
            importo.setImportoIva(Utility.round2Decimali(importoIva));
            importo.setImportoIvaInd(Utility.round2Decimali(ivaNonDetraibile));
            importo.setImportoIvaDetraibile(Utility.round2Decimali(ivaDetraibile));
            importo.setArrAliIva(BigDecimal.ZERO);
        }
        return importo;
    }

 }
