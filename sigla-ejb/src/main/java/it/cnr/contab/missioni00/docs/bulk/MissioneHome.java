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

package it.cnr.contab.missioni00.docs.bulk;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.AnagraficoHome;
import it.cnr.contab.anagraf00.core.bulk.RapportoBulk;
import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.config00.pdcep.bulk.*;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.docamm00.ejb.NumerazioneTempDocAmmComponentSession;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scad_voceBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioHome;
import it.cnr.contab.fondecon00.core.bulk.Fondo_spesaBulk;
import it.cnr.contab.reports.bulk.Print_spoolerBulk;
import it.cnr.contab.reports.bulk.Report;
import it.cnr.contab.reports.service.PrintService;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.spring.service.LDAPService;
import it.cnr.contab.util.SIGLAGroups;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.Broker;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.si.spring.storage.StorageDriver;
import it.cnr.si.spring.storage.StoreService;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MissioneHome extends BulkHome implements
        IDocumentoAmministrativoSpesaHome {
    public MissioneHome(Class aClass, java.sql.Connection conn) {
        super(aClass, conn);
    }

    public MissioneHome(Class aClass, java.sql.Connection conn,
                        PersistentCache persistentCache) {
        super(aClass, conn, persistentCache);
    }

    public MissioneHome(java.sql.Connection conn) {
        super(MissioneBulk.class, conn);
    }

    public MissioneHome(java.sql.Connection conn,
                        PersistentCache persistentCache) {
        super(MissioneBulk.class, conn, persistentCache);
    }

    /**
     * Il metodo inserisco la missione, i dettagli, le tappe con numerazione
     * definitiva
     */
    public void confermaMissioneTemporanea(UserContext userContext,
                                           MissioneBulk missioneTemp, Long pg) throws IntrospectionException,
            PersistencyException {
        // Il progressivo della missione ricevuto come parametro è quello
        // definitivo
        LoggableStatement ps = null;
        java.io.StringWriter sql = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sql);
        String condition = " WHERE ESERCIZIO = ? AND CD_CDS = ? AND CD_UNITA_ORGANIZZATIVA = ? AND PG_MISSIONE = ?";

        /***************** CONFERMO LA TESTATA DELLA MISSIONE ***************************/
        try {
            pw.write("INSERT INTO " + EJBCommonServices.getDefaultSchema()
                    + getColumnMap().getTableName() + " (");
            pw.write(getPersistenColumnNamesReplacingWith(this, null)
                    .toString());
            pw.write(") SELECT ");
            pw.write(getPersistenColumnNamesReplacingWith(this,
                    new String[][]{{"PG_MISSIONE", "?"}}).toString());
            pw.write(" FROM " + EJBCommonServices.getDefaultSchema()
                    + getColumnMap().getTableName());
            pw.write(condition);
            pw.flush();

            ps = new LoggableStatement(getConnection(), sql.toString(), true,
                    this.getClass());
            pw.close();

            ps.setLong(1, pg.longValue());
            ps.setInt(2, missioneTemp.getEsercizio().intValue());
            ps.setString(3, missioneTemp.getCd_cds());
            ps.setString(4, missioneTemp.getCd_unita_organizzativa());
            ps.setLong(5, missioneTemp.getPg_missione().longValue());

            ps.execute();
        } catch (java.sql.SQLException e) {
            throw new PersistencyException(e);
        } finally {
            try {
                ps.close();
            } catch (java.sql.SQLException e) {
            }
            ;
        }
        /***************** CONFERMO LE TAPPE DELLA MISSIONE ***************************/
        try {
            sql = new java.io.StringWriter();
            pw = new java.io.PrintWriter(sql);
            Missione_tappaHome tappaHome = (Missione_tappaHome) getHomeCache()
                    .getHome(Missione_tappaBulk.class);
            pw.write("INSERT INTO " + EJBCommonServices.getDefaultSchema()
                    + tappaHome.getColumnMap().getTableName() + " (");
            pw.write(getPersistenColumnNamesReplacingWith(tappaHome, null)
                    .toString());
            pw.write(") SELECT ");
            pw.write(getPersistenColumnNamesReplacingWith(tappaHome,
                    new String[][]{{"PG_MISSIONE", "?"}}).toString());
            pw.write(" FROM " + EJBCommonServices.getDefaultSchema()
                    + tappaHome.getColumnMap().getTableName());
            pw.write(condition);
            pw.flush();
            ps = new LoggableStatement(getConnection(), sql.toString(), true,
                    this.getClass());
            pw.close();

            ps.setLong(1, pg.longValue());
            ps.setInt(2, missioneTemp.getEsercizio().intValue());
            ps.setString(3, missioneTemp.getCd_cds());
            ps.setString(4, missioneTemp.getCd_unita_organizzativa());
            ps.setLong(5, missioneTemp.getPg_missione().longValue());

            ps.execute();
        } catch (java.sql.SQLException e) {
            throw new PersistencyException(e);
        } finally {
            try {
                ps.close();
            } catch (java.sql.SQLException e) {
            }
            ;
        }
        /***************** CONFERMO LE SPESE DELLA MISSIONE ***************************/
        try {
            sql = new java.io.StringWriter();
            pw = new java.io.PrintWriter(sql);
            Missione_dettaglioHome dettaglioHome = (Missione_dettaglioHome) getHomeCache()
                    .getHome(Missione_dettaglioBulk.class);
            pw.write("INSERT INTO " + EJBCommonServices.getDefaultSchema()
                    + dettaglioHome.getColumnMap().getTableName() + " (");
            pw.write(getPersistenColumnNamesReplacingWith(dettaglioHome, null)
                    .toString());
            pw.write(") SELECT ");
            pw.write(getPersistenColumnNamesReplacingWith(dettaglioHome,
                    new String[][]{{"PG_MISSIONE", "?"}}).toString());
            pw.write(" FROM " + EJBCommonServices.getDefaultSchema()
                    + dettaglioHome.getColumnMap().getTableName());
            pw.write(condition);
            pw.flush();
            ps = new LoggableStatement(getConnection(), sql.toString(), true,
                    this.getClass());
            pw.close();
            ps.setLong(1, pg.longValue());
            ps.setInt(2, missioneTemp.getEsercizio().intValue());
            ps.setString(3, missioneTemp.getCd_cds());
            ps.setString(4, missioneTemp.getCd_unita_organizzativa());
            ps.setLong(5, missioneTemp.getPg_missione().longValue());

            ps.execute();
        } catch (java.sql.SQLException e) {
            throw new PersistencyException(e);
        } finally {
            try {
                ps.close();
            } catch (java.sql.SQLException e) {
            }
            ;
        }

        // Cancello la missione, spese e tappe con numerazione temporanea
        delete(missioneTemp, userContext);

        missioneTemp.setPg_missione(pg);
    }

    private StringBuffer getPersistenColumnNamesReplacingWith(BulkHome home,
                                                              String[][] fields) throws PersistencyException {
        java.io.StringWriter columns = new java.io.StringWriter();

        if (home == null)
            throw new PersistencyException(
                    "Impossibile ottenere la home per l'aggiornamento dei progressivi temporanei della missione!");

        java.io.PrintWriter pw = new java.io.PrintWriter(columns);
        String[] persistenColumns = home.getColumnMap().getColumnNames();
        for (int i = 0; i < persistenColumns.length; i++) {
            String columnName = persistenColumns[i];
            if (fields != null) {
                for (int j = 0; j < fields.length; j++) {
                    String[] field = fields[j];
                    if (columnName.equalsIgnoreCase(field[0])) {
                        columnName = field[1];
                        break;
                    }
                }
            }
            pw.print(columnName);
            pw.print((i == persistenColumns.length - 1) ? "" : ", ");
        }
        pw.close();
        return columns.getBuffer();
    }

    /**
     * Il metodo viene chiamato dal creaConBulk, cioe' in fase di salvataggio
     * temporaneo della missione
     */

    public void initializePrimaryKeyForInsert(
            it.cnr.jada.UserContext userContext, OggettoBulk bulk)
            throws it.cnr.jada.comp.ComponentException {
        MissioneBulk missione = (MissioneBulk) bulk;

        try {
            // Assegno un progressivo temporaneo alla missione

            NumerazioneTempDocAmmComponentSession session = (NumerazioneTempDocAmmComponentSession) it.cnr.jada.util.ejb.EJBCommonServices
                    .createEJB(
                            "CNRDOCAMM00_EJB_NumerazioneTempDocAmmComponentSession",
                            NumerazioneTempDocAmmComponentSession.class);
            Numerazione_doc_ammBulk numerazione = new Numerazione_doc_ammBulk(
                    missione);
            missione.setPg_missione(session.getNextTempPG(userContext,
                    numerazione));
        } catch (Throwable e) {
            throw new it.cnr.jada.comp.ComponentException(e);
        }
    }

    /**
     * Il metodo carica la missione legata al compenso ricevuto come parametro
     */
    public MissioneBulk loadMissione(it.cnr.jada.UserContext userContext,
                                     it.cnr.contab.compensi00.docs.bulk.CompensoBulk compenso)
            throws PersistencyException {

        SQLBuilder sql = createSQLBuilder();
        sql.addSQLClause("AND", "CD_CDS", sql.EQUALS, compenso
                .getCd_cds_missione());
        sql.addSQLClause("AND", "ESERCIZIO", sql.EQUALS, compenso
                .getEsercizio_missione());
        sql.addSQLClause("AND", "CD_UNITA_ORGANIZZATIVA", sql.EQUALS, compenso
                .getCd_uo_missione());
        sql.addSQLClause("AND", "PG_MISSIONE", sql.EQUALS, compenso
                .getPg_missione());

        MissioneBulk missione = null;
        Broker broker = createBroker(sql);
        if (broker.next())
            missione = (MissioneBulk) fetch(broker);
        broker.close();
        getHomeCache().fetchAll(userContext);

        return missione;
    }

    /**
     * Il metodo carica la missione legata all'anticipo ricevuto come parametro
     */
    public MissioneBulk loadMissione(it.cnr.jada.UserContext userContext,
                                     AnticipoBulk anticipo) throws PersistencyException {
        SQLBuilder sql = createSQLBuilder();

        sql.addSQLClause("AND", "CD_CDS_ANTICIPO", sql.EQUALS, anticipo
                .getCd_cds());
        sql.addSQLClause("AND", "ESERCIZIO_ANTICIPO", sql.EQUALS, anticipo
                .getEsercizio());
        sql.addSQLClause("AND", "CD_UO_ANTICIPO", sql.EQUALS, anticipo
                .getCd_unita_organizzativa());
        sql.addSQLClause("AND", "PG_ANTICIPO", sql.EQUALS, anticipo
                .getPg_anticipo());
        sql.addSQLClause("AND", "STATO_COFI", sql.NOT_EQUALS, MissioneBulk.STATO_ANNULLATO);


        MissioneBulk missione = null;
        Broker broker = createBroker(sql);
        if (broker.next())
            missione = (MissioneBulk) fetch(broker);
        broker.close();
        getHomeCache().fetchAll(userContext);

        return missione;
    }

	/**
	 * Il metodo carica la missione legata all'ID di gemis
	 */
	public MissioneBulk loadMissione(it.cnr.jada.UserContext userContext,
			Long idRimborsoMissioneGemis)
			throws PersistencyException {

		SQLBuilder sql = createSQLBuilder();
		sql.addSQLClause("AND", "ID_RIMBORSO_MISSIONE", sql.EQUALS, idRimborsoMissioneGemis);

		MissioneBulk missione = null;
		Broker broker = createBroker(sql);
		if (broker.next())
			missione = (MissioneBulk) fetch(broker);
		broker.close();
		getHomeCache().fetchAll(userContext);

		return missione;
	}


    /**
     * Metodo richiesto dall'interfaccia IDocumentoAmministrativoSpesaHome Il
     * metodo aggiorna la missione dopo che e' stata collegata ad una spesa del
     * Fondo Economale
     */

    public void updateFondoEconomale(Fondo_spesaBulk spesa)
            throws it.cnr.jada.persistency.PersistencyException,
            it.cnr.jada.bulk.OutdatedResourceException,
            it.cnr.jada.bulk.BusyResourceException {
        if (spesa == null)
            return;

        MissioneBulk missione = (MissioneBulk) spesa.getDocumento();

        lock(missione);

        StringBuffer stm = new StringBuffer("UPDATE ");
        stm.append(it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema());
        stm.append(getColumnMap().getTableName());
        stm
                .append(" SET STATO_PAGAMENTO_FONDO_ECO = ?, DT_PAGAMENTO_FONDO_ECO = ?, PG_VER_REC = PG_VER_REC+1, DUVA = ?, UTUV = ?");
        stm.append(" WHERE (");
        stm
                .append("CD_CDS = ? AND CD_UNITA_ORGANIZZATIVA = ? AND ESERCIZIO = ? AND PG_MISSIONE = ? )");

        try {
            LoggableStatement ps = new LoggableStatement(getConnection(), stm
                    .toString(), true, this.getClass());
            try {
                ps
                        .setString(
                                1,
                                (spesa.isToBeCreated() || spesa.isToBeUpdated()) ? missione.REGISTRATO_IN_FONDO_ECO
                                        : missione.FONDO_ECO);
                if (spesa.isToBeCreated() || spesa.isToBeUpdated())
                    ps.setTimestamp(2, spesa.getDt_spesa());
                else
                    ps.setNull(2, java.sql.Types.TIMESTAMP);

                ps.setTimestamp(3, getServerTimestamp());
                ps.setString(4, spesa.getUser());

                ps.setString(5, missione.getCd_cds());
                ps.setString(6, missione.getCd_unita_organizzativa());
                ps.setInt(7, missione.getEsercizio().intValue());
                ps.setLong(8, missione.getPg_missione().longValue());

                ps.executeUpdate();
            } finally {
                try {
                    ps.close();
                } catch (java.sql.SQLException e) {
                }
                ;
            }
        } catch (java.sql.SQLException e) {
            throw it.cnr.jada.persistency.sql.SQLExceptionHandler.getInstance()
                    .handleSQLException(e, spesa);
        }
    }

    @SuppressWarnings("unchecked")
    public void archiviaStampa(UserContext userContext, MissioneBulk missione)
            throws IntrospectionException, PersistencyException {
        Boolean isDipendente = Boolean.FALSE;
        Integer matricola = null;
        String stato_pagamento_fondo_eco = missione.getStato_pagamento_fondo_eco();
        Timestamp dt_pagamento_fondo_eco = missione.getDt_pagamento_fondo_eco();
        if (missione.getTi_provvisorio_definitivo().equalsIgnoreCase("D")) {
            Collection<RapportoBulk> rapporti = ((AnagraficoHome) getHomeCache()
                    .getHome(AnagraficoBulk.class)).findRapporti(missione
                    .getTerzo().getAnagrafico());
            for (RapportoBulk rapporto : rapporti) {
                if (rapporto.getMatricola_dipendente() != null) {
                    isDipendente = Boolean.TRUE;
                    matricola = rapporto.getMatricola_dipendente();
                    break;
                }
            }
        }
        if (missione.getFl_associato_compenso()) {
            stato_pagamento_fondo_eco = missione.getCompenso().getStato_pagamento_fondo_eco();
            dt_pagamento_fondo_eco = missione.getCompenso().getDt_pagamento_fondo_eco();
        }
        if (isDipendente && missione.getTipo_rapporto().isDipendente()) {
            if ((missione.getAnticipo() != null
                    && missione.getAnticipo().getIm_anticipo().compareTo(
                    missione.getIm_netto_pecepiente()) > 0) ||
                    (stato_pagamento_fondo_eco.equals(MissioneBulk.REGISTRATO_IN_FONDO_ECO) &&
                            dt_pagamento_fondo_eco != null)) {
                Print_spoolerBulk print = new Print_spoolerBulk();
                print.setPgStampa(UUID.randomUUID().getLeastSignificantBits());
                print.setFlEmail(false);
                print.setReport("/docamm/docamm/vpg_missione.jasper");
                print.setNomeFile("Missione n. "
                        + missione.getPg_missione()
                        + " della UO "
                        + missione.getCd_unita_organizzativa()
                        + " del "
                        + new SimpleDateFormat("dd-MM-yyyy").format(missione
                        .getDt_inizio_missione()) + ".pdf");
                print.setUtcr(userContext.getUser());
                print.addParam("aCd_cds", missione.getCd_cds(), String.class);
                print.addParam("aCd_uo", missione.getCd_unita_organizzativa(),
                        String.class);
                print.addParam("aEs", missione.getEsercizio(), Integer.class);
                print.addParam("aPg_da", missione.getPg_missione(), Long.class);
                print.addParam("aPg_a", missione.getPg_missione(), Long.class);
                print.addParam("aCd_terzo", String.valueOf(missione
                        .getCd_terzo()), String.class);

                try {
                    missione
                            .setUnitaOrganizzativa((Unita_organizzativaBulk) getHomeCache()
                                    .getHome(Unita_organizzativaBulk.class)
                                    .findByPrimaryKey(
                                            new Unita_organizzativaBulk(
                                                    missione
                                                            .getCd_unita_organizzativa())));
                    String cmisPath;
                    if (missione.getFl_associato_compenso())
                        cmisPath = SpringUtil.getBean(StorePath.class).getPathConcorrentiFormazioneReddito();
                    else
                        cmisPath = SpringUtil.getBean(StorePath.class).getPathNonConcorrentiFormazioneReddito();

                    LDAPService ldapService = SpringUtil.getBean("ldapService",
                            LDAPService.class);
                    String uid = ldapService.getLdapUserFromMatricola(
                            userContext, matricola);

                    Report report = SpringUtil.getBean("printService",
                            PrintService.class).executeReport(userContext,
                            print);
                    SpringUtil.getBean("storeService", StoreService.class).storeSimpleDocument(
                            missione,
                            report.getInputStream(),
                            report.getContentType(),
                            report.getName(),
                            cmisPath,
                            StorageDriver.Permission.construct(uid, StorageDriver.ACLType.Consumer),
                            StorageDriver.Permission.construct(SIGLAGroups.GROUP_EMPPAY_GROUP.name(), StorageDriver.ACLType.Coordinator)
                    );
                } catch (Exception e) {
                    throw new PersistencyException(e);
                }
            }
        }
    }

    public java.util.List<Missione_riga_ecoBulk> findMissioneRigheEcoList(MissioneBulk docRiga ) throws PersistencyException {
        PersistentHome home = getHomeCache().getHome(Missione_riga_ecoBulk.class);

        it.cnr.jada.persistency.sql.SQLBuilder sql = home.createSQLBuilder();
        sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, docRiga.getEsercizio());
        sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, docRiga.getCd_cds());
        sql.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, docRiga.getCd_unita_organizzativa());
        sql.addClause(FindClause.AND, "pg_missione", SQLBuilder.EQUALS, docRiga.getPg_missione());
        return home.fetchAll(sql);
    }

    public ContoBulk getContoCostoDefault(MissioneBulk docRiga) throws ComponentException {
        try {
            Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);
            if (Optional.ofNullable(docRiga).isPresent()) {
                if (Optional.ofNullable(docRiga.getObbligazione_scadenzario()).isPresent()) {
                    Obbligazione_scadenzarioBulk obbligScad = (Obbligazione_scadenzarioBulk) fatpasHome.loadIfNeededObject(docRiga.getObbligazione_scadenzario());

                    if (Optional.ofNullable(obbligScad).isPresent()) {
                        ObbligazioneBulk obblig = (ObbligazioneBulk) fatpasHome.loadIfNeededObject(obbligScad.getObbligazione());
                        Ass_ev_voceepHome assEvVoceEpHome = (Ass_ev_voceepHome) getHomeCache().getHome(Ass_ev_voceepBulk.class);
                        List<Ass_ev_voceepBulk> listAss = assEvVoceEpHome.findVociEpAssociateVoce(new Elemento_voceBulk(obblig.getCd_elemento_voce(), obblig.getEsercizio(), obblig.getTi_appartenenza(), obblig.getTi_gestione()));
                        return Optional.ofNullable(listAss).orElse(new ArrayList<>())
                                .stream().map(Ass_ev_voceepBulk::getVoce_ep)
                                .findAny().orElse(null);
                    }
                }
                //Se lo scadenzario non esiste in quanto compenso coperto da anticipo, allora il conto di costo lo recupero
                //dall'anticipo stesso
                if (Optional.ofNullable(docRiga.getAnticipo()).isPresent()) {
                    AnticipoHome anticipoHome = (AnticipoHome)getHomeCache().getHome(AnticipoBulk.class);
                    return anticipoHome.getContoEconomicoForMissione((AnticipoBulk)anticipoHome.findByPrimaryKey(docRiga.getAnticipo()));
                }
            }
            return null;
        } catch (PersistencyException e) {
            throw new DetailedRuntimeException(e);
        }
    }

    private ContoBulk getContoEconomicoDefault(MissioneBulk missione) throws ComponentException {
        try {
            Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);
            if (Optional.ofNullable(missione).isPresent()) {
                if (Optional.ofNullable(missione.getObbligazione_scadenzario()).isPresent()) {
                    Obbligazione_scadenzarioBulk obbligScad = (Obbligazione_scadenzarioBulk) fatpasHome.loadIfNeededObject(missione.getObbligazione_scadenzario());

                    if (Optional.ofNullable(obbligScad).isPresent()) {
                        ObbligazioneBulk obblig = (ObbligazioneBulk) fatpasHome.loadIfNeededObject(obbligScad.getObbligazione());
                        Ass_ev_voceepHome assEvVoceEpHome = (Ass_ev_voceepHome) getHomeCache().getHome(Ass_ev_voceepBulk.class);
                        List<Ass_ev_voceepBulk> listAss = assEvVoceEpHome.findVociEpAssociateVoce(new Elemento_voceBulk(obblig.getCd_elemento_voce(), obblig.getEsercizio(), obblig.getTi_appartenenza(), obblig.getTi_gestione()));
                        return Optional.ofNullable(listAss).orElse(new ArrayList<>())
                                .stream().map(Ass_ev_voceepBulk::getVoce_ep)
                                .findAny().orElse(null);
                    }
                }
            }
            return null;
        } catch (PersistencyException e) {
            throw new DetailedRuntimeException(e);
        }
    }

    private List<IDocumentoDetailAnaCogeBulk> getDatiAnaliticiDefault(UserContext userContext, MissioneBulk missione, ContoBulk aContoEconomico) throws ComponentException {
        try {
            List<Missione_riga_ecoBulk> result = new ArrayList<>();

            if (Optional.ofNullable(aContoEconomico).isPresent()) {
                List<Voce_analiticaBulk> voceAnaliticaList = ((Voce_analiticaHome) getHomeCache().getHome(Voce_analiticaBulk.class)).findVoceAnaliticaList(aContoEconomico);
                if (voceAnaliticaList.isEmpty())
                    return new ArrayList<>();
                Voce_analiticaBulk voceAnaliticaDef = voceAnaliticaList.stream()
                        .filter(Voce_analiticaBulk::getFl_default).findAny()
                        .orElse(voceAnaliticaList.stream().findAny().orElse(null));

                Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);
                if (Optional.ofNullable(missione.getObbligazione_scadenzario()).isPresent()) {
                    Obbligazione_scadenzarioBulk obbligScad = (Obbligazione_scadenzarioBulk) fatpasHome.loadIfNeededObject(missione.getObbligazione_scadenzario());

                    if (Optional.ofNullable(obbligScad).isPresent()) {
                        //carico i dettagli analitici recuperandoli dall'obbligazione_scad_voce
                        Obbligazione_scadenzarioHome obbligazioneScadenzarioHome = (Obbligazione_scadenzarioHome) getHomeCache().getHome(Obbligazione_scadenzarioBulk.class);
                        List<Obbligazione_scad_voceBulk> scadVoceBulks = obbligazioneScadenzarioHome.findObbligazione_scad_voceList(userContext, obbligScad);
                        BigDecimal totScad = scadVoceBulks.stream().map(Obbligazione_scad_voceBulk::getIm_voce).reduce(BigDecimal.ZERO, BigDecimal::add);
                        for (Obbligazione_scad_voceBulk scadVoce : scadVoceBulks) {
                            Missione_riga_ecoBulk myRigaEco = new Missione_riga_ecoBulk();
                            myRigaEco.setProgressivo_riga_eco((long) result.size() + 1);
                            myRigaEco.setVoce_analitica(voceAnaliticaDef);
                            myRigaEco.setLinea_attivita(scadVoce.getLinea_attivita());
                            myRigaEco.setMissione(missione);
                            myRigaEco.setImporto(scadVoce.getIm_voce().multiply(missione.getImportoCostoEco()).divide(totScad, 2, RoundingMode.HALF_UP));
                            myRigaEco.setToBeCreated();
                            result.add(myRigaEco);
                        }
                        BigDecimal totRipartito = result.stream().map(Missione_riga_ecoBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal diff = totRipartito.subtract(missione.getImportoCostoEco());

                        if (diff.compareTo(BigDecimal.ZERO) > 0) {
                            for (Missione_riga_ecoBulk rigaEco : result) {
                                if (rigaEco.getImporto().compareTo(diff) >= 0) {
                                    rigaEco.setImporto(rigaEco.getImporto().subtract(diff));
                                    break;
                                } else {
                                    diff = diff.subtract(rigaEco.getImporto());
                                    rigaEco.setImporto(BigDecimal.ZERO);
                                }
                            }
                        } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                            for (Missione_riga_ecoBulk rigaEco : result) {
                                rigaEco.setImporto(rigaEco.getImporto().add(diff));
                                break;
                            }
                        }
                    }
                }
            }
            return result.stream()
                    .filter(el->el.getImporto().compareTo(BigDecimal.ZERO)!=0)
                    .collect(Collectors.toList());
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
    }

    public Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> getDatiEconomiciDefault(UserContext userContext, MissioneBulk missione) throws ComponentException {
        if (Optional.ofNullable(missione.getAnticipo()).isPresent()) {
            AnticipoHome anticipoHome = (AnticipoHome) getHomeCache().getHome(AnticipoBulk.class);
            Pair<ContoBulk, List<IDocumentoDetailAnaCogeBulk>> datiEcoAnticipoForMissione = anticipoHome.getDatiEconomiciForMissione(userContext, missione.getAnticipo());
            BigDecimal totaleImportiAnalitici = datiEcoAnticipoForMissione.getSecond().stream().map(IDocumentoDetailAnaCogeBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
            ContoBulk aContoEconomicoAnticipo = datiEcoAnticipoForMissione.getFirst();
            List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = new ArrayList<>();

            if (!Optional.ofNullable(missione.getObbligazione_scadenzario()).isPresent()) {
                for (IDocumentoDetailAnaCogeBulk rigaEco : datiEcoAnticipoForMissione.getSecond()) {
                    Missione_riga_ecoBulk myRigaEco = new Missione_riga_ecoBulk();
                    myRigaEco.setProgressivo_riga_eco((long) aContiAnalitici.size() + 1);
                    myRigaEco.setVoce_analitica(rigaEco.getVoce_analitica());
                    myRigaEco.setLinea_attivita(rigaEco.getLinea_attivita());
                    myRigaEco.setMissione(missione);
                    myRigaEco.setImporto(rigaEco.getImporto().multiply(missione.getImportoCostoEco()).divide(totaleImportiAnalitici, 2, RoundingMode.HALF_UP));
                    myRigaEco.setToBeCreated();
                    aContiAnalitici.add(myRigaEco);
                }

                BigDecimal totRipartito = aContiAnalitici.stream().map(IDocumentoDetailAnaCogeBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal diff = totRipartito.subtract(missione.getImportoCostoEco());

                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    for (IDocumentoDetailAnaCogeBulk rigaEco : aContiAnalitici) {
                        if (rigaEco.getImporto().compareTo(diff) >= 0) {
                            ((Missione_riga_ecoBulk)rigaEco).setImporto(rigaEco.getImporto().subtract(diff));
                            break;
                        } else {
                            diff = diff.subtract(rigaEco.getImporto());
                            ((Missione_riga_ecoBulk)rigaEco).setImporto(BigDecimal.ZERO);
                        }
                    }
                } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                    for (IDocumentoDetailAnaCogeBulk rigaEco : aContiAnalitici) {
                        ((Missione_riga_ecoBulk)rigaEco).setImporto(rigaEco.getImporto().add(diff));
                        break;
                    }
                }
                return Pair.of(aContoEconomicoAnticipo,
                        aContiAnalitici.stream().filter(el -> el.getImporto().compareTo(BigDecimal.ZERO) != 0)
                                .collect(Collectors.toList()));
            } else { //esiste obbligazione
                //carico l'anticipo per intero (L'ANTICIPO PUÒ ESSERE COLLEGATO AD UNA SOLA MISSIONE)
                for (IDocumentoDetailAnaCogeBulk rigaEco : datiEcoAnticipoForMissione.getSecond()) {
                    Missione_riga_ecoBulk myRigaEco = new Missione_riga_ecoBulk();
                    myRigaEco.setProgressivo_riga_eco((long) aContiAnalitici.size() + 1);
                    myRigaEco.setVoce_analitica(rigaEco.getVoce_analitica());
                    myRigaEco.setLinea_attivita(rigaEco.getLinea_attivita());
                    myRigaEco.setMissione(missione);
                    myRigaEco.setImporto(rigaEco.getImporto());
                    myRigaEco.setToBeCreated();
                    aContiAnalitici.add(myRigaEco);
                }
                ContoBulk aContoEconomicoMissione = this.getContoEconomicoDefault(missione);
                List<IDocumentoDetailAnaCogeBulk> aContiAnaliticiMissione = this.getDatiAnaliticiDefault(userContext, missione, aContoEconomicoMissione);
                for (IDocumentoDetailAnaCogeBulk rigaEco : aContiAnaliticiMissione) {
                    ((Missione_riga_ecoBulk)rigaEco).setProgressivo_riga_eco((long) aContiAnalitici.size() + 1);
                    aContiAnalitici.add(rigaEco);
                }
                return Pair.of(aContoEconomicoMissione, aContiAnalitici);
            }
        } else {
            ContoBulk aContoEconomico = this.getContoEconomicoDefault(missione);
            List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.getDatiAnaliticiDefault(userContext, missione, aContoEconomico);
            return Pair.of(aContoEconomico, aContiAnalitici);
        }
    }
}
