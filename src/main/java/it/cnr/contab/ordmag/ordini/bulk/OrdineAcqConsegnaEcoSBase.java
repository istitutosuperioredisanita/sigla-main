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

import it.cnr.jada.persistency.Keyed;

import java.math.BigDecimal;

public class OrdineAcqConsegnaEcoSBase extends OrdineAcqConsegnaEcoSKey implements Keyed {
    private String tipoStorico;

    private String cdCds;
    private String cdUnitaOperativa;
    private Integer esercizio;
    private String cdNumeratore;
    private Integer numero;
    private Integer riga;
    private java.lang.Integer consegna;
    private Long progressivo_riga_eco;

    private java.lang.String cdCdsFattura;
    private java.lang.String cdUnitaOrganizzativaFattura;
    private java.lang.Integer esercizioFattura;
    private java.lang.Long pgFatturaPassiva;
    private java.lang.Long progressivoRigaFattura;

    // ESERCIZIO_VOCE_ANA DECIMAL(4,0) NOT NULL
    private Integer esercizio_voce_ana;

    // CD_VOCE_ANA VARCHAR(45) NOT NULL
    private String cd_voce_ana;

    // CD_CENTRO_RESPONSABILITA VARCHAR(30) NOT NULL
    private String cd_centro_responsabilita;

    // CD_LINEA_ATTIVITA VARCHAR(10) NOT NULL
    private String cd_linea_attivita;

    private BigDecimal importo;

    public OrdineAcqConsegnaEcoSBase() {
        super();
    }

    public OrdineAcqConsegnaEcoSBase(Long id) {
        super(id);
    }

    public String getTipoStorico() {
        return tipoStorico;
    }

    public void setTipoStorico(String tipoStorico) {
        this.tipoStorico = tipoStorico;
    }

    public String getCdCds() {
        return cdCds;
    }

    public void setCdCds(String cdCds) {
        this.cdCds = cdCds;
    }

    public String getCdUnitaOperativa() {
        return cdUnitaOperativa;
    }

    public void setCdUnitaOperativa(String cdUnitaOperativa) {
        this.cdUnitaOperativa = cdUnitaOperativa;
    }

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public String getCdNumeratore() {
        return cdNumeratore;
    }

    public void setCdNumeratore(String cdNumeratore) {
        this.cdNumeratore = cdNumeratore;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getRiga() {
        return riga;
    }

    public void setRiga(Integer riga) {
        this.riga = riga;
    }

    public Integer getConsegna() {
        return consegna;
    }

    public void setConsegna(Integer consegna) {
        this.consegna = consegna;
    }

    public Long getProgressivo_riga_eco() {
        return progressivo_riga_eco;
    }

    public void setProgressivo_riga_eco(Long progressivo_riga_eco) {
        this.progressivo_riga_eco = progressivo_riga_eco;
    }

    public String getCdCdsFattura() {
        return cdCdsFattura;
    }

    public void setCdCdsFattura(String cdCdsFattura) {
        this.cdCdsFattura = cdCdsFattura;
    }

    public String getCdUnitaOrganizzativaFattura() {
        return cdUnitaOrganizzativaFattura;
    }

    public void setCdUnitaOrganizzativaFattura(String cdUnitaOrganizzativaFattura) {
        this.cdUnitaOrganizzativaFattura = cdUnitaOrganizzativaFattura;
    }

    public Integer getEsercizioFattura() {
        return esercizioFattura;
    }

    public void setEsercizioFattura(Integer esercizioFattura) {
        this.esercizioFattura = esercizioFattura;
    }

    public Long getPgFatturaPassiva() {
        return pgFatturaPassiva;
    }

    public void setPgFatturaPassiva(Long pgFatturaPassiva) {
        this.pgFatturaPassiva = pgFatturaPassiva;
    }

    public Long getProgressivoRigaFattura() {
        return progressivoRigaFattura;
    }

    public void setProgressivoRigaFattura(Long progressivoRigaFattura) {
        this.progressivoRigaFattura = progressivoRigaFattura;
    }

    public Integer getEsercizio_voce_ana() {
        return esercizio_voce_ana;
    }

    public void setEsercizio_voce_ana(Integer esercizio_voce_ana) {
        this.esercizio_voce_ana = esercizio_voce_ana;
    }

    public String getCd_voce_ana() {
        return cd_voce_ana;
    }

    public void setCd_voce_ana(String cd_voce_ana) {
        this.cd_voce_ana = cd_voce_ana;
    }

    public String getCd_centro_responsabilita() {
        return cd_centro_responsabilita;
    }

    public void setCd_centro_responsabilita(String cd_centro_responsabilita) {
        this.cd_centro_responsabilita = cd_centro_responsabilita;
    }

    public String getCd_linea_attivita() {
        return cd_linea_attivita;
    }

    public void setCd_linea_attivita(String cd_linea_attivita) {
        this.cd_linea_attivita = cd_linea_attivita;
    }

    public BigDecimal getImporto() {
        return importo;
    }

    public void setImporto(BigDecimal importo) {
        this.importo = importo;
    }
}
