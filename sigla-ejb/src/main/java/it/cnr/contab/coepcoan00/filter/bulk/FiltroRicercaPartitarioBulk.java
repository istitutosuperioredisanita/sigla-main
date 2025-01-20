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

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.coepcoan00.core.bulk.Sezione;
import it.cnr.jada.bulk.OggettoBulk;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FiltroRicercaPartitarioBulk extends OggettoBulk {
    private TerzoBulk terzo;
    private Boolean dettaglioTributi;
    private String partite;
    private java.sql.Timestamp toDataMovimento;
    public enum Partite {
        A("Aperte"), C("Chiuse"), T("Tutte");
        private final String label;

        Partite(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public static Dictionary<String, String> KEYS() {
            return Stream.of(Partite.values())
                    .collect(Collectors.toMap(
                            Partite::name,
                            Partite::label,
                            (oldValue, newValue) -> oldValue, Hashtable::new)
                    );
        }
    }
    public static Dictionary<String, String> tiPartiteKeys = Partite.KEYS();

    public Boolean getDettaglioTributi() {
        return dettaglioTributi;
    }

    public void setDettaglioTributi(Boolean dettaglioTributi) {
        this.dettaglioTributi = dettaglioTributi;
    }

    public TerzoBulk getTerzo() {
        return terzo;
    }

    public void setTerzo(TerzoBulk terzo) {
        this.terzo = terzo;
    }

    public String getPartite() {
        return partite;
    }

    public void setPartite(String partite) {
        this.partite = partite;
    }

    public Timestamp getToDataMovimento() {
        return toDataMovimento;
    }

    public void setToDataMovimento(Timestamp toDataMovimento) {
        this.toDataMovimento = toDataMovimento;
    }

    public boolean isROTerzo() {
        return Optional.ofNullable(terzo)
                .filter(terzoBulk -> terzoBulk.getCrudStatus() == OggettoBulk.NORMAL)
                .isPresent();
    }
}
