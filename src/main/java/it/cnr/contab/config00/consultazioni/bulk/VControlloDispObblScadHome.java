/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 07/02/2023
 */
package it.cnr.contab.config00.consultazioni.bulk;
import java.sql.Connection;
import java.util.Enumeration;

import it.cnr.contab.docamm00.docs.bulk.Fattura_passiva_rigaBulk;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passiva_rigaIBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.persistency.FindException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.Persistent;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.*;

public class VControlloDispObblScadHome extends BulkHome {
	public VControlloDispObblScadHome(Connection conn) {
		super(VControlloDispObblScadBulk.class, conn);
	}

	public VControlloDispObblScadHome(Connection conn, PersistentCache persistentCache) {
		super(VControlloDispObblScadBulk.class, conn, persistentCache);
	}

	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
		SQLBuilder sql = super.selectByClause(usercontext, compoundfindclause);

		sql.addTableToHeader("UNITA_ORGANIZZATIVA");
		sql.addSQLJoin("V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO.CD_UNITA_ORGANIZZATIVA", "UNITA_ORGANIZZATIVA.CD_UNITA_ORGANIZZATIVA");

		if (compoundfindclause != null && compoundfindclause.getClauses() != null) {

			Enumeration e = compoundfindclause.getClauses();

			while (e.hasMoreElements()) {
				FindClause findClause = (FindClause) e.nextElement();
				if (findClause instanceof SimpleFindClause) {
					SimpleFindClause clause = (SimpleFindClause) findClause;
					if(clause.getPropertyName() != null ) {
						if (clause.getPropertyName().equals("unitaOrganizzativa.cd_unita_organizzativa")) {
							sql.addSQLClause("AND", "V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO.CD_UNITA_ORGANIZZATIVA", clause.getOperator(), clause.getValue());
						}
						if (clause.getPropertyName().equals("esercizioObbligazione")) {
							sql.addSQLClause("AND", "V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_OBBLIGAZIONE", clause.getOperator(), clause.getValue());
						}
						if (clause.getPropertyName().equals("esercizioOriObbligazione")) {
							sql.addSQLClause("AND", "V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORI_OBBLIGAZIONE", clause.getOperator(), clause.getValue());
						}
					}
				}
			}
		}
		return sql;
	}

	protected Persistent findByPrimaryKey(Object obj, Class class1, boolean flag) throws PersistencyException {

		VControlloDispObblScadBulk bulk = (VControlloDispObblScadBulk)obj;
		SQLBuilder sql = this.createSQLBuilder();

		sql.addTableToHeader("UNITA_ORGANIZZATIVA");
		sql.addSQLJoin("V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO.CD_UNITA_ORGANIZZATIVA", "UNITA_ORGANIZZATIVA.CD_UNITA_ORGANIZZATIVA");
		sql.addSQLClause("AND", "V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO.CD_UNITA_ORGANIZZATIVA",sql.EQUALS,bulk.getUnitaOrganizzativa().getCd_unita_organizzativa());
		sql.addSQLClause("AND", "V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_OBBLIGAZIONE",sql.EQUALS,bulk.getEsercizioObbligazione());
		sql.addSQLClause("AND", "V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORI_OBBLIGAZIONE",sql.EQUALS,bulk.getEsercizioOriObbligazione());
		sql.addSQLClause("AND", "V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO.CD_CDS_OBBLIGAZIONE",sql.EQUALS,bulk.getCdCdsObbligazione());
		sql.addSQLClause("AND", "V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE",sql.EQUALS,bulk.getPgObbligazione());
		sql.addSQLClause("AND", "V_CONTROLLO_DISP_OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO",sql.EQUALS,bulk.getPgObbligazioneScadenzario());


		SQLBroker sqlbroker = createBroker(sql);
		if (!sqlbroker.next()) {
			return null;
		} else {
			Persistent persistent = sqlbroker.fetch(class1);
			if (sqlbroker.next()) {
				sqlbroker.close();
				throw new FindException("SELECT statement return more than one row");
			} else {
				return persistent;
			}
		}

	}

}