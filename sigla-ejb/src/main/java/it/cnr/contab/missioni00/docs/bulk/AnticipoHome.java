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

package it.cnr.contab.missioni00.docs.bulk;

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrHome;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcep.bulk.*;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.docamm00.ejb.*;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scad_voceBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioHome;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.sql.*;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnticipoHome extends BulkHome implements it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoSpesaHome
{
public AnticipoHome(java.sql.Connection conn) {
	super(AnticipoBulk.class,conn);
}
public AnticipoHome(java.sql.Connection conn,PersistentCache persistentCache) {
	super(AnticipoBulk.class,conn,persistentCache);
}
/**
 * Il metodo ritorna la data di registrazione piu' alta degli anticipi fino ad ora registrati
 */
public java.sql.Timestamp findDataRegistrazioneUltimoAnticipo( AnticipoBulk anticipo ) throws PersistencyException, java.sql.SQLException
{
	LoggableStatement ps = new LoggableStatement(getConnection(),
		"SELECT MAX(DT_REGISTRAZIONE) " +			
		"FROM " +
		it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() + 			
		"ANTICIPO WHERE " +
		"ESERCIZIO = ? AND CD_CDS = ? AND CD_UNITA_ORGANIZZATIVA = ? ",true,this.getClass());
		ps.setObject( 1, anticipo.getEsercizio() );
		ps.setString( 2, anticipo.getCd_cds() );
		ps.setString( 3, anticipo.getCd_unita_organizzativa());
	
	java.sql.ResultSet rs = ps.executeQuery();
	if(rs.next())
		return rs.getTimestamp(1);
	else
		return null;
}
/**
 * Imposta il pg_anticipo di un oggetto AnticipoBulk.
 */

public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext,OggettoBulk bulk) throws it.cnr.jada.comp.ComponentException 
{
	AnticipoBulk anticipo = (AnticipoBulk) bulk;

	try
	{
		// Assegno un nuovo progressivo all'anticipo 
		ProgressiviAmmComponentSession progressiviSession = (ProgressiviAmmComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCAMM00_EJB_ProgressiviAmmComponentSession", ProgressiviAmmComponentSession.class);
		Numerazione_doc_ammBulk numerazione = new Numerazione_doc_ammBulk(anticipo);
		anticipo.setPg_anticipo(progressiviSession.getNextPG(userContext, numerazione));
	}
	catch(Throwable e)
	{
		throw new it.cnr.jada.comp.ComponentException(e);
	}
}
/**
 * Metodo richiesto dall' interfaccia IDocumentoAmministrativoSpesaHome
 * Il metodo aggiorna l'anticipo dopo che e' stato collegato ad una spesa del Fondo Economale
 */

public void updateFondoEconomale(it.cnr.contab.fondecon00.core.bulk.Fondo_spesaBulk spesa) throws PersistencyException, OutdatedResourceException, BusyResourceException 
{
	if (spesa == null) 
		return;

	AnticipoBulk anticipo = (AnticipoBulk)spesa.getDocumento();

	lock(anticipo);
	
	StringBuffer stm = new StringBuffer("UPDATE ");
	stm.append(it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema());
	stm.append(getColumnMap().getTableName());
	stm.append(" SET STATO_PAGAMENTO_FONDO_ECO = ?, DT_PAGAMENTO_FONDO_ECO = ?, PG_VER_REC = PG_VER_REC+1, DUVA = ?, UTUV = ?");
	stm.append(" WHERE (");
	stm.append("CD_CDS = ? AND CD_UNITA_ORGANIZZATIVA = ? AND ESERCIZIO = ? AND PG_ANTICIPO = ? )");
	
	try 
	{
		LoggableStatement ps = new LoggableStatement(getConnection(),stm.toString(),true,this.getClass());
		try 
		{	
			ps.setString(1, (spesa.isToBeCreated() || spesa.isToBeUpdated()) ? anticipo.STATO_REGISTRATO_FONDO_ECO : anticipo.STATO_ASSEGNATO_FONDO_ECO);
			if (spesa.isToBeCreated() || spesa.isToBeUpdated())
				ps.setTimestamp(2, spesa.getDt_spesa());
			else 
				ps.setNull(2, java.sql.Types.TIMESTAMP);

			ps.setTimestamp(3, getServerTimestamp());
			ps.setString(4, spesa.getUser());
				
			ps.setString(5, anticipo.getCd_cds());
			ps.setString(6, anticipo.getCd_unita_organizzativa());
			ps.setInt(7, anticipo.getEsercizio());
			ps.setLong(8, anticipo.getPg_anticipo());

			ps.executeUpdate();
		} 
		finally 
		{
			try{ps.close();}catch( java.sql.SQLException ignored){}
        }
	} 
	catch(java.sql.SQLException e) 
	{
		throw it.cnr.jada.persistency.sql.SQLExceptionHandler.getInstance().handleSQLException(e,spesa);
	}
}

	public java.util.List<Anticipo_riga_ecoBulk> findAnticipoRigheEcoList(AnticipoBulk docRiga ) throws PersistencyException {
		PersistentHome home = getHomeCache().getHome(Anticipo_riga_ecoBulk.class);

		it.cnr.jada.persistency.sql.SQLBuilder sql = home.createSQLBuilder();
		sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, docRiga.getEsercizio());
		sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, docRiga.getCd_cds());
		sql.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, docRiga.getCd_unita_organizzativa());
		sql.addClause(FindClause.AND, "pg_anticipo", SQLBuilder.EQUALS, docRiga.getPg_anticipo());
		return home.fetchAll(sql);
	}

	private ContoBulk getContoEconomicoDefault(AnticipoBulk anticipo) throws ComponentException {
		if (Optional.ofNullable(anticipo).isPresent()) {
			Configurazione_cnrHome configHome = (Configurazione_cnrHome) getHomeCache().getHome(Configurazione_cnrBulk.class);
			return configHome.getContoAnticipo(anticipo);
		}
		return null;
    }

	private List<Anticipo_riga_ecoBulk> getDatiAnaliticiDefault(UserContext userContext, AnticipoBulk anticipo) throws ComponentException, PersistencyException {
		List<Anticipo_riga_ecoBulk> result = new ArrayList<>();

		Fattura_passivaHome fatpasHome = (Fattura_passivaHome) getHomeCache().getHome(Fattura_passivaBulk.class);
		if (Optional.ofNullable(anticipo.getScadenza_obbligazione()).isPresent()) {
			Obbligazione_scadenzarioBulk obbligScad = (Obbligazione_scadenzarioBulk) fatpasHome.loadIfNeededObject(anticipo.getScadenza_obbligazione());

			if (Optional.ofNullable(obbligScad).isPresent()) {
				//carico i dettagli analitici recuperandoli dall'obbligazione_scad_voce
				Obbligazione_scadenzarioHome obbligazioneScadenzarioHome = (Obbligazione_scadenzarioHome) getHomeCache().getHome(Obbligazione_scadenzarioBulk.class);
				List<Obbligazione_scad_voceBulk> scadVoceBulks = obbligazioneScadenzarioHome.findObbligazione_scad_voceList(userContext, obbligScad,Boolean.FALSE);
				BigDecimal totScad = scadVoceBulks.stream().map(Obbligazione_scad_voceBulk::getIm_voce).reduce(BigDecimal.ZERO, BigDecimal::add);
				for (Obbligazione_scad_voceBulk scadVoce : scadVoceBulks) {
					Anticipo_riga_ecoBulk myRigaEco = new Anticipo_riga_ecoBulk();
					myRigaEco.setProgressivo_riga_eco((long) result.size() + 1);
					myRigaEco.setLinea_attivita(scadVoce.getLinea_attivita());
					myRigaEco.setAnticipo(anticipo);
					myRigaEco.setImporto(scadVoce.getIm_voce().multiply(anticipo.getImCostoEco()).divide(totScad, 2, RoundingMode.HALF_UP));
					myRigaEco.setToBeCreated();
					result.add(myRigaEco);
				}
				BigDecimal totRipartito = result.stream().map(Anticipo_riga_ecoBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal diff = totRipartito.subtract(anticipo.getImCostoEco());

				if (diff.compareTo(BigDecimal.ZERO) > 0) {
					for (Anticipo_riga_ecoBulk rigaEco : result) {
						if (rigaEco.getImporto().compareTo(diff) >= 0) {
							rigaEco.setImporto(rigaEco.getImporto().subtract(diff));
							break;
						} else {
							diff = diff.subtract(rigaEco.getImporto());
							rigaEco.setImporto(BigDecimal.ZERO);
						}
					}
				} else if (diff.compareTo(BigDecimal.ZERO) < 0) {
					for (Anticipo_riga_ecoBulk rigaEco : result) {
						rigaEco.setImporto(rigaEco.getImporto().add(diff));
						break;
					}
				}
			}
		} else {
			Configurazione_cnrHome configHome = (Configurazione_cnrHome) getHomeCache().getHome(Configurazione_cnrBulk.class);
			WorkpackageBulk gaeDefault = configHome.getGaeAnticipo(userContext, anticipo);

			Anticipo_riga_ecoBulk myRigaEco = new Anticipo_riga_ecoBulk();
			myRigaEco.setProgressivo_riga_eco(1L);
			myRigaEco.setLinea_attivita(gaeDefault);
			myRigaEco.setAnticipo(anticipo);
			myRigaEco.setImporto(anticipo.getImCostoEco());
			myRigaEco.setToBeCreated();
			result.add(myRigaEco);
		}
		return result.stream()
				.filter(el->el.getImporto().compareTo(BigDecimal.ZERO)!=0)
				.collect(Collectors.toList());
	}

	public ContoBulk getContoEconomicoForMissione(AnticipoBulk anticipo) throws ComponentException {
		try {
			Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);
			if (Optional.ofNullable(anticipo).isPresent()) {
				anticipo = (AnticipoBulk)fatpasHome.loadIfNeededObject(anticipo);
				if (Optional.ofNullable(anticipo.getScadenza_obbligazione()).isPresent()) {
					Obbligazione_scadenzarioBulk obbligScad = (Obbligazione_scadenzarioBulk) fatpasHome.loadIfNeededObject(anticipo.getScadenza_obbligazione());

					if (Optional.ofNullable(obbligScad).isPresent()) {
						ObbligazioneBulk obblig = (ObbligazioneBulk) fatpasHome.loadIfNeededObject(obbligScad.getObbligazione());
						Ass_ev_voceepHome assEvVoceEpHome = (Ass_ev_voceepHome) getHomeCache().getHome(Ass_ev_voceepBulk.class);
						//Metto anticipo.getEsercizio() e non obblig.getEsercizio() perchè quest'ultimo cambia se anno ribaltato
						List<Ass_ev_voceepBulk> listAss = assEvVoceEpHome.findVociEpAssociateVoce(new Elemento_voceBulk(obblig.getCd_elemento_voce(), anticipo.getEsercizio(), obblig.getTi_appartenenza(), obblig.getTi_gestione()));
						return Optional.ofNullable(listAss).orElse(new ArrayList<>())
								.stream().map(Ass_ev_voceepBulk::getVoce_ep)
								.findAny().orElse(null);
					}
				} else {
					Configurazione_cnrHome configHome = (Configurazione_cnrHome) getHomeCache().getHome(Configurazione_cnrBulk.class);
					return configHome.getContoDocumentoNonLiquidabile(anticipo);
				}
			}
			return null;
		} catch (PersistencyException e) {
			throw new DetailedRuntimeException(e);
		}
	}

	public List<IDocumentoDetailAnaCogeBulk> getDatiAnaliticiForMissione(UserContext userContext, AnticipoBulk anticipo, ContoBulk aContoEconomico) throws ComponentException {
		try {
			List<Anticipo_riga_ecoBulk> result = new ArrayList<>();

			if (Optional.ofNullable(aContoEconomico).isPresent()) {
				List<Voce_analiticaBulk> voceAnaliticaList = ((Voce_analiticaHome) getHomeCache().getHome(Voce_analiticaBulk.class)).findVoceAnaliticaList(aContoEconomico);
				if (voceAnaliticaList.isEmpty())
					return new ArrayList<>();
				Voce_analiticaBulk voceAnaliticaDef = voceAnaliticaList.stream()
						.filter(Voce_analiticaBulk::getFl_default).findAny()
						.orElse(voceAnaliticaList.stream().findAny().orElse(null));

				List<Anticipo_riga_ecoBulk> aContiAnalitici = this.findAnticipoRigheEcoList(anticipo);
				if (aContiAnalitici.isEmpty())
					aContiAnalitici = this.getDatiAnaliticiDefault(userContext, anticipo);

				for (Anticipo_riga_ecoBulk rigaEco : aContiAnalitici) {
					Anticipo_riga_ecoBulk myRigaEco = new Anticipo_riga_ecoBulk();
					myRigaEco.setProgressivo_riga_eco((long) result.size() + 1);
					myRigaEco.setVoce_analitica(voceAnaliticaDef);
					myRigaEco.setLinea_attivita(rigaEco.getLinea_attivita());
					myRigaEco.setAnticipo(anticipo);
					myRigaEco.setImporto(rigaEco.getImporto());
					myRigaEco.setToBeCreated();
					result.add(myRigaEco);
				}
			}
			return result.stream()
					.filter(el->el.getImporto().compareTo(BigDecimal.ZERO)!=0)
					.collect(Collectors.toList());
		} catch (PersistencyException e) {
			throw new ComponentException(e);
		}
	}

	public Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> getDatiEconomiciDefault(UserContext userContext, AnticipoBulk anticipo) throws ComponentException, PersistencyException {
		ContoBulk aContoEconomico = this.getContoEconomicoDefault(anticipo);
		List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.getDatiAnaliticiDefault(userContext, anticipo).stream().map(IDocumentoDetailAnaCogeBulk.class::cast).collect(Collectors.toList());
		return Pair.of(aContoEconomico, aContiAnalitici);
	}

	public Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> getDatiEconomiciForMissione(UserContext userContext, AnticipoBulk anticipo) throws ComponentException {
		ContoBulk aContoEconomico = this.getContoEconomicoForMissione(anticipo);
		List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.getDatiAnaliticiForMissione(userContext,anticipo,aContoEconomico);
		return Pair.of(aContoEconomico, aContiAnalitici);
	}
}
