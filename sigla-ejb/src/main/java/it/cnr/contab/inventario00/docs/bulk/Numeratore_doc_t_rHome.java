/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.inventario00.docs.bulk;
import java.sql.Connection;

import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.OutdatedResourceException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
public class Numeratore_doc_t_rHome extends BulkHome {
	public Numeratore_doc_t_rHome(Connection conn) {
		super(Numeratore_doc_t_rBulk.class, conn);
	}
	public Numeratore_doc_t_rHome(Connection conn, PersistentCache persistentCache) {
		super(Numeratore_doc_t_rBulk.class, conn, persistentCache);
	}

	public Long getNextPg(
			UserContext userContext,
			Integer esercizio,
			Long progressivoInventario,
			String tipoDocumento,
			String user)
			throws PersistencyException,
			IntrospectionException,
			it.cnr.jada.comp.ApplicationException
	{
		try{
			Numeratore_doc_t_rBulk numeratore = (Numeratore_doc_t_rBulk)findByPrimaryKey(new Numeratore_doc_t_rKey(progressivoInventario,tipoDocumento,esercizio));
			// non esiste il record - creo un nuovo record
			if (numeratore == null){
				numeratore = new Numeratore_doc_t_rBulk(progressivoInventario,tipoDocumento,esercizio);
				numeratore.setCorrente(new Long("1"));
				numeratore.setIniziale(new Long("0"));
				numeratore.setUser(user);
				insert(numeratore, userContext);
			}
			else{
				numeratore.setCorrente(new Long(numeratore.getCorrente().longValue()+1));
				lock(numeratore);
				update(numeratore, userContext);
			}
			return numeratore.getCorrente();
		}catch (BusyResourceException e) {
			throw new it.cnr.jada.comp.ApplicationException("Operazione effettuata al momento da un'altro utente, riprovare successivamente.");
		}catch (OutdatedResourceException e) {
			throw new it.cnr.jada.comp.ApplicationException("Operazione effettuata al momento da un'altro utente, riprovare successivamente.");
		}
	}
	public Long getNextTempPg(
			UserContext userContext,
			Integer esercizio,
			Long progressivoInventario,
			String tipoDocumento,
			String user)
			throws	PersistencyException, ApplicationException {
		try{
			String tipoDocTemp = null;
			Long pgCorrente = null;
			if (tipoDocumento.equalsIgnoreCase("C")){
				tipoDocTemp = "T";
			}
			else if (tipoDocumento.equalsIgnoreCase("S")){
				tipoDocTemp = "P";
			}

			Numeratore_doc_t_rBulk progressivo  = (Numeratore_doc_t_rBulk)findByPrimaryKey(
					new Numeratore_doc_t_rKey(progressivoInventario,tipoDocTemp,esercizio));
			Numeratore_doc_t_rBulk progressivo_def  = (Numeratore_doc_t_rBulk)findByPrimaryKey(
					new Numeratore_doc_t_rKey(progressivoInventario,tipoDocTemp,esercizio));

			if (progressivo == null) {
				progressivo = new Numeratore_doc_t_rBulk();
				progressivo.setEsercizio(esercizio);
				progressivo.setPgInventario(progressivoInventario);
				progressivo.setTiTrasportoRientro(tipoDocTemp);
				progressivo.setIniziale(new Long(-1));
				pgCorrente = new Long(progressivo.getIniziale().longValue());
				progressivo.setCorrente(pgCorrente);
				progressivo.setUser(user);
				insert(progressivo, userContext);
				if (progressivo_def ==null){
					progressivo_def = new Numeratore_doc_t_rBulk();
					progressivo_def.setEsercizio(esercizio);
					progressivo_def.setPgInventario(progressivoInventario);
					progressivo_def.setTiTrasportoRientro(tipoDocumento);
					progressivo_def.setIniziale(new Long(1));
					progressivo_def.setCorrente(progressivo_def.getIniziale().longValue());
					progressivo_def.setUser(user);
					insert(progressivo_def, userContext);
				}
				return pgCorrente;
			}

			pgCorrente = new Long(progressivo.getCorrente().longValue()-1);
			progressivo.setCorrente(pgCorrente);
			progressivo.setUser(user);
			lock(progressivo);
			update(progressivo, userContext);
			return pgCorrente;
		}catch (BusyResourceException e) {
			throw new it.cnr.jada.comp.ApplicationException("Operazione effettuata al momento da un'altro utente, riprovare successivamente.");
		}catch (OutdatedResourceException e) {
			throw new it.cnr.jada.comp.ApplicationException("Operazione effettuata al momento da un'altro utente, riprovare successivamente.");
		}
	}
}