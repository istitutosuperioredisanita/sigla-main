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
/**
 * Insert the type's description here.
 * Creation date: (9/5/2001 5:02:18 PM)
 *
 * @author: Ardire Alfonso
 */

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.config00.bulk.*;
import it.cnr.contab.config00.latt.bulk.CostantiTi_gestione;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcep.bulk.*;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ApplicationRuntimeException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.*;
import it.cnr.jada.persistency.sql.*;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Documento_generico_rigaHome extends BulkHome {
    public Documento_generico_rigaHome(java.sql.Connection conn) {
        super(Documento_generico_rigaBulk.class, conn);
    }

    public Documento_generico_rigaHome(java.sql.Connection conn, PersistentCache persistentCache) {
        super(Documento_generico_rigaBulk.class, conn, persistentCache);
    }

    /**
     * Inizializza la chiave primaria di un OggettoBulk per un
     * inserimento. Da usare principalmente per riempire i progressivi
     * automatici.
     * @param bulk l'OggettoBulk da inizializzare
     */
    public void initializePrimaryKeyForInsert(it.cnr.jada.UserContext userContext, OggettoBulk bulk) throws PersistencyException, it.cnr.jada.comp.ComponentException {

        if (bulk == null) return;
        try {
            Documento_generico_rigaBulk riga = (Documento_generico_rigaBulk) bulk;
            java.sql.Connection contact = getConnection();
            java.sql.ResultSet rs = contact.createStatement().executeQuery("SELECT MAX(PROGRESSIVO_RIGA) FROM " +
                    it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
                    "DOCUMENTO_GENERICO_RIGA WHERE " +
                    "(ESERCIZIO = " + riga.getEsercizio() + ") AND " +
                    "(CD_CDS = '" + riga.getCd_cds() + "') AND " +
                    "(CD_UNITA_ORGANIZZATIVA = '" + riga.getCd_unita_organizzativa() + "') AND " +
                    "(CD_TIPO_DOCUMENTO_AMM = '" + riga.getDocumento_generico().getCd_tipo_documento_amm() + "') AND " +
                    "(PG_DOCUMENTO_GENERICO = " + riga.getPg_documento_generico() + ")");
            Long x;
            if (rs.next())
                x = Long.valueOf(rs.getLong(1) + 1);
            else
                x = Long.valueOf(0);
            riga.setProgressivo_riga(x);
        } catch (java.sql.SQLException sqle) {
            throw new PersistencyException(sqle);
        }
    }

    public Hashtable loadTiDocumentoAmmKeys(Documento_generico_rigaBulk documentoGenericoRigaBulk) throws PersistencyException {
        SQLBuilder sql = getHomeCache().getHome(Tipo_documento_ammBulk.class).createSQLBuilder();
        List<Tipo_documento_ammBulk> result = getHomeCache().getHome(Tipo_documento_ammBulk.class).fetchAll(sql);
        return new Hashtable(result
                .stream()
                .collect(Collectors.toMap(
                        Tipo_documento_ammKey::getCd_tipo_documento_amm,
                        Tipo_documento_ammBase::getDs_tipo_documento_amm, (key, value) -> value, HashMap::new)
                ));

    }

    public SQLBuilder selectDocumentForReverse(
            UserContext usercontext,
            Documento_generico_rigaBulk documentoGenericoRigaBulk,
            CompoundFindClause compoundfindclause,
            Object... objects
    ) throws PersistencyException {
        PersistentHome persistentHome = getHomeCache().getHome(Documento_generico_rigaBulk.class, "STORNO");
        SQLBuilder sqlBuilder = persistentHome.createSQLBuilder();
        sqlBuilder.setAutoJoins(true);
        Optional.ofNullable(compoundfindclause).ifPresent(sqlBuilder::addClause);
        sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.LESS_EQUALS, CNRUserContext.getEsercizio(usercontext));
        Stream.of(Documento_genericoBulk.STATO_ANNULLATO, Documento_genericoBulk.STATO_PAGATO).forEach(s -> {
            sqlBuilder.addClause(FindClause.AND, "stato_cofi", SQLBuilder.NOT_EQUALS, s);
        });
        sqlBuilder.generateJoin(Documento_genericoBulk.class, CausaleContabileBulk.class, "causaleContabile", "CAUSALE_CONTABILE");
        sqlBuilder.addClause(FindClause.AND, "documento_generico.causaleContabile.cdCausale", SQLBuilder.ISNOTNULL, null);
        sqlBuilder.addClause(FindClause.AND, "documento_generico.cd_uo_origine", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(usercontext));
        sqlBuilder.addClause(FindClause.AND, "documento_generico.fl_storno", SQLBuilder.NOT_EQUALS, Boolean.TRUE);
        sqlBuilder.generateJoin(Documento_genericoBulk.class, Tipo_documento_ammBulk.class,  "tipo_documento", "TIPO_DOCUMENTO_AMM");
        sqlBuilder.addClause(FindClause.AND, "documento_generico.tipo_documento.fl_utilizzo_doc_generico", SQLBuilder.EQUALS, Boolean.TRUE);
        String tiEntrataSpesa = Arrays.stream(objects).map(String::valueOf).findFirst().orElse(null);
        sqlBuilder.addClause(
                FindClause.AND,
                "documento_generico.tipo_documento.ti_entrata_spesa",
                SQLBuilder.EQUALS,
                tiEntrataSpesa
        );
        /*
		Devo escludere dalla selezione delle fatture quelle non riportate
		 */
        String esercizio = Objects.equals(tiEntrataSpesa, CostantiTi_gestione.TI_GESTIONE_SPESE) ? "ESERCIZIO_OBBLIGAZIONE" : "ESERCIZIO_ACCERTAMENTO";
        sqlBuilder.openParenthesis(FindClause.AND);
        sqlBuilder.addSQLClause(FindClause.AND, esercizio, SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
        sqlBuilder.addSQLClause(FindClause.OR, esercizio, SQLBuilder.ISNULL, null);
        sqlBuilder.closeParenthesis();

        SQLBuilder sqlNotExists = createSQLBuilder();
        sqlNotExists.setFromClause(new StringBuffer("DOCUMENTO_GENERICO_RIGA STORNO, DOCUMENTO_GENERICO TESTATA_STORNO"));
        sqlNotExists.resetColumns();
        sqlNotExists.addColumn("1");
        sqlNotExists.addSQLJoin("STORNO.CD_CDS_STORNO", "DOCUMENTO_GENERICO_RIGA.CD_CDS");
        sqlNotExists.addSQLJoin("STORNO.CD_UNITA_ORGANIZZATIVA_STORNO", "DOCUMENTO_GENERICO_RIGA.CD_UNITA_ORGANIZZATIVA");
        sqlNotExists.addSQLJoin("STORNO.ESERCIZIO_STORNO", "DOCUMENTO_GENERICO_RIGA.ESERCIZIO");
        sqlNotExists.addSQLJoin("STORNO.CD_TIPO_DOCUMENTO_AMM_STORNO", "DOCUMENTO_GENERICO_RIGA.CD_TIPO_DOCUMENTO_AMM");
        sqlNotExists.addSQLJoin("STORNO.PG_DOCUMENTO_GENERICO_STORNO", "DOCUMENTO_GENERICO_RIGA.PG_DOCUMENTO_GENERICO");
        sqlNotExists.addSQLJoin("STORNO.PROGRESSIVO_RIGA_STORNO", "DOCUMENTO_GENERICO_RIGA.PROGRESSIVO_RIGA");

        sqlNotExists.addSQLJoin("TESTATA_STORNO.CD_CDS", "STORNO.CD_CDS");
        sqlNotExists.addSQLJoin("TESTATA_STORNO.CD_UNITA_ORGANIZZATIVA", "STORNO.CD_UNITA_ORGANIZZATIVA");
        sqlNotExists.addSQLJoin("TESTATA_STORNO.ESERCIZIO", "STORNO.ESERCIZIO");
        sqlNotExists.addSQLJoin("TESTATA_STORNO.CD_TIPO_DOCUMENTO_AMM", "STORNO.CD_TIPO_DOCUMENTO_AMM");
        sqlNotExists.addSQLJoin("TESTATA_STORNO.PG_DOCUMENTO_GENERICO", "STORNO.PG_DOCUMENTO_GENERICO");
        sqlNotExists.addSQLClause(FindClause.AND, "TESTATA_STORNO.STATO_COFI", SQLBuilder.NOT_EQUALS, Documento_genericoBulk.STATO_ANNULLATO);
        sqlBuilder.addSQLNotExistsClause(FindClause.AND, sqlNotExists);
        return sqlBuilder;
    }

    public Optional<Documento_generico_rigaBulk> findRigaStorno(UserContext userContext, Fattura_attiva_rigaIBulk fatturaAttivaRigaIBulk) throws ComponentException {
        SQLBuilder sqlBuilder = createSQLBuilder();
        sqlBuilder.addClause(FindClause.AND, "fattura_attiva_riga_storno", SQLBuilder.EQUALS, fatturaAttivaRigaIBulk);
        sqlBuilder.addClause(FindClause.AND, "stato_cofi", SQLBuilder.NOT_EQUALS, Documento_generico_rigaBulk.STATO_ANNULLATO);
        try {
            List<Documento_generico_rigaBulk> result = fetchAll(sqlBuilder);
            return result.stream().findAny();
        } catch (PersistencyException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Documento_generico_rigaBulk> findRigaStorno(UserContext userContext, Fattura_passiva_rigaIBulk fatturaPassivaRigaIBulk) throws ComponentException {
        SQLBuilder sqlBuilder = createSQLBuilder();
        sqlBuilder.addClause(FindClause.AND, "fattura_passiva_riga_storno", SQLBuilder.EQUALS, fatturaPassivaRigaIBulk);
        sqlBuilder.addClause(FindClause.AND, "stato_cofi", SQLBuilder.NOT_EQUALS, Documento_generico_rigaBulk.STATO_ANNULLATO);
        try {
            List<Documento_generico_rigaBulk> result = fetchAll(sqlBuilder);
            return result.stream().findAny();
        } catch (PersistencyException e) {
            throw new RuntimeException(e);
        }
    }

    public java.util.List<Documento_generico_riga_ecoBulk> findDocumentoGenericoRigheEcoList(Documento_generico_rigaBulk docRiga ) throws PersistencyException {
        PersistentHome home = getHomeCache().getHome(Documento_generico_riga_ecoBulk.class);
        it.cnr.jada.persistency.sql.SQLBuilder sql = home.createSQLBuilder();
        sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, docRiga.getEsercizio());
        sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, docRiga.getCd_cds());
        sql.addClause(FindClause.AND, "cd_unita_organizzativa", SQLBuilder.EQUALS, docRiga.getCd_unita_organizzativa());
        sql.addClause(FindClause.AND, "cd_tipo_documento_amm", SQLBuilder.EQUALS, docRiga.getCd_tipo_documento_amm());
        sql.addClause(FindClause.AND, "pg_documento_generico", SQLBuilder.EQUALS, docRiga.getPg_documento_generico());
        sql.addClause(FindClause.AND, "progressivo_riga", SQLBuilder.EQUALS, docRiga.getProgressivo_riga());
        return home.fetchAll(sql);
    }

    private ContoBulk getContoEconomicoDefault(UserContext userContext, Documento_generico_rigaBulk docRiga) throws ComponentException {
        try {
            Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);
            if (Optional.ofNullable(docRiga).isPresent()) {
                Documento_genericoBulk docgen = (Documento_genericoBulk) fatpasHome.loadIfNeededObject(docRiga.getDocumento_generico());
                if (Optional.ofNullable(docgen).map(Documento_genericoBulk::getTipoDocumentoEnum).map(TipoDocumentoEnum::isGenericoEntrata).orElse(Boolean.FALSE)) {
                    if (Optional.ofNullable(docgen.getCausaleContabile()).flatMap(el->Optional.ofNullable(el.getCdCausale()))
                            .isPresent()) {
                        CausaleContabileHome causaleHome = (CausaleContabileHome)getHomeCache().getHome(CausaleContabileBase.class);
                        List<AssCausaleVoceEPBulk> assCausale = causaleHome.findAssCausaleVoceEPBulk(userContext,docgen.getCausaleContabile().getCdCausale());
                        Voce_epBulk voceEpBulk = assCausale.stream().filter(AssCausaleVoceEPBulk::isSezioneAvere)
                                .map(AssCausaleVoceEPBulk::getVoceEp)
                                .findFirst()
                                .orElse(null);
                        if (voceEpBulk!=null && voceEpBulk.getCd_voce_ep()!=null) {
                            ContoHome contoHome = (ContoHome) getHomeCache().getHome(ContoBulk.class);
                            return (ContoBulk) contoHome.findByPrimaryKey(new ContoBulk(voceEpBulk.getCd_voce_ep(), voceEpBulk.getEsercizio()));
                        }
                    }
                    if (Optional.ofNullable(docRiga.getAccertamento_scadenziario()).isPresent()) {
                        Accertamento_scadenzarioBulk accertScad = (Accertamento_scadenzarioBulk) fatpasHome.loadIfNeededObject(docRiga.getAccertamento_scadenziario());

                        if (Optional.ofNullable(accertScad).isPresent()) {
                            AccertamentoBulk accert = (AccertamentoBulk) fatpasHome.loadIfNeededObject(accertScad.getAccertamento());
                            Ass_ev_voceepHome assEvVoceEpHome = (Ass_ev_voceepHome) getHomeCache().getHome(Ass_ev_voceepBulk.class);
                            //Metto docRiga.getEsercizio() e non accert.getEsercizio() perchè quest'ultimo cambia se anno ribaltato
                            List<Ass_ev_voceepBulk> listAss = assEvVoceEpHome.findVociEpAssociateVoce(new Elemento_voceBulk(accert.getCd_elemento_voce(), docRiga.getEsercizio(), accert.getTi_appartenenza(), accert.getTi_gestione()));
                            //Se lista è vuota cerco associazione nell'anno dell'accertamento
                            if (Optional.ofNullable(listAss).orElse(new ArrayList<>()).isEmpty() && docRiga.getEsercizio().compareTo(accert.getEsercizio())!=0)
                                listAss = assEvVoceEpHome.findVociEpAssociateVoce(new Elemento_voceBulk(accert.getCd_elemento_voce(), accert.getEsercizio(), accert.getTi_appartenenza(), accert.getTi_gestione()));
                            return Optional.ofNullable(listAss).orElse(new ArrayList<>())
                                   .stream().map(Ass_ev_voceepBulk::getVoce_ep)
                                   .findAny().orElse(null);
                        }
                    }
                } else { //PASSIVO
                    if (Optional.ofNullable(docgen.getCausaleContabile()).flatMap(el->Optional.ofNullable(el.getCdCausale()))
                            .isPresent()) {
                        CausaleContabileHome causaleHome = (CausaleContabileHome)getHomeCache().getHome(CausaleContabileBase.class);
                        List<AssCausaleVoceEPBulk> assCausale = causaleHome.findAssCausaleVoceEPBulk(userContext,docgen.getCausaleContabile().getCdCausale());
                        Voce_epBulk voceEpBulk = assCausale.stream().filter(AssCausaleVoceEPBulk::isSezioneDare)
                                .map(AssCausaleVoceEPBulk::getVoceEp)
                                .findFirst()
                                .orElse(null);
                        if (voceEpBulk!=null && voceEpBulk.getCd_voce_ep()!=null) {
                            ContoHome contoHome = (ContoHome) getHomeCache().getHome(ContoBulk.class);
                            return (ContoBulk) contoHome.findByPrimaryKey(new ContoBulk(voceEpBulk.getCd_voce_ep(), voceEpBulk.getEsercizio()));
                        }
                    }
                    if (Optional.ofNullable(docRiga.getObbligazione_scadenziario()).isPresent()) {
                        Obbligazione_scadenzarioBulk obbligScad = (Obbligazione_scadenzarioBulk) fatpasHome.loadIfNeededObject(docRiga.getObbligazione_scadenziario());

                        if (Optional.ofNullable(obbligScad).isPresent()) {
                            ObbligazioneBulk obblig = (ObbligazioneBulk) fatpasHome.loadIfNeededObject(obbligScad.getObbligazione());
                            Ass_ev_voceepHome assEvVoceEpHome = (Ass_ev_voceepHome) getHomeCache().getHome(Ass_ev_voceepBulk.class);
                            //Metto docRiga.getEsercizio() e non obblig.getEsercizio() perchè quest'ultimo cambia se anno ribaltato
                            List<Ass_ev_voceepBulk> listAss = assEvVoceEpHome.findVociEpAssociateVoce(new Elemento_voceBulk(obblig.getCd_elemento_voce(), docRiga.getEsercizio(), obblig.getTi_appartenenza(), obblig.getTi_gestione()));
                            //Se lista è vuota cerco associazione nell'anno dell'obbligazione
                            if (Optional.ofNullable(listAss).orElse(new ArrayList<>()).isEmpty() && docRiga.getEsercizio().compareTo(obblig.getEsercizio())!=0) {
                                listAss = assEvVoceEpHome.findVociEpAssociateVoce(new Elemento_voceBulk(obblig.getCd_elemento_voce(), obblig.getEsercizio(), obblig.getTi_appartenenza(), obblig.getTi_gestione()));
                                return Optional.ofNullable(listAss).orElse(new ArrayList<>())
                                        .stream().map(Ass_ev_voceepBulk::getVoce_ep)
                                        .findAny().orElseThrow(() -> new ApplicationRuntimeException("Non risultano associati conti economici alla voce di bilancio " + obblig.getTi_gestione() + "/" + obblig.getCd_elemento_voce() +
                                                " sia nell'esercizio del documento (" + docRiga.getEsercizio() + ") che in quello dell'obbligazione (" + obblig.getEsercizio() + ")!"));
                            }
                            return Optional.ofNullable(listAss).orElse(new ArrayList<>())
                                    .stream().map(Ass_ev_voceepBulk::getVoce_ep)
                                    .findAny().orElseThrow(() -> new ApplicationRuntimeException("Non risultano associati conti economici alla voce di bilancio " + obblig.getEsercizio()+"/"+obblig.getTi_gestione() + "/" + obblig.getCd_elemento_voce()+"!"));
                        }
                    } else {
                        Configurazione_cnrHome configHome = (Configurazione_cnrHome) getHomeCache().getHome(Configurazione_cnrBulk.class);
                        return configHome.getContoDocumentoNonLiquidabile(docRiga);
                    }
                }
            }
            return null;
        } catch (PersistencyException e) {
            throw new DetailedRuntimeException(e);
        }
    }

    private List<IDocumentoDetailAnaCogeBulk> getDatiAnaliticiDefault(UserContext userContext, Documento_generico_rigaBulk docRiga, ContoBulk aContoEconomico) throws ComponentException {
        try {
            List<Documento_generico_riga_ecoBulk> result = new ArrayList<>();

            if (Optional.ofNullable(aContoEconomico).isPresent()) {
                List<Voce_analiticaBulk> voceAnaliticaList = ((Voce_analiticaHome) getHomeCache().getHome(Voce_analiticaBulk.class)).findVoceAnaliticaList(aContoEconomico);
                if (voceAnaliticaList.isEmpty())
                    return new ArrayList<>();
                Voce_analiticaBulk voceAnaliticaDef = voceAnaliticaList.stream()
                        .filter(Voce_analiticaBulk::getFl_default).findAny()
                        .orElse(voceAnaliticaList.stream().findAny().orElse(null));

                Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);
                Documento_genericoBulk docgen = (Documento_genericoBulk) fatpasHome.loadIfNeededObject(docRiga.getDocumento_generico());
                if (Optional.ofNullable(docgen).map(Documento_genericoBulk::getTipoDocumentoEnum).map(TipoDocumentoEnum::isGenericoEntrata).orElse(Boolean.FALSE)) {
                    if (Optional.ofNullable(docRiga.getAccertamento_scadenziario()).isPresent()) {
                        Accertamento_scadenzarioBulk accertScad = (Accertamento_scadenzarioBulk) fatpasHome.loadIfNeededObject(docRiga.getAccertamento_scadenziario());

                        if (Optional.ofNullable(accertScad).isPresent()) {
                            //carico i dettagli analitici recuperandoli dall'accertamento_scad_voce
                            Accertamento_scadenzarioHome accertamentoScadenzarioHome = (Accertamento_scadenzarioHome) getHomeCache().getHome(Accertamento_scadenzarioBulk.class);
                            List<Accertamento_scad_voceBulk> scadVoceBulks = accertamentoScadenzarioHome.findAccertamento_scad_voceList(userContext, accertScad, Boolean.FALSE);
                            BigDecimal totScad = scadVoceBulks.stream().map(Accertamento_scad_voceBulk::getIm_voce).reduce(BigDecimal.ZERO, BigDecimal::add);
                            for (Accertamento_scad_voceBulk scadVoce : scadVoceBulks) {
                                Documento_generico_riga_ecoBulk myRigaEco = new Documento_generico_riga_ecoBulk();
                                myRigaEco.setProgressivo_riga_eco((long) result.size() + 1);
                                myRigaEco.setVoce_analitica(voceAnaliticaDef);
                                myRigaEco.setLinea_attivita(scadVoce.getLinea_attivita());
                                myRigaEco.setDocumento_generico_rigaBulk(docRiga);
                                if (totScad.compareTo(BigDecimal.ZERO)!=0)
                                    myRigaEco.setImporto(scadVoce.getIm_voce().multiply(docRiga.getImCostoEco()).divide(totScad, 2, RoundingMode.HALF_UP));
                                else
                                    myRigaEco.setImporto(docRiga.getImCostoEco().divide(BigDecimal.valueOf(scadVoceBulks.size()), 2, RoundingMode.HALF_UP));
                                myRigaEco.setToBeCreated();
                                result.add(myRigaEco);
                            }

                            BigDecimal totRipartito = result.stream().map(Documento_generico_riga_ecoBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal diff = totRipartito.subtract(docRiga.getImCostoEco());

                            if (diff.compareTo(BigDecimal.ZERO)>0) {
                                for (Documento_generico_riga_ecoBulk rigaEco : result) {
                                    if (rigaEco.getImporto().compareTo(diff) >= 0) {
                                        rigaEco.setImporto(rigaEco.getImporto().subtract(diff));
                                        break;
                                    } else {
                                        diff = diff.subtract(rigaEco.getImporto());
                                        rigaEco.setImporto(BigDecimal.ZERO);
                                    }
                                }
                            } else if (diff.compareTo(BigDecimal.ZERO)<0) {
                                for (Documento_generico_riga_ecoBulk rigaEco : result) {
                                    rigaEco.setImporto(rigaEco.getImporto().add(diff));
                                    break;
                                }
                            }
                        }
                    }
                } else { //GENERICO PASSIVO
                    if (Optional.ofNullable(docRiga.getObbligazione_scadenziario()).isPresent()) {
                        Obbligazione_scadenzarioBulk obbligScad = (Obbligazione_scadenzarioBulk) fatpasHome.loadIfNeededObject(docRiga.getObbligazione_scadenziario());

                        if (Optional.ofNullable(obbligScad).isPresent()) {
                            //carico i dettagli analitici recuperandoli dall'obbligazione_scad_voce
                            Obbligazione_scadenzarioHome obbligazioneScadenzarioHome = (Obbligazione_scadenzarioHome) getHomeCache().getHome(Obbligazione_scadenzarioBulk.class);
                            List<Obbligazione_scad_voceBulk> scadVoceBulks = obbligazioneScadenzarioHome.findObbligazione_scad_voceList(userContext, obbligScad, Boolean.FALSE);
                            BigDecimal totScad = scadVoceBulks.stream().map(Obbligazione_scad_voceBulk::getIm_voce).reduce(BigDecimal.ZERO, BigDecimal::add);
                            for (Obbligazione_scad_voceBulk scadVoce : scadVoceBulks) {
                                Documento_generico_riga_ecoBulk myRigaEco = new Documento_generico_riga_ecoBulk();
                                myRigaEco.setProgressivo_riga_eco((long) result.size() + 1);
                                myRigaEco.setVoce_analitica(voceAnaliticaDef);
                                myRigaEco.setLinea_attivita(scadVoce.getLinea_attivita());
                                myRigaEco.setDocumento_generico_rigaBulk(docRiga);
                                if (totScad.compareTo(BigDecimal.ZERO)!=0)
                                    myRigaEco.setImporto(scadVoce.getIm_voce().multiply(docRiga.getImCostoEco()).divide(totScad, 2, RoundingMode.HALF_UP));
                                else
                                    myRigaEco.setImporto(docRiga.getImCostoEco().divide(BigDecimal.valueOf(scadVoceBulks.size()), 2, RoundingMode.HALF_UP));
                                myRigaEco.setToBeCreated();
                                result.add(myRigaEco);
                            }
                            BigDecimal totRipartito = result.stream().map(Documento_generico_riga_ecoBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal diff = totRipartito.subtract(docRiga.getImCostoEco());

                            if (diff.compareTo(BigDecimal.ZERO)>0) {
                                for (Documento_generico_riga_ecoBulk rigaEco : result) {
                                    if (rigaEco.getImporto().compareTo(diff) >= 0) {
                                        rigaEco.setImporto(rigaEco.getImporto().subtract(diff));
                                        break;
                                    } else {
                                        diff = diff.subtract(rigaEco.getImporto());
                                        rigaEco.setImporto(BigDecimal.ZERO);
                                    }
                                }
                            } else if (diff.compareTo(BigDecimal.ZERO)<0) {
                                for (Documento_generico_riga_ecoBulk rigaEco : result) {
                                    rigaEco.setImporto(rigaEco.getImporto().add(diff));
                                    break;
                                }
                            }
                        } else {
                            Configurazione_cnrHome configHome = (Configurazione_cnrHome) getHomeCache().getHome(Configurazione_cnrBulk.class);
                            WorkpackageBulk gaeDefault = configHome.getGaeDocumentoNonLiquidabile(userContext, docRiga.getFather());

                            Documento_generico_riga_ecoBulk myRigaEco = new Documento_generico_riga_ecoBulk();
                            myRigaEco.setProgressivo_riga_eco(1L);
                            myRigaEco.setVoce_analitica(voceAnaliticaDef);
                            myRigaEco.setLinea_attivita(gaeDefault);
                            myRigaEco.setDocumento_generico_rigaBulk(docRiga);
                            myRigaEco.setImporto(docRiga.getImCostoEco());
                            myRigaEco.setToBeCreated();
                            result.add(myRigaEco);
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

    public Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> getDatiEconomiciDefault(UserContext userContext, Documento_generico_rigaBulk docRiga) throws ComponentException, PersistencyException {
        Fattura_passivaHome fatpasHome = (Fattura_passivaHome)getHomeCache().getHome(Fattura_passivaBulk.class);
        if (docRiga.isDocumentoStorno() && docRiga.getRigaStorno().isPresent()) {
            Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> datiEcoDoc = null;
            if (docRiga.getDocumento_generico_riga_storno()!=null) {
                Documento_generico_rigaBulk rigaCollegata = (Documento_generico_rigaBulk) fatpasHome.loadIfNeededObject(docRiga.getDocumento_generico_riga_storno());
                if (Optional.ofNullable(rigaCollegata.getVoce_ep()).flatMap(el->Optional.ofNullable(el.getCd_voce_ep())).isEmpty())
                    datiEcoDoc = this.getDatiEconomiciDefault(userContext, rigaCollegata);
                else
                    datiEcoDoc = this.getDatiEconomici(rigaCollegata);
            } else if (docRiga.getFattura_attiva_riga_storno()!=null) {
                Fattura_attiva_rigaIHome fatattrigaHome = (Fattura_attiva_rigaIHome) getHomeCache().getHome(Fattura_attiva_rigaIBulk.class);
                Fattura_attiva_rigaIBulk rigaCollegata = (Fattura_attiva_rigaIBulk) fatpasHome.loadIfNeededObject(docRiga.getFattura_attiva_riga_storno());
                if (Optional.ofNullable(rigaCollegata.getVoce_ep()).flatMap(el->Optional.ofNullable(el.getCd_voce_ep())).isEmpty())
                    datiEcoDoc = fatattrigaHome.getDatiEconomiciDefault(userContext, rigaCollegata);
                else
                    datiEcoDoc = fatattrigaHome.getDatiEconomici(rigaCollegata);
            } else if (docRiga.getFattura_passiva_riga_storno()!=null) {
                Fattura_passiva_rigaIHome fatpasrigaHome = (Fattura_passiva_rigaIHome) getHomeCache().getHome(Fattura_passiva_rigaIBulk.class);
                Fattura_passiva_rigaIBulk rigaCollegata = (Fattura_passiva_rigaIBulk) fatpasHome.loadIfNeededObject(docRiga.getFattura_passiva_riga_storno());
                rigaCollegata.setFattura_passivaI((Fattura_passiva_IBulk)fatpasHome.loadIfNeededObject(rigaCollegata.getFattura_passivaI()));
                if (Optional.ofNullable(rigaCollegata.getVoce_ep()).flatMap(el->Optional.ofNullable(el.getCd_voce_ep())).isEmpty())
                    datiEcoDoc = fatpasrigaHome.getDatiEconomiciDefault(userContext, rigaCollegata);
                else
                    datiEcoDoc = fatpasrigaHome.getDatiEconomici(rigaCollegata);
            }
            if (datiEcoDoc!=null) {
                BigDecimal totaleImportiAnalitici = datiEcoDoc.getSecond().stream().map(IDocumentoDetailAnaCogeBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);

                ContoBulk aContoEconomico = datiEcoDoc.getFirst();
                List<Documento_generico_riga_ecoBulk> aContiAnalitici = new ArrayList<>();

                for (IDocumentoDetailAnaCogeBulk rigaEco : datiEcoDoc.getSecond()) {
                    Documento_generico_riga_ecoBulk myRigaEco = new Documento_generico_riga_ecoBulk();
                    myRigaEco.setProgressivo_riga_eco((long) aContiAnalitici.size() + 1);
                    myRigaEco.setVoce_analitica(rigaEco.getVoce_analitica());
                    myRigaEco.setLinea_attivita(rigaEco.getLinea_attivita());
                    myRigaEco.setDocumento_generico_rigaBulk(docRiga);
                    myRigaEco.setImporto(rigaEco.getImporto().multiply(docRiga.getImCostoEco()).divide(totaleImportiAnalitici, 2, RoundingMode.HALF_UP));
                    myRigaEco.setToBeCreated();
                    aContiAnalitici.add(myRigaEco);
                }

                BigDecimal totRipartito = aContiAnalitici.stream().map(IDocumentoDetailAnaCogeBulk::getImporto).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal diff = totRipartito.subtract(docRiga.getImCostoEco());

                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    for (Documento_generico_riga_ecoBulk rigaEco : aContiAnalitici) {
                        if (rigaEco.getImporto().compareTo(diff) >= 0) {
                            rigaEco.setImporto(rigaEco.getImporto().subtract(diff));
                            break;
                        } else {
                            diff = diff.subtract(rigaEco.getImporto());
                            rigaEco.setImporto(BigDecimal.ZERO);
                        }
                    }
                } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                    for (Documento_generico_riga_ecoBulk rigaEco : aContiAnalitici) {
                        rigaEco.setImporto(rigaEco.getImporto().add(diff));
                        break;
                    }
                }
                return Pair.of(aContoEconomico,
                        aContiAnalitici.stream().filter(el -> el.getImporto().compareTo(BigDecimal.ZERO) != 0)
                                .collect(Collectors.toList()));
            }
            return null;
        } else {
            ContoBulk aContoEconomico = this.getContoEconomicoDefault(userContext, docRiga);
            List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.getDatiAnaliticiDefault(userContext, docRiga, aContoEconomico);
            return Pair.of(aContoEconomico, aContiAnalitici);
        }
    }

    public Pair<ContoBulk,List<IDocumentoDetailAnaCogeBulk>> getDatiEconomici(Documento_generico_rigaBulk docRiga) throws PersistencyException {
        ContoBulk aContoEconomico = docRiga.getVoce_ep();
        List<IDocumentoDetailAnaCogeBulk> aContiAnalitici = this.findDocumentoGenericoRigheEcoList(docRiga).stream()
                .map(IDocumentoDetailAnaCogeBulk.class::cast)
                .collect(Collectors.toList());
        return Pair.of(aContoEconomico, aContiAnalitici);
    }
}