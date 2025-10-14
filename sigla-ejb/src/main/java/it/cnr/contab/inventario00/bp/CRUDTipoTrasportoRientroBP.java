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
        if (!isEditable())
            return false;

        return ((Tipo_trasporto_rientroBulk)getModel()).isCancellabile();
    }

    /**
     * Restituisce TRUE se il Tipo di Movimento è di TRASPORTO.
     *
     * @return <code>boolean</code>
     **/
    public boolean isMovimentoTrasporto() {
        Tipo_trasporto_rientroBulk movimento = (Tipo_trasporto_rientroBulk)getModel();
        if (movimento.getTiDocumento() == null) {
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
        if (movimento.getTiDocumento() == null) {
            return false;
        }
        return movimento.getTiDocumento().equals(movimento.TIPO_RIENTRO);
    }
}