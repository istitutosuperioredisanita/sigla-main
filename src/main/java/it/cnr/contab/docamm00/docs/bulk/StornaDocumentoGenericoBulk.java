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
package it.cnr.contab.docamm00.docs.bulk;

import it.cnr.contab.docamm00.tabrif.bulk.Tipo_documento_genericoBulk;
import it.cnr.jada.bulk.OggettoBulk;

import java.sql.Timestamp;
import java.util.Dictionary;

public class StornaDocumentoGenericoBulk extends OggettoBulk {
    private final Dictionary<String, String> tiCausaleContabileKeys;

    private String causaleContabile;
    private String descrizione;
    private Timestamp dt_registrazione;
    private Timestamp dt_da_competenza_coge;
    private Timestamp dt_a_competenza_coge;
    private Tipo_documento_genericoBulk tipoDocumentoGenerico;

    public StornaDocumentoGenericoBulk(Dictionary<String, String> tiCausaleContabileKeys) {
        this.tiCausaleContabileKeys = tiCausaleContabileKeys;
    }

    public Dictionary<String, String> getTiCausaleContabileKeys() {
        return tiCausaleContabileKeys;
    }

    public String getCausaleContabile() {
        return causaleContabile;
    }

    public void setCausaleContabile(String causaleContabile) {
        this.causaleContabile = causaleContabile;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Timestamp getDt_registrazione() {
        return dt_registrazione;
    }

    public void setDt_registrazione(Timestamp dt_registrazione) {
        this.dt_registrazione = dt_registrazione;
    }

    public Timestamp getDt_da_competenza_coge() {
        return dt_da_competenza_coge;
    }

    public void setDt_da_competenza_coge(Timestamp dt_da_competenza_coge) {
        this.dt_da_competenza_coge = dt_da_competenza_coge;
    }

    public Timestamp getDt_a_competenza_coge() {
        return dt_a_competenza_coge;
    }

    public void setDt_a_competenza_coge(Timestamp dt_a_competenza_coge) {
        this.dt_a_competenza_coge = dt_a_competenza_coge;
    }

    public Tipo_documento_genericoBulk getTipoDocumentoGenerico() {
        return tipoDocumentoGenerico;
    }

    public void setTipoDocumentoGenerico(Tipo_documento_genericoBulk tipoDocumentoGenerico) {
        this.tipoDocumentoGenerico = tipoDocumentoGenerico;
    }
}
