/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.ordmag.magazzino.bulk;

import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.ordmag.anag00.RaggrMagazzinoBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.action.CRUDBP;

import java.sql.Timestamp;
import java.util.Dictionary;

/**
 * Insert the type's description here.
 * Creation date: (23/01/2003 16.02.55)
 *
 * @author: Roberto Fantino
 */
public class Chiusura_magazzinoBulk extends Stampa_inventarioBulk {

    public final static String CALCOLO_RIMANENZE = "C";
    public final static String VALORIZZAZIONE_RIMANENZE = "V";

    public final static String RAGGR_REPORT_CAT_GRUPPO = "G";
    public final static String RAGGR_REPORT_ARTICOLO = "A";

    public final static String VALORIZZAZIONE_RIM_CMP = "CMP";

    public final static Dictionary ti_operazioneKeys;
    public final static Dictionary ti_valorizzazioneKeys;
    public final static Dictionary ti_raggr_reportKeys;

    private Integer esercizio;
    private RaggrMagazzinoBulk raggrMagazzino = new RaggrMagazzinoBulk();
    private Timestamp dataInventarioInizio;
    private Timestamp dataChiusuraMovimento;
    private boolean calcoloRimanenze = true;
    private java.lang.String ti_operazione;
    private java.lang.String ti_valorizzazione;
    private String ti_raggr_report;

    private boolean nascondiCampiStampa = true;
    private boolean bloccaCampiCalcoloDefinitivo;

    private String tipoChiusura=ChiusuraAnnoBulk.TIPO_CHIUSURA_MAGAZZINO;
    private String tipoReport;

    private it.cnr.jada.util.OrderedHashtable anniList = new it.cnr.jada.util.OrderedHashtable();


    static {
        ti_operazioneKeys = new it.cnr.jada.util.OrderedHashtable();
        ti_operazioneKeys.put(CALCOLO_RIMANENZE, "Calcolo Rimanenze");
        ti_operazioneKeys.put(VALORIZZAZIONE_RIMANENZE, "Valorizzazione Rimanenze");

        ti_valorizzazioneKeys = new it.cnr.jada.util.OrderedHashtable();
        ti_valorizzazioneKeys.put(VALORIZZAZIONE_RIM_CMP, "C.M.P.");

        ti_raggr_reportKeys = new it.cnr.jada.util.OrderedHashtable();
        ti_raggr_reportKeys.put(RAGGR_REPORT_CAT_GRUPPO, "Categoria Gruppo");
        ti_raggr_reportKeys.put(RAGGR_REPORT_ARTICOLO, "Codice Articolo");

    }

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public java.util.Dictionary getTi_operazioneKeys() {
        return ti_operazioneKeys;
    }

    public java.util.Dictionary getTi_valorizzazioneKeys() {
        return ti_valorizzazioneKeys;
    }

    public boolean isCalcoloRimanenze() {
        return calcoloRimanenze;
    }

    public void setCalcoloRimanenze(boolean calcoloRimanenze) {
        this.calcoloRimanenze = calcoloRimanenze;
    }

    public String getTi_operazione() {
        return ti_operazione;
    }

    public void setTi_operazione(String ti_operazione) {
        this.ti_operazione = ti_operazione;
    }

    public String getTi_valorizzazione() {
        return ti_valorizzazione;
    }

    public void setTi_valorizzazione(String ti_valorizzazione) {
        this.ti_valorizzazione = ti_valorizzazione;
    }


    public RaggrMagazzinoBulk getRaggrMagazzino() {
        return raggrMagazzino;
    }

    public void setRaggrMagazzino(RaggrMagazzinoBulk raggrMagazzino) {
        this.raggrMagazzino = raggrMagazzino;
    }

    public Timestamp getDataInventarioInizio() {
        return dataInventarioInizio;
    }

    public void setDataInventarioInizio(Timestamp dataInventarioInizio) {
        this.dataInventarioInizio = dataInventarioInizio;
    }

    public void validate() throws it.cnr.jada.bulk.ValidationException {

        if (getEsercizio() == null)
            throw new it.cnr.jada.bulk.ValidationException("Imposta l'esercizio!");

        if (getDataInventario() == null)
            throw new it.cnr.jada.bulk.ValidationException("Imposta la data di riferimento!");

        if (getRaggrMagazzino() == null)
            throw new it.cnr.jada.bulk.ValidationException("Imposta il raggruppamento magazzino!");

        if (getTi_raggr_report() == null )
            throw new it.cnr.jada.bulk.ValidationException("Selezionare il raggruppamento per il report");

       /* if (getOrdinamento() == null || getOrdinamento().trim().isEmpty())
            throw new it.cnr.jada.bulk.ValidationException("Selezionare l'ordinamento!");
        if ( getTi_operazione()==null) {
            throw new it.cnr.jada.bulk.ValidationException("Selezionare il tipo operazione!");
        }else if(getTi_operazione().equals(VALORIZZAZIONE_RIMANENZE)){
            if(getTi_valorizzazione() == null){
                throw new it.cnr.jada.bulk.ValidationException("Impostare il tipo di calcolo!");
            }
        }*/

    }

    public String getCdCatGrpForPrint() {
        if (this.getCatgrp() == null)
            return Valori_magazzinoBulk.TUTTI;
        if (this.getCatgrp().getCd_categoria_gruppo() == null)
            return Valori_magazzinoBulk.TUTTI;

        return this.getCatgrp().getLivello().equals(Integer.valueOf(0))  ? this.getCatgrp().getCd_proprio():this.getCatgrp().getCd_categoria_padre();
    }

    public String getCdRaggrMagazzinoForPrint() {
        if (this.getRaggrMagazzino() == null)
            return Valori_magazzinoBulk.TUTTI;
        if (this.getRaggrMagazzino().getCdRaggrMagazzino() == null)
            return Valori_magazzinoBulk.TUTTI;

        return this.getRaggrMagazzino().getCdRaggrMagazzino();
    }
    public String getCdCdsRaggrMagazzinoForPrint() {
        if (this.getRaggrMagazzino() == null)
            return Valori_magazzinoBulk.TUTTI;
        if (this.getRaggrMagazzino().getCdRaggrMagazzino() == null)
            return Valori_magazzinoBulk.TUTTI;

        return this.getRaggrMagazzino().getCdCds();
    }

    public String getCdTipoOperazioneForPrint() {
        return this.getTi_operazione();
    }

    public String getCdTipoValorizForPrint() {
        return this.getTi_valorizzazione();
    }

    public Timestamp getDataChiusuraMovimento() {
        return dataChiusuraMovimento;
    }

    public void setDataChiusuraMovimento(Timestamp dataChiusuraMovimento) {
        this.dataChiusuraMovimento = dataChiusuraMovimento;
    }

    public boolean isNascondiCampiStampa() {
        return nascondiCampiStampa;
    }

    public void setNascondiCampiStampa(boolean nascondiCampiStampa) {
        this.nascondiCampiStampa = nascondiCampiStampa;
    }

    public String getTipoChiusura() {
        return tipoChiusura;
    }

    public String getTi_raggr_report() {
        return ti_raggr_report;
    }

    public void setTi_raggr_report(String ti_raggr_report) {
        this.ti_raggr_report = ti_raggr_report;
    }

    public boolean isBloccaCampiCalcoloDefinitivo() {
        return bloccaCampiCalcoloDefinitivo;
    }

    public void setBloccaCampiCalcoloDefinitivo(boolean bloccaCampiCalcoloDefinitivo) {
        this.bloccaCampiCalcoloDefinitivo = bloccaCampiCalcoloDefinitivo;
    }

    public String getTipoReport() {
        return tipoReport;
    }

    public void setTipoReport(String tipoReport) {
        this.tipoReport = tipoReport;
    }

    public void setTipoChiusura(String tipoChiusura) {

        this.tipoChiusura = tipoChiusura;
    }

    public OrderedHashtable getAnniList() {
        return anniList;
    }

    public void setAnniList(OrderedHashtable anniList) {
        this.anniList = anniList;
    }
    public void caricaAnniList(ActionContext actioncontext) {
        caricaAnniList(actioncontext.getUserContext());
    }
    public void caricaAnniList(UserContext usercontext) {
        for (int i = CNRUserContext.getEsercizio(usercontext).intValue(); i>=2023; i--)
            getAnniList().put(Integer.valueOf(i), Integer.valueOf(i));
    }
    public OggettoBulk initialize(CRUDBP crudbp, ActionContext actioncontext) {
        super.initialize(crudbp, actioncontext);
        caricaAnniList(actioncontext);
        return this;
    }
    public OggettoBulk initializeForEdit(CRUDBP crudbp, ActionContext actioncontext) {
        caricaAnniList(actioncontext);
        return super.initializeForEdit(crudbp, actioncontext);
    }

    @Override
    public OggettoBulk initializeForPrint(BulkBP bulkBP, ActionContext actioncontext) {
        caricaAnniList(actioncontext);
        return super.initializeForPrint(bulkBP, actioncontext);
    }
}


