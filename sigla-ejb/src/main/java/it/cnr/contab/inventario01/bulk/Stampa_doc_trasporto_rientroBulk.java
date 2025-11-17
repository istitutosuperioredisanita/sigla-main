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

package it.cnr.contab.inventario01.bulk;

public class Stampa_doc_trasporto_rientroBulk extends Doc_trasporto_rientroBulk {

    private java.lang.Integer pgInizio;
    private java.lang.Integer pgFine;

    private it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk uoForPrint;

    private boolean isUOForPrintEnabled;

    /**
     * Stampa_doc_trasporto_rientroBulk constructor comment.
     */
    public Stampa_doc_trasporto_rientroBulk() {
        super();
    }


    public Stampa_doc_trasporto_rientroBulk(Long pg_inventario, String ti_documento,
                                            Integer esercizio, Long pg_doc_trasporto_rientro) {
        super(pg_inventario, ti_documento, esercizio,pg_doc_trasporto_rientro);
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/01/2003 16.50.12)
     * @return java.lang.String
     */
    public String getCdUOCRForPrint() {
        return getUoForPrint().getCd_unita_organizzativa().toString();
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/05/2003 18.01.42)
     * @return java.lang.Integer
     */
    public java.lang.Integer getPgFine() {
        return pgFine;
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/05/2003 18.01.42)
     * @return java.lang.Integer
     */
    public java.lang.Integer getPgInizio() {
        return pgInizio;
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/05/2003 18.01.42)
     * @return it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk
     */
    public it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk getUoForPrint() {
        return uoForPrint;
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/12/2002 10.47.40)
     * @return boolean
     */
    public boolean isROUoForPrint() {
        return getUoForPrint()==null || getUoForPrint().getCrudStatus()==NORMAL;
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/05/2003 18.01.42)
     * @return boolean
     */
    public boolean isUOForPrintEnabled() {
        return isUOForPrintEnabled;
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/05/2003 18.01.42)
     * @param newIsUOForPrintEnabled boolean
     */
    public void setIsUOForPrintEnabled(boolean newIsUOForPrintEnabled) {
        isUOForPrintEnabled = newIsUOForPrintEnabled;
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/05/2003 18.01.42)
     * @param newPgFine java.lang.Integer
     */
    public void setPgFine(java.lang.Integer newPgFine) {
        pgFine = newPgFine;
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/05/2003 18.01.42)
     * @param newPgInizio java.lang.Integer
     */
    public void setPgInizio(java.lang.Integer newPgInizio) {
        pgInizio = newPgInizio;
    }

    /**
     * Insert the method's description here.
     * Creation date: (20/05/2003 18.01.42)
     * @param newUoForPrint it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk
     */
    public void setUoForPrint(it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk newUoForPrint) {
        uoForPrint = newUoForPrint;
    }

    public void validate() throws it.cnr.jada.bulk.ValidationException {
    }
}