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

package it.cnr.contab.web.rest.model;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;

import java.math.BigDecimal;
import java.util.Optional;

public class FondiFunzionamentoUODTO {
    private final Unita_organizzativaBulk unitaOrganizzativaBulk;

    public FondiFunzionamentoUODTO(Unita_organizzativaBulk unitaOrganizzativaBulk) {
        this.unitaOrganizzativaBulk = unitaOrganizzativaBulk;
    }

    public String getCodice() {
        return Optional.ofNullable(unitaOrganizzativaBulk)
                .map(Unita_organizzativaBulk::getCd_unita_organizzativa)
                .orElse(null);
    }

    public String getDescrizione() {
        return Optional.ofNullable(unitaOrganizzativaBulk)
                .map(Unita_organizzativaBulk::getDs_unita_organizzativa)
                .orElse(null);
    }

    public BigDecimal getImportoFinanziato() {
        return Optional.ofNullable(unitaOrganizzativaBulk)
                .map(Unita_organizzativaBulk::getImportoFinanziato)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getImportoUtilizzato() {
        return Optional.ofNullable(unitaOrganizzativaBulk)
                .map(Unita_organizzativaBulk::getImportoUtilizzato)
                .orElse(BigDecimal.ZERO);
    }

}
