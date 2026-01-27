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

/*
 * Created by Aurelio's BulkGenerator 1.0
 * Date 09/03/2007
 */
package it.cnr.contab.doccont00.consultazioni.bulk;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class V_cons_scad_accert_voceBulk extends V_cons_scad_accertBulk {

    private java.math.BigDecimal im_voce;
    private java.lang.String cd_progetto;

    private java.lang.Integer pg_progetto;

    private java.lang.String ds_progetto;

    private java.sql.Timestamp dtInizio;

    private java.sql.Timestamp dtFine;

    private java.sql.Timestamp dtProroga;
    private java.lang.String cd_tipo_progetto;

    private java.lang.String ds_tipo_progetto;

    private java.lang.String note_progetto;


    public BigDecimal getIm_voce() {
        return im_voce;
    }

    public void setIm_voce(BigDecimal im_voce) {
        this.im_voce = im_voce;
    }

    public String getCd_progetto() {
        return cd_progetto;
    }

    public void setCd_progetto(String cd_progetto) {
        this.cd_progetto = cd_progetto;
    }

    public Integer getPg_progetto() {
        return pg_progetto;
    }

    public void setPg_progetto(Integer pg_progetto) {
        this.pg_progetto = pg_progetto;
    }

    public String getDs_progetto() {
        return ds_progetto;
    }

    public void setDs_progetto(String ds_progetto) {
        this.ds_progetto = ds_progetto;
    }

    public Timestamp getDtInizio() {
        return dtInizio;
    }

    public void setDtInizio(Timestamp dtInizio) {
        this.dtInizio = dtInizio;
    }

    public Timestamp getDtFine() {
        return dtFine;
    }

    public void setDtFine(Timestamp dtFine) {
        this.dtFine = dtFine;
    }

    public Timestamp getDtProroga() {
        return dtProroga;
    }

    public void setDtProroga(Timestamp dtProroga) {
        this.dtProroga = dtProroga;
    }

    public String getCd_tipo_progetto() {
        return cd_tipo_progetto;
    }

    public void setCd_tipo_progetto(String cd_tipo_progetto) {
        this.cd_tipo_progetto = cd_tipo_progetto;
    }

    public String getDs_tipo_progetto() {
        return ds_tipo_progetto;
    }

    public void setDs_tipo_progetto(String ds_tipo_progetto) {
        this.ds_tipo_progetto = ds_tipo_progetto;
    }

    public String getNote_progetto() {
        return note_progetto;
    }

    public void setNote_progetto(String note_progetto) {
        this.note_progetto = note_progetto;
    }
}