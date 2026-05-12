/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/07/2024
 */
package it.cnr.contab.progettiric00.core.bulk;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;

public class V_saldi_plurien_voce_progettoHome extends BulkHome {
	public V_saldi_plurien_voce_progettoHome(Connection conn) {
		super(V_saldi_plurien_voce_progettoBulk.class, conn);
	}
	public V_saldi_plurien_voce_progettoHome(Connection conn, PersistentCache persistentCache) {
		super(V_saldi_plurien_voce_progettoBulk.class, conn, persistentCache);
	}

	public List<V_saldi_plurien_voce_progettoBulk> cercaPluriennalePianoEconomico(Progetto_piano_economicoBulk bulk) throws PersistencyException
	{
		SQLBuilder sql = this.createSQLBuilder();
		sql.addToHeader("");

		sql.addSQLClause(FindClause.AND,"PG_PROGETTO",SQLBuilder.EQUALS,bulk.getProgetto().getPg_progetto());
		//sql.addSQLClause(FindClause.AND,"ESERCIZIO",SQLBuilder.EQUALS,bulk.get().getEsercizio_piano());
		//sql.addSQLClause(FindClause.AND,"TI_GESTIONE",SQLBuilder.EQUALS,bulk.get);
		sql.addSQLClause(FindClause.AND,"CD_ELEMENTO_VOCE",SQLBuilder.EQUALS,bulk.getVoce_piano_economico().getCd_voce_piano());

		return fetchAll(sql);

	}
}