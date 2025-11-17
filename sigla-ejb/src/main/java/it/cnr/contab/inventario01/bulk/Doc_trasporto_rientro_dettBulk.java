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

import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_voceBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Condizione_beneBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Ubicazione_beneBulk;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util.enumeration.TipoIVA;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.jada.bulk.BulkCollection;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.StrServ;

import java.rmi.RemoteException;
import java.util.Optional;

/**
 * Dettaglio documento Trasporto/Rientro.
 * <p>
 * PATTERN: Delega completa alle entità correlate
 * - I campi PK (pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro)
 * sono delegati a doc_trasporto_rientro
 * - I campi PK (nr_inventario, progressivo) sono delegati a bene
 * <p>
 * RELAZIONI DATABASE:
 * - FK verso DOC_TRASPORTO_RIENTRO (testata) → OBBLIGATORIA, ON DELETE CASCADE
 * - FK verso DOC_TRASPORTO_RIENTRO_DETT (riferimento) → per i rientri
 */
public class Doc_trasporto_rientro_dettBulk extends Doc_trasporto_rientro_dettBase {

    // ========================================
    // ATTRIBUTI
    // ========================================

    /**
     * Bene associato al dettaglio.
     * Contiene nr_inventario e progressivo (parte della PK).
     */
    private Inventario_beniBulk bene;

    /**
     * TESTATA del documento (FOREIGN KEY OBBLIGATORIA).
     * Contiene pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro (parte della PK).
     * Relazione master-detail con ON DELETE CASCADE.
     */
    private Doc_trasporto_rientroBulk doc_trasporto_rientro;


    // Altri attributi
    private int gruppi;
    private Boolean fl_accessorio_contestuale = Boolean.FALSE;
    protected Boolean fl_bene_accessorio;
    private Categoria_gruppo_voceBulk cat_voce;


    // ========================================
    // COSTRUTTORI
    // ========================================

    public Doc_trasporto_rientro_dettBulk() {
        super();
        // NON inizializzare nulla qui - lascia che JADA usi la classe base
    }

    public Doc_trasporto_rientro_dettBulk(Long pg_inventario, String ti_documento,
                                          Integer esercizio, Long pg_doc_trasporto_rientro,
                                          Long nr_inventario, Integer progressivo) {
        super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro,
                nr_inventario, progressivo);

        // Inizializza SOLO quando costruisci manualmente
        setDoc_trasporto_rientro(new Doc_trasporto_rientroBulk(
                pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro));
        setBene(new Inventario_beniBulk(nr_inventario, pg_inventario,
                Long.valueOf(progressivo)));
    }

    // ========================================
    // GETTER E SETTER - Oggetti Relazionati
    // ========================================

    public Inventario_beniBulk getBene() {
        return bene;
    }

    public void setBene(Inventario_beniBulk bulk) {
        bene = bulk;
    }

    public Doc_trasporto_rientroBulk getDoc_trasporto_rientro() {
        return doc_trasporto_rientro;
    }

    public void setDoc_trasporto_rientro(Doc_trasporto_rientroBulk bulk) {
        doc_trasporto_rientro = bulk;
    }


    // ========================================
    // GETTER E SETTER - Campi Delegati
    // ========================================

    public void setPg_inventario(Long pg_inventario) {
        this.getDoc_trasporto_rientro().setPgInventario(pg_inventario);
    }

    public Long getPg_inventario() {
        return this.getDoc_trasporto_rientro().getPgInventario();
    }

    public Long getNr_inventario() {
        return this.getBene().getNr_inventario();
    }

    public void setNr_inventario(Long nr_inventario) {
        this.getBene().setNr_inventario(nr_inventario);
    }

    public Integer getProgressivo() {
        return new Integer(this.getBene().getProgressivo().intValue());
    }

    public void setProgressivo(Integer progressivo) {
        this.getBene().setProgressivo(new Long(progressivo.longValue()));
    }

    public void setTi_documento(String ti_documento) {
        this.getDoc_trasporto_rientro().setTiDocumento(ti_documento);
    }

    public String getTi_documento() {
        return this.getDoc_trasporto_rientro().getTiDocumento();
    }

    public void setEsercizio(Integer esercizio) {
        this.getDoc_trasporto_rientro().setEsercizio(esercizio);
    }

    public Integer getEsercizio() {
        return this.getDoc_trasporto_rientro().getEsercizio();
    }


    public void setPg_doc_trasporto_rientro(Long pg_doc_trasporto_rientro) {
        this.getDoc_trasporto_rientro().setPgDocTrasportoRientro(pg_doc_trasporto_rientro);
    }

    public Long getPg_doc_trasporto_rientro() {
        return this.getDoc_trasporto_rientro().getPgDocTrasportoRientro();
    }

    // ========================================
    // METODI UTILITY - Codifiche
    // ========================================

    /**
     * Restituisce il codice del bene formato come "NR_INVENTARIO-PROGRESSIVO".
     * Il progressivo viene formattato con 3 cifre (es: 001, 002, etc.)
     */
    public String getCod_bene() {
        if (getNr_inventario() == null || getProgressivo() == null) {
            return "";
        }
        java.text.DecimalFormat formato = new java.text.DecimalFormat("000");
        return getNr_inventario().toString() + "-" + formato.format(getProgressivo());
    }

    /**
     * Restituisce una chiave hash univoca per il dettaglio.
     * Formato: "NR_INVENTARIO.PROGRESSIVO.ETICHETTA"
     */
    public String getChiaveHash() {
        if (getNr_inventario() == null || getProgressivo() == null) {
            return null;
        }
        return getNr_inventario().toString() + "." + getProgressivo().toString() + "." + getEtichetta();
    }

    // ========================================
    // METODI UTILITY - Validazioni
    // ========================================

    //TODO creare un metodo isValidoPerRientro()

    /**
     * Verifica se il bene è totalmente scaricato.
     */
    public boolean isTotalmenteScaricato() {
        return bene != null && Boolean.TRUE.equals(bene.getFl_totalmente_scaricato());
    }

    /**
     * Verifica se il bene è in transito.
     */
    public boolean isBeneInTransito() {
        return bene != null && bene.getId_transito_beni_ordini() != null;
    }

    // ========================================
    // METODI DI INIZIALIZZAZIONE
    // ========================================

    @Override
    public OggettoBulk initialize(it.cnr.jada.util.action.CRUDBP bp, it.cnr.jada.action.ActionContext context) {
        bene = new Inventario_beniBulk();
        bene.setTi_commerciale_istituzionale(TipoIVA.ISTITUZIONALE.value());
        return this;
    }

    @Override
    public OggettoBulk initializeForInsert(it.cnr.jada.util.action.CRUDBP bp, it.cnr.jada.action.ActionContext context) {
        bene = new Inventario_beniBulk();
        bene.setTi_commerciale_istituzionale(TipoIVA.ISTITUZIONALE.value());

        try {
            if (Utility.createConfigurazioneCnrComponentSession()
                    .isGestioneEtichettaInventarioBeneAttivo(context.getUserContext())) {
                setQuantita(1L);
            }
        } catch (RemoteException | ComponentException e) {
            // Log error se necessario
        }

        return this;
    }

    // ========================================
    // ACCESSORI E ETICHETTE
    // ========================================

    public boolean isAccessorioContestuale() {
        return Boolean.TRUE.equals(fl_accessorio_contestuale);
    }

    public boolean isAssociatoConAccessorioContestuale() {
        Doc_trasporto_rientroBulk doc = getDoc_trasporto_rientro();
        if (getChiaveHash() == null || doc == null || doc.getAccessoriContestualiHash() == null) {
            return false;
        }
        return doc.getAccessoriContestualiHash().containsKey(getChiaveHash());
    }

    public boolean isROsearchTool() {
        return isBeneAccessorio();
    }

    public boolean isROcategoriaBene() {
        return isAccessorioContestuale();
    }

    public boolean isROcollocazione() {
        return bene != null && bene.getCategoria_Bene() != null;
    }

    public boolean isROfl_accessorio() {
        return isAccessorioContestuale();
    }

    public boolean isROEtichetta() {
        return isBeneAccessorio();
    }

    /**
     * Restituisce l'etichetta del bene.
     * Se il bene è accessorio, restituisce l'etichetta del bene principale.
     */
    public String getEtichetta() {
        if (isBeneAccessorio() || isAccessorioContestuale()) {
            if (getBene() != null &&
                    getBene().getBene_principale() != null &&
                    getBene().getBene_principale().getEtichetta() != null) {
                return getBene().getBene_principale().getEtichetta();
            }
            return "";
        }
        return getBene() != null && getBene().getEtichetta() != null ? getBene().getEtichetta() : "";
    }

    public Boolean getFl_accessorio_contestuale() {
        return fl_accessorio_contestuale;
    }

    public void setFl_accessorio_contestuale(Boolean value) {
        fl_accessorio_contestuale = value;
    }

    public boolean isBeneAccessorio() {
        return Boolean.TRUE.equals(fl_bene_accessorio);
    }

    public Boolean getFl_bene_accessorio() {
        return fl_bene_accessorio;
    }

    public void setFl_bene_accessorio(Boolean value) {
        fl_bene_accessorio = value;
    }

    // ========================================
    // METODI DELEGATI AL BENE
    // ========================================

    public it.cnr.contab.anagraf00.core.bulk.TerzoBulk getAssegnatario() {
        return bene.getAssegnatario();
    }

    public String getCollocazione() {
        return bene.getCollocazione();
    }

    public Categoria_gruppo_inventBulk getCategoria_Bene() {
        return bene.getCategoria_Bene();
    }

    public String getDs_bene() {
        return bene.getDs_bene();
    }

    public Ubicazione_beneBulk getUbicazione() {
        return bene.getUbicazione();
    }

    public Condizione_beneBulk getCondizioneBene() {
        return bene.getCondizioneBene();
    }

    // ========================================
    // ALTRI GETTER/SETTER
    // ========================================

    public int getGruppi() {
        return gruppi;
    }

    public void setGruppi(int gruppi) {
        this.gruppi = gruppi;
    }

    public Categoria_gruppo_voceBulk getCat_voce() {
        return cat_voce;
    }

    public void setCat_voce(Categoria_gruppo_voceBulk cat_voce) {
        this.cat_voce = cat_voce;
    }

    public String constructCMISNomeFile() {
        StringBuffer nomeFile = new StringBuffer();
        nomeFile = nomeFile.append(StrServ.lpad(this.getPg_doc_trasporto_rientro().toString(), 9, "0"));
        return nomeFile.toString();
    }

}