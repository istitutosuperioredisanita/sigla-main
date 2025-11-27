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
public  class DocumentoTrasportoDettBulk extends Doc_trasporto_rientro_dettBulk {

    // ========================================
    // ATTRIBUTI E RELAZIONI
    // ========================================

    /**
     * Bene inventariale associato a questa riga di dettaglio.
     * Delega le PK: nr_inventario, progressivo.
     */
    private DocumentoTrasportoBulk documentoTrasporto;

    /**
     * Riferimento al documento di TRASPORTO (dettaglio)
     */

    private DocumentoRientroDettBulk docRientroDettRif;

    public DocumentoRientroDettBulk getDocRientroDettRif() {
        return docRientroDettRif;
    }

    public void setDocRientroDettRif(DocumentoRientroDettBulk docRientroDettRif) {
        this.docRientroDettRif = docRientroDettRif;
    }

    public DocumentoTrasportoBulk getDocumentoTrasporto() {
        return documentoTrasporto;
    }

    public void setDocumentoTrasporto(DocumentoTrasportoBulk documentoTrasporto) {
        this.documentoTrasporto = documentoTrasporto;
    }



    /**
     * Testata del documento (FK obbligatoria).
     * Delega le PK: pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro.
     */

    // ========================================
    // COSTRUTTORI
    // ========================================

    public DocumentoTrasportoDettBulk() {
        super();
        // NON inizializzare campi delegati qui (logica JADA)
    }

    public DocumentoTrasportoDettBulk(Long pg_inventario, String ti_documento,
                                      Integer esercizio, Long pg_doc_trasporto_rientro,
                                      Long nr_inventario, Integer progressivo) {
        super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro,
                nr_inventario, progressivo);

        setDocumentoTrasporto(new DocumentoTrasportoBulk(pg_inventario,ti_documento,esercizio,pg_doc_trasporto_rientro));

    }

    @Override
    public Doc_trasporto_rientroBulk getDoc_trasporto_rientro() {
        return getDocumentoTrasporto();
    }

    @Override
    public void setDoc_trasporto_rientro(Doc_trasporto_rientroBulk bulk) {
        setDocumentoTrasporto((DocumentoTrasportoBulk) bulk);
    }

    @Override
    public Doc_trasporto_rientro_dettBulk getDoc_trasporto_rientroDettRif() {
        return getDocRientroDettRif();
    }

    @Override
    public void setDoc_trasporto_rientroDettRif(Doc_trasporto_rientro_dettBulk bulk) {
        setDocRientroDettRif((DocumentoRientroDettBulk) bulk);
    }


}