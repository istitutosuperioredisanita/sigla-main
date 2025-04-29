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

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_analiticaBulk;

import java.util.Optional;

public abstract class Fattura_passiva_riga_ecoBulk extends Fattura_passiva_riga_ecoBase implements IDocumentoAmministrativoRigaEcoBulk {
    protected Voce_analiticaBulk voce_analitica = new Voce_analiticaBulk();

    WorkpackageBulk linea_attivita = new WorkpackageBulk();

    public Fattura_passiva_riga_ecoBulk() {
        super();
    }

    public Fattura_passiva_riga_ecoBulk(String cd_cds, String cd_unita_organizzativa, Integer esercizio, Long pg_fattura_passiva, Long progressivo_riga, Long progressivo_riga_eco) {
        super(cd_cds, cd_unita_organizzativa, esercizio, pg_fattura_passiva, progressivo_riga, progressivo_riga_eco);
    }

    public Voce_analiticaBulk getVoce_analitica() {
        return voce_analitica;
    }

    public void setVoce_analitica(Voce_analiticaBulk voce_analitica) {
        this.voce_analitica = voce_analitica;
    }

    @Override
    public java.lang.Integer getEsercizio_voce_ana() {
        return Optional.ofNullable(this.getVoce_analitica())
                .map(Voce_analiticaBulk::getEsercizio)
                .orElse(null);
    }

    @Override
    public void setEsercizio_voce_ana(java.lang.Integer esercizio_voce_ana) {
        Optional.ofNullable(this.getVoce_analitica()).ifPresent(el->el.setEsercizio(esercizio_voce_ana));
    }

    @Override
    public java.lang.String getCd_voce_ana() {
        return Optional.ofNullable(this.getVoce_analitica())
                .map(Voce_analiticaBulk::getCd_voce_ana)
                .orElse(null);
    }

    @Override
    public void setCd_voce_ana(java.lang.String cd_voce_ana) {
        Optional.ofNullable(this.getVoce_analitica()).ifPresent(el->el.setCd_voce_ana(cd_voce_ana));
    }

    public it.cnr.contab.config00.latt.bulk.WorkpackageBulk getLinea_attivita() {
        return linea_attivita;
    }

    public void setLinea_attivita(it.cnr.contab.config00.latt.bulk.WorkpackageBulk newLinea_attivita) {
        linea_attivita = newLinea_attivita;
    }

    @Override
    public java.lang.String getCd_linea_attivita() {
        return Optional.ofNullable(this.getLinea_attivita())
                .map(WorkpackageBulk::getCd_linea_attivita)
                .orElse(null);
    }

    @Override
    public void setCd_linea_attivita(java.lang.String cd_linea_attivita) {
        Optional.ofNullable(this.getLinea_attivita()).ifPresent(el->el.setCd_linea_attivita(cd_linea_attivita));
    }

    @Override
    public java.lang.String getCd_centro_responsabilita() {
        return Optional.ofNullable(this.getLinea_attivita())
                .map(WorkpackageBulk::getCd_centro_responsabilita)
                .orElse(null);
    }

    @Override
    public void setCd_centro_responsabilita(java.lang.String cd_centro_responsabilita) {
        Optional.ofNullable(this.getLinea_attivita()).ifPresent(el->el.setCd_centro_responsabilita(cd_centro_responsabilita));
    }

    public IDocumentoAmministrativoRigaBulk getFather() {
        return getFattura_passiva_riga();
    }

    public abstract Fattura_passiva_rigaBulk getFattura_passiva_riga();

    public abstract void setFattura_passiva_riga(Fattura_passiva_rigaBulk fattura_passiva_riga);
}