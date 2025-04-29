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

package it.cnr.contab.docamm00.docs.bulk;

import it.cnr.contab.doccont00.core.bulk.V_doc_passivo_obbligazioneBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Optional;

public class Fattura_passivaHome extends BulkHome {
    protected Fattura_passivaHome(Class clazz, java.sql.Connection connection) {
        super(clazz, connection);
    }

    protected Fattura_passivaHome(Class clazz, java.sql.Connection connection, PersistentCache persistentCache) {
        super(clazz, connection, persistentCache);
    }

    public Fattura_passivaHome(java.sql.Connection conn) {
        super(Fattura_passivaBulk.class, conn);
    }

    public Fattura_passivaHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Fattura_passivaBulk.class, conn, persistentCache);
    }

    public java.util.List findDuplicateFatturaFornitore(Persistent clause) throws PersistencyException {
        return fetchAll(selectDuplicateFatturaFornitore((Fattura_passivaBulk) clause));
    }

    /**
     * Viene ricercata la Data di Registrazione del documento immediatamente precedente
     * a quello che si sta registrando/modificando.
     * Viene restituita la data trovata, NULL altrimenti
     *
     * @param FatturaPassivaBulk
     * @return Timestamp
     * @throws PersistencyException, IntrospectionException
     */
    public Timestamp findDataRegFatturaPrecedente(Fattura_passivaBulk fatturaPassiva) throws PersistencyException, IntrospectionException {
        SQLBuilder sql = createSQLBuilder();
        sql.setHeader("SELECT TRUNC(MAX(DT_REGISTRAZIONE)) AS DT_REGISTRAZIONE");
        sql.addClause("AND", "cd_cds", SQLBuilder.EQUALS, fatturaPassiva.getCd_cds());
        sql.addClause("AND", "cd_unita_organizzativa", SQLBuilder.EQUALS, fatturaPassiva.getCd_unita_organizzativa());
        sql.addClause("AND", "esercizio", SQLBuilder.EQUALS, fatturaPassiva.getEsercizio());
        sql.addClause("AND", "pg_fattura_passiva", SQLBuilder.LESS, fatturaPassiva.getPg_fattura_passiva());

        Broker broker = createBroker(sql);
        Object value = null;
        if (broker.next()) {
            value = broker.fetchPropertyValue("dt_registrazione", getIntrospector().getPropertyType(getPersistentClass(), "dt_registrazione"));
            broker.close();
        }
        return (Timestamp) value;
    }

    /**
     * Viene ricercata la Data di Registrazione del documento immediatamente successivo
     * a quello che si sta registrando/modificando.
     * Viene restituita la data trovata, NULL altrimenti
     *
     * @param FatturaPassivaBulk
     * @return Timestamp
     * @throws PersistencyException, IntrospectionException
     */
    public Timestamp findDataRegFatturaSuccessiva(Fattura_passivaBulk fatturaPassiva) throws PersistencyException, IntrospectionException {
        SQLBuilder sql = createSQLBuilder();
        sql.setHeader("SELECT TRUNC(MIN(DT_REGISTRAZIONE)) AS DT_REGISTRAZIONE");
        sql.addClause("AND", "cd_cds", SQLBuilder.EQUALS, fatturaPassiva.getCd_cds());
        sql.addClause("AND", "cd_unita_organizzativa", SQLBuilder.EQUALS, fatturaPassiva.getCd_unita_organizzativa());
        sql.addClause("AND", "esercizio", SQLBuilder.EQUALS, fatturaPassiva.getEsercizio());
        sql.addClause("AND", "pg_fattura_passiva", SQLBuilder.GREATER, fatturaPassiva.getPg_fattura_passiva());

        Broker broker = createBroker(sql);
        Object value = null;
        if (broker.next()) {
            value = broker.fetchPropertyValue("dt_registrazione", getIntrospector().getPropertyType(getPersistentClass(), "dt_registrazione"));
            broker.close();
        }
        return (Timestamp) value;
    }

    public SQLBuilder selectDuplicateFatturaFornitore(Fattura_passivaBulk clause) {

        clause.setTi_fattura(null);
        SQLBuilder sql = createSQLBuilder();
        sql.addClausesUsing(clause, false);
        return sql;
    }

    public SQLBuilder selectModalita(Fattura_passivaBulk fatturaPassivaBulk, it.cnr.contab.docamm00.tabrif.bulk.DivisaHome divisaHome, it.cnr.contab.docamm00.tabrif.bulk.DivisaBulk clause) {

        return divisaHome.createSQLBuilder();
    }

    public SQLBuilder selectTermini(Fattura_passivaBulk fatturaPassivaBulk, it.cnr.contab.docamm00.tabrif.bulk.DivisaHome divisaHome, it.cnr.contab.docamm00.tabrif.bulk.DivisaBulk clause) {

        return divisaHome.createSQLBuilder();
    }

    public SQLBuilder selectValuta(Fattura_passivaBulk fatturaPassivaBulk, it.cnr.contab.docamm00.tabrif.bulk.DivisaHome divisaHome, it.cnr.contab.docamm00.tabrif.bulk.DivisaBulk clause) {

        SQLBuilder sql = divisaHome.createSQLBuilder();

        sql.addTableToHeader("CAMBIO");
        sql.addSQLJoin("DIVISA.CD_DIVISA", "CAMBIO.CD_DIVISA");

        return sql;
    }

    public java.util.List<Fattura_passiva_rigaIBulk> findFatturaPassivaRigheList(V_doc_passivo_obbligazioneBulk docPassivo) throws PersistencyException {
        if (TipoDocumentoEnum.fromValue(docPassivo.getCd_tipo_documento_amm()).isDocumentoAmministrativoPassivo()) {
            PersistentHome home = getHomeCache().getHome(Fattura_passiva_rigaIBulk.class);
            it.cnr.jada.persistency.sql.SQLBuilder sql = home.createSQLBuilder();
            sql.addClause(FindClause.AND, "pg_documento_generico", SQLBuilder.EQUALS, docPassivo.getPg_documento_amm());
            sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, docPassivo.getCd_cds());
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, docPassivo.getEsercizio());
            sql.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, docPassivo.getCd_unita_organizzativa());
            sql.addClause(FindClause.AND, "cd_tipo_documento_amm", SQLBuilder.EQUALS, docPassivo.getCd_tipo_documento_amm());
            sql.addClause(FindClause.AND, "cd_cds_obbligazione", SQLBuilder.EQUALS, docPassivo.getCd_cds_obbligazione());
            sql.addClause(FindClause.AND, "esercizio_obbligazione", SQLBuilder.EQUALS, docPassivo.getEsercizio_obbligazione());
            sql.addClause(FindClause.AND, "esercizio_ori_obbligazione", SQLBuilder.EQUALS, docPassivo.getEsercizio_ori_obbligazione());
            sql.addClause(FindClause.AND, "pg_obbligazione", SQLBuilder.EQUALS, docPassivo.getPg_obbligazione());
            sql.addClause(FindClause.AND, "pg_obbligazione_scadenzario()", SQLBuilder.EQUALS, docPassivo.getPg_obbligazione_scadenzario());

            return home.fetchAll(sql);
        }
        return Collections.EMPTY_LIST;
    }

    public OggettoBulk loadIfNeededObject(OggettoBulk object) {
        return Optional.of(object).filter(el->el.getCrudStatus()!=OggettoBulk.UNDEFINED).orElseGet(()-> {
            try {
                BulkHome bulkHome = (BulkHome)getHomeCache().getHome(object.getClass());
                return (OggettoBulk)bulkHome.findByPrimaryKey(object);
            } catch (PersistencyException ex) {
                throw new DetailedRuntimeException(ex);
            }
        });
    }
}
