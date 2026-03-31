package it.cnr.contab.inventario00.bp;

import it.cnr.contab.inventario00.tabrif.bulk.*;

/**
 * Un BusinessProcess controller che permette di effettuare le operazioni di CRUD su istanze
 * di Tipo_trasporto_rientroBulk, per la gestione dei Tipi di Movimenti di Trasporto e Rientro
 * dell'Inventario
 **/
public class CRUDTipoTrasportoRientroBP extends it.cnr.jada.util.action.SimpleCRUDBP {

    public CRUDTipoTrasportoRientroBP() {
        super();
    }

    public CRUDTipoTrasportoRientroBP(String function) {
        super(function);
    }

    /**
     * Abilita il pulsante di Elimina.
     * Il pulsante di elimina viene abilitato solo se il Tipo di Movimento non è stato già cancellato.
     *
     * @return <code>boolean</code>
     **/
    public boolean isDeleteButtonEnabled() {
        Tipo_trasporto_rientroBulk m = (Tipo_trasporto_rientroBulk)getModel();
        if (m == null) {
            return false;
        }

        // Non permettere eliminazione se già cancellato logicamente
        if (m.getDtCancellazione() != null) {
            return false;
        }

        return super.isDeleteButtonEnabled() && m.isCancellabile();
    }

    /**
     * Restituisce TRUE se il Tipo di Movimento è di TRASPORTO.
     *
     * @return <code>boolean</code>
     **/
    public boolean isMovimentoTrasporto() {
        Tipo_trasporto_rientroBulk movimento = (Tipo_trasporto_rientroBulk)getModel();
        if (movimento == null || movimento.getTiDocumento() == null) {
            return false;
        }
        return movimento.getTiDocumento().equals(movimento.TIPO_TRASPORTO);
    }

    /**
     * Restituisce TRUE se il Tipo di Movimento è di RIENTRO.
     *
     * @return <code>boolean</code>
     **/
    public boolean isMovimentoRientro() {
        Tipo_trasporto_rientroBulk movimento = (Tipo_trasporto_rientroBulk)getModel();
        if (movimento == null || movimento.getTiDocumento() == null) {
            return false;
        }
        return movimento.getTiDocumento().equals(movimento.TIPO_RIENTRO);
    }

    /**
     * Controlla se il record può essere modificato.
     * Un record cancellato logicamente (con dtCancellazione valorizzato) può essere modificato
     * solo per aggiornare alcuni campi specifici.
     */
    @Override
    public boolean isEditable() {
        Tipo_trasporto_rientroBulk m = (Tipo_trasporto_rientroBulk)getModel();
        if (m == null) {
            return false;
        }
        // Permetti sempre la modifica, la gestione dei campi specifici
        // è delegata ai metodi isInputReadonly
        return super.isEditable();
    }

    /**
     * Controlla se i campi principali devono essere in sola lettura.
     * Restituisce true se il record è stato cancellato logicamente.
     */
    public boolean isInputReadonly() {
        Tipo_trasporto_rientroBulk m = (Tipo_trasporto_rientroBulk)getModel();
        if (m == null) {
            return false;
        }
        return m.getDtCancellazione() != null;
    }

    /**
     * Controlla se il campo Tipo Documento deve essere in sola lettura.
     */
    public boolean isTiDocumentoReadonly() {
        return isInputReadonly();
    }

    /**
     * Controlla se il campo Note Aggiuntive deve essere in sola lettura.
     */
    public boolean isFlAbilitaNoteReadonly() {
        return isInputReadonly();
    }

    /**
     * Controlla se il campo Data Cancellazione deve essere in sola lettura.
     * La data di cancellazione è sempre disabilitata (readonly).
     */
    public boolean isDtCancellazioneReadonly() {
        return true;
    }
}