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

/*
* Created by Generator 1.0
* Date 05/05/2006
*/
package it.cnr.contab.prevent00.bulk;

import it.cnr.contab.util.Utility;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class V_assestatoallBulk extends OggettoBulk implements KeyedPersistent {
	public V_assestatoallBulk() {
		super();
	}
	public V_assestatoallBulk(Integer esercizio, Integer esercizio_res, String cd_centro_responsabilita, String cd_linea_attivita, String ti_appartenenza, String ti_gestione, String cd_elemento_voce) {
		super();
		this.esercizio = esercizio;
		this.esercizio_res = esercizio_res;
		this.cd_centro_responsabilita = cd_centro_responsabilita;	
		this.cd_linea_attivita = cd_linea_attivita;
		this.ti_appartenenza = ti_appartenenza;
		this.ti_gestione = ti_gestione;
		this.cd_elemento_voce = cd_elemento_voce;
	}	
	
//    ESERCIZIO DECIMAL(4,0)
	private Integer esercizio;
 
//    ESERCIZIO_RES DECIMAL(4,0)
	private Integer esercizio_res;
 
//    CD_CENTRO_RESPONSABILITA VARCHAR(30)
	private String cd_centro_responsabilita;
 
//    CD_LINEA_ATTIVITA VARCHAR(10)
	private String cd_linea_attivita;
 
//    DS_LINEA_ATTIVITA VARCHAR(300)
	private String ds_linea_attivita;

//    CD_NATURA VARCHAR(1)
	private String cd_natura;

//    TI_APPARTENENZA CHAR(1)
	private String ti_appartenenza;
 
//    TI_GESTIONE CHAR(1)
	private String ti_gestione;
 
//    CD_VOCE VARCHAR(50)
	private String cd_voce;
 
//    CD_ELEMENTO_VOCE VARCHAR(20)
	private String cd_elemento_voce;
 
//    CD_MODULO VARCHAR(50)
	private String cd_modulo;

//    STANZIAMENTO_INIZIALE DECIMAL(22,0)
	private java.math.BigDecimal stanziamento_iniziale;

//    VARIAZIONI_POSITIVE DECIMAL(22,0)
	private java.math.BigDecimal variazioni_positive;

//    VARIAZIONI_NEGATIVE DECIMAL(22,0)
	private java.math.BigDecimal variazioni_negative;

//    VARIAZIONI_RESIDUI_PROPRI DECIMAL(22,0)
	private java.math.BigDecimal variazioni_residui_propri;

//    ASSESTATO_INIZIALE DECIMAL(22,0)
	private java.math.BigDecimal assestato_iniziale;
 
//    IMPORTO_UTILIZZATO DECIMAL(22,0)
	private java.math.BigDecimal importo_utilizzato;
 
//    IMPORTO_DISPONIBILE DECIMAL(22,0)
	private java.math.BigDecimal importo_disponibile;


	private java.math.BigDecimal importo_ini_residui_propri;

//    VARIAZIONI_PROVVISORIE DECIMAL(22,0)
	private java.math.BigDecimal variazioni_provvisorie;
 
//    VARIAZIONI_DEFINITIVE DECIMAL(22,0)
	private java.math.BigDecimal variazioni_definitive;
 
//    ASSESTATO_FINALE DECIMAL(22,0)
	private java.math.BigDecimal assestato_finale;


	private java.math.BigDecimal assestato_cassa;

//  IMPORTO_VINCOLI DECIMAL(22,0)
	private java.math.BigDecimal importo_vincoli;


	private java.math.BigDecimal importo_manrev;

	protected Timestamp progetto_dt_inizio;
	
	protected Timestamp progetto_dt_fine;
	
	protected Timestamp progetto_dt_proroga;
	
	//    DB_IMP_UTILIZZATO DECIMAL(22,0)
//    utilizzata per memorizzare il valore iniziale dell'importo dell'oggetto 
//    che risulta già sottratto alla disponibilità mostrata
	private java.math.BigDecimal db_imp_utilizzato;
	
	public Integer getEsercizio () {
		return esercizio;
	}
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	public Integer getEsercizio_res () {
		return esercizio_res;
	}
	public void setEsercizio_res(Integer esercizio_res)  {
		this.esercizio_res=esercizio_res;
	}
	public String getCd_centro_responsabilita () {
		return cd_centro_responsabilita;
	}
	public void setCd_centro_responsabilita(String cd_centro_responsabilita)  {
		this.cd_centro_responsabilita=cd_centro_responsabilita;
	}
	public String getCd_linea_attivita () {
		return cd_linea_attivita;
	}
	public void setCd_linea_attivita(String cd_linea_attivita)  {
		this.cd_linea_attivita=cd_linea_attivita;
	}
	public String getTi_appartenenza () {
		return ti_appartenenza;
	}
	public void setTi_appartenenza(String ti_appartenenza)  {
		this.ti_appartenenza=ti_appartenenza;
	}
	public String getTi_gestione () {
		return ti_gestione;
	}
	public void setTi_gestione(String ti_gestione)  {
		this.ti_gestione=ti_gestione;
	}
	public String getCd_voce () {
		return cd_voce;
	}
	public void setCd_voce(String cd_voce)  {
		this.cd_voce=cd_voce;
	}
	public String getCd_elemento_voce () {
		return cd_elemento_voce;
	}
	public void setCd_elemento_voce(String cd_elemento_voce)  {
		this.cd_elemento_voce=cd_elemento_voce;
	}
	public java.math.BigDecimal getAssestato_iniziale () {
		return assestato_iniziale;
	}
	public void setAssestato_iniziale(java.math.BigDecimal assestato_iniziale)  {
		this.assestato_iniziale=assestato_iniziale;
	}
	public java.math.BigDecimal getImporto_utilizzato () {
		return importo_utilizzato;
	}
	public void setImporto_utilizzato(java.math.BigDecimal importo_utilizzato)  {
		this.importo_utilizzato=importo_utilizzato;
	}
	public java.math.BigDecimal getImporto_disponibile () {
		return importo_disponibile;
	}
	public void setImporto_disponibile(java.math.BigDecimal importo_disponibile)  {
		this.importo_disponibile=importo_disponibile;
	}
	public java.math.BigDecimal getVariazioni_provvisorie () {
		return variazioni_provvisorie;
	}
	public void setVariazioni_provvisorie(java.math.BigDecimal variazioni_provvisorie)  {
		this.variazioni_provvisorie=variazioni_provvisorie;
	}
	public java.math.BigDecimal getVariazioni_definitive () {
		return variazioni_definitive;
	}
	public void setVariazioni_definitive(java.math.BigDecimal variazioni_definitive)  {
		this.variazioni_definitive=variazioni_definitive;
	}
	public java.math.BigDecimal getAssestato_finale () {
		return assestato_finale;
	}
	public void setAssestato_finale(java.math.BigDecimal assestato_finale)  {
		this.assestato_finale=assestato_finale;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this == o) return true;
		if (!(o instanceof V_assestatoallBulk)) return false;
		V_assestatoallBulk k = (V_assestatoallBulk)o;
		if(!compareKey(getEsercizio(),k.getEsercizio())) return false;
		if(!compareKey(getEsercizio_res(),k.getEsercizio_res())) return false;			
		if(!compareKey(getCd_centro_responsabilita(),k.getCd_centro_responsabilita())) return false;
		if(!compareKey(getCd_linea_attivita(),k.getCd_linea_attivita())) return false;
		if(!compareKey(getTi_appartenenza(),k.getTi_appartenenza())) return false;
		if(!compareKey(getTi_gestione(),k.getTi_gestione())) return false;
		if(!compareKey(getCd_elemento_voce(),k.getCd_elemento_voce())) return false;
		return true;		
	}
	public int primaryKeyHashCode() {
		return
			calculateKeyHashCode(getEsercizio())+
			calculateKeyHashCode(getEsercizio_res())+
			calculateKeyHashCode(getCd_centro_responsabilita())+
			calculateKeyHashCode(getCd_linea_attivita())+
			calculateKeyHashCode(getTi_appartenenza())+
			calculateKeyHashCode(getTi_gestione()) +
		    calculateKeyHashCode(getCd_elemento_voce());
	}
	public String getDs_linea_attivita() {
		return ds_linea_attivita;
	}
	
	public void setDs_linea_attivita(String ds_linea_attivita) {
		this.ds_linea_attivita = ds_linea_attivita;
	}

	public String getDett_imp_voce () {
		String label = "Stanziamento Iniziale: " + new it.cnr.contab.util.EuroFormat().format(getStanziamento_iniziale());
		if (getVariazioni_positive().subtract(getVariazioni_negative()).compareTo(Utility.ZERO)!=0)
			label = label.concat("\rVariazioni: " + new it.cnr.contab.util.EuroFormat().format(getVariazioni_positive().subtract(getVariazioni_negative())));
		if (getVariazioni_residui_propri().compareTo(Utility.ZERO)!=0)
			label = label.concat("\rVariazioni Residui Propri: " + new it.cnr.contab.util.EuroFormat().format(getVariazioni_residui_propri().negate()));
		if (getImporto_utilizzato().compareTo(Utility.ZERO)!=0)
			label = label.concat("\rImpegni assunti: " + new it.cnr.contab.util.EuroFormat().format(getImporto_utilizzato().subtract(Utility.nvl(getDb_imp_utilizzato()))));
		if (getImporto_vincoli().compareTo(Utility.ZERO)!=0)
			label = label.concat("\rVincoli: " + new it.cnr.contab.util.EuroFormat().format(getImporto_vincoli()));
		return label;
	}

	public java.math.BigDecimal getStanziamento_iniziale() {
		return stanziamento_iniziale;
	}
	
	public void setStanziamento_iniziale(java.math.BigDecimal stanziamento_iniziale) {
		this.stanziamento_iniziale = stanziamento_iniziale;
	}
	
	public java.math.BigDecimal getVariazioni_negative() {
		return variazioni_negative;
	}
	
	public void setVariazioni_negative(java.math.BigDecimal variazioni_negative) {
		this.variazioni_negative = variazioni_negative;
	}
	
	public java.math.BigDecimal getVariazioni_positive() {
		return variazioni_positive;
	}
	
	public void setVariazioni_positive(java.math.BigDecimal variazioni_positive) {
		this.variazioni_positive = variazioni_positive;
	}
	
	public String getCd_modulo() {
		return cd_modulo;
	}
	
	public void setCd_modulo(String cd_modulo) {
		this.cd_modulo = cd_modulo;
	}


	public java.math.BigDecimal getVariazioni_residui_propri() {
		return variazioni_residui_propri;
	}
	
	public void setVariazioni_residui_propri(
			java.math.BigDecimal variazioni_residui_propri) {
		this.variazioni_residui_propri = variazioni_residui_propri;
	}

	/**
	 * metodo utilizzato per memorizzare il valore iniziale dell'importo dell'oggetto 
	 * che risulta già sottratto alla disponibilità mostrata
	 */
	public java.math.BigDecimal getDb_imp_utilizzato() {
		return db_imp_utilizzato;
	}

	/**
	 * metodo utilizzato per memorizzare il valore iniziale dell'importo dell'oggetto 
	 * che risulta già sottratto alla disponibilità mostrata
	 */
	public void setDb_imp_utilizzato(java.math.BigDecimal db_imp_utilizzato) {
		this.db_imp_utilizzato = db_imp_utilizzato;
	}
	
	public boolean equals(Object obj) {
		return equalsByPrimaryKey(obj);
	}
	public String getCd_natura() {
		return cd_natura;
	}
	public void setCd_natura(String cd_natura) {
		this.cd_natura = cd_natura;
	}
	
	public java.math.BigDecimal getImporto_vincoli() {
		return importo_vincoli;
	}
	
	public void setImporto_vincoli(java.math.BigDecimal importo_vincoli) {
		this.importo_vincoli = importo_vincoli;
	}
	public Timestamp getProgetto_dt_inizio() {
		return progetto_dt_inizio;
	}
	public void setProgetto_dt_inizio(Timestamp progetto_dt_inizio) {
		this.progetto_dt_inizio = progetto_dt_inizio;
	}
	public Timestamp getProgetto_dt_fine() {
		return progetto_dt_fine;
	}
	public void setProgetto_dt_fine(Timestamp progetto_dt_fine) {
		this.progetto_dt_fine = progetto_dt_fine;
	}
	public Timestamp getProgetto_dt_proroga() {
		return progetto_dt_proroga;
	}
	public void setProgetto_dt_proroga(Timestamp progetto_dt_proroga) {
		this.progetto_dt_proroga = progetto_dt_proroga;
	}


	public BigDecimal getImporto_ini_residui_propri() {
		return importo_ini_residui_propri;
	}

	public void setImporto_ini_residui_propri(BigDecimal importo_ini_residui_propri) {
		this.importo_ini_residui_propri = importo_ini_residui_propri;
	}

	public BigDecimal getImporto_manrev() {
		return importo_manrev;
	}

	public void setImporto_manrev(BigDecimal importo_manrev) {
		this.importo_manrev = importo_manrev;
	}

	public BigDecimal getAssestato_cassa() {
		return assestato_cassa;
	}

	public void setAssestato_cassa(BigDecimal assestato_cassa) {
		this.assestato_cassa = assestato_cassa;
	}


}