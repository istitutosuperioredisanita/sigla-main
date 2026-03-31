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

import it.cnr.contab.coepcoan00.comp.ScritturaPartitaDoppiaComponent;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.doccont00.dto.EnumSiopeBilancioGestione;
import it.cnr.contab.doccont00.dto.SiopeBilancioDTO;
import it.cnr.contab.doccont00.dto.SiopeBilancioKeyDto;
import it.cnr.contab.doccont00.tabrif.bulk.CupBulk;
import it.cnr.contab.missioni00.docs.bulk.AnticipoBulk;
import it.cnr.contab.missioni00.docs.bulk.MissioneBulk;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ApplicationRuntimeException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;

import jakarta.ejb.EJBException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MandatoHome extends BulkHome {
    public MandatoHome(Class clazz, java.sql.Connection conn) {
        super(clazz, conn);
    }

    public MandatoHome(Class clazz, java.sql.Connection conn, PersistentCache persistentCache) {
        super(clazz, conn, persistentCache);
    }

    /**
     * <!-- @TODO: da completare -->
     * Costruisce un MandatoHome
     *
     * @param conn La java.sql.Connection su cui vengono effettuate le operazione di persistenza
     */
    public MandatoHome(java.sql.Connection conn) {
        super(MandatoBulk.class, conn);
    }

    /**
     * <!-- @TODO: da completare -->
     * Costruisce un MandatoHome
     *
     * @param conn            La java.sql.Connection su cui vengono effettuate le operazione di persistenza
     * @param persistentCache La PersistentCache in cui vengono cachati gli oggetti persistenti caricati da questo Home
     */
    public MandatoHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(MandatoBulk.class, conn, persistentCache);
    }

    /**
     * <!-- @TODO: da completare -->
     *
     * @param mandato
     * @return
     * @throws PersistencyException
     */
    public Timestamp findDataUltimoMandatoPerCds(MandatoBulk mandato) throws PersistencyException {
        try {
            LoggableStatement ps = new LoggableStatement(getConnection(),
                    "SELECT TRUNC(MAX(DT_EMISSIONE)) " +
                            "FROM " +
                            it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
                            "MANDATO WHERE " +
                            "ESERCIZIO = ? AND CD_CDS = ?", true, this.getClass());
            try {
                ps.setObject(1, mandato.getEsercizio());
                ps.setString(2, mandato.getCds().getCd_unita_organizzativa());

                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next())
                        return rs.getTimestamp(1);
                    else
                        return null;
                } catch (SQLException e) {
                    throw new PersistencyException(e);
                } finally {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                    }
                    ;
                }
            } catch (SQLException e) {
                throw new PersistencyException(e);
            } finally {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
                ;
            }
        } catch (SQLException e) {
            throw new PersistencyException(e);
        }
    }

    /**
     * <!-- @TODO: da completare -->
     *
     * @param mandato
     * @return
     * @throws PersistencyException
     * @throws IntrospectionException
     */
    public abstract Collection findMandato_riga(UserContext userContext, MandatoBulk mandato) throws PersistencyException;

    public abstract Collection findMandato_riga(UserContext userContext, MandatoBulk mandato, boolean fetchAll) throws PersistencyException;

    /**
     * <!-- @TODO: da completare -->
     *
     * @param mandato
     * @return
     * @throws PersistencyException
     * @throws IntrospectionException
     */
    public abstract Mandato_terzoBulk findMandato_terzo(UserContext userContext, MandatoBulk mandato) throws PersistencyException;

    public abstract Mandato_terzoBulk findMandato_terzo(UserContext userContext, MandatoBulk mandato, boolean fetchAll) throws PersistencyException;

    /**
     * Metodo per cercare i sospesi associati al mandato.
     *
     * @param mandato <code>MandatoBulk</code> il mandato
     * @return result i sospesi associati al mandato
     */
    public Collection findSospeso_det_usc(UserContext userContext, MandatoBulk mandato) throws PersistencyException, IntrospectionException {
        return findRiscontroSospeso_det_usc(userContext, mandato, SospesoBulk.TI_SOSPESO);
    }

    public Collection findRiscontro_det_usc(UserContext userContext, MandatoBulk mandato) throws PersistencyException, IntrospectionException {
        return findRiscontroSospeso_det_usc(userContext, mandato, SospesoBulk.TI_RISCONTRO);
    }
    public Collection findRiscontroSospeso_det_usc(UserContext userContext, MandatoBulk mandato, String sospesoRiscontro) throws PersistencyException, IntrospectionException {
        PersistentHome home = getHomeCache().getHome(Sospeso_det_uscBulk.class);
        SQLBuilder sql = home.createSQLBuilder();
        sql.addClause("AND", "esercizio", sql.EQUALS, mandato.getEsercizio());
        sql.addClause("AND", "cd_cds_mandato", sql.EQUALS, mandato.getCd_cds());
        sql.addClause("AND", "pg_mandato", sql.EQUALS, mandato.getPg_mandato());
        sql.addClause("AND", "ti_sospeso_riscontro", sql.EQUALS, sospesoRiscontro);
//	sql.addClause( "AND", "stato", sql.EQUALS, Sospeso_det_uscBulk.STATO_DEFAULT);
        Collection result = home.fetchAll(sql);
        getHomeCache().fetchAll(userContext);
        return result;
    }

    /**
     * Imposta il pg_mandato di un oggetto <code>MandatoBulk</code>.
     *
     * @param bulk <code>OggettoBulkBulk</code>
     * @throws PersistencyException
     */

    public void initializePrimaryKeyForInsert(UserContext userContext, OggettoBulk bulk) throws PersistencyException, ComponentException {
        try {
            MandatoBulk mandato = (MandatoBulk) bulk;
            Long pg;
            Numerazione_doc_contHome numHome = (Numerazione_doc_contHome) getHomeCache().getHome(Numerazione_doc_contBulk.class);
            if (Utility.createParametriCnrComponentSession().getParametriCnr(userContext, mandato.getEsercizio()).getFl_tesoreria_unica().booleanValue()) {
                Unita_organizzativa_enteBulk uoEnte = (Unita_organizzativa_enteBulk) (getHomeCache().getHome(Unita_organizzativa_enteBulk.class).findAll().get(0));
                pg = numHome.getNextPg(userContext, mandato.getEsercizio(), uoEnte.getCd_cds(), Numerazione_doc_contBulk.TIPO_MAN, mandato.getUser());
            } else {
                pg = numHome.getNextPg(userContext, mandato.getEsercizio(), mandato.getCd_cds(), Numerazione_doc_contBulk.TIPO_MAN, mandato.getUser());
            }
            mandato.setPg_mandato(pg);
        } catch (IntrospectionException e) {
            throw new PersistencyException(e);
        } catch (ApplicationException e) {
            throw new ComponentException(e);
        } catch (RemoteException e) {
            throw new ComponentException(e);
        } catch (EJBException e) {
            throw new ComponentException(e);
        }
    }

    /**
     * <!-- @TODO: da completare -->
     *
     * @param bulk
     * @return
     * @throws PersistencyException
     */
    public Hashtable loadTipoDocumentoKeys(MandatoBulk bulk) throws PersistencyException {
        SQLBuilder sql = getHomeCache().getHome(Tipo_documento_ammBulk.class).createSQLBuilder();
        sql.addClause("AND", "ti_entrata_spesa", sql.EQUALS, "S");
        List result = getHomeCache().getHome(Tipo_documento_ammBulk.class).fetchAll(sql);
        Hashtable ht = new Hashtable();
        Tipo_documento_ammBulk tipo;
        for (Iterator i = result.iterator(); i.hasNext(); ) {
            tipo = (Tipo_documento_ammBulk) i.next();
            ht.put(tipo.getCd_tipo_documento_amm(), tipo.getDs_tipo_documento_amm());
        }
        return ht;
    }

    /**
     * <!-- @TODO: da completare -->
     *
     * @param bulk
     * @return
     * @throws PersistencyException
     */
    public Hashtable loadTipoDocumentoPerRicercaKeys(MandatoBulk bulk) throws PersistencyException {
        SQLBuilder sql = getHomeCache().getHome(Tipo_documento_ammBulk.class).createSQLBuilder();
//	sql.addClause( "AND", "ti_entrata_spesa", sql.EQUALS, "S" );
        sql.openParenthesis("AND");
        sql.addSQLClause("AND", "fl_manrev_utente", sql.EQUALS, "M");
        sql.addSQLClause("OR", "fl_manrev_utente", sql.EQUALS, "E");
        sql.closeParenthesis();
        List result = getHomeCache().getHome(Tipo_documento_ammBulk.class).fetchAll(sql);
        Hashtable ht = new Hashtable();
        Tipo_documento_ammBulk tipo;
        for (Iterator i = result.iterator(); i.hasNext(); ) {
            tipo = (Tipo_documento_ammBulk) i.next();
            ht.put(tipo.getCd_tipo_documento_amm(), tipo.getDs_tipo_documento_amm());
        }
        return ht;
    }

    /**
     *
     * @param mandato
     * @return
     * @throws IntrospectionException
     * @throws PersistencyException
     */
    public List findMandato(MandatoBulk mandato) throws IntrospectionException, PersistencyException {
        final SQLBuilder sql = createSQLBuilder();
        Optional.ofNullable(mandato)
                .ifPresent(mandatoBulk -> sql.addClause(mandatoBulk.buildFindClauses(null)));
        return fetchAll(sql);
    }

    /**
     * Recupera tutti i CUP collegati al Mandato.
     *
     * @param mandatoBulk Mandato in uso.
     * @return java.util.Collection Collezione di oggetti <code>CUP</code>
     */
    public Collection<CupBulk> findCodiciSiopeCupCollegati(UserContext usercontext, MandatoBulk mandatoBulk) throws PersistencyException {
        PersistentHome mandatoSiopeCupHome = getHomeCache().getHome(MandatoSiopeCupIBulk.class);
        SQLBuilder sql = mandatoSiopeCupHome.createSQLBuilder();
        sql.setAutoJoins(true);
        sql.generateJoin("mandato_siopeI", "MANDATO_SIOPE");

        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.CD_CDS", SQLBuilder.EQUALS, mandatoBulk.getCd_cds());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.ESERCIZIO", SQLBuilder.EQUALS, mandatoBulk.getEsercizio());
        sql.addSQLClause(FindClause.AND, "MANDATO_SIOPE.PG_MANDATO", SQLBuilder.EQUALS, mandatoBulk.getPg_mandato());
        final Stream<MandatoSiopeCupBulk> stream = mandatoSiopeCupHome.fetchAll(sql)
                .stream()
                .filter(MandatoSiopeCupBulk.class::isInstance)
                .map(MandatoSiopeCupBulk.class::cast);
        getHomeCache().fetchAll(usercontext);
        return stream.filter(mandatoSiopeCupBulk -> Optional.ofNullable(mandatoSiopeCupBulk.getCdCup()).isPresent())
                .map(MandatoSiopeCupBulk::getCup).collect(Collectors.toList());
    }

    /**
     * Recupera tutti i Codici CUP collegati al Mandato.
     *
     * @param mandatoBulk Mandato in uso.
     * @return java.util.Collection Collezione di oggetti <code>CUP</code>
     */
    public Collection<String> findCodiciCupCollegati(UserContext usercontext, MandatoBulk mandatoBulk) throws PersistencyException {
        PersistentHome mandatoCupHome = getHomeCache().getHome(MandatoCupIBulk.class);
        SQLBuilder sql = mandatoCupHome.createSQLBuilder();
        sql.setAutoJoins(true);
        sql.generateJoin("mandato_rigaI", "MANDATO_RIGA");

        sql.addSQLClause(FindClause.AND, "MANDATO_RIGA.CD_CDS", SQLBuilder.EQUALS, mandatoBulk.getCd_cds());
        sql.addSQLClause(FindClause.AND, "MANDATO_RIGA.ESERCIZIO", SQLBuilder.EQUALS, mandatoBulk.getEsercizio());
        sql.addSQLClause(FindClause.AND, "MANDATO_RIGA.PG_MANDATO", SQLBuilder.EQUALS, mandatoBulk.getPg_mandato());
        final Stream<MandatoCupBulk> stream = mandatoCupHome.fetchAll(sql)
                .stream()
                .filter(MandatoCupBulk.class::isInstance)
                .map(MandatoCupBulk.class::cast);
        return stream.map(t -> t.getCdCup())
                .distinct()
                .collect(Collectors.toList());
    }
    public MandatoBulk findAndLockMandatoAnnullato(UserContext userContext, String cdCds, Integer esercizio, Long pgMandato) throws PersistencyException, OutdatedResourceException, BusyResourceException {

        return findAndLockMandato(cdCds, esercizio, pgMandato, true);
    }

    private MandatoBulk findAndLockMandato(String cdCds, Integer esercizio, Long pgMandato, Boolean annullato) throws PersistencyException, OutdatedResourceException, BusyResourceException {
        SQLBuilder sql = createSQLBuilder();
        sql.addClause("AND", "cd_cds", sql.EQUALS, cdCds);
        sql.addClause("AND", "esercizio", sql.EQUALS, esercizio);
        sql.addClause("AND", "pg_mandato", sql.EQUALS, pgMandato);
        sql.addClause("AND", "stato", annullato ? sql.EQUALS : sql.NOT_EQUALS, MandatoBulk.STATO_MANDATO_ANNULLATO);
        List mandati = fetchAll(sql);
        if (mandati == null || mandati.size() == 0) {
            return null;
        } else if (mandati.size() == 1) {
            MandatoBulk man = (MandatoBulk) mandati.get(0);
            lock(man);
            return man;
        } else {
            throw new PersistencyException("Errore nel recupero del Mandato " + esercizio + "-" + pgMandato);
        }
    }

    public MandatoBulk findAndLockMandatoNonAnnullato(UserContext userContext, String cdCds, Integer esercizio, Long pgMandato) throws PersistencyException, OutdatedResourceException, BusyResourceException {
        return findAndLockMandato(cdCds, esercizio, pgMandato, false);
    }

    public List getSiopeBilancio(UserContext userContext, MandatoBulk mandato ){
        List<SiopeBilancioDTO> bilancio = new ArrayList<SiopeBilancioDTO>();

            Optional.ofNullable(mandato).ifPresent(s -> {
                    s.getMandato_rigaColl().stream().forEach(m -> {
                        EnumSiopeBilancioGestione gestione = EnumSiopeBilancioGestione.COMPETENZA;
                        Integer annoResiduo = null;
                        if ( m.getEsercizio_obbligazione().compareTo(m.getEsercizio_ori_obbligazione())!=0) {
                            gestione = EnumSiopeBilancioGestione.RESIDUO;
                            annoResiduo=m.getEsercizio_ori_obbligazione();
                        }
                        final SiopeBilancioKeyDto keyBilancio = new SiopeBilancioKeyDto(m.elemento_voce.getCd_voce(),
                                gestione,
                                annoResiduo) ;
                        Optional<SiopeBilancioDTO> el=bilancio.stream().
                                filter(b->b.equals(keyBilancio)).
                                findFirst();
                        if ( el.isPresent()){
                            SiopeBilancioDTO voceBilancio = el.get();
                            voceBilancio.setImporto(voceBilancio.getImporto().add( m.getIm_mandato_riga()));
                        }else{
                            SiopeBilancioDTO voceBilancio = new SiopeBilancioDTO(keyBilancio);
                                voceBilancio.setDescrzioneVoceBilancio( m.elemento_voce.getDs_elemento_voce());
                                voceBilancio.setImporto(m.getIm_mandato_riga());
                                bilancio.add(voceBilancio);
                        }
                    });
                });
        return bilancio;
    }

    public static class MandatoRigaComplete {
        public MandatoRigaComplete(IDocumentoAmministrativoBulk docamm, Mandato_rigaBulk mandatoRiga, List<IDocumentoAmministrativoRigaBulk> docammRighe, Integer cdTerzo) {
            super();
            this.docamm = docamm;
            this.mandatoRiga = mandatoRiga;
            this.docammRighe = docammRighe;
            this.cdTerzo = cdTerzo;
        }

        private final IDocumentoAmministrativoBulk docamm;
        private final Mandato_rigaBulk mandatoRiga;
        private final List<IDocumentoAmministrativoRigaBulk> docammRighe;
        private final Integer cdTerzo;
        public IDocumentoAmministrativoBulk getDocamm() {
            return docamm;
        }

        public Mandato_rigaBulk getMandatoRiga() {
            return mandatoRiga;
        }

        public List<IDocumentoAmministrativoRigaBulk> getDocammRighe() {
            return docammRighe;
        }

        public Integer getCdTerzo() {
            return cdTerzo;
        }
    }

    public List<IDocumentoAmministrativoRigaBulk> getRigheDocamm(UserContext userContext, IDocumentoAmministrativoBulk docamm) {
        return Optional.ofNullable(docamm.getChildren()).filter(el -> !el.isEmpty()).orElseGet(() -> {
            try {
                List<IDocumentoAmministrativoRigaBulk> result;
                if (docamm instanceof Documento_genericoBulk) {
                    Documento_genericoHome home = (Documento_genericoHome) getHomeCache().getHome(Documento_genericoBulk.class);
                    result = home.findDocumentoGenericoRigheList((Documento_genericoBulk) docamm);
                    ((Documento_genericoBulk)docamm).setDocumento_generico_dettColl(new BulkList(result));
                } else if (docamm instanceof Fattura_passivaBulk) {
                    result = Utility.createFatturaPassivaComponentSession().findDettagli(userContext, (Fattura_passivaBulk) docamm);
                    ((Fattura_passivaBulk)docamm).setFattura_passiva_dettColl(new BulkList(result));
                } else if (docamm instanceof Fattura_attivaBulk) {
                    result = Utility.createFatturaAttivaSingolaComponentSession().findDettagli(userContext, (Fattura_attivaBulk) docamm);
                    ((Fattura_attivaBulk) docamm).setFattura_attiva_dettColl(new BulkList(result));
                } else
                    throw new ApplicationMessageFormatException("Non risulta gestito il recupero delle righe di dettaglio di un documento di tipo {0}.",docamm.getCd_tipo_doc());
                return result;
            } catch (ComponentException | PersistencyException | RemoteException | IntrospectionException e) {
                throw new ApplicationRuntimeException(e);
            }
        });
    }

    public List<MandatoRigaComplete> completeRigheMandato(UserContext userContext, MandatoBulk mandato) {
        //raggruppo i mandatiRiga per Partita
        Map<Integer, Map<String, Map<String, Map<String, Map<Long, List<Mandato_rigaBulk>>>>>> mapRigheMandato =
                mandato.getMandato_rigaColl().stream()
                        .collect(Collectors.groupingBy(Mandato_rigaBulk::getEsercizio_doc_amm,
                                Collectors.groupingBy(Mandato_rigaBulk::getCd_tipo_documento_amm,
                                        Collectors.groupingBy(Mandato_rigaBulk::getCd_cds_doc_amm,
                                                Collectors.groupingBy(Mandato_rigaBulk::getCd_uo_doc_amm,
                                                        Collectors.groupingBy(Mandato_rigaBulk::getPg_doc_amm))))));

        List<MandatoRigaComplete> mandatoRigaCompleteList = new ArrayList<>();
        mapRigheMandato.keySet().forEach(aEsercizioDocamm -> {
            Map<String, Map<String, Map<String, Map<Long, List<Mandato_rigaBulk>>>>> mapEsercizioDocamm = mapRigheMandato.get(aEsercizioDocamm);
            mapEsercizioDocamm.keySet().forEach(aTipoDocamm -> {
                Map<String, Map<String, Map<Long, List<Mandato_rigaBulk>>>> mapTipoDocamm = mapEsercizioDocamm.get(aTipoDocamm);
                mapTipoDocamm.keySet().forEach(aCdCdsDocamm -> {
                    Map<String, Map<Long, List<Mandato_rigaBulk>>> mapCdCdsDocamm = mapTipoDocamm.get(aCdCdsDocamm);
                    mapCdCdsDocamm.keySet().forEach(aCdUoDocamm -> {
                        Map<Long, List<Mandato_rigaBulk>> mapCdUoDocamm = mapCdCdsDocamm.get(aCdUoDocamm);
                        mapCdUoDocamm.keySet().forEach(aPgDocamm -> {
                            try {
                                List<Mandato_rigaBulk> listRigheMandato = mapCdUoDocamm.get(aPgDocamm);

                                IDocumentoAmministrativoBulk docamm;
                                if (TipoDocumentoEnum.fromValue(aTipoDocamm).isDocumentoAmministrativoPassivo()) {
                                    docamm = (Documento_amministrativo_passivoBulk) getHomeCache().getHome(Documento_amministrativo_passivoBulk.class)
                                            .findByPrimaryKey(new Documento_amministrativo_passivoBulk(aCdCdsDocamm, aCdUoDocamm, aEsercizioDocamm, aPgDocamm));
                                } else if (TipoDocumentoEnum.fromValue(aTipoDocamm).isDocumentoGenericoPassivo()) {
                                    Documento_genericoHome home = (Documento_genericoHome) getHomeCache().getHome(Documento_genericoBulk.class);
                                    docamm = (IDocumentoAmministrativoBulk)home.findByPrimaryKey(new Documento_genericoBulk(aCdCdsDocamm, aTipoDocamm, aCdUoDocamm, aEsercizioDocamm, aPgDocamm));
                                } else if (TipoDocumentoEnum.fromValue(aTipoDocamm).isMissione()) {
                                    docamm = (MissioneBulk) getHomeCache().getHome(MissioneBulk.class)
                                            .findByPrimaryKey(new MissioneBulk(aCdCdsDocamm,aCdUoDocamm, aEsercizioDocamm, aPgDocamm));
                                } else if (TipoDocumentoEnum.fromValue(aTipoDocamm).isAnticipo()) {
                                    docamm = (AnticipoBulk) getHomeCache().getHome(AnticipoBulk.class)
                                            .findByPrimaryKey(new AnticipoBulk(aCdCdsDocamm,aCdUoDocamm, aEsercizioDocamm, aPgDocamm));
                                } else {
                                    throw new ApplicationRuntimeException("Errore non gestito per la tipologia di documento " + aTipoDocamm +
                                            " collegato al mandato " + mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_manrev() + ".");
                                }
                                mandatoRigaCompleteList.addAll(listRigheMandato.stream().map(rigaMandato->{
                                    try {
                                        Integer cdTerzo;
                                        if (docamm instanceof MissioneBulk)
                                            return new MandatoRigaComplete(docamm, rigaMandato, null, ((MissioneBulk) docamm).getCd_terzo());
                                        else if (docamm instanceof AnticipoBulk)
                                            return new MandatoRigaComplete(docamm, rigaMandato, null, ((AnticipoBulk) docamm).getCd_terzo());
                                        else {
                                            List<IDocumentoAmministrativoRigaBulk> docammRighe = this.getRigheDocamm(userContext, docamm).stream()
                                                    .filter(el->el.getScadenzaDocumentoContabile() instanceof Obbligazione_scadenzarioBulk)
                                                    .filter(el->
                                                            ((Obbligazione_scadenzarioBulk)el.getScadenzaDocumentoContabile()).getEsercizio().equals(rigaMandato.getEsercizio_obbligazione()) &&
                                                                    ((Obbligazione_scadenzarioBulk)el.getScadenzaDocumentoContabile()).getEsercizio_originale().equals(rigaMandato.getEsercizio_ori_obbligazione()) &&
                                                                    ((Obbligazione_scadenzarioBulk)el.getScadenzaDocumentoContabile()).getCd_cds().equals(rigaMandato.getCd_cds()) &&
                                                                    ((Obbligazione_scadenzarioBulk)el.getScadenzaDocumentoContabile()).getPg_obbligazione().equals(rigaMandato.getPg_obbligazione()) &&
                                                                    ((Obbligazione_scadenzarioBulk)el.getScadenzaDocumentoContabile()).getPg_obbligazione_scadenzario().equals(rigaMandato.getPg_obbligazione_scadenzario())
                                                    ).collect(Collectors.toList());

                                            if (docammRighe.isEmpty())
                                                throw new ApplicationException("Non è stato possibile individuare correttamente la riga del documento " +
                                                        docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getCd_uo()+"/"+docamm.getPg_doc_amm()+
                                                        " associata alla riga del mandato "+ mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_manrev() +".");

                                            if (docammRighe.stream().collect(Collectors.groupingBy(IDocumentoAmministrativoRigaBulk::getCd_terzo)).size()>1)
                                                throw new ApplicationException("Risultano più righe del documento " +
                                                        docamm.getCd_tipo_doc()+"/"+docamm.getEsercizio()+"/"+docamm.getCd_uo()+"/"+docamm.getPg_doc_amm()+
                                                        " con terzi diversi associate alla stessa riga del mandato "+ mandato.getEsercizio() + "/" + mandato.getCd_cds() + "/" + mandato.getPg_manrev() +".");
                                            return new MandatoRigaComplete(docamm, rigaMandato, docammRighe,
                                                    docammRighe.stream().findAny().map(IDocumentoAmministrativoRigaBulk::getCd_terzo).orElse(null));
                                        }
                                    } catch (ComponentException ex) {
                                        throw new ApplicationRuntimeException(ex);
                                    }
                                }).collect(Collectors.toList()));
                            } catch (PersistencyException e) {
                                throw new ApplicationRuntimeException(e);
                            }
                        });
                    });
                });
            });
        });
        return mandatoRigaCompleteList;
    }
}
