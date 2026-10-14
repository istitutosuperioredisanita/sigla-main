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

package it.cnr.contab.docamm00.tabrif.bulk;

import it.cnr.contab.config00.sto.bulk.CdrBulk;
import it.cnr.contab.config00.sto.bulk.CdrHome;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.ColumnMapping;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Categoria_gruppo_inventHome extends BulkHome {
    public Categoria_gruppo_inventHome(java.sql.Connection conn) {
        super(Categoria_gruppo_inventBulk.class, conn);
    }

    public Categoria_gruppo_inventHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Categoria_gruppo_inventBulk.class, conn, persistentCache);
    }

    /**
     * Insert the method's description here.
     * Creation date: (28/11/2001 11.23.36)
     *
     * @param bulk it.cnr.contab.inventario00.tabrif.bulk.Ubicazione_beneBulk
     * @return it.cnr.contab.inventario00.tabrif.bulk.Ubicazione_beneBulk
     */
    public Categoria_gruppo_inventBulk getParent(Categoria_gruppo_inventBulk bulk) throws PersistencyException, IntrospectionException {

        if (bulk == null)
            return null;

        SQLBuilder sql = createSQLBuilder();

        sql.addSQLClause("AND", "CD_CATEGORIA_GRUPPO", SQLBuilder.EQUALS, bulk.getCd_categoria_gruppo());

        java.util.Collection coll = this.fetchAll(sql);
        if (coll.size() != 1)
            return null;

        return (Categoria_gruppo_inventBulk) coll.iterator().next();
    }

    /**
     * Recupera i figli dell'oggetto bulk
     * Creation date: (28/11/2001 10.57.42)
     *
     * @param bulk it.cnr.contab.inventario00.tabrif.bulk.Ubicazione_beneBulk
     * @return it.cnr.jada.persistency.sql.SQLBuilder
     */

    public SQLBuilder selectChildrenFor(it.cnr.jada.UserContext aUC, Categoria_gruppo_inventBulk cgi) throws it.cnr.jada.comp.ComponentException {

        SQLBuilder sql = createSQLBuilder();

        sql.addSQLClause("AND", "LIVELLO", SQLBuilder.EQUALS, "0");
        if (cgi == null)
            sql.addSQLClause("AND", "CD_CATEGORIA_PADRE", SQLBuilder.ISNULL, null);
        else {
            if (cgi.getCd_categoria_padre() != null)
                sql.addSQLClause("AND", "CATEGORIA_GRUPPO_INVENT.CD_CATEGORIA_GRUPPO", SQLBuilder.EQUALS, cgi.getCd_categoria_padre());
            if (cgi.getAssociazioneVoci() == null)
                throw new it.cnr.jada.comp.ComponentException("Operazione non valida.");
        }
        sql.addOrderBy("CD_CATEGORIA_GRUPPO");
        return sql;
    }

    public java.util.Collection findAssociazioneVoci(UserContext usercontext, Categoria_gruppo_inventBulk testata) throws IntrospectionException, PersistencyException {
        PersistentHome dettHome = getHomeCache().getHome(Categoria_gruppo_voceBulk.class);
        SQLBuilder sql = dettHome.createSQLBuilder();
        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
        sql.addSQLClause("AND", "CD_CATEGORIA_GRUPPO", SQLBuilder.EQUALS, testata.getCd_categoria_gruppo());
        sql.addOrderBy("CD_ELEMENTO_VOCE");
        return dettHome.fetchAll(sql);
    }

	public List<Categoria_gruppo_inventBulk> findRegistroInventario(UserContext userContext, Integer esercizio, String cdUnitaOrganizzativa) throws ComponentException, PersistencyException {
		CdrBulk cdrEnte = ((CdrHome) getHomeCache().getHome(CdrBulk.class)).findCdrEnte(esercizio);
		setColumnMap("REGISTRO_INVENTARIO");
		SQLBuilder sqlBuilder = super.createSQLBuilder();
		sqlBuilder.addTableToHeader("V_CONS_REGISTRO_INVENTARIO");
		sqlBuilder.addSQLJoin("CATEGORIA_GRUPPO_INVENT.CD_CATEGORIA_GRUPPO", "V_CONS_REGISTRO_INVENTARIO.CD_CATEGORIA_GRUPPO");
		sqlBuilder.addSQLClause(FindClause.AND, "V_CONS_REGISTRO_INVENTARIO.ESERCIZIO", SQLBuilder.EQUALS, esercizio);
		Optional.ofNullable(cdUnitaOrganizzativa)
				.ifPresent(s -> {
					sqlBuilder.addTableToHeader("CDR");
					sqlBuilder.addTableToHeader("V_UNITA_ORGANIZZATIVA_VALIDA");
					sqlBuilder.addSQLJoin("V_UNITA_ORGANIZZATIVA_VALIDA.CD_UNITA_ORGANIZZATIVA", "CDR.CD_UNITA_ORGANIZZATIVA");
					sqlBuilder.addSQLJoin("CDR.CD_CENTRO_RESPONSABILITA", "COALESCE(V_CONS_REGISTRO_INVENTARIO.CD_UTILIZZATORE_CDR, '" + cdrEnte.getCd_centro_responsabilita() + "')");
					sqlBuilder.addSQLJoin("V_UNITA_ORGANIZZATIVA_VALIDA.ESERCIZIO", "V_CONS_REGISTRO_INVENTARIO.ESERCIZIO");
					sqlBuilder.addSQLClause(FindClause.AND, "V_UNITA_ORGANIZZATIVA_VALIDA.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, s);
				});
		Collection<ColumnMapping> columnMappings = getColumnMap().getColumnMappings();
		columnMappings
				.stream()
				.filter(columnMapping -> !columnMapping.isCount())
				.map(ColumnMapping::getColumnName)
				.map(s -> "CATEGORIA_GRUPPO_INVENT.".concat(s))
				.forEach(sqlBuilder::addSQLGroupBy);
		return fetchAll(sqlBuilder);

	}

}
