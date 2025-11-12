/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

/**
 * Rappresenta un allegato associato a un dettaglio di documento di Trasporto/Rientro.
 * Questa classe estende AllegatoDocTrasportoRientroBulk per aggiungere il riferimento
 * specifico al dettaglio (Doc_trasporto_rientro_dettBulk).
 */
public class AllegatoDocTraspRientDettaglioBulk extends AllegatoDocTraspRientBulk {

    /**
     * Riferimento al dettaglio del documento di trasporto/rientro
     */
    private Doc_trasporto_rientro_dettBulk dettaglio;

    /**
     * Flag che indica se il dettaglio è stato aggiunto
     */
    private Boolean isDetailAdded = false;

    // ========================================
    // COSTRUTTORI
    // ========================================

    /**
     * Costruttore di default
     */
    public AllegatoDocTraspRientDettaglioBulk() {
        super();
    }

    /**
     * Costruttore con storage key
     *
     * @param storageKey la chiave di storage dell'allegato
     */
    public AllegatoDocTraspRientDettaglioBulk(String storageKey) {
        super(storageKey);
    }

    // ========================================
    // GETTER E SETTER - Dettaglio
    // ========================================

    /**
     * Restituisce il dettaglio del documento associato all'allegato
     *
     * @return il dettaglio del documento
     */
    public Doc_trasporto_rientro_dettBulk getDettaglio() {
        return dettaglio;
    }

    /**
     * Imposta il dettaglio del documento associato all'allegato
     *
     * @param dettaglio il dettaglio del documento
     */
    public void setDettaglio(Doc_trasporto_rientro_dettBulk dettaglio) {
        this.dettaglio = dettaglio;
    }

    /**
     * Metodo alias per setDettaglio() - usato per coerenza con il naming pattern
     *
     * @param dettaglio il dettaglio del documento
     */
    public void setDocTrasportoRientroDettaglio(Doc_trasporto_rientro_dettBulk dettaglio) {
        setDettaglio(dettaglio);
    }

    /**
     * Metodo alias per getDettaglio() - usato per coerenza con il naming pattern
     *
     * @return il dettaglio del documento
     */
    public Doc_trasporto_rientro_dettBulk getDocTrasportoRientroDettaglio() {
        return getDettaglio();
    }

    // ========================================
    // GETTER E SETTER - Flag
    // ========================================

    /**
     * Restituisce il flag che indica se il dettaglio è stato aggiunto
     *
     * @return true se il dettaglio è stato aggiunto, false altrimenti
     */
    public Boolean getIsDetailAdded() {
        return isDetailAdded;
    }

    /**
     * Imposta il flag che indica se il dettaglio è stato aggiunto
     *
     * @param isDetailAdded true se il dettaglio è stato aggiunto, false altrimenti
     */
    public void setIsDetailAdded(Boolean isDetailAdded) {
        this.isDetailAdded = isDetailAdded;
    }

    // ========================================
    // METODI UTILITY
    // ========================================

    /**
     * Restituisce la testata del documento tramite il dettaglio.
     * Questo metodo è utile per accedere alla testata quando si lavora con un allegato del dettaglio.
     *
     * @return la testata del documento, o null se il dettaglio non è impostato
     */
    public Doc_trasporto_rientroBulk getDocTrasportoRientro() {
        if (dettaglio != null) {
            return dettaglio.getDoc_trasporto_rientro();
        }
        // Se non c'è dettaglio, prova a recuperare dalla classe parent
        return super.getDocTrasportoRientro();
    }

    /**
     * Imposta la testata del documento.
     * Questo metodo override il metodo della classe parent per gestire
     * sia il riferimento diretto che quello tramite dettaglio.
     *
     * @param docTrasportoRientro la testata del documento
     */
    @Override
    public void setDocTrasportoRientro(Doc_trasporto_rientroBulk docTrasportoRientro) {
        super.setDocTrasportoRientro(docTrasportoRientro);
    }

    /**
     * Verifica se l'allegato ha un dettaglio associato
     *
     * @return true se c'è un dettaglio associato, false altrimenti
     */
    public boolean hasDettaglio() {
        return dettaglio != null;
    }

    /**
     * Verifica se l'allegato ha una testata associata (direttamente o tramite dettaglio)
     *
     * @return true se c'è una testata associata, false altrimenti
     */
    public boolean hasTestata() {
        return getDocTrasportoRientro() != null;
    }
}