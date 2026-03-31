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

package it.cnr.contab.docamm00.docs.bulk;

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_analiticaBulk;

import java.util.Optional;

public class Documento_generico_riga_ecoBulk extends Documento_generico_riga_ecoBase implements IDocumentoDetailAnaCogeBulk {
    private Documento_generico_rigaBulk documento_generico_rigaBulk;

    protected Voce_analiticaBulk voce_analitica = new Voce_analiticaBulk();

    WorkpackageBulk linea_attivita = new WorkpackageBulk();

    public Documento_generico_riga_ecoBulk() {
        super();
    }

    public Documento_generico_riga_ecoBulk(String cd_cds, String cd_unita_organizzativa, Integer esercizio, String cd_tipo_documento_amm, Long pg_documento_generico, Long progressivo_riga, Long progressivo_riga_eco) {
        super(cd_cds, cd_unita_organizzativa, esercizio, cd_tipo_documento_amm, pg_documento_generico, progressivo_riga, progressivo_riga_eco);
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

    public IDocumentoAmministrativoRigaBulk getFather() {
        return getDocumento_generico_rigaBulk();
    }

    public Documento_generico_rigaBulk getDocumento_generico_rigaBulk() {
        return documento_generico_rigaBulk;
    }

    public void setDocumento_generico_rigaBulk(Documento_generico_rigaBulk documento_generico_rigaBulk) {
        this.documento_generico_rigaBulk = documento_generico_rigaBulk;
    }

    @Override
    public Integer getEsercizio() {
        return Optional.ofNullable(this.getDocumento_generico_rigaBulk())
                .map(Documento_generico_rigaBulk::getEsercizio)
                .orElse(null);
    }

    @Override
    public void setEsercizio(Integer esercizio) {
        Optional.ofNullable(this.getDocumento_generico_rigaBulk()).ifPresent(el->el.setEsercizio(esercizio));
    }

    @Override
    public String getCd_cds() {
        return Optional.ofNullable(this.getDocumento_generico_rigaBulk())
                .map(Documento_generico_rigaBulk::getCd_cds)
                .orElse(null);
    }

    @Override
    public void setCd_cds(String cd_cds) {
        Optional.ofNullable(this.getDocumento_generico_rigaBulk()).ifPresent(el->el.setCd_cds(cd_cds));
    }

    @Override
    public String getCd_unita_organizzativa() {
        return Optional.ofNullable(this.getDocumento_generico_rigaBulk())
                .map(Documento_generico_rigaBulk::getCd_unita_organizzativa)
                .orElse(null);
    }

    @Override
    public void setCd_unita_organizzativa(String cd_unita_organizzativa) {
        Optional.ofNullable(this.getDocumento_generico_rigaBulk()).ifPresent(el->el.setCd_unita_organizzativa(cd_unita_organizzativa));
    }

    @Override
    public String getCd_tipo_documento_amm() {
        return Optional.ofNullable(this.getDocumento_generico_rigaBulk())
                .map(Documento_generico_rigaBulk::getCd_tipo_documento_amm)
                .orElse(null);
    }

    @Override
    public void setCd_tipo_documento_amm(String cd_tipo_documento_amm) {
        Optional.ofNullable(this.getDocumento_generico_rigaBulk()).ifPresent(el->el.setCd_tipo_documento_amm(cd_tipo_documento_amm));
    }

    @Override
    public Long getPg_documento_generico() {
        return Optional.ofNullable(this.getDocumento_generico_rigaBulk())
                .map(Documento_generico_rigaBulk::getPg_documento_generico)
                .orElse(null);
    }

    @Override
    public void setPg_documento_generico(Long pg_documento_generico) {
        Optional.ofNullable(this.getDocumento_generico_rigaBulk()).ifPresent(el->el.setPg_documento_generico(pg_documento_generico));
    }

    @Override
    public Long getProgressivo_riga() {
        return Optional.ofNullable(this.getDocumento_generico_rigaBulk())
                .map(Documento_generico_rigaBulk::getProgressivo_riga)
                .orElse(null);
    }

    @Override
    public void setProgressivo_riga(Long progressivo_riga) {
        Optional.ofNullable(this.getDocumento_generico_rigaBulk()).ifPresent(el->el.setProgressivo_riga(progressivo_riga));
    }
}