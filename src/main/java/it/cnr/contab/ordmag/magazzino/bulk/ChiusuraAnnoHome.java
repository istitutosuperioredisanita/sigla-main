/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/02/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import it.cnr.contab.logs.bulk.Batch_log_tstaBulk;
import it.cnr.contab.ordmag.anag00.TipoMovimentoMagBulk;
import it.cnr.contab.preventvar00.bulk.Var_bilancioBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.DateUtils;
import it.cnr.jada.util.Orderable;

public class ChiusuraAnnoHome extends BulkHome {
	public ChiusuraAnnoHome(Connection conn) {
		super(ChiusuraAnnoBulk.class, conn);
	}
	public ChiusuraAnnoHome(Connection conn, PersistentCache persistentCache) {
		super(ChiusuraAnnoBulk.class, conn, persistentCache);
	}

	public Integer recuperaNuovoProgressivo(UserContext userContext, ChiusuraAnnoBulk chiusura) throws ComponentException, BusyResourceException, PersistencyException {
		try{
			Integer tmp = (Integer)findAndLockMax(chiusura, "pgChiusura",null);
			if (tmp==null)
				tmp = Integer.valueOf(0);

			return (Integer.valueOf(tmp.intValue()+1));

		}catch(it.cnr.jada.bulk.BusyResourceException ex){
			throw new BusyResourceException();
		}catch(it.cnr.jada.persistency.PersistencyException ex){
			throw new PersistencyException();
		}
	}


	public ChiusuraAnnoBulk getChiusuraAnno(Integer anno, String tipoChiusura) throws PersistencyException
	{
		SQLBuilder sql = this.createSQLBuilder();

		sql.addClause(FindClause.AND, "anno", SQLBuilder.EQUALS, anno);
		sql.addClause(FindClause.AND, "tipoChiusura", SQLBuilder.EQUALS, tipoChiusura);

		List <ChiusuraAnnoBulk> l =  this.fetchAll(sql);
		return l.stream().findFirst().orElse(null);
	}
	public ChiusuraAnnoBulk getStatoChiusuraAnno(Integer anno, String tipoChiusura) throws PersistencyException
	{
		SQLBuilder sql = this.createSQLBuilder();

		sql.addClause(FindClause.AND, "anno", SQLBuilder.EQUALS, anno);
		sql.addClause(FindClause.AND, "tipoChiusura", SQLBuilder.EQUALS, tipoChiusura);

		List <ChiusuraAnnoBulk> l =  this.fetchAll(sql);
		return l.stream().findFirst().orElse(null);
	}

	public boolean isJobChiusuraInventarioComplete(Integer esercizio) throws PersistencyException {
		SQLBuilder sql = createSQLBuilder();

		sql.addSQLClause( "AND", "ANNO", SQLBuilder.EQUALS, esercizio);
		sql.addSQLClause("AND", "TIPO_CHIUSURA", SQLBuilder.EQUALS,"I");
		sql.addSQLClause("AND", "STATO_JOB", SQLBuilder.EQUALS, Batch_log_tstaBulk.STATO_JOB_COMPLETE);
		java.util.List l = fetchAll(sql);

		if(l!= null && l.size()>0){
			return true;
		}
		return false;
	}

}