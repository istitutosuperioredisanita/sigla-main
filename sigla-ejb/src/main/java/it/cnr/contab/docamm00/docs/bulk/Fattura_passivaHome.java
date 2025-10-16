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

import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.doccont00.core.bulk.V_doc_passivo_obbligazioneBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<Fattura_passiva_rigaBulk> findFatturaPassivaRigheList(Fattura_passivaBulk fatturaPassiva) throws PersistencyException {
        PersistentHome home;
        if (fatturaPassiva instanceof Nota_di_creditoBulk)
            home = getHomeCache().getHome(Nota_di_credito_rigaBulk.class);
        else if (fatturaPassiva instanceof Nota_di_debitoBulk)
            home = getHomeCache().getHome(Nota_di_debito_rigaBulk.class);
        else
            home = getHomeCache().getHome(Fattura_passiva_rigaIBulk.class);

        it.cnr.jada.persistency.sql.SQLBuilder sql = home.createSQLBuilder();
        sql.addClause(FindClause.AND, "pg_fattura_passiva", SQLBuilder.EQUALS, fatturaPassiva.getPg_fattura_passiva());
        sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, fatturaPassiva.getCd_cds());
        sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, fatturaPassiva.getEsercizio());
        sql.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, fatturaPassiva.getCd_unita_organizzativa());
        return home.fetchAll(sql);
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

    public String callVerificaStatoRiporto(UserContext userContext, Fattura_passivaBulk fatturaPassiva) throws PersistencyException {
            Unita_organizzativa_enteBulk ente = (Unita_organizzativa_enteBulk) getHomeCache().getHome(Unita_organizzativa_enteBulk.class).findAll().get(0);
            String aCdCdsEnte = ente.getUnita_padre().getCd_unita_organizzativa();

            List<Fattura_passiva_rigaBulk> righeFattura = this.findFatturaPassivaRigheList(fatturaPassiva);

            List<Integer> eserciziDoccont = null;
            if (fatturaPassiva instanceof Nota_di_creditoBulk && fatturaPassiva.getCd_cds().equals(aCdCdsEnte))
                eserciziDoccont = righeFattura.stream()
                    .map(Fattura_passiva_rigaBulk::getEsercizio_accertamento)
                    .distinct()
                    .collect(Collectors.toList());
            else
                eserciziDoccont = righeFattura.stream()
                        .filter(el->Optional.ofNullable(el.getObbligazione_scadenziario()).isPresent())
                        .map(el->el.getObbligazione_scadenziario().getEsercizio())
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

            int aEsScr = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext);
            int aEsDocCont = eserciziDoccont.stream().mapToInt(v -> v).max().orElseThrow(NoSuchElementException::new);
            int aEs = fatturaPassiva.getEsercizio();

            // 1. Es = EsScr = aEsDocCont
            if (aEs == aEsScr && aEsScr == aEsDocCont)
                return IDocumentoAmministrativoBulk.NON_RIPORTATO;
            // 2. Es = aEsDocCont < aEsScr
            //caso non significativo, il documento non è comunque modificabile
            else if (aEs == aEsDocCont && aEsDocCont < aEsScr)
                return IDocumentoAmministrativoBulk.NON_RIPORTATO;
            // 3. Es < EsScr = aEsDocCont
            else if (aEs < aEsScr && aEsScr == aEsDocCont)
                return IDocumentoAmministrativoBulk.NON_RIPORTATO;
            // 4. Es = EsScr < aEsDocCont
            else if (aEs == aEsScr && aEsScr < aEsDocCont)
                return eserciziDoccont.size()>1?IDocumentoAmministrativoBulk.PARZIALMENTE_RIPORTATO
                        :IDocumentoAmministrativoBulk.COMPLETAMENTE_RIPORTATO;
            // 5. Es < aEsDocCont < EsScr
            // caso non significativo, il documento  non è comunque modificabile
            else if (aEs < aEsDocCont && aEsDocCont < aEsScr)
                return IDocumentoAmministrativoBulk.NON_RIPORTATO;
            // 6. Es < EsScr < aEsDocCont
            else if (aEs < aEsScr && aEsScr < aEsDocCont)
                return eserciziDoccont.size()>1?IDocumentoAmministrativoBulk.PARZIALMENTE_RIPORTATO
                        :IDocumentoAmministrativoBulk.COMPLETAMENTE_RIPORTATO;
        return eserciziDoccont.size()>1?IDocumentoAmministrativoBulk.PARZIALMENTE_RIPORTATO
                :IDocumentoAmministrativoBulk.COMPLETAMENTE_RIPORTATO;
    }

    public String callVerificaStatoRiportoInScrivania(UserContext userContext, Fattura_passivaBulk fatturaPassiva) throws PersistencyException {
        Unita_organizzativa_enteBulk ente = (Unita_organizzativa_enteBulk) getHomeCache().getHome(Unita_organizzativa_enteBulk.class).findAll().get(0);
        String aCdCdsEnte = ente.getUnita_padre().getCd_unita_organizzativa();

        List<Fattura_passiva_rigaBulk> righeFattura = this.findFatturaPassivaRigheList(fatturaPassiva);

        List<Integer> eserciziDoccont = null;
        if (fatturaPassiva instanceof Nota_di_creditoBulk && fatturaPassiva.getCd_cds().equals(aCdCdsEnte))
            eserciziDoccont = righeFattura.stream()
                    .map(Fattura_passiva_rigaBulk::getEsercizio_accertamento)
                    .distinct()
                    .collect(Collectors.toList());
        else
            eserciziDoccont = righeFattura.stream()
                    .filter(el->Optional.ofNullable(el.getObbligazione_scadenziario()).isPresent())
                    .map(el->el.getObbligazione_scadenziario().getEsercizio())
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

        int aEsScr = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext);
        int aEsDocCont = eserciziDoccont.stream().mapToInt(v -> v).max().orElseThrow(NoSuchElementException::new);
        int aEs = fatturaPassiva.getEsercizio();

        if (aEsDocCont == aEsScr) {
            if (aEs == aEsDocCont)
                // documento mai riportato
                return IDocumentoAmministrativoBulk.NON_RIPORTATO;
            else
                return eserciziDoccont.size() > 1 ? IDocumentoAmministrativoBulk.PARZIALMENTE_RIPORTATO
                        : IDocumentoAmministrativoBulk.COMPLETAMENTE_RIPORTATO;
        } else
            return IDocumentoAmministrativoBulk.NON_RIPORTATO;
    }
}
