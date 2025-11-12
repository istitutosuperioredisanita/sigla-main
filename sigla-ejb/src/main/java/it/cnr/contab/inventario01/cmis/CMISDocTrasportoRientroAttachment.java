/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.inventario01.cmis;

public enum CMISDocTrasportoRientroAttachment {

    SIGLA_DOC_TRASPORTO_RIENTRO_ATTACHMENT_STAMPA("D:doc_trasporto_rientro_attachment:stampa"),
    SIGLA_DOC_TRASPORTO_RIENTRO_ATTACHMENT_ALLEGATO_GENERICO("D:doc_trasporto_rientro_attachment:allegato_generico"),
    SIGLA_DOC_TRASPORTO_RIENTRO_ATTACHMENT_DDT("D:doc_trasporto_rientro_attachment:ddt"),
    SIGLA_DOC_TRASPORTO_RIENTRO_ATTACHMENT_VERBALE_CONSEGNA("D:doc_trasporto_rientro_attachment:verbale_consegna"),
    SIGLA_DOC_TRASPORTO_RIENTRO_ATTACHMENT_AUTORIZZAZIONE("D:doc_trasporto_rientro_attachment:autorizzazione"),
    SIGLA_DOC_TRASPORTO_RIENTRO_ATTACHMENT_FOTO("D:doc_trasporto_rientro_attachment:foto");

    private final String value;

    private CMISDocTrasportoRientroAttachment(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static CMISDocTrasportoRientroAttachment fromValue(String v) {
        for (CMISDocTrasportoRientroAttachment c : CMISDocTrasportoRientroAttachment.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}