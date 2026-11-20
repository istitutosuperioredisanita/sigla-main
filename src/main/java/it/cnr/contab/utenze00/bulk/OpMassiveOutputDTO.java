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

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.ordmag.anag00.AbilUtenteUopOperBulk;
import it.cnr.contab.ordmag.anag00.TipoOperazioneOrdBulk;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;

/**
 * DTO di output della gestione massiva delle utenze: record letti dal file, da inserire o cancellare.
 * In base alla gestione vengono valorizzate:
 * ACCESSI -> utenti, unitaOrganizzative, accessi;
 * RUOLI -> utenti, unitaOrganizzative, ruoli;
 * ORDINI_MAGAZZINO -> utenti, ruoli, funzioni.
 */
public class OpMassiveOutputDTO extends OggettoBulk {
    private static final long serialVersionUID = 1L;

    private BulkList<UtenteBulk> utenti = new BulkList<>();
    private BulkList<Unita_organizzativaBulk> unitaOrganizzative = new BulkList<>();
    private BulkList<AccessoBulk> accessi = new BulkList<>();
    private BulkList<RuoloBulk> ruoli = new BulkList<>();
    private BulkList<TipoOperazioneOrdBulk> funzioni = new BulkList<>();
    private BulkList<AbilUtenteUopOperBulk> abilitazioniOrdini = new BulkList<>();

    public OpMassiveOutputDTO() {
        super();
    }

    public BulkList<UtenteBulk> getUtenti() {
        return utenti;
    }

    public void setUtenti(BulkList<UtenteBulk> utenti) {
        this.utenti = utenti;
    }

    public BulkList<Unita_organizzativaBulk> getUnitaOrganizzative() {
        return unitaOrganizzative;
    }

    public void setUnitaOrganizzative(BulkList<Unita_organizzativaBulk> unitaOrganizzative) {
        this.unitaOrganizzative = unitaOrganizzative;
    }

    public BulkList<AccessoBulk> getAccessi() {
        return accessi;
    }

    public void setAccessi(BulkList<AccessoBulk> accessi) {
        this.accessi = accessi;
    }

    public BulkList<RuoloBulk> getRuoli() {
        return ruoli;
    }

    public void setRuoli(BulkList<RuoloBulk> ruoli) {
        this.ruoli = ruoli;
    }

    public BulkList<TipoOperazioneOrdBulk> getFunzioni() {
        return funzioni;
    }

    public void setFunzioni(BulkList<TipoOperazioneOrdBulk> funzioni) {
        this.funzioni = funzioni;
    }

    public BulkList<AbilUtenteUopOperBulk> getAbilitazioniOrdini() {
        return abilitazioniOrdini;
    }

    public void setAbilitazioniOrdini(BulkList<AbilUtenteUopOperBulk> abilitazioniOrdini) {
        this.abilitazioniOrdini = abilitazioniOrdini;
    }
}
