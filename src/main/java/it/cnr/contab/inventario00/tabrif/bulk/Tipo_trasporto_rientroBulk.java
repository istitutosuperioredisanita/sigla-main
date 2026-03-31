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

package it.cnr.contab.inventario00.tabrif.bulk;

import it.cnr.jada.bulk.*;

public class Tipo_trasporto_rientroBulk extends Tipo_trasporto_rientroBase {

	public final static java.lang.String TIPO_TRASPORTO = "T";
	public final static java.lang.String TIPO_RIENTRO = "R";

	private final static java.util.Dictionary tipoDocumentoKeys;

	static {
		tipoDocumentoKeys = new it.cnr.jada.util.OrderedHashtable();
		tipoDocumentoKeys.put(TIPO_TRASPORTO, "Trasporto");
		tipoDocumentoKeys.put(TIPO_RIENTRO, "Rientro");
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: TIPO_TRASPORTO_RIENTRO
	 **/
	public Tipo_trasporto_rientroBulk() {
		super();
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: TIPO_TRASPORTO_RIENTRO
	 **/
	public Tipo_trasporto_rientroBulk(String cdTipoTrasportoRientro) {
		super(cdTipoTrasportoRientro);
	}

	/**
	 * Restituisce il dictionary con le chiavi dei tipi documento
	 * @return java.util.Dictionary
	 */
	public final java.util.Dictionary getTipoDocumentoKeys() {
		return tipoDocumentoKeys;
	}

	/**
	 * Inizializza il ricevente per la visualizzazione in un <code>FormController</code>
	 * in stato <code>INSERT</code>.
	 * Questo metodo viene invocato automaticamente da un
	 * <code>it.cnr.jada.util.action.CRUDBP</code> quando viene inizializzato
	 * per l'inserimento di un OggettoBulk.
	 */
	public OggettoBulk initializeForInsert(it.cnr.jada.util.action.CRUDBP bp, it.cnr.jada.action.ActionContext context) {
		setTiDocumento(TIPO_TRASPORTO);
		setFlAbilitaNote(Boolean.FALSE);
		return super.initializeForInsert(bp, context);
	}

	/**
	 * Verifica se il tipo trasporto/rientro è cancellabile
	 * @return boolean
	 */
	public boolean isCancellabile() {
		return getDtCancellazione() == null;
	}

	/**
	 * Verifica se per questa tipologia è abilitato l'inserimento di note
	 * @return boolean
	 */
	public boolean isAbilitaNote() {
		if (getFlAbilitaNote() != null)
			return getFlAbilitaNote().booleanValue();
		else
			return false;
	}

	/**
	 * Verifica se il tipo documento è un trasporto
	 * @return boolean
	 */
	public boolean isTrasporto() {
		return TIPO_TRASPORTO.equals(getTiDocumento());
	}

	/**
	 * Verifica se il tipo documento è un rientro
	 * @return boolean
	 */
	public boolean isRientro() {
		return TIPO_RIENTRO.equals(getTiDocumento());
	}
}