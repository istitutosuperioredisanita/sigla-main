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

package it.cnr.contab.progettiric00.bp;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;

import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.progettiric00.core.bulk.ProgettoBulk;
import it.cnr.contab.progettiric00.ejb.ProgettoRicercaComponentSession;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;


public class EstraiProgettiDaRiportareBP extends it.cnr.jada.util.action.ConsultazioniBP {

    private static final long serialVersionUID = 1L;
    private Integer esercizio;

    public EstraiProgettiDaRiportareBP() {
    }

    public EstraiProgettiDaRiportareBP(String s) {
        super(s);
    }

    protected void init(it.cnr.jada.action.Config config,it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
        try {

            super.init(config, context);
            setFreeSearchSet("estrazione_riporta_progetti");
            setColumns(getBulkInfo().getColumnFieldPropertyDictionary("estrazione_riporta_progetti"));
        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
    }
    public String getFormTitle() {
        return "Estrazione Progetti da Riportare";
    }
    public boolean isUoEnte(ActionContext context){
        Unita_organizzativaBulk uo = it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa(context);
        if (uo.getCd_tipo_unita().equals(it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome.TIPO_UO_ENTE))
            return true;
        return false;
    }
    public void openIterator(it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
        try	{

            Unita_organizzativaBulk uo = it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa(context);
            Integer esercizio = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(context.getUserContext());

            ProgettoBulk progetto = (ProgettoBulk) getBulkInfo().getBulkClass().newInstance();
            progetto.setEsercizio(esercizio);
            progetto.setUnita_organizzativa(uo);
            progetto.setDt_inizio(primoGennaio(esercizio));
            progetto.setDt_fine(trentunoDicembre(esercizio));
            progetto.setTipo_fase(ProgettoBulk.TIPO_FASE_NON_DEFINITA);
            progetto.setLivello(ProgettoBulk.LIVELLO_PROGETTO_SECONDO);

            CompoundFindClause clauses = new CompoundFindClause();

            if (!uo.getCd_tipo_unita().equals(it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome.TIPO_UO_ENTE))
            {
                clauses.addClause("AND", "CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, uo.getCd_unita_organizzativa());
            }

            setIterator(context,getComponentSession().getProgettiDaRiportare(context.getUserContext(),  progetto,clauses));
        }catch(Throwable e) {
            throw new BusinessProcessException(e);
        }
    }

    public Timestamp primoGennaio(Integer anno) {
        return Timestamp.valueOf(
                LocalDate.of(anno, 1, 1).atStartOfDay()
        );
    }
    public Timestamp trentunoDicembre(Integer anno) {
        return Timestamp.valueOf(
                LocalDate.of(anno, 12, 31).atStartOfDay()
        );
    }
    protected ProgettoRicercaComponentSession getComponentSession() {
        return Utility.createProgettoRicercaComponentSession();

    }

}