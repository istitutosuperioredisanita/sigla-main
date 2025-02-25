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

package it.cnr.contab.coepcoan00.core.bulk;


public class Numerazione_coge_coanBulk extends Numerazione_coge_coanBase {
    public final static String TI_DOCUMENTO_COGE = "COGE";
    public final static String TI_DOCUMENTO_COAN = "COAN";


    public Numerazione_coge_coanBulk() {
        super();
    }

    public Numerazione_coge_coanBulk(String cd_cds, String cd_unita_organizzativa, Integer esercizio,String ti_documento) {
        super(cd_cds,cd_unita_organizzativa,esercizio,ti_documento);
    }

    public Numerazione_coge_coanBulk(Scrittura_partita_doppiaBulk scritturaPartitaDoppia) {
        super();

        setTi_documento(TI_DOCUMENTO_COGE);

        setCd_cds(scritturaPartitaDoppia.getCd_cds());
        setEsercizio(scritturaPartitaDoppia.getEsercizio());
        setCd_unita_organizzativa(scritturaPartitaDoppia.getCd_unita_organizzativa());
    }

    public Numerazione_coge_coanBulk(Scrittura_analiticaBulk scritturaAnalitica) {
        super();

        setTi_documento(TI_DOCUMENTO_COAN);

        setCd_cds(scritturaAnalitica.getCd_cds());
        setEsercizio(scritturaAnalitica.getEsercizio());
        setCd_unita_organizzativa(scritturaAnalitica.getCd_unita_organizzativa());
    }
}
