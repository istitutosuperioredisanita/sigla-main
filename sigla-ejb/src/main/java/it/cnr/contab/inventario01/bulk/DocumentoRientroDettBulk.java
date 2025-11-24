/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.inventario01.bulk;

/**
 * Dettaglio (riga) di un documento di Trasporto/Rientro.
 * Le chiavi primarie composite (PK) sono delegate alle entità correlate (Testata e Bene).
 */
public  class DocumentoRientroDettBulk extends Doc_trasporto_rientro_dettBulk {

    // ========================================
    // ATTRIBUTI E RELAZIONI
    // ========================================

    /**
     * Bene inventariale associato a questa riga di dettaglio.
     * Delega le PK: nr_inventario, progressivo.
     */
    private DocumentoRientroBulk documentoRientro;


    public DocumentoRientroBulk getDocumentoRientro() {
        return documentoRientro;
    }

    public void setDocumentoRientro(DocumentoRientroBulk documentoRientro) {
        this.documentoRientro = documentoRientro;
    }

    /**
     * Testata del documento (FK obbligatoria).
     * Delega le PK: pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro.
     */

    // ========================================
    // COSTRUTTORI
    // ========================================

    public DocumentoRientroDettBulk() {
        super();
        // NON inizializzare campi delegati qui (logica JADA)
    }

    public DocumentoRientroDettBulk(Long pg_inventario, String ti_documento,
                                    Integer esercizio, Long pg_doc_trasporto_rientro,
                                    Long nr_inventario, Integer progressivo) {
        super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro,
                nr_inventario, progressivo);

        setDoc_trasporto_rientro(new DocumentoRientroBulk(pg_inventario,ti_documento,esercizio,pg_doc_trasporto_rientro));

    }

    @Override
    public Doc_trasporto_rientroBulk getDoc_trasporto_rientro() {
        return getDocumentoRientro();
    }

    @Override
    public void setDoc_trasporto_rientro(Doc_trasporto_rientroBulk bulk) {
        setDocumentoRientro((DocumentoRientroBulk) bulk);

    }


}