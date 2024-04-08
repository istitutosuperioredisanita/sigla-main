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

package it.cnr.contab.coepcoan00.core.bulk;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.util.enumeration.TipoIVA;
//import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
//import it.cnr.contab.utenze00.bp.CNRUserContext;

/**
 * Insert the type's description here.
 * Creation date: (08/08/2005 16.02.55)
 *
 * @author: Marco Spasiano
 */
public class Stampa_elenco_movimentiBulk extends it.cnr.jada.bulk.OggettoBulk {

    final public static String TIPO_TUTTO = "*";
    final public static String ATTIVA_SI = "Y";
    final public static String ATTIVA_NO = "N";
    final public static String ATTIVA_TUTTO = "*";
    private static final java.util.Dictionary ti_tipologiaKeys = new it.cnr.jada.util.OrderedHashtable();
    private static final java.util.Dictionary ti_attivaKeys = new it.cnr.jada.util.OrderedHashtable();

    static {
        for (TipoIVA tipoIVA : TipoIVA.values()) {
            ti_tipologiaKeys.put(tipoIVA.value(), tipoIVA.label());
        }
        ti_tipologiaKeys.put(TIPO_TUTTO, "Tutto");
    }

    static {
        ti_attivaKeys.put(ATTIVA_SI, "Si");
        ti_attivaKeys.put(ATTIVA_NO, "No");
        ti_attivaKeys.put(ATTIVA_TUTTO, "Tutte");
    }

    //	Esercizio di scrivania
    private Integer esercizio;
    private String tipologia;
    private it.cnr.contab.config00.sto.bulk.CdsBulk cdsForPrint;
    private it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk uoForPrint;
    private boolean cdsForPrintEnabled;
    private boolean uoForPrintEnabled;
    private String attiva;
    private TerzoBulk terzoForPrint;
    private ContoBulk contoForPrint;
    private boolean terzoForPrintEnabled;
    private boolean contoForPrintEnabled;
    private Boolean ragr_causale;
    private Boolean ragr_chiusura;
    private Boolean ragr_doc_amm;
    private Boolean ragr_doc_cont;
    private Boolean ragr_liquid_iva;
    private Boolean ragr_mig_beni;
    private Boolean ragr_stipendi;
    private Boolean ragr_manuale;

    /**
     * Stampa_elenco_movimentiBulk constructor comment.
     */
    public Stampa_elenco_movimentiBulk() {
        super();
    }

    /**
     * Insert the method's description here.
     * Creation date: (11/04/2005 12:34:48)
     *
     * @return java.util.Dictionary
     */
    public final java.util.Dictionary getti_tipologiaKeys() {
        return ti_tipologiaKeys;
    }

    public final java.util.Dictionary getti_attivaKeys() {
        return ti_attivaKeys;
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/01/2003 16.50.12)
     *
     * @return java.sql.Timestamp
     */
    public String getCdUoForPrint() {
        if (getUoForPrint() == null)
            return "*";
        if (getUoForPrint().getCd_unita_organizzativa() == null)
            return "*";
        return getUoForPrint().getCd_unita_organizzativa();
    }

    public String getCdCdsForPrint() {
        if (getCdsForPrint() == null)
            return "*";
        if (getCdsForPrint().getCd_unita_organizzativa() == null)
            return "*";
        return getCdsForPrint().getCd_unita_organizzativa();
    }

    public String getCdTerzoForPrint() {
        if (getTerzoForPrint() == null)
            return "*";
        if (getTerzoForPrint().getCd_terzo() == null)
            return "*";
        return getTerzoForPrint().getCd_terzo().toString();
    }


    /**
     * Inizializza gli attributi di ragruppamento
     *
     * @param bp      business process corrente
     * @param context contesto dell'Action che e' stata generata
     * @return OggettoBulk Stampa_elenco_movimentiBulk con i flag inizializzati
     */

    public void inizializzaRagruppamenti() {
        setRagr_causale(Boolean.FALSE);
        setRagr_chiusura(Boolean.FALSE);
        setRagr_doc_amm(Boolean.TRUE);
        setRagr_doc_cont(Boolean.FALSE);
        setRagr_liquid_iva(Boolean.FALSE);
        setRagr_mig_beni(Boolean.FALSE);
        setRagr_stipendi(Boolean.FALSE);
    }

    public void selezionaRagruppamenti() {
		setRagr_manuale(Boolean.valueOf(!getRagr_manuale().booleanValue()));
        setRagr_causale(Boolean.valueOf(!getRagr_causale().booleanValue()));
        setRagr_chiusura(Boolean.valueOf(!getRagr_chiusura().booleanValue()));
        setRagr_doc_amm(Boolean.valueOf(!getRagr_doc_amm().booleanValue()));
        setRagr_doc_cont(Boolean.valueOf(!getRagr_doc_cont().booleanValue()));
        setRagr_liquid_iva(Boolean.valueOf(!getRagr_liquid_iva().booleanValue()));
        setRagr_mig_beni(Boolean.valueOf(!getRagr_mig_beni().booleanValue()));
        setRagr_stipendi(Boolean.valueOf(!getRagr_stipendi().booleanValue()));
    }

    /**
     * @return
     */
    public Boolean getRagr_causale() {
        return ragr_causale;
    }

    /**
     * @param boolean1
     */
    public void setRagr_causale(Boolean boolean1) {
        ragr_causale = boolean1;
    }

    public Boolean getRagr_chiusura() {
        return ragr_chiusura;
    }

    public void setRagr_chiusura(Boolean boolean1) {
        ragr_chiusura = boolean1;
    }

    public Boolean getRagr_doc_amm() {
        return ragr_doc_amm;
    }

    public void setRagr_doc_amm(Boolean boolean1) {
        ragr_doc_amm = boolean1;
    }

    public Boolean getRagr_doc_cont() {
        return ragr_doc_cont;
    }

    public void setRagr_doc_cont(Boolean boolean1) {
        ragr_doc_cont = boolean1;
    }

    public Boolean getRagr_liquid_iva() {
        return ragr_liquid_iva;
    }

    public void setRagr_liquid_iva(Boolean boolean1) {
        ragr_liquid_iva = boolean1;
    }

    public Boolean getRagr_mig_beni() {
        return ragr_mig_beni;
    }

    public void setRagr_mig_beni(Boolean boolean1) {
        ragr_mig_beni = boolean1;
    }

    public Boolean getRagr_stipendi() {
        return ragr_stipendi;
    }

    public void setRagr_stipendi(Boolean boolean1) {
        ragr_stipendi = boolean1;
    }

    /**
     * Insert the method's description here.
     * Creation date: (14/05/2003 9.28.52)
     *
     * @return java.lang.Integer
     */
    public java.lang.Integer getEsercizio() {
        return esercizio;
    }

    /**
     * Insert the method's description here.
     * Creation date: (14/05/2003 9.28.52)
     *
     * @param newEsercizio java.lang.Integer
     */


    public void setEsercizio(java.lang.Integer newEsercizio) {
        esercizio = newEsercizio;
    }

    /**
     * Insert the method's description here.
     * Creation date: (23/01/2003 12.00.24)
     *
     * @return it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk
     */
    public it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk getUoForPrint() {
        return uoForPrint;
    }

    /**
     * Insert the method's description here.
     * Creation date: (23/01/2003 12.00.24)
     *
     * @return it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk
     */
    public void setUoForPrint(it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk unitaOrganizzativa) {
        this.uoForPrint = unitaOrganizzativa;
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/12/2002 10.47.40)
     *
     * @param newCdUOEmittente java.lang.String
     */
    public boolean isROUoForPrint() {
        return !isUoForPrintEnabled();
    }

    public boolean isROCdsForPrint() {
        return !isCdsForPrintEnabled();
    }

    public boolean isROContoForPrint() {
        return getContoForPrint() == null || getContoForPrint().getCrudStatus() == NORMAL;
    }

    /**
     * @return
     */
    public it.cnr.contab.config00.sto.bulk.CdsBulk getCdsForPrint() {
        return cdsForPrint;
    }

    /**
     * Insert the method's description here.
     * Creation date: (19/05/2003 15.45.26)
     *
     * @param newCdsForPrint java.lang.String
     */
    public void setCdsForPrint(it.cnr.contab.config00.sto.bulk.CdsBulk newCdsForPrint) {
        cdsForPrint = newCdsForPrint;
    }

    /**
     * @return
     */
    public String gettipologia() {
        return tipologia;
    }

    public String getattiva() {
        return attiva;
    }

    /**
     * @param string
     */
    public void settipologia(String string) {
        tipologia = string;
    }

    public void setattiva(String string) {
        attiva = string;
    }


    /**
     * @return
     */
    public TerzoBulk getTerzoForPrint() {
        return terzoForPrint;
    }

    /**
     * @param bulk
     */
    public void setTerzoForPrint(TerzoBulk bulk) {
        terzoForPrint = bulk;
    }

    /**
     * @return
     */
    public ContoBulk getContoForPrint() {
        return contoForPrint;
    }

    /**
     * @param bulk
     */
    public void setContoForPrint(ContoBulk bulk) {
        contoForPrint = bulk;
    }

    /**
     * @return
     */
    public boolean isCdsForPrintEnabled() {
        return cdsForPrintEnabled;
    }

    /**
     * @param b
     */
    public void setCdsForPrintEnabled(boolean b) {
        cdsForPrintEnabled = b;
    }

    /**
     * @return
     */
    public boolean isUoForPrintEnabled() {
        return uoForPrintEnabled;
    }

    /**
     * @param b
     */
    public void setUoForPrintEnabled(boolean b) {
        uoForPrintEnabled = b;
    }

    public Boolean getRagr_manuale() {
        return ragr_manuale;
    }

    public void setRagr_manuale(Boolean ragr_manuale) {
        this.ragr_manuale = ragr_manuale;
    }
}
