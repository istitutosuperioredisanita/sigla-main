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

package it.cnr.contab.coepcoan00.comp;

import it.cnr.contab.anagraf00.core.bulk.*;
import it.cnr.contab.coepcoan00.core.bulk.*;
import it.cnr.contab.coepcoan00.tabrif.bulk.Ass_anag_voce_epBulk;
import it.cnr.contab.coepcoan00.tabrif.bulk.Ass_anag_voce_epHome;
import it.cnr.contab.config00.esercizio.bulk.EsercizioBulk;
import it.cnr.contab.config00.pdcep.bulk.*;
import it.cnr.contab.config00.sto.bulk.*;
import it.cnr.contab.doccont00.comp.DateServices;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.*;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.*;
import it.cnr.jada.util.ArrayRemoteIterator;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.ejb.EJBCommonServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJBException;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

public class ScritturaPartitaDoppiaComponent extends CRUDComponent implements IScritturaPartitaDoppiaMgr,ICRUDMgr, Serializable,IPrintMgr{
	private final static Logger logger = LoggerFactory.getLogger(ScritturaPartitaDoppiaComponent.class);

	private final static Boolean DEFAULT_MODIFICABILE = Boolean.FALSE;
	private final static Boolean DEFAULT_ACCORPABILE = Boolean.TRUE;
	private final static String CD_VOCE_DOCUMENTO_NON_LIQUIDABILE = "XXXXXXXXX";

	/**
	 * ScritturaPartitaDoppiaComponent constructor comment.
	 */
	public ScritturaPartitaDoppiaComponent() {
		super();
	}
	/**
	 *
	 * Nome: Aggiornamento saldi
	 * Pre: È stata richiesto l'aggiornamento dei saldi coge come conseguenza di un inserimento di
	 *       una nuova scrittura in partita doppia
	 * Post: È stata richiamata la stored procedure che esegue l'aggiornamento dei saldi coge relativi ai
	 *       conti di tutti i movimenti dare e avere della scrittura
	 *
	 * @param userContext <code>UserContext</code>
	 * @param scrittura <code>Scrittura_partita_doppiaBulk</code>  che deve essere inserita
	 *
	 */

	private void aggiornaSaldiCoge (UserContext userContext, Scrittura_partita_doppiaBulk scrittura ) throws ComponentException
	{
		try
		{
			LoggableStatement cs = new LoggableStatement(getConnection( userContext ),
					"{ call " +
							EJBCommonServices.getDefaultSchema() +
							"CNRCTB200.aggiornaSaldoCoge(?, ?, ?, ?, ?)}",false,this.getClass());
			try
			{
				cs.setString( 1, scrittura.getCd_cds() );
				cs.setObject( 2, scrittura.getEsercizio() );
				cs.setString( 3, scrittura.getCd_unita_organizzativa() );
				cs.setObject( 4, scrittura.getPg_scrittura() );
				cs.setString( 5, scrittura.getUser());
				cs.executeQuery();
			}
			catch ( Exception e )
			{
				throw new ComponentException( e );
			}
			finally
			{
				cs.close();
			}
		}
		catch ( SQLException e )
		{
			throw handleException(e);
		}
	}
	/**
	 * Nome: Ricerca dell'attributo relativo al terzo con codice '0'
	 * Pre: E' stata richiesta la ricerca di un terzo e l'utente ha specificato come codice
	 *       il valore '0' ad indicare tutte le classificazioni anagrafiche
	 * Post: Viene restituito un RemoteIterator contenente solamente l'oggetto fittizio (con codice '0') che rappresenta
	 *       tutte le classificazioni anagrafiche
	 * <p>
	 * Nome: Ricerca dell'attributo relativo al terzo con codice '0'
	 * Pre: È stata richiesta la ricerca di un terzo e l'utente ha specificato come codice
	 *       un valore diverso da '0'
	 * Post: Viene restituito un RemoteIterator contenente la lista di oggetti di tipo TerzoBulk
	 *       risultante dall'esecuzione della query sul database
	 * <p>
	 * Nome: Ricerca di un attributo diverso da terzo
	 * Pre: È stata richiesta la ricerca di un attributo diverso da 'terzo'
	 * Post: Viene restituito un RemoteIterator contenente la lista degli oggettiBulk
	 *       risultante dall'esecuzione della query sul database
	 * <p>
	 *
	 * @param userContext <code>UserContext</code>
	 * @param clausole <code>CompoundFindClause</code>  clausole specificate dall'utente
	 * @param bulk <code>OggettoBulk</code>  oggettoBulk da ricercare
	 * @param contesto <code>Scrittura_partita_doppiaBulk</code>  contesto della ricerca
	 * @param attributo nome dell'attributo del contesto che deve essere ricercato
	 * @return <code>RemoteIterator</code>  elenco di oggetti trovati
	 *
	 */

	public RemoteIterator cerca(UserContext userContext, CompoundFindClause clausole, OggettoBulk bulk, OggettoBulk contesto, String attributo) throws ComponentException {
		if ( attributo.equals("terzo") )
		{
			if ( (((TerzoBulk) bulk).getCd_terzo() != null && ((TerzoBulk) bulk).getCd_terzo().equals(TerzoBulk.TERZO_NULLO)))
			{
				TerzoBulk terzo = getTerzoNullo();
				return new ArrayRemoteIterator(new TerzoBulk[] { terzo });
			}
		}
		return super.cerca(userContext,clausole,bulk,contesto,attributo);
	}
	/**
	 *  Scrittura in Partita Doppia - Esercizio COEP/COAN chiuso
	 *    PreCondition:
	 *      L'esrcizio COEP/COAN risulta chiuso per il CdS di scrivania
	 *    PostCondition:
	 *      Non viene consentita il salvataggio.
	 * <p>
	 *  Tutti i controlli superati.
	 *    PreCondition:
	 *      Nessun errore rilevato.
	 *    PostCondition:
	 *      Viene consentito il salvataggio.
	 *
	 * @param userContext lo <code>UserContext</code> che ha generato la richiesta
	 * @param bulk <code>OggettoBulk</code> il Bulk da creare
	 *
	 * @return l'oggetto <code>OggettoBulk</code> creato
	 **/
	public OggettoBulk creaConBulk(UserContext userContext, OggettoBulk bulk) throws ComponentException{

		if (isEsercizioChiuso(userContext))
			throw new ApplicationException ("Attenzione: esercizio economico chiuso o in fase di chiusura.");


		return super.creaConBulk(userContext, bulk);
	}
	/**
	 *
	 * Nome: Inserimento di una scrittura
	 * Pre: È stata richiesta l'inserimento di una scrittura in partita doppia già validata
	 * Post: L'importo della scrittura viene impostato come la somma degli importi dei movimenti avere (che coincide con la
	 *       somma degli importi dei movimenti dare) e il saldo coge relativo ai conti econ-patr. dei movimenti viene aggiornato
	 *       (metodo aggiornaSaldiCoge)
	 *
	 * @param userContext <code>UserContext</code>
	 * @param bulk <code>Scrittura_partita_doppiaBulk</code>  che deve essere inserita
	 * @return <code>Scrittura_partita_doppiaBulk</code>  inserita
	 *
	 */

	protected OggettoBulk eseguiCreaConBulk(UserContext userContext,OggettoBulk bulk) throws ComponentException,PersistencyException
	{
		Scrittura_partita_doppiaBulk scrittura = (Scrittura_partita_doppiaBulk) bulk;
		scrittura.setIm_scrittura( scrittura.getImTotaleAvere());
		makeBulkPersistent(userContext,scrittura);
		aggiornaSaldiCoge( userContext, scrittura );
		return bulk;
	}

	/**
	 *
	 * Nome: Storno e riemissione di una scrittura
	 * Pre: È stata richiesta la cancellazione di una scrittura in partita doppia
	 * Post: È stata richiamata la stored procedure che esegue lo storno e la riemissione della partita doppia e
	 *       aggiorna i saldi coge
	 *
	 * @param userContext <code>UserContext</code>
	 * @param bulk <code>Scrittura_partita_doppiaBulk</code>  che deve essere inserita
	 *
	 */

	protected void eseguiEliminaConBulk(UserContext userContext,OggettoBulk bulk) throws ComponentException
	{
		try
		{
			Scrittura_partita_doppiaBulk scrittura = (Scrittura_partita_doppiaBulk) bulk;

			LoggableStatement cs = new LoggableStatement(getConnection( userContext ),
					"{ call " +
							EJBCommonServices.getDefaultSchema() +
							"CNRCTB200.creaScrittStornoCoge(?, ?, ?, ?, ?)}",false,this.getClass());
			try
			{
				cs.setString( 1, scrittura.getCd_cds() );
				cs.setObject( 2, scrittura.getEsercizio() );
				cs.setString( 3, scrittura.getCd_unita_organizzativa() );
				cs.setObject( 4, scrittura.getPg_scrittura() );
				cs.setString( 5, scrittura.getUser());
				cs.executeQuery();
			}
			catch ( Exception e )
			{
				throw new ComponentException( e );
			}
			finally
			{
				cs.close();
			}
		}
		catch ( SQLException e )
		{
			throw handleException(e);
		}
	}
	/* restitusce un'istanza fittizia di TerzoBulk con codice = 0 a rappresentare
       la non presenza di un terzo */
	private TerzoBulk getTerzoNullo()
	{
		TerzoBulk terzo = new TerzoBulk();
		terzo.setCd_terzo( TerzoBulk.TERZO_NULLO);
		terzo.setDenominazione_sede("Terzo non definito");
		terzo.setCrudStatus( TerzoBulk.NORMAL);
		return terzo;
	}
	/**
	 *
	 * Nome: Inizializzazione di una scrittura
	 * Pre: È stata richiesta l'inizializzazione per inserimento di una scrittura in partita doppia
	 * Post: La scrittura viene restituita con inizializzata la data di contabilizzazione (metodo inizializzaDataContabilizzazione)
	 *       e il cds (con il cds di scrivania)
	 *
	 * @param userContext <code>UserContext</code>
	 * @param bulk <code>Scrittura_partita_doppiaBulk</code>  che deve essere inizializzato per inserimento
	 * @return <code>Scrittura_partita_doppiaBulk</code>  inizializzato per inserimento
	 *
	 */

	public OggettoBulk inizializzaBulkPerInserimento (UserContext userContext,OggettoBulk bulk) throws ComponentException
	{

		/* Gennaro Borriello - (30/09/2004 11.39.59)
		 *	In caso di esercizio COAN/COEP chiuso, blocca l'utente.
		 *	(Su indicazione di Massimo Bartolucci)
		 */
		if(isEsercizioChiuso(userContext))
			throw  new ApplicationException("Attenzione: esercizio economico chiuso o in fase di chiusura.");

		try
		{
			bulk = super.inizializzaBulkPerInserimento( userContext, bulk);
			if ( bulk instanceof Scrittura_partita_doppiaBulk )
			{
				Scrittura_partita_doppiaBulk scrittura = (Scrittura_partita_doppiaBulk) bulk;
				inizializzaDataContabilizzazione(userContext, scrittura);
				scrittura.setCds((CdsBulk)getHome( userContext, CdsBulk.class).findByPrimaryKey( new CdsBulk( scrittura.getUo().getCd_unita_padre())));
				scrittura.setCd_cds_documento( scrittura.getCd_cds() );
				return scrittura;
			}

			return bulk;
		}
		catch ( Exception e )
		{
			throw handleException( bulk, e );
		}
	}
	/**
	 *
	 * Nome: Inizializzazione di una scrittura
	 * Pre: È stata richiesta l'inizializzazione per modifica di una scrittura in partita doppia
	 * Post: La scrittura viene restituita con inizializzata la collezione di movimenti dare e movimenti avere
	 * <p>
	 * Nome: Inizializzazione di un movimento
	 * Pre:  E' stata richiesta l'inizializzazione per visualizzazione di un movimento coge
	 * Post: Il movimento viene restituito con l'inizializzazione di default
	 * <p>
	 * Nome: Inizializzazione di un saldo
	 * Pre:  E' stata richiesta l'inizializzazione per visualizzazione di un saldo coge
	 * Post: Il saldo viene restituito con l'inizializzazione di default
	 *
	 * @param userContext <code>UserContext</code>
	 * @param bulk <code>Scrittura_partita_doppiaBulk</code> oppure <code>Saldo_cogeBulk</code> oppure <code>Movimento_cogeBulk</code> che devono essere inizializzati per modifica
	 * @return <code>Scrittura_partita_doppiaBulk</code> oppure <code>Saldo_cogeBulk</code> oppure <code>Movimento_cogeBulk</code> inizializzati per modifica
	 *
	 */
	public OggettoBulk inizializzaBulkPerModifica(UserContext userContext,OggettoBulk bulk) throws ComponentException {
		try
		{
			bulk = super.inizializzaBulkPerModifica( userContext, bulk );

			if ( bulk instanceof Scrittura_partita_doppiaBulk )
			{
				Scrittura_partita_doppiaBulk scrittura = (Scrittura_partita_doppiaBulk) bulk;
				scrittura.setMovimentiDareColl( new BulkList( ((Scrittura_partita_doppiaHome) getHome( userContext, scrittura.getClass())).findMovimentiDareColl( userContext, scrittura )));
				scrittura.setMovimentiAvereColl( new BulkList( ((Scrittura_partita_doppiaHome) getHome( userContext, scrittura.getClass())).findMovimentiAvereColl( userContext, scrittura )));
				if (Optional.ofNullable(scrittura.getCd_terzo()).isPresent()) {
					if ( scrittura.getCd_terzo().equals( TerzoBulk.TERZO_NULLO))
						scrittura.setTerzo( getTerzoNullo());
					else
						scrittura.setTerzo( (TerzoBulk) getHome( userContext, TerzoBulk.class).findByPrimaryKey( scrittura.getTerzo()));
				}
			}
			else if ( bulk instanceof Saldo_cogeBulk )
			{
				Saldo_cogeBulk saldo = (Saldo_cogeBulk) bulk;
				if ( saldo.getCd_terzo().equals( TerzoBulk.TERZO_NULLO))
					saldo.setTerzo( getTerzoNullo());
				else
					saldo.setTerzo( (TerzoBulk) getHome( userContext, TerzoBulk.class).findByPrimaryKey( saldo.getTerzo()));
			}
			else if ( bulk instanceof Movimento_cogeBulk )
			{
				Movimento_cogeBulk mov = (Movimento_cogeBulk) bulk;
				Optional.ofNullable(mov.getTerzo())
						.ifPresent(terzoBulk -> mov.getScrittura().setTerzo(terzoBulk));
			}

			if(isEsercizioChiuso(userContext))
				return asRO(bulk,"Attenzione: esercizio economico chiuso o in fase di chiusura.");

			return bulk;
		} catch(Exception e) {
			throw handleException(e);
		}
	}
	/**
	 *
	 * Nome: Inizializzazione di una scrittura
	 * Pre:  E' stata richiesta l'inizializzazione per eseguire una ricerca di una scrittura in partita doppia
	 * Post: La scrittura viene restituita con inizializzato come Cds quello di scrivania
	 * <p>
	 * Nome: Inizializzazione di un movimento
	 * Pre:  E' stata richiesta l'inizializzazione per eseguire una ricerca di un movimento coge
	 * Post: Il movimento viene restituito con inizializzato come Cds quello di scrivania
	 * <p>
	 * Nome: Inizializzazione di un saldo
	 * Pre:  E' stata richiesta l'inizializzazione per eseguire una ricerca di un saldo coge
	 * Post: Il saldo viene restituito con inizializzato come Cds quello di scrivania
	 *
	 * @param userContext <code>UserContext</code>
	 * @param bulk <code>Scrittura_partita_doppiaBulk</code> oppure <code>Saldo_cogeBulk</code> oppure <code>Movimento_cogeBulk</code> che devono essere inizializzati per ricerca
	 * @return <code>Scrittura_partita_doppiaBulk</code> oppure <code>Saldo_cogeBulk</code> oppure <code>Movimento_cogeBulk</code> inizializzati per ricerca
	 *
	 */

	public OggettoBulk inizializzaBulkPerRicerca (UserContext userContext,OggettoBulk bulk) throws ComponentException
	{

		try
		{
			bulk = super.inizializzaBulkPerRicerca( userContext, bulk);
			if ( bulk instanceof Scrittura_partita_doppiaBulk )
			{
				Scrittura_partita_doppiaBulk scrittura = (Scrittura_partita_doppiaBulk) bulk;
				scrittura.setCds((CdsBulk)getHome( userContext, CdsBulk.class).findByPrimaryKey( new CdsBulk( scrittura.getUo().getCd_unita_padre())));
				return scrittura;
			}
			else if ( bulk instanceof Saldo_cogeBulk )
			{
				((Saldo_cogeBulk)bulk).setCds((CdsBulk)getHome( userContext, CdsBulk.class).findByPrimaryKey( ((Saldo_cogeBulk)bulk).getCds()));
			}
			else if ( bulk instanceof Movimento_cogeBulk )
			{
				((Movimento_cogeBulk)bulk).setCds((CdsBulk)getHome( userContext, CdsBulk.class).findByPrimaryKey( ((Movimento_cogeBulk)bulk).getCds()));
			}


			return bulk;
		}
		catch ( Exception e )
		{
			throw handleException( bulk, e );
		}
	}
	/**
	 *
	 * Nome: Inizializzazione di una scrittura
	 * Pre:  E' stata richiesta l'inizializzazione per eseguire una ricerca libera di una scrittura in partita doppia
	 * Post: La scrittura viene restituita con inizializzato come Cds quello di scrivania
	 * <p>
	 * Nome: Inizializzazione di un movimento
	 * Pre:  E' stata richiesta l'inizializzazione per eseguire una ricerca libera di un movimento coge
	 * Post: Il movimento viene restituito con inizializzato come Cds quello di scrivania
	 * <p>
	 * Nome: Inizializzazione di un saldo
	 * Pre:  E' stata richiesta l'inizializzazione per eseguire una ricerca libera di un saldo coge
	 * Post: Il saldo viene restituito con inizializzato come Cds quello di scrivania
	 *
	 * @param userContext <code>UserContext</code>
	 * @param bulk <code>Scrittura_partita_doppiaBulk</code> oppure <code>Saldo_cogeBulk</code> oppure <code>Movimento_cogeBulk</code> che devono essere inizializzati per ricerca
	 * @return <code>Scrittura_partita_doppiaBulk</code> oppure <code>Saldo_cogeBulk</code> oppure <code>Movimento_cogeBulk</code> inizializzati per ricerca
	 *
	 */

	public OggettoBulk inizializzaBulkPerRicercaLibera (UserContext userContext,OggettoBulk bulk) throws ComponentException
	{

		try
		{
			bulk = super.inizializzaBulkPerRicercaLibera( userContext, bulk);
			if ( bulk instanceof Scrittura_partita_doppiaBulk )
			{
				Scrittura_partita_doppiaBulk scrittura = (Scrittura_partita_doppiaBulk) bulk;
				scrittura.setCds((CdsBulk)getHome( userContext, CdsBulk.class).findByPrimaryKey( new CdsBulk( scrittura.getUo().getCd_unita_padre())));
				return scrittura;
			}
			else if ( bulk instanceof Saldo_cogeBulk )
			{
				((Saldo_cogeBulk)bulk).setCds((CdsBulk)getHome( userContext, CdsBulk.class).findByPrimaryKey( ((Saldo_cogeBulk)bulk).getCds()));
			}
			else if ( bulk instanceof Movimento_cogeBulk )
			{
				((Movimento_cogeBulk)bulk).setCds((CdsBulk)getHome( userContext, CdsBulk.class).findByPrimaryKey( ((Movimento_cogeBulk)bulk).getCds()));
			}


			return bulk;
		}
		catch ( Exception e )
		{
			throw handleException( bulk, e );
		}
	}
	/**
	 * Nome: Esercizio aperto
	 * Pre:  E' stata richiesta l'inizializzazione della data di contabilizzazione di una scrittura
	 * e l'esercizio contabile e' APERTO
	 * Post: La data di contabilizzzaione viene inizializzata con la data odierna
	 * <p>
	 * Nome: Esercizio chiuso priovvisoriamente
	 * Pre:  E' stata richiesta l'inizializzazione della data di contabilizzazione di una scrittura
	 * e l'esercizio contabile e' CHIUSO PROVVISORIO
	 * Post: La data di contabilizzzaione viene inizializzata con il 31/12/esercizio di scrivania
	 * <p>
	 * Nome: Esercizio in altro stato
	 * Pre:  E' stata richiesta l'inizializzazione della data di contabilizzazione di una scrittura
	 * e l'esercizio contabile ha stato diverso da APERTO o  CHIUSO PROVVISORIO
	 * Post: Una segnalazione di errore comunica all'utente l'impossibilità di creare una scrittura in partita doppia
	 *
	 * @param userContext <code>UserContext</code>
	 * @param scrittura   <code>Scrittura_partita_doppiaBulk</code>  la cui data deve essere inizializzata
	 */

	private void inizializzaDataContabilizzazione (UserContext userContext, Scrittura_partita_doppiaBulk scrittura ) throws PersistencyException, ComponentException, EJBException
	{
		EsercizioBulk esercizio = (EsercizioBulk) getHome( userContext, EsercizioBulk.class ).findByPrimaryKey( new EsercizioBulk( ((CNRUserContext)userContext).getCd_cds(), ((CNRUserContext)userContext).getEsercizio()));
		if ( esercizio == null )
			throw new ApplicationException( "Attenzione esercizio non definito per il cds di scrivania!");
		/* Gennaro Borriello - (23/09/2004 10.33.19)
		 *	Err. 838 - Deve essere possibile poter registrare delle scritture coep/coan
		 *	anche se l'esercizio finanziario è chiuso.
		 */
		if ( !esercizio.getSt_apertura_chiusura().equals(EsercizioBulk.STATO_APERTO) &&
				!esercizio.getSt_apertura_chiusura().equals(EsercizioBulk.STATO_CHIUSO_DEF))
			throw new ApplicationException( "Attenzione esercizio non in stato aperto per il cds di scrivania!");
		scrittura.setDt_contabilizzazione( DateServices.getDt_valida( userContext));
	}

	/**
	 *	Controllo se l'esercizio di scrivania e' aperto
	 * <p>
	 * Nome: Controllo chiusura esercizio
	 * Pre:  E' stata richiesta la creazione o modifica di una scrittura
	 * Post: Viene chiamata una stored procedure che restituisce
	 *		  -		'Y' se il campo stato della tabella CHIUSURA_COEP vale C
	 *		  -		'N' altrimenti
	 *		  Se l'esercizio e' chiuso e' impossibile proseguire
	 *
	 * @param  userContext <code>UserContext</code>

	 * @return boolean : TRUE se stato = C
	 *					  FALSE altrimenti
	 */
	private boolean isEsercizioChiuso(UserContext userContext) throws ComponentException {
        return ((Chiusura_coepHome)getHome(userContext, Chiusura_coepBase.class))
                .isChiusuraCoepDef(CNRUserContext.getEsercizio(userContext),CNRUserContext.getCd_cds(userContext));
    }

	protected void ridefinisciClausoleConTimestamp(UserContext userContext,CompoundFindClause clauses)
	{
		SimpleFindClause clause;
		for ( Iterator<SimpleFindClause> i = clauses.iterator(); i.hasNext(); )
		{
			clause = i.next();
			if ( clause.getPropertyName().equalsIgnoreCase( "dt_contabilizzazione" ) )
				if ( clause.getOperator() == SQLBuilder.ISNOTNULL || clause.getOperator() == SQLBuilder.ISNULL )
					clause.setSqlClause( "TRUNC( " + clause.getPropertyName() + ") " + SQLBuilder.getSQLOperator(clause.getOperator()) );
				else
					clause.setSqlClause( "TRUNC( " + clause.getPropertyName() + ") " + SQLBuilder.getSQLOperator(clause.getOperator()) + " ? ");

		}


	}
	/* per le date salvate nel database come timestamp bisogna ridefinire la query nel modo seguente:
            TRUNC( dt_nel_db) operator 'GG/MM/YYYY'
    */
	protected Query select(UserContext userContext,CompoundFindClause clauses,OggettoBulk bulk) throws ComponentException, PersistencyException
	{
		/* COMPORTAMENTO DI DEFAULT - INIZIO */
		if (clauses == null)
		{
			if (bulk != null)
				clauses = bulk.buildFindClauses(null);
		} else
			clauses = CompoundFindClause.and(clauses,bulk.buildFindClauses(Boolean.FALSE));
		/* COMPORTAMENTO DI DEFAULT - FINE */

		if (clauses != null)
			ridefinisciClausoleConTimestamp(userContext, clauses);

		if(bulk instanceof Movimento_cogeBulk)
		{
			for ( Iterator i = clauses.iterator(); i.hasNext(); )
			{
				SimpleFindClause clause = (SimpleFindClause) i.next();
				if ( clause.getPropertyName().equalsIgnoreCase( "scrittura.attiva" ))
					if (clause.getOperator() == SQLBuilder.ISNOTNULL || clause.getOperator() == SQLBuilder.ISNULL )
						clause.setSqlClause("SCRITTURA_PARTITA_DOPPIA.ATTIVA " + SQLBuilder.getSQLOperator(clause.getOperator()) );
					else
						clause.setSqlClause( "SCRITTURA_PARTITA_DOPPIA.ATTIVA " + SQLBuilder.getSQLOperator(clause.getOperator()) + "'" + clause.getValue() + "'");

				if ( clause.getPropertyName().equalsIgnoreCase( "scrittura.pg_scrittura_annullata" ))
					if (clause.getOperator() == SQLBuilder.ISNOTNULL || clause.getOperator() == SQLBuilder.ISNULL  )
						clause.setSqlClause("SCRITTURA_PARTITA_DOPPIA.PG_SCRITTURA_ANNULLATA " + SQLBuilder.getSQLOperator(clause.getOperator()) );
					else
						clause.setSqlClause( "SCRITTURA_PARTITA_DOPPIA.PG_SCRITTURA_ANNULLATA " + SQLBuilder.getSQLOperator(clause.getOperator()) + "'" + clause.getValue() + "'");
			}
		}

		return getHome(userContext,bulk).selectByClause(clauses);
	}
	/**
	 * esegue la seleziona un conto economico patrimoniale per un movimento coge
	 * <p>
	 * Nome: Seleziona conto per movimeto avere
	 * Pre:  E' stata richiesta la ricerca di un conto economico patrimoniale per un movimento avere
	 * Post: Viene restituito il SQLBuilder con le clausole specificate dall'utente ed inoltre le clausole che il conto
	 *       abbia esercizio uguale all'esercizio del movimento coge e il tipo sezione diverso da DARE
	 * <p>
	 * Nome: Seleziona conto per movimeto dare
	 * Pre:  E' stata richiesta la ricerca di un conto economico patrimoniale per un movimento dare
	 * Post: Viene restituito il SQLBuilder con le clausole specificate dall'utente ed inoltre le clausole che il conto
	 *       abbia esercizio uguale all'esercizio del movimento coge e il tipo sezione diverso da AVERE
	 *
	 * @param userContext <code>UserContext</code>
	 * @param movimento <code>Movimento_cogeBulk</code> per cui ricercare il conto
	 * @param conto <code>ContoBulk</code> conto econom.patrimoniale da ricercare
	 * @param clauses <code>CompoundFindClause</code> clausole specificate dall'utente
	 * @return SQLBuilder
	 *
	 */

	public SQLBuilder selectContoByClause (UserContext userContext, Movimento_cogeBulk movimento, ContoBulk conto, CompoundFindClause clauses ) throws ComponentException
	{
		SQLBuilder sql = getHome( userContext, conto.getClass()).createSQLBuilder();
		sql.addClause( clauses );
		sql.addClause( FindClause.AND, "esercizio", SQLBuilder.EQUALS, movimento.getEsercizio());
		if ( Movimento_cogeBulk.SEZIONE_AVERE.equals( movimento.getSezione()))
			sql.addClause( FindClause.AND, "ti_sezione", SQLBuilder.NOT_EQUALS, ContoHome.SEZIONE_DARE);
		else if ( Movimento_cogeBulk.SEZIONE_DARE.equals( movimento.getSezione()))
			sql.addClause( FindClause.AND, "ti_sezione", SQLBuilder.NOT_EQUALS, ContoHome.SEZIONE_AVERE);
		final Optional<String> naturaConto = Optional.ofNullable(movimento.getTi_riga())
				.map(Movimento_cogeBulk.TipoRiga::getValueFrom)
				.map(Movimento_cogeBulk.TipoRiga::naturaConto);
        naturaConto.ifPresent(s -> sql.addClause(FindClause.AND, "natura_voce", SQLBuilder.EQUALS, s));
		return sql;
	}
	/**
	 * esegue la seleziona un conto economico patrimoniale per un saldo coge
	 * <p>
	 * Nome: Seleziona conto per saldo coge
	 * Pre:  E' stata richiesta la ricerca di un conto economico patrimoniale per un saldo coge
	 * Post: Viene restituito il SQLBuilder con le clausole specificate dall'utente ed inoltre le clausole che il conto
	 *       abbia esercizio uguale all'esercizio del saldo coge
	 *
	 * @param userContext <code>UserContext</code>
	 * @param saldo <code>Saldo_cogeBulk</code> per cui ricercare il conto
	 * @param conto <code>ContoBulk</code> conto econom.patrimoniale da ricercare
	 * @param clauses <code>CompoundFindClause</code> clausole specificate dall'utente
	 * @return SQLBuilder
	 *
	 */

	public SQLBuilder selectContoByClause (UserContext userContext, Saldo_cogeBulk saldo, ContoBulk conto, CompoundFindClause clauses ) throws ComponentException
	{
		SQLBuilder sql = getHome( userContext, conto.getClass()).createSQLBuilder();
		sql.addClause( clauses );
		sql.addClause( FindClause.AND, "esercizio", SQLBuilder.EQUALS, saldo.getEsercizio());
		return sql;
	}


	/**
	 * esegue la seleziona un terzo per una scrittura
	 * <p>
	 * Nome: Seleziona terzo per scrittura
	 * Pre:  E' stata richiesta la ricerca di un terzo per una scrittura in partita doppia
	 * Post: Viene restituito il SQLBuilder con le clausole specificate dall'utente ed inoltre le clausole che il terzo
	 *       non abbia data di fine rapporto < della data odierna
	 *
	 * @param userContext <code>UserContext</code>
	 * @param scrittura <code>Scrittura_partita_doppiaBulk</code> per cui ricercare il terzo
	 * @param terzo <code>TerzoBulk</code> terzo da ricercare
	 * @param clauses <code>CompoundFindClause</code> clausole specificate dall'utente
	 * @return SQLBuilder
	 *
	 */

	public SQLBuilder selectTerzoByClause (UserContext userContext, Scrittura_partita_doppiaBulk scrittura, TerzoBulk terzo, CompoundFindClause clauses ) throws ComponentException
	{
		SQLBuilder sql = getHome( userContext, terzo.getClass(), "V_TERZO_CF_PI").createSQLBuilder();
		sql.addClause( clauses );
		sql.addSQLClause( "AND", "(DT_FINE_RAPPORTO >= SYSDATE OR DT_FINE_RAPPORTO IS NULL)");
		return sql;
	}
	/**
	 * valida la correttezza dell'associazione fra anagrafico e conto
	 * <p>
	 * Nome: Nessuna associzione fra anagrafico-conto
	 * Pre:  Per la scrittura in partita doppia e' stato selezionato un terzo le cui caratteristiche anagrafiche non
	 *       sono state messe in relazione con neanche un conto definito per la scrittura (sia in avere che in dare)
	 * Post: Una segnalazione di errore viene restituita all'utente
	 * <p>
	 * Nome: Almeno un'associzione fra anagrafico-conto
	 * Pre:  Per la scrittura in partita doppia e' stato selezionato un terzo le cui caratteristiche anagrafiche
	 *       sono state messe in relazione con un conto definito per la scrittura (in avere o in dare)
	 * Post: La scrittura supera la validazione anagrafica-conto
	 *
	 * @param userContext <code>UserContext</code>
	 * @param scrittura <code>Scrittura_partita_doppiaBulk</code> da validare
	 *
	 */

	private void validaAssociazioneAnagConto (UserContext userContext, Scrittura_partita_doppiaBulk scrittura ) throws ComponentException, PersistencyException
	{
		List<Ass_anag_voce_epBulk> result = ((Ass_anag_voce_epHome) getHome(userContext, Ass_anag_voce_epBulk.class)).findAssociazioniPerScrittura( scrittura );
		if ( result == null || result.isEmpty())
			throw new ApplicationException( "Non esiste nessuna associazione fra l'anagrafica selezionata e i conti economico-patrimoniali" );
		Ass_anag_voce_epBulk ass = result.get(0);
		Movimento_cogeBulk movimento;
		for (Movimento_cogeBulk movimento_cogeBulk : scrittura.getMovimentiAvereColl()) {
			if (movimento_cogeBulk.getConto().equalsByPrimaryKey(ass.getConto()))
				return;
		}
		for (Movimento_cogeBulk movimento_cogeBulk : scrittura.getMovimentiDareColl()) {
			if (movimento_cogeBulk.getConto().equalsByPrimaryKey(ass.getConto()))
				return;
		}
		throw new ApplicationException( "Non esiste nessuna associazione fra l'anagrafica selezionata e i conti economico-patrimoniali" );
	}

	/**
	 * valida la correttezza di un oggetto di tipo <code>Scrittura_partita_doppiaBulk</code> passato in ingresso.
	 * <p>
	 * Nome: validazione superata
	 * Pre:  La scrittura supera la validazione ( metodo validaScrittura)
	 * Post: La scrittura può essere inserita nel database
	 * <p>
	 * Nome: validazione non superata
	 * Pre:  La scrittura non supera la validazione ( metodo validaScrittura)
	 * Post: Una segnalazione di errore viene restituita all'utente
	 * <p>
	 *
	 * @param userContext <code>UserContext</code>
	 * @param bulk <code>Scrittura_partita_doppiaBulk</code> da validare
	 *
	 */

	protected void validaCreaModificaConBulk(UserContext userContext,OggettoBulk bulk) throws ComponentException
	{
		super.validaCreaModificaConBulk( userContext, bulk );
		try
		{
			validaScrittura( userContext, (Scrittura_partita_doppiaBulk) bulk );
		}
		catch ( Exception e )
		{
			throw handleException( e )	;
		}

	}
	/**
	 * valida la correttezza di un oggetto di tipo <code>Scrittura_partita_doppiaBulk</code> passato in ingresso.
	 * <p>
	 * Nome: Nessun movimento avere
	 * Pre:  Per la scrittura in partita doppia non e' stato definito nessun movimento avere
	 * Post: Una segnalazione di errore viene restituita all'utente
	 * <p>
	 * Nome: Nessun movimento dare
	 * Pre:  Per la scrittura in partita doppia non e' stato definito nessun movimento dare
	 * Post: Una segnalazione di errore viene restituita all'utente
	 * <p>
	 * Nome: Somma movimenti dare diversa da somma movimenti avere
	 * Pre:  La somma degli importi dei movimenti dare e' diversa dalla somma degli importi dei movimenti avere
	 * Post: Una segnalazione di errore viene restituita all'utente
	 * <p>
	 * Nome: Terzo
	 * Pre:  La scrittura e' stata definita per un terzo e la validazione terzo-conti (metodo 'validaAssociazioneAnagConto')
	 *       non e' stata superata
	 * Post: Una segnalazione di errore viene restituita all'utente

	 * Nome: Tutti i controlli superati
	 * Pre:  La scrittura ha almeno un movimento avere, ha almeno un movimento dare e la somma degli importi dei
	 *       movimenti avere e dare coincide. Inoltre non e' stato specificato un terzo oppure e' stato specificato un
	 *       terzo e tale terzo ha superato la validazione coi conti.
	 * Post: La scrittura supera la validazione e può pertanto essere salvata
	 *
	 * @param userContext <code>UserContext</code>
	 * @param scrittura <code>Scrittura_partita_doppiaBulk</code> da validare
	 *
	 */

	private void validaScrittura (UserContext userContext, Scrittura_partita_doppiaBulk scrittura ) throws ComponentException, PersistencyException
	{
		if (scrittura.getMovimentiAvereColl().isEmpty())
			throw new ApplicationException( "E' necessario definire almeno un movimento Avere");
		if (scrittura.getMovimentiDareColl().isEmpty())
			throw new ApplicationException( "E' necessario definire almeno un movimento Dare");
		if ( scrittura.getImTotaleAvere().compareTo( scrittura.getImTotaleDare()) != 0 )
			throw new ApplicationException( "Gli importi totali dei movimenti Dare e Avere devono essere uguali");

		if ( scrittura.getTerzo() != null && scrittura.getTerzo().getCd_terzo() != null && !scrittura.getTerzo().equalsByPrimaryKey( getTerzoNullo()))
			validaAssociazioneAnagConto( userContext, scrittura );



	}
	/* (non-Javadoc)
	 * @see it.cnr.jada.comp.IPrintMgr#inizializzaBulkPerStampa(it.cnr.jada.UserContext, it.cnr.jada.bulk.OggettoBulk)
	 */
	public OggettoBulk inizializzaBulkPerStampa(UserContext usercontext,OggettoBulk oggettobulk) throws ComponentException {
		if (oggettobulk instanceof Stampa_elenco_movimentiBulk)
			return  inizializzaBulkPerStampa(usercontext,(Stampa_elenco_movimentiBulk)oggettobulk);
		return oggettobulk;
	}

	public OggettoBulk inizializzaBulkPerStampa(UserContext usercontext,Stampa_elenco_movimentiBulk stampa) throws ComponentException {
		//	Imposta l'Esercizio come quello di scrivania
		stampa.setEsercizio(CNRUserContext.getEsercizio(usercontext));


		try{
			String cd_cds_scrivania = CNRUserContext.getCd_cds(usercontext);

			CdsHome cdsHome = (CdsHome)getHome(usercontext, CdsBulk.class);
			CdsBulk cds = (CdsBulk)cdsHome.findByPrimaryKey(new CdsBulk(cd_cds_scrivania));
			Unita_organizzativa_enteBulk ente = (Unita_organizzativa_enteBulk) getHome( usercontext, Unita_organizzativa_enteBulk.class).findAll().get(0);

			if (!cds.getCd_unita_organizzativa().equals(ente.getCd_unita_padre())){
				stampa.setCdsForPrint(cds);
				stampa.setCdsForPrintEnabled(true);
			} else {
				stampa.setCdsForPrint(new CdsBulk());
				stampa.setCdsForPrintEnabled(false);
			}

		} catch (PersistencyException pe){
			throw new ComponentException(pe);
		}

		try{
			String cd_uo_scrivania = CNRUserContext.getCd_unita_organizzativa(usercontext);

			Unita_organizzativaHome uoHome = (Unita_organizzativaHome)getHome(usercontext, Unita_organizzativaBulk.class);
			Unita_organizzativaBulk uo = (Unita_organizzativaBulk)uoHome.findByPrimaryKey(new Unita_organizzativaBulk(cd_uo_scrivania));

			if (!uo.isUoCds()){
				stampa.setUoForPrint(uo);
				stampa.setUoForPrintEnabled(true);
			} else {
				stampa.setUoForPrint(new Unita_organizzativaBulk());
				stampa.setUoForPrintEnabled(false);
			}

		} catch (PersistencyException pe){
			throw new ComponentException(pe);
		}

		return stampa;
	}

	public OggettoBulk stampaConBulk(UserContext usercontext, OggettoBulk oggettobulk) throws ComponentException {
		if (oggettobulk instanceof Stampa_elenco_movimentiBulk)
			return  stampaConBulk(usercontext,(Stampa_elenco_movimentiBulk)oggettobulk);
		return oggettobulk;
	}

	public OggettoBulk stampaConBulk(UserContext usercontext, Stampa_elenco_movimentiBulk stampa) throws ComponentException {
		if ( stampa.getContoForPrint()==null || stampa.getContoForPrint().getCd_voce_ep() == null)
			throw new ApplicationException( "E' necessario inserire un Conto");
		if ( stampa.getattiva()==null )
			throw new ApplicationException( "E' necessario valorizzare il campo 'Attiva'");
		if ( stampa.gettipologia()==null )
			throw new ApplicationException( "E' necessario valorizzare il campo 'Tipologia'");
		if (!stampa.getRagr_causale() && !stampa.getRagr_manuale() && !stampa.getRagr_chiusura() && !stampa.getRagr_stipendi() &&
			!stampa.getRagr_doc_amm() && !stampa.getRagr_doc_cont() && !stampa.getRagr_liquid_iva() && !stampa.getRagr_mig_beni())
			throw new ApplicationException( "E' necessario valorizzare almeno una 'Origine della Scrittura'");

		return stampa;
	}
	public SQLBuilder selectContoForPrintByClause (UserContext userContext, Stampa_elenco_movimentiBulk stampa, ContoBulk conto, CompoundFindClause clauses ) throws ComponentException
	{
		SQLBuilder sql = getHome( userContext, conto.getClass()).createSQLBuilder();
		sql.addSQLClause(FindClause.AND,"ESERCIZIO",SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
		sql.addClause( clauses );
		return sql;
	}
	public SQLBuilder selectTerzoForPrintByClause (UserContext userContext, Stampa_elenco_movimentiBulk stampa, TerzoBulk terzo, CompoundFindClause clauses ) throws ComponentException
	{
		SQLBuilder sql = getHome( userContext, terzo.getClass(),"V_TERZO_CF_PI").createSQLBuilder();
		sql.addClause( clauses );
		return sql;
	}
	public SQLBuilder selectUoForPrintByClause (UserContext userContext, Stampa_elenco_movimentiBulk stampa, Unita_organizzativaBulk uo, CompoundFindClause clauses ) throws ComponentException
	{
		SQLBuilder sql = getHome(userContext, uo, "V_UNITA_ORGANIZZATIVA_VALIDA").createSQLBuilder();
		sql.addSQLClause(FindClause.AND, "ESERCIZIO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio( userContext ) );
		sql.addSQLClause(FindClause.AND, "CD_UNITA_PADRE", SQLBuilder.EQUALS, stampa.getCdsForPrint().getCd_unita_organizzativa());
		sql.addClause( clauses );
		return sql;
	}
	public SQLBuilder selectCdsForPrintByClause (UserContext userContext, Stampa_elenco_movimentiBulk stampa, CdsBulk cds, CompoundFindClause clauses ) throws ComponentException
	{
		SQLBuilder sql = getHome(userContext, cds.getClass(), "V_CDS_VALIDO").createSQLBuilder();
		sql.addClause( clauses );
		sql.addSQLClause(FindClause.AND, "ESERCIZIO", SQLBuilder.EQUALS, ((CNRUserContext) userContext).getEsercizio());
		return sql;

	}

	public Long getNextProgressivo(UserContext userContext,Scrittura_partita_doppiaBulk scritturaPartitaDoppia) throws it.cnr.jada.comp.ComponentException {
		try {
			Numerazione_coge_coanBulk numerazione = new Numerazione_coge_coanBulk(scritturaPartitaDoppia);
			return this.getNextProgressivo(userContext, numerazione);
		} catch (Throwable t) {
			throw handleException(scritturaPartitaDoppia, t);
		}
	}

	public Long getNextProgressivo(UserContext userContext,Scrittura_analiticaBulk scritturaAnalitica) throws it.cnr.jada.comp.ComponentException {
		try {
			Numerazione_coge_coanBulk numerazione = new Numerazione_coge_coanBulk(scritturaAnalitica);
			return this.getNextProgressivo(userContext, numerazione);
		} catch (Throwable t) {
			throw handleException(scritturaAnalitica, t);
		}
	}

	private Long getNextProgressivo (UserContext userContext,Numerazione_coge_coanBulk progressivo) throws ComponentException {
		try {
			String cds = progressivo.getCd_cds();
			String cdUnitaOrg = progressivo.getCd_unita_organizzativa();
			String tipoDoc = progressivo.getTi_documento();
			Integer es = progressivo.getEsercizio();
			Numerazione_coge_coanHome home = (Numerazione_coge_coanHome)getHome(userContext, progressivo);
			try {
				progressivo = (Numerazione_coge_coanBulk)home.findAndLock(progressivo);
			} catch (it.cnr.jada.persistency.ObjectNotFoundException e) {
				progressivo = null;
			}
			Long pgCorrente;
			if (progressivo == null) {
				progressivo = new Numerazione_coge_coanBulk();
				progressivo.setTi_documento(tipoDoc);
				progressivo.setEsercizio(es);
				progressivo.setCd_cds(cds);
				progressivo.setCd_unita_organizzativa(cdUnitaOrg);
				progressivo.setPrimo(0L);
				progressivo.setUltimo(9999999999L);
				progressivo.setUser(userContext.getUser());
				pgCorrente = 1L;
				progressivo.setCorrente(pgCorrente);
				home.insert(progressivo, userContext);
				return pgCorrente;
			}
			pgCorrente = progressivo.getCorrente() + 1;
			progressivo.setCorrente(pgCorrente);
			progressivo.setUser(userContext.getUser());
			home.lock(progressivo);
			home.update(progressivo, userContext);
			return pgCorrente;
		} catch(Exception e) {
			throw handleException(progressivo, e);
		}
	}
}