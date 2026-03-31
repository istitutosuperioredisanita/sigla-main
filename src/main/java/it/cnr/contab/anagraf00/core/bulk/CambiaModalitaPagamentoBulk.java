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
package it.cnr.contab.anagraf00.core.bulk;

import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.jada.bulk.OggettoBulk;

import java.util.Collection;

public class CambiaModalitaPagamentoBulk extends OggettoBulk {
    protected Rif_modalita_pagamentoBulk modalita_pagamento;
    protected BancaBulk banca;
    protected final TerzoBulk terzo;

    private Collection<Rif_modalita_pagamentoBulk> modalita;

    public CambiaModalitaPagamentoBulk(TerzoBulk terzo, BancaBulk banca, Rif_modalita_pagamentoBulk modalita_pagamento, Collection<Rif_modalita_pagamentoBulk> modalita) {
        this.terzo = terzo;
        this.modalita_pagamento = modalita_pagamento;
        this.banca = banca;
        this.modalita = modalita;
    }

    public Rif_modalita_pagamentoBulk getModalita_pagamento() {
        return modalita_pagamento;
    }

    public void setModalita_pagamento(Rif_modalita_pagamentoBulk modalita_pagamento) {
        this.modalita_pagamento = modalita_pagamento;
    }

    public BancaBulk getBanca() {
        return banca;
    }

    public void setBanca(BancaBulk banca) {
        this.banca = banca;
    }

    public Collection<Rif_modalita_pagamentoBulk> getModalita() {
        return modalita;
    }

    public void setModalita(Collection<Rif_modalita_pagamentoBulk> modalita) {
        this.modalita = modalita;
    }

    public TerzoBulk getTerzo() {
        return terzo;
    }
}
