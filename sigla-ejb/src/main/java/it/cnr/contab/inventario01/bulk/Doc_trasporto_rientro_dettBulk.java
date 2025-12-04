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

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_voceBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Condizione_beneBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Ubicazione_beneBulk;
import it.cnr.contab.util.enumeration.TipoIVA;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.StrServ;

/**
 * Dettaglio (riga) di un documento di Trasporto/Rientro.
 * Le chiavi primarie composite (PK) sono delegate alle entità correlate (Testata e Bene).
 */
public abstract class Doc_trasporto_rientro_dettBulk extends Doc_trasporto_rientro_dettBase {

    // ========================================
    // ATTRIBUTI E RELAZIONI
    // ========================================

    /**
     * Bene inventariale associato a questa riga di dettaglio.
     * Delega le PK: nr_inventario, progressivo.
     */
    private Inventario_beniBulk bene;

    /**
     * Testata del documento (FK obbligatoria).
     * Delega le PK: pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro.
     */

    // Altri attributi
    private int gruppi;
    private Boolean fl_accessorio_contestuale = Boolean.FALSE; // Flag se è un accessorio aggiunto in contesto
    protected Boolean fl_bene_accessorio;                     // Flag se è un bene accessorio pre-esistente
    private Categoria_gruppo_voceBulk cat_voce;

    private TerzoBulk terzoAssegnatario;  // FK mappata su CD_TERZO_ASSEGNATARIO


    // ========================================
    // COSTRUTTORI
    // ========================================

    public Doc_trasporto_rientro_dettBulk() {
        super();
        // NON inizializzare campi delegati qui (logica JADA)
    }

    public Doc_trasporto_rientro_dettBulk(Long pg_inventario, String ti_documento,
                                          Integer esercizio, Long pg_doc_trasporto_rientro,
                                          Long nr_inventario, Integer progressivo) {
        super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro,
                nr_inventario, progressivo);

        // Inizializza gli oggetti bulk relazionati (PK delegate)

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

    public abstract Doc_trasporto_rientroBulk getDoc_trasporto_rientro() ;

    public abstract void setDoc_trasporto_rientro(Doc_trasporto_rientroBulk bulk) ;

    public abstract Doc_trasporto_rientro_dettBulk getDoc_trasporto_rientroDettRif() ;

    public abstract void setDoc_trasporto_rientroDettRif(Doc_trasporto_rientro_dettBulk bulk) ;

    public Categoria_gruppo_voceBulk getCat_voce() {
        return cat_voce;
    }

    public void setCat_voce(Categoria_gruppo_voceBulk cat_voce) {
        this.cat_voce = cat_voce;
    }


    // ========================================
    // GETTER E SETTER - Campi PK Delegati
    // ========================================

    // Delega: PG_INVENTARIO
    public void setPg_inventario(Long pg_inventario) {
        if (this.getDoc_trasporto_rientro() != null) {
            this.getDoc_trasporto_rientro().setPgInventario(pg_inventario);
        }
    }

    public Long getPg_inventario() {
        return this.getDoc_trasporto_rientro() == null ? null : this.getDoc_trasporto_rientro().getPgInventario();
    }

    // Delega: NR_INVENTARIO
    public Long getNr_inventario() {
        return this.getBene() == null ? null : this.getBene().getNr_inventario();
    }

    public void setNr_inventario(Long nr_inventario) {
        if (this.getBene() != null) {
            this.getBene().setNr_inventario(nr_inventario);
        }
    }

    // Delega: PROGRESSIVO (mappato come Long in Inventario_beniBulk)
    public Integer getProgressivo() {
        if (this.getBene() == null || this.getBene().getProgressivo() == null) return null;
        return new Integer(this.getBene().getProgressivo().intValue());
    }

    public void setProgressivo(Integer progressivo) {
        if (this.getBene() != null) {
            this.getBene().setProgressivo(new Long(progressivo.longValue()));
        }
    }

    // Delega: TI_DOCUMENTO
    public void setTi_documento(String ti_documento) {
        if (this.getDoc_trasporto_rientro() != null) {
            this.getDoc_trasporto_rientro().setTiDocumento(ti_documento);
        }
    }

    public String getTi_documento() {
        return this.getDoc_trasporto_rientro() == null ? null : this.getDoc_trasporto_rientro().getTiDocumento();
    }

    // Delega: ESERCIZIO
    public void setEsercizio(Integer esercizio) {
        if (this.getDoc_trasporto_rientro() != null) {
            this.getDoc_trasporto_rientro().setEsercizio(esercizio);
        }
    }

    public Integer getEsercizio() {
        return this.getDoc_trasporto_rientro() == null ? null : this.getDoc_trasporto_rientro().getEsercizio();
    }

    // Delega: PG_DOC_TRASPORTO_RIENTRO
    public void setPg_doc_trasporto_rientro(Long pg_doc_trasporto_rientro) {
        if (this.getDoc_trasporto_rientro() != null) {
            this.getDoc_trasporto_rientro().setPgDocTrasportoRientro(pg_doc_trasporto_rientro);
        }
    }

    public Long getPg_doc_trasporto_rientro() {
        return this.getDoc_trasporto_rientro() == null ? null : this.getDoc_trasporto_rientro().getPgDocTrasportoRientro();
    }

    // ========================================
    // METODI UTILITY - Codifiche
    // ========================================

    /**
     * Restituisce il codice del bene nel formato "NR_INVENTARIO-PROGRESSIVO" (progressivo a 3 cifre).
     */
    public String getCod_bene() {
        if (getNr_inventario() == null || getProgressivo() == null) {
            return "";
        }
        return bene.getNumeroBeneCompleto();
        //java.text.DecimalFormat formato = new java.text.DecimalFormat("000");
       // return getNr_inventario().toString() + "-" + formato.format(getProgressivo());
    }

    /**
     * Restituisce una chiave hash univoca per il dettaglio, usata per gli accessori contestuali.
     * Formato: "NR_INVENTARIO.PROGRESSIVO.ETICHETTA".
     */
    public String getChiaveHash() {
        if (getNr_inventario() == null || getProgressivo() == null) {
            return null;
        }
        // Utilizza l'etichetta del dettaglio (che può essere quella del bene principale)
        return getNr_inventario().toString() + "." + getProgressivo().toString() + "." + getEtichetta();
    }

    // ========================================
    // METODI UTILITY - Validazioni e Stati
    // ========================================

    //TODO creare un metodo isValidoPerRientro()

    /**
     * Verifica se il bene associato è totalmente scaricato.
     */
    public boolean isTotalmenteScaricato() {
        return bene != null && Boolean.TRUE.equals(bene.getFl_totalmente_scaricato());
    }

    /**
     * Verifica se il bene è attualmente in transito (gestione specifica).
     */
    public boolean isBeneInTransito() {
        return bene != null && bene.getId_transito_beni_ordini() != null;
    }

    // ========================================
    // METODI DI INIZIALIZZAZIONE
    // ========================================

    @Override
    public OggettoBulk initialize(it.cnr.jada.util.action.CRUDBP bp, it.cnr.jada.action.ActionContext context) {
        // Inizializzazione standard
        bene = new Inventario_beniBulk();
        bene.setTi_commerciale_istituzionale(TipoIVA.ISTITUZIONALE.value());
        return this;
    }



    // ========================================
    // GESTIONE ACCESSORI E ETICHETTE
    // ========================================

    public boolean isAccessorioContestuale() {
        return Boolean.TRUE.equals(fl_accessorio_contestuale);
    }

    /**
     * Verifica se la riga corrente è associata ad accessori contestuali nella Testata.
     */
    public boolean isAssociatoConAccessorioContestuale() {
        Doc_trasporto_rientroBulk doc = getDoc_trasporto_rientro();
        if (getChiaveHash() == null || doc == null || doc.getAccessoriContestualiHash() == null) {
            return false;
        }
        return doc.getAccessoriContestualiHash().containsKey(getChiaveHash());
    }

    // Metodi per controllare la readonly (RO) dei campi in base al tipo di bene/dettaglio

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
    // METODI DELEGATI AL BENE (Accesso diretto ai campi dell'Inventario_beniBulk)
    // ========================================

    public it.cnr.contab.anagraf00.core.bulk.TerzoBulk getAssegnatario() {
        return bene == null ? null : bene.getAssegnatario();
    }

    public String getCollocazione() {
        return bene == null ? null : bene.getCollocazione();
    }

    public Categoria_gruppo_inventBulk getCategoria_Bene() {
        return bene == null ? null : bene.getCategoria_Bene();
    }

    public String getDs_bene() {
        return bene == null ? null : bene.getDs_bene();
    }

    public Ubicazione_beneBulk getUbicazione() {
        return bene == null ? null : bene.getUbicazione();
    }

    public Condizione_beneBulk getCondizioneBene() {
        return bene == null ? null : bene.getCondizioneBene();
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

    // Costruisce il nome file CMIS
    public String constructCMISNomeFile() {
        StringBuffer nomeFile = new StringBuffer();
        nomeFile = nomeFile.append(StrServ.lpad(this.getPg_doc_trasporto_rientro().toString(), 9, "0"));
        return nomeFile.toString();
    }

    /**
     * Getter per Terzo Assegnatario.
     * Questo campo viene popolato automaticamente dal Component:
     * - Modalità normale: dal CD_ASSEGNATARIO del bene
     * - Modalità smartworking: dal terzo selezionato in testata
     */
    public TerzoBulk getTerzoAssegnatario() {
        return terzoAssegnatario;
    }

    public void setTerzoAssegnatario(TerzoBulk terzoAssegnatario) {
        this.terzoAssegnatario = terzoAssegnatario;
        if (terzoAssegnatario != null) {
            setCdTerzoAssegnatario(terzoAssegnatario.getCd_terzo());
        } else {
            setCdTerzoAssegnatario(null);
        }
    }

    @Override
    public Integer getCdTerzoAssegnatario() {
        if (terzoAssegnatario != null) {
            return terzoAssegnatario.getCd_terzo();
        }
        return super.getCdTerzoAssegnatario();
    }

    @Override
    public void setCdTerzoAssegnatario(Integer cdTerzoAssegnatario) {
        super.setCdTerzoAssegnatario(cdTerzoAssegnatario);
        if (terzoAssegnatario == null && cdTerzoAssegnatario != null) {
            terzoAssegnatario = new TerzoBulk();
        }
        if (terzoAssegnatario != null && cdTerzoAssegnatario != null) {
            terzoAssegnatario.setCd_terzo(cdTerzoAssegnatario);
        }
    }

}