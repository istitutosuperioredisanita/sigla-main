/*
 * Copyright (C) 2026  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.utenze00.bulk;

import it.cnr.contab.util00.bulk.storage.AllegatoParentIBulk;
import it.cnr.jada.util.OrderedHashtable;

import java.util.Dictionary;

/**
 * DTO di input per le operazioni massive (INSERIMENTO/CANCELLAZIONE)
 */
public class OpMassiveInputDTO extends AllegatoParentIBulk {
    private static final long serialVersionUID = 1L;

    public static final String INSERIMENTO = "I";
    public static final String CANCELLAZIONE = "C";

    public static final Dictionary tipoOperazioneKeys = new OrderedHashtable();

    static {
        tipoOperazioneKeys.put(INSERIMENTO, "Inserimento");
        tipoOperazioneKeys.put(CANCELLAZIONE, "Cancellazione");
    }

    private String tipoOperazione = INSERIMENTO;
    private String nota;

    public OpMassiveInputDTO() {
        super();
    }

    public String getTipoOperazione() {
        return tipoOperazione;
    }

    public void setTipoOperazione(String tipoOperazione) {
        this.tipoOperazione = tipoOperazione;
    }

    public Dictionary getTipoOperazioneKeys() {
        return tipoOperazioneKeys;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public boolean isRONota() {
        return true;
    }

    public boolean isInserimento() {
        return INSERIMENTO.equals(tipoOperazione);
    }
}
