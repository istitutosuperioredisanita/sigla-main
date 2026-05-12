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

package it.cnr.contab.prevent01.bp;

import it.cnr.contab.config00.pdcfin.cla.bulk.Parametri_livelliBulk;
import it.cnr.contab.prevent01.bulk.StampaMastroRendicontoFinanziarioBulk;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;

import java.util.Enumeration;
import java.util.Optional;


public class StampaMastroRendicontoFinanziarioBP extends ParametricPrintBP {

    /**
     *
     */
    private static final long serialVersionUID = -3023075061400467210L;

    /**
     * StampaRendicontoFinanziarioBP constructor comment.
     */
    public StampaMastroRendicontoFinanziarioBP() {
        super();
    }

    /**
     * StampaRendicontoFinanziarioBP constructor comment.
     *
     * @param function java.lang.String
     */
    public StampaMastroRendicontoFinanziarioBP(String function) {
        super(function);
    }

    @Override
    public OggettoBulk initializeBulkForPrint(ActionContext context, OggettoBulk bulk) throws BusinessProcessException {
        try {
            OggettoBulk oggettoBulk = super.initializeBulkForPrint(context, bulk);
            if (oggettoBulk instanceof StampaMastroRendicontoFinanziarioBulk) {
                ((StampaMastroRendicontoFinanziarioBulk) oggettoBulk).setEsercizio(CNRUserContext.getEsercizio(context.getUserContext()));
                ((StampaMastroRendicontoFinanziarioBulk) oggettoBulk).setTi_stampa(StampaMastroRendicontoFinanziarioBulk.TIPO_GESTIONALE);
                ((StampaMastroRendicontoFinanziarioBulk) oggettoBulk).setTi_aggregazione(StampaMastroRendicontoFinanziarioBulk.TIPO_FINANZIARIO);
                ((StampaMastroRendicontoFinanziarioBulk) oggettoBulk).setTi_origine(StampaMastroRendicontoFinanziarioBulk.TIPO_REALE);
                ((StampaMastroRendicontoFinanziarioBulk) oggettoBulk).setTi_gestione(StampaMastroRendicontoFinanziarioBulk.TIPO_GESTIONE_ENTRATA);
                ((StampaMastroRendicontoFinanziarioBulk) oggettoBulk).setTi_parte(StampaMastroRendicontoFinanziarioBulk.TIPO_PARTE_ENTRAMBE);
                ((StampaMastroRendicontoFinanziarioBulk) oggettoBulk).setLivello_stampa(StampaMastroRendicontoFinanziarioBulk.TIPO_VOCE);
                loadModelBulkOptions(context, (StampaMastroRendicontoFinanziarioBulk) oggettoBulk);
            }
            return oggettoBulk;
        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
    }

    public void loadModelBulkOptions(ActionContext context) throws BusinessProcessException {
        try {
            OggettoBulk oggettoBulk = this.getModel();
            if (oggettoBulk instanceof StampaMastroRendicontoFinanziarioBulk)
                loadModelBulkOptions(context, (StampaMastroRendicontoFinanziarioBulk) oggettoBulk);
        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
    }

    private StampaMastroRendicontoFinanziarioBulk loadModelBulkOptions(ActionContext context, StampaMastroRendicontoFinanziarioBulk stampa) throws BusinessProcessException {
        try {
            Parametri_livelliBulk parliv = Utility.createClassificazioneVociComponentSession().findParametriLivelli(context.getUserContext(), stampa.getEsercizio());

            it.cnr.jada.util.OrderedHashtable livelliOptions = new it.cnr.jada.util.OrderedHashtable();

            int index = 1;

            stampa.setVoce_a(null);
            stampa.setVoce_da(null);
            stampa.setVoce_a(null);
            stampa.setVoce_da(null);


            if (Optional.ofNullable(stampa)
                    .filter(StampaMastroRendicontoFinanziarioBulk::isTipoGestioneEntrata)
                    .isPresent()) {

                stampa.setTi_aggregazione(StampaMastroRendicontoFinanziarioBulk.TIPO_FINANZIARIO);

                if (parliv.getDs_livello1e() != null)
                    livelliOptions.put(index++, parliv.getDs_livello1e());
                if (parliv.getDs_livello2e() != null)
                    livelliOptions.put(index++, parliv.getDs_livello2e());
                if (parliv.getDs_livello3e() != null)
                    livelliOptions.put(index++, parliv.getDs_livello3e());
                if (parliv.getDs_livello4e() != null)
                    livelliOptions.put(index++, parliv.getDs_livello4e());
                if (parliv.getDs_livello5e() != null)
                    livelliOptions.put(index++, parliv.getDs_livello5e());
                if (parliv.getDs_livello6e() != null)
                    livelliOptions.put(index++, parliv.getDs_livello6e());
                if (parliv.getDs_livello7e() != null)
                    livelliOptions.put(index++, parliv.getDs_livello7e());
            } else {
                if (parliv.getDs_livello1s() != null)
                    livelliOptions.put(index++, parliv.getDs_livello1s());
                if (parliv.getDs_livello2s() != null)
                    livelliOptions.put(index++, parliv.getDs_livello2s());
                if (parliv.getDs_livello3s() != null)
                    livelliOptions.put(index++, parliv.getDs_livello3s());
                if (parliv.getDs_livello4s() != null)
                    livelliOptions.put(index++, parliv.getDs_livello4s());
                if (parliv.getDs_livello5s() != null)
                    livelliOptions.put(index++, parliv.getDs_livello5s());
                if (parliv.getDs_livello6s() != null)
                    livelliOptions.put(index++, parliv.getDs_livello6s());
                if (parliv.getDs_livello7s() != null)
                    livelliOptions.put(index++, parliv.getDs_livello7s());
            }

            stampa.setLivelliOptions(livelliOptions);
            if (stampa.getTi_livello() != null) {
                Enumeration a = livelliOptions.keys();
                while (a.hasMoreElements()) {
                    Integer key = (Integer) a.nextElement();
                    if (livelliOptions.get(key).equals(stampa.getTi_livello()))
                        return stampa;
                }
            }
            stampa.setTi_livello(livelliOptions.get(0).toString());


            // annullo selezioni voceDa e voceA

        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
        return stampa;
    }

    @Override
    public String getFormTitle() {
        return "Stampa Mastro Rendiconto Finanziario";
    }

    @Override
    public String getReportName() {
        return "/preventivo/preventivo/mastro.jasper";
    }
}
