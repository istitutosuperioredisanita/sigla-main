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

package it.cnr.contab.inventario00.consultazioni.bulk;

import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;

import java.time.format.DateTimeFormatter;

/**
 * Estensione con campi calcolati per REST.
 */
public class Inventario_beniRestBulk extends Inventario_beniBulk {

    // Valore ottenuto via JOIN da query SQL
    private String sigla_int_ente;

    /** EDIFICIO: prima parte prima dello spazio, prima di '-' */
    public String getEdificio() {
        if (getUbicazione() != null && getUbicazione().getDs_ubicazione_bene() != null) {
            String ds = getUbicazione().getDs_ubicazione_bene();
            int space = ds.indexOf(' ');
            if (space > 0) {
                String[] parts = ds.substring(0, space).split("-");
                return parts.length > 0 ? parts[0] : null;
            }
        }
        return null;
    }

    /** SCALA: fisso '.' */
    public String getScala() {
        return ".";
    }

    /** PIANO: seconda parte prima dello spazio, separata da '-' */
    public String getPiano() {
        if (getUbicazione() != null && getUbicazione().getDs_ubicazione_bene() != null) {
            String ds = getUbicazione().getDs_ubicazione_bene();
            int space = ds.indexOf(' ');
            if (space > 0) {
                String[] parts = ds.substring(0, space).split("-");
                return parts.length > 1 ? parts[1] : null;
            }
        }
        return null;
    }

    /** STANZA: terza parte prima dello spazio, separata da '-' */
    public String getStanza() {
        if (getUbicazione() != null && getUbicazione().getDs_ubicazione_bene() != null) {
            String ds = getUbicazione().getDs_ubicazione_bene();
            int space = ds.indexOf(' ');
            if (space > 0) {
                String[] parts = ds.substring(0, space).split("-");
                return parts.length > 2 ? parts[2] : null;
            }
        }
        return null;
    }

    /** CATASTO: tutto dopo il primo spazio */
    public String getCatasto() {
        if (getUbicazione() != null && getUbicazione().getDs_ubicazione_bene() != null) {
            String ds = getUbicazione().getDs_ubicazione_bene();
            int space = ds.indexOf(' ');
            if (space > 0 && space < ds.length() - 1) {
                return ds.substring(space + 1).trim();
            }
        }
        return null;
    }

    /** Categoria padre */
    public String getCd_categoria() {
        return getCategoria_Bene() != null ? getCategoria_Bene().getCd_categoria_padre() : null;
    }

    /** Gruppo */
    public String getCd_gruppo() {
        return getCategoria_Bene() != null ? getCategoria_Bene().getCd_proprio() : null;
    }

    /** Descrizione categoria/gruppo */
    public String getDs_cat_gruppo() {
        return getCategoria_Bene() != null ? getCategoria_Bene().getDs_categoria_gruppo() : null;
    }

    /** Condizione del bene */
    public String getCondizione_bene() {
        return getCondizioneBene() != null ? getCondizioneBene().getDs_condizione_bene() : null;
    }

    /** SIGLA_INT_ENTE da JOIN */
    public String getSigla_int_ente() {
        return sigla_int_ente;
    }

    public void setSigla_int_ente(String sigla_int_ente) {
        this.sigla_int_ente = sigla_int_ente;
    }

    /** Dismesso: default 'N' */
    public String getDismesso() {
        if (getFl_totalmente_scaricato() == null) return "N";
        return getFl_totalmente_scaricato() ? "Y" : "N";
    }


    /** Data acquisizione formattata come yyyy-MM-dd */
    public String getDt_acquisizione_formatted() {
        if (getDt_acquisizione() == null) return null;
        return getDt_acquisizione().toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}