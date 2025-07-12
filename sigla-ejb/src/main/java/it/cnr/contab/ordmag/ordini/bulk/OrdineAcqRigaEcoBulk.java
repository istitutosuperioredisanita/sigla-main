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

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_analiticaBulk;
import it.cnr.contab.docamm00.docs.bulk.Documento_generico_rigaBulk;
import it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoRigaBulk;

import java.util.Optional;

public class OrdineAcqRigaEcoBulk extends OrdineAcqRigaEcoBase implements IDocumentoDetailAnaCogeBulk {
    private OrdineAcqRigaBulk ordineAcqRigaBulk;

    protected Voce_analiticaBulk voce_analitica = new Voce_analiticaBulk();

    WorkpackageBulk linea_attivita = new WorkpackageBulk();

    public OrdineAcqRigaEcoBulk() {
        super();
    }

    public OrdineAcqRigaEcoBulk(java.lang.String cdCds, java.lang.String cdUnitaOperativa, java.lang.Integer esercizio, java.lang.String cdNumeratore, java.lang.Integer numero, java.lang.Integer riga, java.lang.Long progressivo_riga_eco) {
        super(cdCds, cdUnitaOperativa, esercizio, cdNumeratore, numero, riga, progressivo_riga_eco);
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
        return getOrdineAcqRigaBulk();
    }

    public OrdineAcqRigaBulk getOrdineAcqRigaBulk() {
        return ordineAcqRigaBulk;
    }

    public void setOrdineAcqRigaBulk(OrdineAcqRigaBulk ordineAcqRigaBulk) {
        this.ordineAcqRigaBulk = ordineAcqRigaBulk;
    }

    @Override
    public String getCdCds() {
        return Optional.ofNullable(this.getOrdineAcqRigaBulk())
                .map(OrdineAcqRigaBulk::getCdCds)
                .orElse(null);
    }

    @Override
    public void setCdCds(String cdCds) {
        Optional.ofNullable(this.getOrdineAcqRigaBulk()).ifPresent(el->el.setCdCds(cdCds));
    }

    @Override
    public String getCdUnitaOperativa() {
        return Optional.ofNullable(this.getOrdineAcqRigaBulk())
                .map(OrdineAcqRigaBulk::getCdUnitaOperativa)
                .orElse(null);
    }

    @Override
    public void setCdUnitaOperativa(String cdUnitaOperativa) {
        Optional.ofNullable(this.getOrdineAcqRigaBulk()).ifPresent(el->el.setCdUnitaOperativa(cdUnitaOperativa));
    }

    @Override
    public Integer getEsercizio() {
        return Optional.ofNullable(this.getOrdineAcqRigaBulk())
                .map(OrdineAcqRigaBulk::getEsercizio)
                .orElse(null);
    }

    @Override
    public void setEsercizio(Integer esercizio) {
        Optional.ofNullable(this.getOrdineAcqRigaBulk()).ifPresent(el->el.setEsercizio(esercizio));
    }

    @Override
    public String getCdNumeratore() {
        return Optional.ofNullable(this.getOrdineAcqRigaBulk())
                .map(OrdineAcqRigaBulk::getCdNumeratore)
                .orElse(null);
    }

    @Override
    public void setCdNumeratore(String cdNumeratore) {
        Optional.ofNullable(this.getOrdineAcqRigaBulk()).ifPresent(el->el.setCdNumeratore(cdNumeratore));
    }

    @Override
    public Integer getNumero() {
        return Optional.ofNullable(this.getOrdineAcqRigaBulk())
                .map(OrdineAcqRigaBulk::getNumero)
                .orElse(null);
    }

    @Override
    public void setNumero(Integer numero) {
        Optional.ofNullable(this.getOrdineAcqRigaBulk()).ifPresent(el->el.setNumero(numero));
    }

    @Override
    public Integer getRiga() {
        return Optional.ofNullable(this.getOrdineAcqRigaBulk())
                .map(OrdineAcqRigaBulk::getRiga)
                .orElse(null);
    }

    @Override
    public void setRiga(Integer riga) {
        Optional.ofNullable(this.getOrdineAcqRigaBulk()).ifPresent(el->el.setRiga(riga));
    }
}