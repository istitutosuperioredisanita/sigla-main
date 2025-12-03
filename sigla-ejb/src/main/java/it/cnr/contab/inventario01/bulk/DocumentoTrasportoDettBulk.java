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

import java.util.Optional;

/**
 * Dettaglio (riga) di un documento di TRASPORTO.
 * Estende Doc_trasporto_rientro_dettBulk specificando il tipo di documento.
 */
public class DocumentoTrasportoDettBulk extends Doc_trasporto_rientro_dettBulk {

    // ========================================
    // ATTRIBUTI E RELAZIONI
    // ========================================

    private DocumentoTrasportoBulk documentoTrasporto;
    private DocumentoRientroDettBulk docRientroDettRif;

    // ========================================
    // COSTRUTTORI
    // ========================================

    public DocumentoTrasportoDettBulk() {
        super();
    }

    public DocumentoTrasportoDettBulk(Long pg_inventario, String ti_documento,
                                      Integer esercizio, Long pg_doc_trasporto_rientro,
                                      Long nr_inventario, Integer progressivo) {
        super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro,
                nr_inventario, progressivo);

        setDocumentoTrasporto(new DocumentoTrasportoBulk(pg_inventario, ti_documento,
                esercizio, pg_doc_trasporto_rientro));
    }

    // ========================================
    // GETTER E SETTER
    // ========================================

    public DocumentoTrasportoBulk getDocumentoTrasporto() {
        return documentoTrasporto;
    }

    public void setDocumentoTrasporto(DocumentoTrasportoBulk documentoTrasporto) {
        this.documentoTrasporto = documentoTrasporto;
    }

    public DocumentoRientroDettBulk getDocRientroDettRif() {
        return docRientroDettRif;
    }

    public void setDocRientroDettRif(DocumentoRientroDettBulk docRientroDettRif) {
        this.docRientroDettRif = docRientroDettRif;
    }

    // ========================================
    // OVERRIDE - Metodi astratti della classe padre
    // ========================================

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

    // ========================================
    // GETTER E SETTER - Campi FK Delegati per Riferimento
    // ========================================

    // Delega: PG_INVENTARIO
    public void setPgInventarioRif(Long pg_inventario) {
        if (this.getDoc_trasporto_rientroDettRif() != null) {
            this.getDoc_trasporto_rientroDettRif().setPg_inventario(pg_inventario);
        }
    }

    public Long getPgInventarioRif() {
        return this.getDoc_trasporto_rientroDettRif() == null ? null
                : this.getDoc_trasporto_rientroDettRif().getPg_inventario();
    }

    // Delega: NR_INVENTARIO
    public Long getNrInventarioRif() {
        if (Optional.ofNullable(this.getDoc_trasporto_rientroDettRif()).isPresent()
                && Optional.ofNullable(this.getDoc_trasporto_rientroDettRif().getBene()).isPresent())
            return this.getDoc_trasporto_rientroDettRif().getBene().getNr_inventario();
        return null;
    }

    public void setNrInventarioRif(Long nr_inventario) {
        if (Optional.ofNullable(this.getDoc_trasporto_rientroDettRif()).isPresent()
                && Optional.ofNullable(this.getDoc_trasporto_rientroDettRif().getBene()).isPresent())
            this.getDoc_trasporto_rientroDettRif().getBene().setNr_inventario(nr_inventario);
    }

    // Delega: PROGRESSIVO
    public Integer getProgressivoRif() {
        if (Optional.ofNullable(this.getDoc_trasporto_rientroDettRif()).isPresent()
                && Optional.ofNullable(this.getDoc_trasporto_rientroDettRif().getBene()).isPresent())
            return new Integer(this.getDoc_trasporto_rientroDettRif().getBene().getProgressivo().intValue());
        return null;
    }

    public void setProgressivoRif(Integer progressivoRif) {
        if (Optional.ofNullable(this.getDoc_trasporto_rientroDettRif()).isPresent()
                && Optional.ofNullable(this.getDoc_trasporto_rientroDettRif().getBene()).isPresent())
            this.getDoc_trasporto_rientroDettRif().getBene().setProgressivo(new Long(progressivoRif.longValue()));
    }

    // Delega: TI_DOCUMENTO
    public String getTiDocumentoRif() {
        if (Optional.ofNullable(getDoc_trasporto_rientroDettRif()).isPresent())
            return getDoc_trasporto_rientroDettRif().getTi_documento();
        return null;
    }

    public void setTiDocumentoRif(String ti_documento_rif) {
        if (Optional.ofNullable(getDoc_trasporto_rientroDettRif()).isPresent())
            getDoc_trasporto_rientroDettRif().setTi_documento(ti_documento_rif);
    }

    // Delega: ESERCIZIO
    public Integer getEsercizioRif() {
        if (Optional.ofNullable(getDoc_trasporto_rientroDettRif()).isPresent())
            return getDoc_trasporto_rientroDettRif().getEsercizio();
        return null;
    }

    public void setEsercizioRif(Integer esercizioRif) {
        if (Optional.ofNullable(getDoc_trasporto_rientroDettRif()).isPresent())
            getDoc_trasporto_rientroDettRif().setEsercizio(esercizioRif);
    }

    // Delega: PG_DOC_TRASPORTO_RIENTRO
    public Long getPgDocTrasportoRientroRif() {
        if (Optional.ofNullable(getDoc_trasporto_rientroDettRif()).isPresent())
            return getDoc_trasporto_rientroDettRif().getPg_doc_trasporto_rientro();
        return null;
    }

    public void setPgDocTrasportoRientroRif(Long pgDocTrasportoRientroRif) {
        if (Optional.ofNullable(getDoc_trasporto_rientroDettRif()).isPresent())
            getDoc_trasporto_rientroDettRif().setPg_doc_trasporto_rientro(pgDocTrasportoRientroRif);
    }
}