/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 25/01/2023
 */
package it.cnr.contab.config00.consultazioni.bulk;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;

import it.cnr.contab.anagraf00.core.bulk.BancaBulk;
import it.cnr.contab.config00.sto.bulk.DipartimentoBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.inventario00.docs.bulk.Transito_beni_ordiniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioHome;
import it.cnr.contab.ordmag.anag00.BeneServizioTipoGestBulk;
import it.cnr.contab.ordmag.anag00.MagazzinoBulk;
import it.cnr.contab.ordmag.anag00.MagazzinoHome;
import it.cnr.contab.ordmag.anag00.TipoOperazioneOrdBulk;
import it.cnr.contab.ordmag.magazzino.bulk.LottoMagBulk;
import it.cnr.contab.ordmag.magazzino.bulk.MovimentiMagBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqConsegnaBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqRigaBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.sql.*;

import jakarta.ejb.EJBException;

public class VControlloDispUnitaOrgHome extends BulkHome {
	public VControlloDispUnitaOrgHome(Connection conn) {
		super(VControlloDispUnitaOrgBulk.class, conn);
	}

	public VControlloDispUnitaOrgHome(Connection conn, PersistentCache persistentCache) {
		super(VControlloDispUnitaOrgBulk.class, conn, persistentCache);
	}

	public SQLBuilder selectByClause(UserContext usercontext, CompoundFindClause compoundfindclause) throws PersistencyException {
		SQLBuilder sql = super.selectByClause(usercontext, compoundfindclause);
		sql.addTableToHeader("UNITA_ORGANIZZATIVA");
		sql.addSQLJoin("V_CONTROLLO_DISP_UNITA_ORGANIZZATIVA.CD_UNITA_ORGANIZZATIVA", "UNITA_ORGANIZZATIVA.CD_UNITA_ORGANIZZATIVA");
		if (compoundfindclause != null && compoundfindclause.getClauses() != null) {

			Enumeration e = compoundfindclause.getClauses();

			while (e.hasMoreElements()) {
				FindClause findClause = (FindClause) e.nextElement();
				if (findClause instanceof SimpleFindClause) {
					SimpleFindClause clause = (SimpleFindClause) findClause;

					if (clause.getPropertyName() != null && clause.getPropertyName().equals("unitaOrganizzativa.cd_unita_organizzativa")) {

						//sql.addSQLJoin("V_CONTROLLO_DISP_UNITA_ORGANIZZATIVA.CD_UNITA_ORGANIZZATIVA", "UNITA_ORGANIZZATIVA.CD_UNITA_ORGANIZZATIVA");
						sql.addSQLClause("AND", "V_CONTROLLO_DISP_UNITA_ORGANIZZATIVA.CD_UNITA_ORGANIZZATIVA", clause.getOperator(), clause.getValue());
					}
				}
			}
		}
		return sql;
	}

	protected Persistent findByPrimaryKey(Object obj, Class class1, boolean flag) throws PersistencyException {

		VControlloDispUnitaOrgBulk bulk = (VControlloDispUnitaOrgBulk)obj;
		SQLBuilder sql = this.createSQLBuilder();

		sql.addTableToHeader("UNITA_ORGANIZZATIVA");
		sql.addSQLJoin("V_CONTROLLO_DISP_UNITA_ORGANIZZATIVA.CD_UNITA_ORGANIZZATIVA", "UNITA_ORGANIZZATIVA.CD_UNITA_ORGANIZZATIVA");
		sql.addSQLClause("AND", "V_CONTROLLO_DISP_UNITA_ORGANIZZATIVA.CD_UNITA_ORGANIZZATIVA",sql.EQUALS,bulk.getUnitaOrganizzativa().getCd_unita_organizzativa());

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