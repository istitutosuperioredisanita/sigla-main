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

package it.cnr.contab.inventario01.bulk;

import it.cnr.jada.util.OrderedHashtable;

public class AllegatoDocumentoRientroBulk extends AllegatoDocTraspRientroBulk {
	private static final long serialVersionUID = 1L;

	public static OrderedHashtable aspectNamesKeys = new OrderedHashtable();

	public static final String P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO = "P:sigla_doctrientro_attachment:altro";
	public static final String P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO = "P:sigla_docrientro_attachment:docrientro_firmato";

	static {
		aspectNamesKeys.put(P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO,"Altro");
		aspectNamesKeys.put(P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO,"Doc. Rientro Firmato");


	}
	private String aspectName;

	public AllegatoDocumentoRientroBulk() {
		super();
	}

	public AllegatoDocumentoRientroBulk(String storageKey) {
		super(storageKey);
	}

	public String getAspectName() {
		return aspectName;
	}
	public void setAspectName(String aspectName) {
		this.aspectName = aspectName;
	}

	public static OrderedHashtable getAspectnameskeys() {
		return aspectNamesKeys;
	}

	
}