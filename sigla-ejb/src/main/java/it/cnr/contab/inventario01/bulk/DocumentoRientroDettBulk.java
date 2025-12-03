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
 * Dettaglio (riga) di un documento di RIENTRO.
 * Estende Doc_trasporto_rientro_dettBulk specificando il tipo di documento.
 */
public class DocumentoRientroDettBulk extends Doc_trasporto_rientro_dettBulk {

    // ========================================
    // ATTRIBUTI E RELAZIONI
    // ========================================
    private DocumentoRientroBulk documentoRientro;
    private DocumentoTrasportoDettBulk docTrasportoDettRif;

    public DocumentoTrasportoDettBulk getDocTrasportoDettRif() {
        return docTrasportoDettRif;
    }

    public void setDocTrasportoDettRif(DocumentoTrasportoDettBulk docTrasportoDettRif) {
        this.docTrasportoDettRif = docTrasportoDettRif;
    }

    // ========================================
    // COSTRUTTORI
    // ========================================

    public DocumentoRientroDettBulk() {
        super();
    }

    public DocumentoRientroDettBulk(Long pg_inventario, String ti_documento,
                                    Integer esercizio, Long pg_doc_trasporto_rientro,
                                    Long nr_inventario, Integer progressivo) {
        super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro,
                nr_inventario, progressivo);

        setDoc_trasporto_rientro(new DocumentoRientroBulk(pg_inventario, ti_documento,
                esercizio, pg_doc_trasporto_rientro));
    }

    // ========================================
    // GETTER E SETTER - Documento Rientro
    // ========================================

    public DocumentoRientroBulk getDocumentoRientro() {
        return documentoRientro;
    }

    public void setDocumentoRientro(DocumentoRientroBulk documentoRientro) {
        this.documentoRientro = documentoRientro;
    }



    // ========================================
    // OVERRIDE - Metodi astratti della classe padre
    // ========================================

    @Override
    public Doc_trasporto_rientroBulk getDoc_trasporto_rientro() {
        return getDocumentoRientro();
    }


    @Override
    public void setDoc_trasporto_rientro(Doc_trasporto_rientroBulk bulk) {
        setDocumentoRientro((DocumentoRientroBulk) bulk);
    }

    @Override
    public Doc_trasporto_rientro_dettBulk getDoc_trasporto_rientroDettRif() {
        return docTrasportoDettRif;
    }

    @Override
    public void setDoc_trasporto_rientroDettRif(Doc_trasporto_rientro_dettBulk bulk) {
        setDocTrasportoDettRif((DocumentoTrasportoDettBulk) bulk);
    }

    //
    // ========================================
    // GETTER E SETTER - Campi FK Delegati
    // ========================================

    // Delega: PG_INVENTARIO

    public void setPgInventarioRif(Long pg_inventario) {
        if (this.getDoc_trasporto_rientroDettRif() != null) {
            this.getDoc_trasporto_rientroDettRif().setPgInventario(pg_inventario);
        }
    }

    public Long getPgInventarioRif() {
        return this.getDoc_trasporto_rientroDettRif() == null ? null : this.getDoc_trasporto_rientroDettRif().getDoc_trasporto_rientro().getPgInventario();
    }

    // Delega: NR_INVENTARIO
    public Long getNrInventarioRif() {
        if (Optional.ofNullable(this.getDoc_trasporto_rientroDettRif()).isPresent() && Optional.ofNullable(this.getDoc_trasporto_rientroDettRif().getBene()).isPresent())
            return this.getDoc_trasporto_rientroDettRif().getBene().getNr_inventario();
        return null;
    }

    public void setNrInventarioRif(Long nr_inventario) {
        if (Optional.ofNullable(this.getDoc_trasporto_rientroDettRif()).isPresent() && Optional.ofNullable(this.getDoc_trasporto_rientroDettRif().getBene()).isPresent())
            this.getDoc_trasporto_rientroDettRif().getBene().setNr_inventario(nr_inventario);
    }

    // Delega: PROGRESSIVO (mappato come Long in Inventario_beniBulk)
    public Integer getProgressivoRif() {
        if (Optional.ofNullable(this.getDoc_trasporto_rientroDettRif()).isPresent() && Optional.ofNullable(this.getDoc_trasporto_rientroDettRif().getBene()).isPresent())
            return new Integer(this.getBene().getProgressivo().intValue());
        return null;
    }

    public void setProgressivoRif(Integer progressivoRif) {
        if (Optional.ofNullable(this.getDoc_trasporto_rientroDettRif()).isPresent() && Optional.ofNullable(this.getDoc_trasporto_rientroDettRif().getBene()).isPresent())
            this.getDoc_trasporto_rientroDettRif().getBene().setProgressivo(new Long(progressivoRif.longValue()));
    }

    // Delega: TI_DOCUMENTO
    public void setTiDocumentoRif(String ti_documento_rif) {
        if (this.getDoc_trasporto_rientro() != null) {
            this.getDoc_trasporto_rientro().setTiDocumento(ti_documento_rif);
        }
    }

    public String getTiDocumentoRif() {
        if ( Optional.ofNullable(getDoc_trasporto_rientroDettRif()).isPresent())
            return getDoc_trasporto_rientroDettRif().getTiDocumento();
        return null;
    }

    // Delega: ESERCIZIO
    public void setEsercizioRif(Integer esercizioRif) {
        if ( Optional.ofNullable(getDoc_trasporto_rientroDettRif()).isPresent())
            getDoc_trasporto_rientroDettRif().setEsercizio(esercizioRif);

    }

    public Integer getEsercizioRif() {
        if ( Optional.ofNullable(getDoc_trasporto_rientroDettRif()).isPresent())
            getDoc_trasporto_rientroDettRif().getEsercizio();
        return null;
    }

    // Delega: PG_DOC_TRASPORTO_RIENTRO
    public void setPgDocTrasportoRientroRif(Long pgDocTrasportoRientroRif) {
        if (getDoc_trasporto_rientroDettRif() != null) {
            getDoc_trasporto_rientroDettRif().setPgDocTrasportoRientro(pgDocTrasportoRientroRif);
        }
    }

    public Long getPgDocTrasportoRientroRif() {
        if ( Optional.ofNullable(getDoc_trasporto_rientro()).isPresent())
            return (( DocumentoTrasportoDettBulk)getDoc_trasporto_rientroDettRif()).getDocumentoTrasporto().getPgDocTrasportoRientro();
        return null;

    }


}