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

import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.progettiric00.core.bulk.V_saldi_voce_progettoBulk;

import java.math.BigDecimal;
import java.util.Optional;

public class VociBilancioProgettoDTO {
    private final V_saldi_voce_progettoBulk vSaldiVoceProgettoBulk;

    public VociBilancioProgettoDTO(V_saldi_voce_progettoBulk vSaldiVoceProgettoBulk) {
        this.vSaldiVoceProgettoBulk = vSaldiVoceProgettoBulk;
    }

    public String getCodice() {
        return Optional.ofNullable(vSaldiVoceProgettoBulk)
                .map(V_saldi_voce_progettoBulk::getCd_elemento_voce)
                .orElse(null);
    }

    public String getDescrizione() {
        return Optional.ofNullable(vSaldiVoceProgettoBulk)
                .flatMap(v -> Optional.ofNullable(v.getElemento_voce()))
                .map(Elemento_voceBulk::getDs_elemento_voce)
                .orElse(null);
    }

    public BigDecimal getImAssestatoSpesaFinanziato() {
        return Optional.ofNullable(vSaldiVoceProgettoBulk)
                .map(V_saldi_voce_progettoBulk::getAssestatoFinanziamento)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getImUtilizzatoSpesaFinanziato() {
        return Optional.ofNullable(vSaldiVoceProgettoBulk)
                .map(V_saldi_voce_progettoBulk::getUtilizzatoAssestatoFinanziamento)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getImPagatoSpesaFinanziato() {
        return Optional.ofNullable(vSaldiVoceProgettoBulk)
                .map(V_saldi_voce_progettoBulk::getManrisFin)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getImAssestatoSpesaCofinanziato() {
        return Optional.ofNullable(vSaldiVoceProgettoBulk)
                .map(V_saldi_voce_progettoBulk::getAssestatoCofinanziamento)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getImUtilizzatoSpesaCofinanziato() {
        return Optional.ofNullable(vSaldiVoceProgettoBulk)
                .map(V_saldi_voce_progettoBulk::getUtilizzatoAssestatoCofinanziamento)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getImPagatoSpesaCofinanziato() {
        return Optional.ofNullable(vSaldiVoceProgettoBulk)
                .map(V_saldi_voce_progettoBulk::getManrisCofin)
                .orElse(BigDecimal.ZERO);
    }
}
