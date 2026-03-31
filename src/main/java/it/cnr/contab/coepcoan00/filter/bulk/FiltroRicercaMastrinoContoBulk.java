/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.coepcoan00.filter.bulk;

import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.jada.bulk.OggettoBulk;

import java.sql.Timestamp;
import java.util.Optional;

public class FiltroRicercaMastrinoContoBulk extends OggettoBulk {
    private ContoBulk conto;
    private Boolean filtraUnitaOrganizzativa;
    private Unita_organizzativaBulk unitaOrganizzativa;
    private java.sql.Timestamp fromDataMovimento;
    private java.sql.Timestamp toDataMovimento;

    public ContoBulk getConto() {
        return conto;
    }

    public void setConto(ContoBulk conto) {
        this.conto = conto;
    }

    public Boolean getFiltraUnitaOrganizzativa() {
        return filtraUnitaOrganizzativa;
    }

    public void setFiltraUnitaOrganizzativa(Boolean filtraUnitaOrganizzativa) {
        this.filtraUnitaOrganizzativa = filtraUnitaOrganizzativa;
    }

    public Unita_organizzativaBulk getUnitaOrganizzativa() {
        return unitaOrganizzativa;
    }

    public void setUnitaOrganizzativa(Unita_organizzativaBulk unitaOrganizzativa) {
        this.unitaOrganizzativa = unitaOrganizzativa;
    }

    public Timestamp getFromDataMovimento() {
        return fromDataMovimento;
    }

    public void setFromDataMovimento(Timestamp fromDataMovimento) {
        this.fromDataMovimento = fromDataMovimento;
    }

    public Timestamp getToDataMovimento() {
        return toDataMovimento;
    }

    public void setToDataMovimento(Timestamp toDataMovimento) {
        this.toDataMovimento = toDataMovimento;
    }

    public boolean isROUnitaOrganizzativa() {
        return !Optional.ofNullable(this.getFiltraUnitaOrganizzativa())
                .orElse(Boolean.FALSE);
    }
}
