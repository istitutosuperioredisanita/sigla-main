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

package it.cnr.contab.inventario00.tabrif.bulk;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.persistency.Broker;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.Optional;

public class Inventario_ap_chHome extends BulkHome {
    public Inventario_ap_chHome(java.sql.Connection conn) {
        super(Inventario_ap_chBulk.class, conn);
    }

    public Inventario_ap_chHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Inventario_ap_chBulk.class, conn, persistentCache);
    }

    /**
     * Insert the method's description here.
     * Creation date: (06/12/2001 14.42.23)
     *
     * @throws ApplicationException
     */
    public Inventario_ap_chBulk findLastAperturaChiusuraObjFor(UserContext userContext, Id_inventarioBulk inv, Integer esercizio) throws IntrospectionException, PersistencyException, ApplicationException {
        Unita_organizzativaBulk uo = (Unita_organizzativaBulk) getHomeCache()
                .getHome(Unita_organizzativaBulk.class)
                .findByPrimaryKey(new Unita_organizzativaBulk(CNRUserContext.getCd_unita_organizzativa(userContext)));
        final Long pgInventario = Optional.ofNullable(inv)
                .map(Id_inventarioBulk::getPg_inventario)
                .orElse(null);

        if (pgInventario == null && !uo.isUoEnte())
            throw new it.cnr.jada.comp.ApplicationException("Attenzione: non esiste alcun inventario associato alla UO");

        SQLBuilder sql = createSQLBuilder();
        if (pgInventario != null)
            sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, pgInventario);
        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, esercizio);
        sql.addOrderBy("DT_APERTURA DESC");

        Broker broker = createBroker(sql);
        Inventario_ap_chBulk obj = null;
        if (broker.next())
            obj = (Inventario_ap_chBulk) fetch(broker);

        broker.close();
        return obj;
    }

    /**
     * Insert the method's description here.
     * Creation date: (06/12/2001 14.42.23)
     */
    public java.util.List findStoriaApChFor(Id_inventarioBulk inv, Integer esercizio) throws IntrospectionException, PersistencyException {

        SQLBuilder sql = createSQLBuilder();
        sql.addSQLClause("AND", "PG_INVENTARIO", SQLBuilder.EQUALS, inv.getPg_inventario());
        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, esercizio);

        return fetchAll(sql);
    }

    public java.util.List findAllPerAnno(Integer esercizio) throws IntrospectionException, PersistencyException {

        SQLBuilder sql = createSQLBuilder();
        sql.addSQLClause("AND", "ESERCIZIO", SQLBuilder.EQUALS, esercizio);

        return fetchAll(sql);
    }

    /**
     * Insert the method's description here.
     * Creation date: (06/12/2001 14.42.23)
     *
     * @throws ApplicationException
     */
    public boolean isAperto(UserContext userContext, Id_inventarioBulk inv, Integer esercizio) throws PersistencyException, IntrospectionException, ApplicationException {

        Inventario_ap_chBulk invApCh = findLastAperturaChiusuraObjFor(userContext, inv, esercizio);
        return (invApCh != null) && (invApCh.getStato().equals(Inventario_ap_chBulk.OPEN));
    }

    /**
     * Insert the method's description here.
     * Creation date: (06/12/2001 14.42.23)
     *
     * @throws ApplicationException
     */
    public boolean isAperto(UserContext userContext, Inventario_ap_chBulk invApCh, Integer esercizio) throws PersistencyException, IntrospectionException, ApplicationException {

        return isAperto(userContext, invApCh.getInventario(), esercizio);
    }

    /**
     * Insert the method's description here.
     * Creation date: (06/12/2001 14.42.23)
     *
     * @throws ApplicationException
     */
    public boolean isChiuso(UserContext userContext, Id_inventarioBulk inv, Integer esercizio) throws PersistencyException, IntrospectionException, ApplicationException {

        Inventario_ap_chBulk invApCh = findLastAperturaChiusuraObjFor(userContext, inv, esercizio);
        return (invApCh != null) && (invApCh.getStato().equals(Inventario_ap_chBulk.CLOSE));
    }

    /**
     * Insert the method's description here.
     * Creation date: (06/12/2001 14.42.23)
     *
     * @throws ApplicationException
     */
    public boolean isChiuso(UserContext userContext, Inventario_ap_chBulk invApCh, Integer esercizio) throws PersistencyException, IntrospectionException, ApplicationException {

        return isChiuso(userContext, invApCh.getInventario(), esercizio);
    }

    /**
     * Insert the method's description here.
     * Creation date: (06/12/2001 14.42.23)
     *
     * @throws ApplicationException
     */
    public boolean isPending(UserContext userContext, Id_inventarioBulk inv, Integer esercizio) throws PersistencyException, IntrospectionException, ApplicationException {

        Inventario_ap_chBulk invApCh = findLastAperturaChiusuraObjFor(userContext, inv, esercizio);
        return invApCh == null;
    }

    /**
     * Insert the method's description here.
     * Creation date: (06/12/2001 14.42.23)
     *
     * @throws ApplicationException
     */
    public boolean isPending(UserContext userContext, Inventario_ap_chBulk invApCh, Integer esercizio) throws PersistencyException, IntrospectionException, ApplicationException {
        return isPending(userContext, invApCh.getInventario(), esercizio);
    }
}
