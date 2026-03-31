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

package it.cnr.contab.compensi00.docs.bulk;

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailEcoCogeBulk;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_analiticaBulk;

import java.util.Optional;

public class Compenso_riga_ecoBulk extends Compenso_riga_ecoBase implements IDocumentoDetailAnaCogeBulk {
    private CompensoBulk compenso;

    protected Voce_analiticaBulk voce_analitica = new Voce_analiticaBulk();

    WorkpackageBulk linea_attivita = new WorkpackageBulk();

    public Compenso_riga_ecoBulk() {
        super();
    }

    public Compenso_riga_ecoBulk(String cd_cds, String cd_unita_organizzativa, Integer esercizio, Long pg_compenso, Long progressivo_riga_eco) {
        super(cd_cds, cd_unita_organizzativa, esercizio, pg_compenso, progressivo_riga_eco);
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
        return getCompenso();
    }

    public CompensoBulk getCompenso() {
        return compenso;
    }

    public void setCompenso(CompensoBulk compenso) {
        this.compenso = compenso;
    }

    @Override
    public Integer getEsercizio() {
        return Optional.ofNullable(this.getCompenso())
                .map(CompensoBulk::getEsercizio)
                .orElse(null);
    }

    @Override
    public void setEsercizio(Integer esercizio) {
        Optional.ofNullable(this.getCompenso()).ifPresent(el->el.setEsercizio(esercizio));
    }

    @Override
    public String getCd_cds() {
        return Optional.ofNullable(this.getCompenso())
                .map(CompensoBulk::getCd_cds)
                .orElse(null);
    }

    @Override
    public void setCd_cds(String cd_cds) {
        Optional.ofNullable(this.getCompenso()).ifPresent(el->el.setCd_cds(cd_cds));
    }

    @Override
    public String getCd_unita_organizzativa() {
        return Optional.ofNullable(this.getCompenso())
                .map(CompensoBulk::getCd_unita_organizzativa)
                .orElse(null);
    }

    @Override
    public void setCd_unita_organizzativa(String cd_unita_organizzativa) {
        Optional.ofNullable(this.getCompenso()).ifPresent(el->el.setCd_unita_organizzativa(cd_unita_organizzativa));
    }

    @Override
    public Long getPg_compenso() {
        return Optional.ofNullable(this.getCompenso())
                .map(CompensoBulk::getPg_compenso)
                .orElse(null);
    }

    @Override
    public void setPg_compenso(Long pg_compenso) {
        Optional.ofNullable(this.getCompenso()).ifPresent(el->el.setPg_compenso(pg_compenso));
    }
}