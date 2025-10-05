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

package it.cnr.contab.ordmag.ordini.bulk;

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailEcoCogeBulk;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_analiticaBulk;

import java.util.Optional;

public class OrdineAcqConsegnaEcoSBulk extends OrdineAcqConsegnaEcoSBase {
    public static final String TIPO_STORICO_ORDINE = "INI";
    public static final String TIPO_STORICO_RISCONTRO_VALORE = "RSV";
    public static final String TIPO_STORICO_ANNULLO_RISCONTRO_VALORE = "RSA";

    private OrdineAcqConsegnaBulk ordineAcqConsegna;

    protected Voce_analiticaBulk voce_analitica = new Voce_analiticaBulk();

    WorkpackageBulk linea_attivita = new WorkpackageBulk();

    public OrdineAcqConsegnaEcoSBulk() {
        super();
    }

    public OrdineAcqConsegnaEcoSBulk(Long id) {
        super(id);
    }

    public Voce_analiticaBulk getVoce_analitica() {
        return voce_analitica;
    }

    public void setVoce_analitica(Voce_analiticaBulk voce_analitica) {
        this.voce_analitica = voce_analitica;
    }

    @Override
    public Integer getEsercizio_voce_ana() {
        return Optional.ofNullable(this.getVoce_analitica())
                .map(Voce_analiticaBulk::getEsercizio)
                .orElse(null);
    }

    @Override
    public void setEsercizio_voce_ana(Integer esercizio_voce_ana) {
        Optional.ofNullable(this.getVoce_analitica()).ifPresent(el->el.setEsercizio(esercizio_voce_ana));
    }

    @Override
    public String getCd_voce_ana() {
        return Optional.ofNullable(this.getVoce_analitica())
                .map(Voce_analiticaBulk::getCd_voce_ana)
                .orElse(null);
    }

    @Override
    public void setCd_voce_ana(String cd_voce_ana) {
        Optional.ofNullable(this.getVoce_analitica()).ifPresent(el->el.setCd_voce_ana(cd_voce_ana));
    }

    public WorkpackageBulk getLinea_attivita() {
        return linea_attivita;
    }

    public void setLinea_attivita(WorkpackageBulk newLinea_attivita) {
        linea_attivita = newLinea_attivita;
    }

    @Override
    public String getCd_linea_attivita() {
        return Optional.ofNullable(this.getLinea_attivita())
                .map(WorkpackageBulk::getCd_linea_attivita)
                .orElse(null);
    }

    @Override
    public void setCd_linea_attivita(String cd_linea_attivita) {
        Optional.ofNullable(this.getLinea_attivita()).ifPresent(el->el.setCd_linea_attivita(cd_linea_attivita));
    }

    @Override
    public String getCd_centro_responsabilita() {
        return Optional.ofNullable(this.getLinea_attivita())
                .map(WorkpackageBulk::getCd_centro_responsabilita)
                .orElse(null);
    }

    @Override
    public void setCd_centro_responsabilita(String cd_centro_responsabilita) {
        Optional.ofNullable(this.getLinea_attivita()).ifPresent(el->el.setCd_centro_responsabilita(cd_centro_responsabilita));
    }

    public IDocumentoDetailEcoCogeBulk getFather() {
        return getOrdineAcqConsegna();
    }

    public OrdineAcqConsegnaBulk getOrdineAcqConsegna() {
        return ordineAcqConsegna;
    }

    public void setOrdineAcqConsegna(OrdineAcqConsegnaBulk ordineAcqConsegna) {
        this.ordineAcqConsegna = ordineAcqConsegna;
    }

    @Override
    public String getCdCds() {
        return Optional.ofNullable(this.getOrdineAcqConsegna())
                .map(OrdineAcqConsegnaBulk::getCdCds)
                .orElse(null);
    }

    @Override
    public void setCdCds(String cdCds) {
        Optional.ofNullable(this.getOrdineAcqConsegna()).ifPresent(el->el.setCdCds(cdCds));
    }

    @Override
    public String getCdUnitaOperativa() {
        return Optional.ofNullable(this.getOrdineAcqConsegna())
                .map(OrdineAcqConsegnaBulk::getCdUnitaOperativa)
                .orElse(null);
    }

    @Override
    public void setCdUnitaOperativa(String cdUnitaOperativa) {
        Optional.ofNullable(this.getOrdineAcqConsegna()).ifPresent(el->el.setCdUnitaOperativa(cdUnitaOperativa));
    }

    @Override
    public Integer getEsercizio() {
        return Optional.ofNullable(this.getOrdineAcqConsegna())
                .map(OrdineAcqConsegnaBulk::getEsercizio)
                .orElse(null);
    }

    @Override
    public void setEsercizio(Integer esercizio) {
        Optional.ofNullable(this.getOrdineAcqConsegna()).ifPresent(el->el.setEsercizio(esercizio));
    }

    @Override
    public String getCdNumeratore() {
        return Optional.ofNullable(this.getOrdineAcqConsegna())
                .map(OrdineAcqConsegnaBulk::getCdNumeratore)
                .orElse(null);
    }

    @Override
    public void setCdNumeratore(String cdNumeratore) {
        Optional.ofNullable(this.getOrdineAcqConsegna()).ifPresent(el->el.setCdNumeratore(cdNumeratore));
    }

    @Override
    public Integer getNumero() {
        return Optional.ofNullable(this.getOrdineAcqConsegna())
                .map(OrdineAcqConsegnaBulk::getNumero)
                .orElse(null);
    }

    @Override
    public void setNumero(Integer numero) {
        Optional.ofNullable(this.getOrdineAcqConsegna()).ifPresent(el->el.setNumero(numero));
    }

    @Override
    public Integer getRiga() {
        return Optional.ofNullable(this.getOrdineAcqConsegna())
                .map(OrdineAcqConsegnaBulk::getRiga)
                .orElse(null);
    }

    @Override
    public void setRiga(Integer riga) {
        Optional.ofNullable(this.getOrdineAcqConsegna()).ifPresent(el->el.setRiga(riga));
    }

    @Override
    public Integer getConsegna() {
        return Optional.ofNullable(this.getOrdineAcqConsegna())
                .map(OrdineAcqConsegnaBulk::getConsegna)
                .orElse(null);
    }

    @Override
    public void setConsegna(Integer consegna) {
        Optional.ofNullable(this.getOrdineAcqConsegna()).ifPresent(el->el.setConsegna(consegna));
    }
}