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

package it.cnr.contab.doccont00.core.bulk;

import it.cnr.contab.docamm00.docs.bulk.Numerazione_doc_ammBulk;
import it.cnr.contab.docamm00.docs.bulk.Tipo_documento_ammBulk;
import it.cnr.contab.ordmag.ordini.bulk.OrdineAcqConsegnaBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.OutdatedResourceException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class Obbligazione_scadenzarioHome extends BulkHome implements IScadenzaDocumentoContabileHome {
    /**
     * <!-- @TODO: da completare -->
     * Costruisce un Obbligazione_scadenzarioHome
     *
     * @param conn La java.sql.Connection su cui vengono effettuate le operazione di persistenza
     */
    public Obbligazione_scadenzarioHome(Class clazz, java.sql.Connection conn) {
        super(clazz, conn);
    }

    /**
     * <!-- @TODO: da completare -->
     * Costruisce un Obbligazione_scadenzarioHome
     *
     * @param conn            La java.sql.Connection su cui vengono effettuate le operazione di persistenza
     * @param persistentCache La PersistentCache in cui vengono cachati gli oggetti persistenti caricati da questo Home
     */
    public Obbligazione_scadenzarioHome(Class clazz, java.sql.Connection conn, PersistentCache persistentCache) {
        super(clazz, conn, persistentCache);
    }

    /**
     * <!-- @TODO: da completare -->
     * Costruisce un Obbligazione_scadenzarioHome
     *
     * @param conn La java.sql.Connection su cui vengono effettuate le operazione di persistenza
     */
    public Obbligazione_scadenzarioHome(java.sql.Connection conn) {
        super(Obbligazione_scadenzarioBulk.class, conn);
    }

    /**
     * <!-- @TODO: da completare -->
     * Costruisce un Obbligazione_scadenzarioHome
     *
     * @param conn            La java.sql.Connection su cui vengono effettuate le operazione di persistenza
     * @param persistentCache La PersistentCache in cui vengono cachati gli oggetti persistenti caricati da questo Home
     */
    public Obbligazione_scadenzarioHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Obbligazione_scadenzarioBulk.class, conn, persistentCache);
    }

    public void aggiornaImportoAssociatoADocAmm(
            UserContext userContext,
            IScadenzaDocumentoContabileBulk scadenzaDocCont)
            throws PersistencyException, BusyResourceException, OutdatedResourceException {

        if (scadenzaDocCont == null) return;

        Obbligazione_scadenzarioBulk scadenza = (Obbligazione_scadenzarioBulk) scadenzaDocCont;
        try {
            ((BulkHome) getHomeCache().getHome(scadenza.getFather().getClass())).lock((OggettoBulk) scadenza.getFather());
            //getHomeCache().getHome(scadenza.getFather().getClass()).update((Persistent)scadenza.getFather());
        } catch (OutdatedResourceException e) {
        }

        lock(scadenza);

        getHomeCache().getHome(Obbligazione_scadenzarioBulk.class, "IMPORTO_ASSOCIATO").update(scadenza, userContext);

    }

    /**
     * Metodo per cercare i documenti passivi associati all'obbligazione.
     *
     * @param os <code>Obbligazione_scadenzarioBulk</code> la scadenza dell'obbligazione
     * @return <code>V_doc_passivo_obbligazioneBulk</code> i documenti passivi associati all'obbligazione
     * null non è stato trovato nessun documento passivo associato all'obbligazione
     */
    public V_doc_passivo_obbligazioneBulk findDoc_passivo(Obbligazione_scadenzarioBulk os) throws IntrospectionException, PersistencyException {
        PersistentHome docHome = getHomeCache().getHome(V_doc_passivo_obbligazioneBulk.class);
        SQLBuilder sql = docHome.createSQLBuilder();
        sql.addSQLClause("AND", "CD_CDS_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getCd_cds());
        sql.addSQLClause("AND", "ESERCIZIO_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getEsercizio());
        sql.addSQLClause("AND", "ESERCIZIO_ORI_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getEsercizio_originale());
        sql.addSQLClause("AND", "PG_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getPg_obbligazione());
        sql.addSQLClause("AND", "PG_OBBLIGAZIONE_SCADENZARIO", SQLBuilder.EQUALS, os.getPg_obbligazione_scadenzario());
        sql.addSQLClause("AND", "CD_TIPO_DOCUMENTO_AMM", SQLBuilder.NOT_EQUALS, Numerazione_doc_ammBulk.TIPO_ORDINE);
        sql.addOrderBy("FL_SELEZIONE DESC");
        sql.addOrderBy("DT_FATTURA_FORNITORE ASC");
        List l =  docHome.fetchAll(sql);
        if ( l.size() > 0 )
            return (V_doc_passivo_obbligazioneBulk) l.get(0);
        else
            return null;
    }

    public List<V_doc_passivo_obbligazioneBulk> findDocs_ordine(Obbligazione_scadenzarioBulk os) throws PersistencyException {
        PersistentHome docHome = getHomeCache().getHome(V_doc_passivo_obbligazioneBulk.class);
        SQLBuilder sql = docHome.createSQLBuilder();
        sql.addSQLClause("AND", "CD_CDS_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getCd_cds());
        sql.addSQLClause("AND", "ESERCIZIO_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getEsercizio());
        sql.addSQLClause("AND", "ESERCIZIO_ORI_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getEsercizio_originale());
        sql.addSQLClause("AND", "PG_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getPg_obbligazione());
        sql.addSQLClause("AND", "PG_OBBLIGAZIONE_SCADENZARIO", SQLBuilder.EQUALS, os.getPg_obbligazione_scadenzario());
        sql.addSQLClause("AND", "CD_TIPO_DOCUMENTO_AMM", SQLBuilder.EQUALS, Numerazione_doc_ammBulk.TIPO_ORDINE);
        sql.addOrderBy("FL_SELEZIONE DESC");
        return docHome.fetchAll(sql);
    }
    public V_doc_passivo_obbligazioneBulk findDoc_ordine(Obbligazione_scadenzarioBulk os) throws PersistencyException {
        return findDocs_ordine(os).stream().findFirst().orElse(null);
    }

    public List<OrdineAcqConsegnaBulk> findConsegne(Obbligazione_scadenzarioBulk os) throws PersistencyException {
        PersistentHome docHome = getHomeCache().getHome(OrdineAcqConsegnaBulk.class);
        SQLBuilder sql = docHome.createSQLBuilder();
        sql.addClause(FindClause.AND, "obbligazioneScadenzario", SQLBuilder.EQUALS, os);
        return docHome.fetchAll(sql);
    }

    /**
     * <!-- @TODO: da completare -->
     *
     * @param os
     * @return
     * @throws IntrospectionException
     * @throws PersistencyException
     */
    public Mandato_rigaBulk findMandato(Obbligazione_scadenzarioBulk os) throws IntrospectionException, PersistencyException {
        PersistentHome mrHome = getHomeCache().getHome(Mandato_rigaIBulk.class);
        SQLBuilder sql = mrHome.createSQLBuilder();
        sql.addSQLClause("AND", "CD_CDS", SQLBuilder.EQUALS, os.getCd_cds());
        sql.addSQLClause("AND", "ESERCIZIO_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getEsercizio());
        sql.addSQLClause("AND", "ESERCIZIO_ORI_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getEsercizio_originale());
        sql.addSQLClause("AND", "PG_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getPg_obbligazione());
        sql.addSQLClause("AND", "PG_OBBLIGAZIONE_SCADENZARIO", SQLBuilder.EQUALS, os.getPg_obbligazione_scadenzario());
        sql.addSQLClause("AND", "STATO", SQLBuilder.NOT_EQUALS, Mandato_rigaBulk.STATO_ANNULLATO);
        List l = mrHome.fetchAll(sql);
        if (l.size() > 0)
            return (Mandato_rigaBulk) l.get(0);
        return null;
    }

    public java.util.List findObbligazione_scad_voceList(it.cnr.jada.UserContext userContext, Obbligazione_scadenzarioBulk os) throws PersistencyException {
        return findObbligazione_scad_voceList(userContext, os, Boolean.TRUE);
    }

    public java.util.List findObbligazione_scad_voceList(it.cnr.jada.UserContext userContext, Obbligazione_scadenzarioBulk os, boolean fetchAll) throws PersistencyException {
//	PersistentHome osvHome = getHomeCache().getHome(Obbligazione_scad_voceBulk.class, "default", "it.cnr.contab.doccont00.comp.ObbligazioneComponent.edit" );
        PersistentHome osvHome = getHomeCache().getHome(Obbligazione_scad_voceBulk.class);
        SQLBuilder sql = osvHome.createSQLBuilder();
        sql.addSQLClause(FindClause.AND, "CD_CDS", SQLBuilder.EQUALS, os.getObbligazione().getCds().getCd_unita_organizzativa());
        sql.addSQLClause(FindClause.AND, "ESERCIZIO", SQLBuilder.EQUALS, os.getObbligazione().getEsercizio());
        sql.addSQLClause(FindClause.AND, "ESERCIZIO_ORIGINALE", SQLBuilder.EQUALS, os.getObbligazione().getEsercizio_originale());
        sql.addSQLClause(FindClause.AND, "PG_OBBLIGAZIONE", SQLBuilder.EQUALS, os.getObbligazione().getPg_obbligazione());
        sql.addSQLClause(FindClause.AND, "PG_OBBLIGAZIONE_SCADENZARIO", SQLBuilder.EQUALS, os.getPg_obbligazione_scadenzario());
        sql.addOrderBy("CD_LINEA_ATTIVITA");
        List l = osvHome.fetchAll(sql);
        if (fetchAll)
            getHomeCache().fetchAll(userContext);
        return l;
    }

    /**
     * <!-- @TODO: da completare -->
     *
     * @param bulk
     * @return
     * @throws PersistencyException
     */
    public java.util.Hashtable loadTipoDocumentoKeys(Obbligazione_scadenzarioBulk bulk) throws PersistencyException {
        SQLBuilder sql = getHomeCache().getHome(Tipo_documento_ammBulk.class).createSQLBuilder();
        sql.addClause("AND", "ti_entrata_spesa", SQLBuilder.EQUALS, "S");
        List result = getHomeCache().getHome(Tipo_documento_ammBulk.class).fetchAll(sql);
        Hashtable ht = new Hashtable();
        Tipo_documento_ammBulk tipo;
        for (Iterator i = result.iterator(); i.hasNext(); ) {
            tipo = (Tipo_documento_ammBulk) i.next();
            ht.put(tipo.getCd_tipo_documento_amm(), tipo.getDs_tipo_documento_amm());
        }
        return ht;
    }
}
