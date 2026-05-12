package it.cnr.contab.doccont00.comp;

import it.cnr.contab.config00.bulk.Codici_siopeBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrBase;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrHome;
import it.cnr.contab.config00.latt.bulk.CostantiTi_gestione;
import it.cnr.contab.config00.pdcfin.bulk.Ass_ev_siopeBulk;
import it.cnr.contab.config00.pdcfin.bulk.Ass_ev_siopeHome;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.config00.pdcfin.bulk.V_voce_f_partita_giroBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativa_enteBulk;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.contab.doccont00.ejb.MandatoComponentSession;
import it.cnr.contab.doccont00.ejb.ReversaleComponentSession;
import it.cnr.contab.doccont00.tabrif.bulk.CambiaVocePGiroBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.contab.util.enumeration.EsitoOperazione;
import it.cnr.contab.util.enumeration.StatoVariazioneSostituzione;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CambiaVocePGiroComponent extends CRUDComponent {
    public CambiaVocePGiroComponent() {
    }

    public SQLBuilder selectVoceInizialeByClause(
            UserContext userContext,
            CambiaVocePGiroBulk cambiaVocePGiroBulk,
            V_voce_f_partita_giroBulk vVoceFPartitaGiroBulk,
            CompoundFindClause compoundFindClause) throws ComponentException, PersistencyException {
        final Optional<String> cdElementoVoce = Optional.ofNullable(cambiaVocePGiroBulk)
                .flatMap(cambiaVocePGiroBulk1 -> Optional.ofNullable(cambiaVocePGiroBulk1.getVoceFinale()))
                .flatMap(vVoceFPartitaGiroBulk1 -> Optional.ofNullable(vVoceFPartitaGiroBulk1.getCd_elemento_voce()));
        if (cdElementoVoce.isPresent()) {
            compoundFindClause = Optional.ofNullable(compoundFindClause).orElse(new CompoundFindClause());
            compoundFindClause.addClause(FindClause.AND, "cd_elemento_voce", SQLBuilder.NOT_EQUALS, cdElementoVoce.get());
        }
        return basicSelectVoceByClause(userContext,cambiaVocePGiroBulk, vVoceFPartitaGiroBulk, compoundFindClause);
    }
    public SQLBuilder selectVoceFinaleByClause(
            UserContext userContext,
            CambiaVocePGiroBulk cambiaVocePGiroBulk,
            V_voce_f_partita_giroBulk vVoceFPartitaGiroBulk,
            CompoundFindClause compoundFindClause) throws ComponentException, PersistencyException {
        final Optional<String> cdElementoVoce = Optional.ofNullable(cambiaVocePGiroBulk)
                .flatMap(cambiaVocePGiroBulk1 -> Optional.ofNullable(cambiaVocePGiroBulk1.getVoceIniziale()))
                .flatMap(vVoceFPartitaGiroBulk1 -> Optional.ofNullable(vVoceFPartitaGiroBulk1.getCd_elemento_voce()));
        if (cdElementoVoce.isPresent()) {
            compoundFindClause = Optional.ofNullable(compoundFindClause).orElse(new CompoundFindClause());
            compoundFindClause.addClause(FindClause.AND, "cd_elemento_voce", SQLBuilder.NOT_EQUALS, cdElementoVoce.get());
        }
        return basicSelectVoceByClause(userContext,cambiaVocePGiroBulk, vVoceFPartitaGiroBulk, compoundFindClause);
    }
    public SQLBuilder basicSelectVoceByClause(
            UserContext userContext,
            CambiaVocePGiroBulk cambiaVocePGiroBulk,
            V_voce_f_partita_giroBulk vVoceFPartitaGiroBulk,
            CompoundFindClause compoundFindClause) throws ComponentException, PersistencyException {
        final BulkHome home = getHome(userContext, V_voce_f_partita_giroBulk.class);
        final SQLBuilder sqlBuilder = home.createSQLBuilder();
        Optional.ofNullable(vVoceFPartitaGiroBulk)
                .ifPresent(elementoVoceBulk1 -> {
                    sqlBuilder.addClause(elementoVoceBulk1.buildFindClauses(true));
                });
        Optional.ofNullable(compoundFindClause)
                .ifPresent(compoundFindClause1 -> {
                    sqlBuilder.addClause(compoundFindClause1);
                });
        sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
        sqlBuilder.addClause(FindClause.AND, "fl_partita_giro", SQLBuilder.EQUALS, Boolean.TRUE);
        sqlBuilder.addClause(FindClause.AND, "ti_gestione", SQLBuilder.EQUALS, cambiaVocePGiroBulk.getTiGestione());
        return sqlBuilder;
    }

    public RemoteIterator cercaReversali(UserContext userContext, CambiaVocePGiroBulk cambiaVocePGiroBulk, V_voce_f_partita_giroBulk vVoceFPartitaGiroBulk, CompoundFindClause compoundFindClause) throws ComponentException {
        try {
            final BulkHome home = getHome(userContext, Reversale_rigaBulk.class);
            final SQLBuilder sqlBuilder = home.createSQLBuilder();
            Optional.ofNullable(compoundFindClause)
                    .ifPresent(compoundFindClause1 -> sqlBuilder.addClause(compoundFindClause1));
            sqlBuilder.addTableToHeader("ACCERTAMENTO_SCADENZARIO");
            sqlBuilder.addTableToHeader("ACCERTAMENTO");
            sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
            sqlBuilder.addClause(FindClause.AND, "stato", SQLBuilder.NOT_EQUALS, ReversaleBulk.STATO_REVERSALE_ANNULLATO);
            //	Se uo 999.000 in scrivania: visualizza tutto l'elenco
            Unita_organizzativa_enteBulk ente = (Unita_organizzativa_enteBulk) getHome(userContext, Unita_organizzativa_enteBulk.class).findAll().get(0);
            Unita_organizzativaBulk uoScrivania = (Unita_organizzativaBulk) getHome(userContext, Unita_organizzativaBulk.class)
                    .findByPrimaryKey(new Unita_organizzativaBulk(CNRUserContext.getCd_unita_organizzativa(userContext)));
            if (!(CNRUserContext.getCd_unita_organizzativa(userContext).equals(ente.getCd_unita_organizzativa()))) {
                if (uoScrivania.isUoCds())
                    sqlBuilder.addSQLClause(FindClause.AND, "REVERSALE_RIGA.CD_CDS", SQLBuilder.EQUALS, CNRUserContext.getCd_cds(userContext));
                else
                    sqlBuilder.addSQLClause(FindClause.AND, "REVERSALE_RIGA.CD_UO_DOC_AMM", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(userContext));
            }

            sqlBuilder.addSQLJoin("REVERSALE_RIGA.CD_CDS", "ACCERTAMENTO_SCADENZARIO.CD_CDS");
            sqlBuilder.addSQLJoin("REVERSALE_RIGA.ESERCIZIO_ACCERTAMENTO", "ACCERTAMENTO_SCADENZARIO.ESERCIZIO");
            sqlBuilder.addSQLJoin("REVERSALE_RIGA.ESERCIZIO_ORI_ACCERTAMENTO", "ACCERTAMENTO_SCADENZARIO.ESERCIZIO_ORIGINALE");
            sqlBuilder.addSQLJoin("REVERSALE_RIGA.PG_ACCERTAMENTO", "ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO");
            sqlBuilder.addSQLJoin("REVERSALE_RIGA.PG_ACCERTAMENTO_SCADENZARIO", "ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO_SCADENZARIO");

            sqlBuilder.addSQLJoin("ACCERTAMENTO_SCADENZARIO.CD_CDS", "ACCERTAMENTO.CD_CDS");
            sqlBuilder.addSQLJoin("ACCERTAMENTO_SCADENZARIO.ESERCIZIO", "ACCERTAMENTO.ESERCIZIO");
            sqlBuilder.addSQLJoin("ACCERTAMENTO_SCADENZARIO.ESERCIZIO_ORIGINALE", "ACCERTAMENTO.ESERCIZIO_ORIGINALE");
            sqlBuilder.addSQLJoin("ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO", "ACCERTAMENTO.PG_ACCERTAMENTO");

            sqlBuilder.addSQLClause(FindClause.AND, "cd_elemento_voce", SQLBuilder.EQUALS, vVoceFPartitaGiroBulk.getCd_elemento_voce());
            cambiaVocePGiroBulk
                    .getDettagliEntrata()
                    .stream()
                    .forEach(reversaleRigaBulk -> {
                        final SQLBuilder sqlBuilder1 = home.createSQLBuilder();
                        sqlBuilder1.resetColumns();
                        sqlBuilder1.addColumn("CD_CDS");
                        sqlBuilder1.addColumn("ESERCIZIO");
                        sqlBuilder1.addColumn("PG_REVERSALE");
                        sqlBuilder1.addColumn("ESERCIZIO_ACCERTAMENTO");
                        sqlBuilder1.addColumn("ESERCIZIO_ORI_ACCERTAMENTO");
                        sqlBuilder1.addColumn("PG_ACCERTAMENTO");
                        sqlBuilder1.addColumn("PG_ACCERTAMENTO_SCADENZARIO");
                        sqlBuilder1.addColumn("CD_CDS_DOC_AMM");
                        sqlBuilder1.addColumn("CD_UO_DOC_AMM");
                        sqlBuilder1.addColumn("ESERCIZIO_DOC_AMM");
                        sqlBuilder1.addColumn("CD_TIPO_DOCUMENTO_AMM");
                        sqlBuilder1.addColumn("PG_DOC_AMM");
                        sqlBuilder1.addSQLClause(FindClause.AND, "CD_CDS", SQLBuilder.EQUALS, reversaleRigaBulk.getCd_cds());
                        sqlBuilder1.addSQLClause(FindClause.AND, "ESERCIZIO", SQLBuilder.EQUALS, reversaleRigaBulk.getEsercizio());
                        sqlBuilder1.addSQLClause(FindClause.AND, "PG_REVERSALE", SQLBuilder.EQUALS, reversaleRigaBulk.getPg_reversale());
                        sqlBuilder1.addSQLClause(FindClause.AND, "ESERCIZIO_ACCERTAMENTO", SQLBuilder.EQUALS, reversaleRigaBulk.getEsercizio_accertamento());
                        sqlBuilder1.addSQLClause(FindClause.AND, "ESERCIZIO_ORI_ACCERTAMENTO", SQLBuilder.EQUALS, reversaleRigaBulk.getEsercizio_ori_accertamento());
                        sqlBuilder1.addSQLClause(FindClause.AND, "PG_ACCERTAMENTO", SQLBuilder.EQUALS, reversaleRigaBulk.getPg_accertamento());
                        sqlBuilder1.addSQLClause(FindClause.AND, "PG_ACCERTAMENTO_SCADENZARIO", SQLBuilder.EQUALS, reversaleRigaBulk.getPg_accertamento_scadenzario());
                        sqlBuilder1.addSQLClause(FindClause.AND, "CD_CDS_DOC_AMM", SQLBuilder.EQUALS, reversaleRigaBulk.getCd_cds_doc_amm());
                        sqlBuilder1.addSQLClause(FindClause.AND, "CD_UO_DOC_AMM", SQLBuilder.EQUALS, reversaleRigaBulk.getCd_uo_doc_amm());
                        sqlBuilder1.addSQLClause(FindClause.AND, "ESERCIZIO_DOC_AMM", SQLBuilder.EQUALS, reversaleRigaBulk.getEsercizio_doc_amm());
                        sqlBuilder1.addSQLClause(FindClause.AND, "CD_TIPO_DOCUMENTO_AMM", SQLBuilder.EQUALS, reversaleRigaBulk.getCd_tipo_documento_amm());
                        sqlBuilder1.addSQLClause(FindClause.AND, "PG_DOC_AMM", SQLBuilder.EQUALS, reversaleRigaBulk.getPg_doc_amm());
                        sqlBuilder.addSQLNOTINClause(FindClause.AND, "(" +
                                "REVERSALE_RIGA.CD_CDS,REVERSALE_RIGA.ESERCIZIO,REVERSALE_RIGA.PG_REVERSALE,REVERSALE_RIGA.ESERCIZIO_ACCERTAMENTO," +
                                "REVERSALE_RIGA.ESERCIZIO_ORI_ACCERTAMENTO,REVERSALE_RIGA.PG_ACCERTAMENTO,REVERSALE_RIGA.PG_ACCERTAMENTO_SCADENZARIO," +
                                "REVERSALE_RIGA.CD_CDS_DOC_AMM,REVERSALE_RIGA.CD_UO_DOC_AMM,REVERSALE_RIGA.ESERCIZIO_DOC_AMM," +
                                "REVERSALE_RIGA.CD_TIPO_DOCUMENTO_AMM,REVERSALE_RIGA.PG_DOC_AMM)", sqlBuilder1);
                    });

            return iterator(userContext, sqlBuilder, Reversale_rigaBulk.class, null);
        } catch (PersistencyException _ex) {
            throw handleException(_ex);
        }
    }

    public RemoteIterator cercaMandati(UserContext userContext, CambiaVocePGiroBulk cambiaVocePGiroBulk, V_voce_f_partita_giroBulk vVoceFPartitaGiroBulk, CompoundFindClause compoundFindClause) throws ComponentException {
        try {
            final BulkHome home = getHome(userContext, Mandato_rigaIBulk.class);
            final SQLBuilder sqlBuilder = home.createSQLBuilder();
            Optional.ofNullable(compoundFindClause)
                .ifPresent(compoundFindClause1 -> sqlBuilder.addClause(compoundFindClause1));
            sqlBuilder.addTableToHeader("OBBLIGAZIONE_SCADENZARIO");
            sqlBuilder.addTableToHeader("OBBLIGAZIONE");
            sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
            sqlBuilder.addClause(FindClause.AND, "stato", SQLBuilder.NOT_EQUALS, MandatoBulk.STATO_MANDATO_ANNULLATO);
            //	Se uo 999.000 in scrivania: visualizza tutto l'elenco
            Unita_organizzativa_enteBulk ente = (Unita_organizzativa_enteBulk) getHome(userContext, Unita_organizzativa_enteBulk.class).findAll().get(0);
            Unita_organizzativaBulk uoScrivania = (Unita_organizzativaBulk) getHome(userContext, Unita_organizzativaBulk.class)
                    .findByPrimaryKey(new Unita_organizzativaBulk(CNRUserContext.getCd_unita_organizzativa(userContext)));
            if (!(CNRUserContext.getCd_unita_organizzativa(userContext).equals(ente.getCd_unita_organizzativa()))) {
                if (uoScrivania.isUoCds())
                    sqlBuilder.addSQLClause(FindClause.AND, "MANDATO_RIGA.CD_CDS", SQLBuilder.EQUALS, CNRUserContext.getCd_cds(userContext));
                else
                    sqlBuilder.addSQLClause(FindClause.AND, "MANDATO_RIGA.CD_UO_DOC_AMM", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(userContext));
            }

            sqlBuilder.addSQLJoin("MANDATO_RIGA.CD_CDS", "OBBLIGAZIONE_SCADENZARIO.CD_CDS");
            sqlBuilder.addSQLJoin("MANDATO_RIGA.ESERCIZIO_OBBLIGAZIONE", "OBBLIGAZIONE_SCADENZARIO.ESERCIZIO");
            sqlBuilder.addSQLJoin("MANDATO_RIGA.ESERCIZIO_ORI_OBBLIGAZIONE", "OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE");
            sqlBuilder.addSQLJoin("MANDATO_RIGA.PG_OBBLIGAZIONE", "OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE");
            sqlBuilder.addSQLJoin("MANDATO_RIGA.PG_OBBLIGAZIONE_SCADENZARIO", "OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO");

            sqlBuilder.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.CD_CDS", "OBBLIGAZIONE.CD_CDS");
            sqlBuilder.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.ESERCIZIO", "OBBLIGAZIONE.ESERCIZIO");
            sqlBuilder.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE", "OBBLIGAZIONE.ESERCIZIO_ORIGINALE");
            sqlBuilder.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE", "OBBLIGAZIONE.PG_OBBLIGAZIONE");

            sqlBuilder.addSQLClause(FindClause.AND, "cd_elemento_voce", SQLBuilder.EQUALS, vVoceFPartitaGiroBulk.getCd_elemento_voce());
            cambiaVocePGiroBulk
                .getDettagliSpesa()
                .stream()
                .forEach(mandatoRigaBulk -> {
                    final SQLBuilder sqlBuilder1 = home.createSQLBuilder();
                    sqlBuilder1.resetColumns();
                    sqlBuilder1.addColumn("CD_CDS");
                    sqlBuilder1.addColumn("ESERCIZIO");
                    sqlBuilder1.addColumn("PG_MANDATO");
                    sqlBuilder1.addColumn("ESERCIZIO_OBBLIGAZIONE");
                    sqlBuilder1.addColumn("ESERCIZIO_ORI_OBBLIGAZIONE");
                    sqlBuilder1.addColumn("PG_OBBLIGAZIONE");
                    sqlBuilder1.addColumn("PG_OBBLIGAZIONE_SCADENZARIO");
                    sqlBuilder1.addColumn("CD_CDS_DOC_AMM");
                    sqlBuilder1.addColumn("CD_UO_DOC_AMM");
                    sqlBuilder1.addColumn("ESERCIZIO_DOC_AMM");
                    sqlBuilder1.addColumn("CD_TIPO_DOCUMENTO_AMM");
                    sqlBuilder1.addColumn("PG_DOC_AMM");
                    sqlBuilder1.addSQLClause(FindClause.AND, "CD_CDS", SQLBuilder.EQUALS, mandatoRigaBulk.getCd_cds());
                    sqlBuilder1.addSQLClause(FindClause.AND, "ESERCIZIO", SQLBuilder.EQUALS, mandatoRigaBulk.getEsercizio());
                    sqlBuilder1.addSQLClause(FindClause.AND, "PG_MANDATO", SQLBuilder.EQUALS, mandatoRigaBulk.getPg_mandato());
                    sqlBuilder1.addSQLClause(FindClause.AND, "ESERCIZIO_OBBLIGAZIONE", SQLBuilder.EQUALS, mandatoRigaBulk.getEsercizio_obbligazione());
                    sqlBuilder1.addSQLClause(FindClause.AND, "ESERCIZIO_ORI_OBBLIGAZIONE", SQLBuilder.EQUALS, mandatoRigaBulk.getEsercizio_ori_obbligazione());
                    sqlBuilder1.addSQLClause(FindClause.AND, "PG_OBBLIGAZIONE", SQLBuilder.EQUALS, mandatoRigaBulk.getPg_obbligazione());
                    sqlBuilder1.addSQLClause(FindClause.AND, "PG_OBBLIGAZIONE_SCADENZARIO", SQLBuilder.EQUALS, mandatoRigaBulk.getPg_obbligazione_scadenzario());
                    sqlBuilder1.addSQLClause(FindClause.AND, "CD_CDS_DOC_AMM", SQLBuilder.EQUALS, mandatoRigaBulk.getCd_cds_doc_amm());
                    sqlBuilder1.addSQLClause(FindClause.AND, "CD_UO_DOC_AMM", SQLBuilder.EQUALS, mandatoRigaBulk.getCd_uo_doc_amm());
                    sqlBuilder1.addSQLClause(FindClause.AND, "ESERCIZIO_DOC_AMM", SQLBuilder.EQUALS, mandatoRigaBulk.getEsercizio_doc_amm());
                    sqlBuilder1.addSQLClause(FindClause.AND, "CD_TIPO_DOCUMENTO_AMM", SQLBuilder.EQUALS, mandatoRigaBulk.getCd_tipo_documento_amm());
                    sqlBuilder1.addSQLClause(FindClause.AND, "PG_DOC_AMM", SQLBuilder.EQUALS, mandatoRigaBulk.getPg_doc_amm());
                    sqlBuilder.addSQLNOTINClause(FindClause.AND, "(" +
                            "MANDATO_RIGA.CD_CDS,MANDATO_RIGA.ESERCIZIO,MANDATO_RIGA.PG_MANDATO,MANDATO_RIGA.ESERCIZIO_OBBLIGAZIONE," +
                            "MANDATO_RIGA.ESERCIZIO_ORI_OBBLIGAZIONE,MANDATO_RIGA.PG_OBBLIGAZIONE,MANDATO_RIGA.PG_OBBLIGAZIONE_SCADENZARIO," +
                            "MANDATO_RIGA.CD_CDS_DOC_AMM,MANDATO_RIGA.CD_UO_DOC_AMM,MANDATO_RIGA.ESERCIZIO_DOC_AMM," +
                            "MANDATO_RIGA.CD_TIPO_DOCUMENTO_AMM,MANDATO_RIGA.PG_DOC_AMM)", sqlBuilder1);
                });
            return iterator(userContext, sqlBuilder, Mandato_rigaIBulk.class, null);
        } catch (PersistencyException _ex) {
            throw handleException(_ex);
        }
    }

    public void elabora(UserContext userContext, CambiaVocePGiroBulk cambiaVocePGiroBulk) throws ComponentException{
        try {
            final ReversaleComponentSession reversaleComponentSession = (ReversaleComponentSession)
                    EJBCommonServices.createEJB("CNRDOCCONT00_EJB_ReversaleComponentSession");
            final MandatoComponentSession mandatoComponentSession = (MandatoComponentSession)
                    EJBCommonServices.createEJB("CNRDOCCONT00_EJB_MandatoComponentSession");
            final Integer numMaxVociBilancio = Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
                    .filter(Configurazione_cnrHome.class::isInstance)
                    .map(Configurazione_cnrHome.class::cast)
                    .map(home -> {
                        try {
                            return home.getConfigurazione(
                                    CNRUserContext.getEsercizio(userContext),
                                    null,
                                    Configurazione_cnrBulk.PK_FLUSSO_ORDINATIVI,
                                    Configurazione_cnrBulk.SK_INVIA_TAG_BILANCIO
                            );
                        } catch (PersistencyException e) {
                            throw new DetailedRuntimeException(e);
                        }
                    })
                    .filter(configurazioneCnrBulk -> Boolean.valueOf(configurazioneCnrBulk.getVal01()))
                    .map(Configurazione_cnrBase::getVal02)
                    .map(Integer::valueOf)
                    .orElse(0);
            if (cambiaVocePGiroBulk.getTiGestione().equalsIgnoreCase(CostantiTi_gestione.TI_GESTIONE_ENTRATE)) {
                final List<AccertamentoBulk> accertamentoBulks = cambiaVocePGiroBulk.getDettagliEntrata()
                        .stream()
                        .map(reversaleRigaBulk -> new AccertamentoBulk(
                                reversaleRigaBulk.getCd_cds(),
                                reversaleRigaBulk.getEsercizio_accertamento(),
                                reversaleRigaBulk.getEsercizio_ori_accertamento(),
                                reversaleRigaBulk.getPg_accertamento()
                        ))
                        .map(accertamentoBulk -> {
                            try {
                                return findByPrimaryKey(userContext, accertamentoBulk);
                            } catch (ComponentException e) {
                                throw new DetailedRuntimeException(e);
                            }
                        })
                        .map(AccertamentoBulk.class::cast)
                        .distinct()
                        .collect(Collectors.toList());
                final List<ReversaleBulk> reversaleBulks = cambiaVocePGiroBulk.getDettagliEntrata()
                        .stream()
                        .map(Reversale_rigaBulk::getReversale)
                        .distinct()
                        .collect(Collectors.toList());
                accertamentoBulks
                        .stream()
                        .forEach(accertamentoBulk -> {
                            try {
                                final AccertamentoBulk accertamentoBulk1 = (AccertamentoBulk)super.inizializzaBulkPerModifica(userContext, accertamentoBulk);
                                accertamentoBulk1.setToBeUpdated();
                                accertamentoBulk1.setCd_elemento_voce(cambiaVocePGiroBulk.getVoceFinale().getCd_elemento_voce());
                                accertamentoBulk1.setCd_voce(cambiaVocePGiroBulk.getVoceFinale().getCd_voce());
                                super.modificaConBulk(userContext, accertamentoBulk1);
                            } catch (ComponentException e) {
                                throw new DetailedRuntimeException(e);
                            }
                        });
                if (numMaxVociBilancio > 0) {
                    ReversaleHome reversaleHome = Optional.ofNullable(getHome(userContext, ReversaleIBulk.class))
                            .map(ReversaleHome.class::cast)
                            .orElseThrow(() -> new DetailedRuntimeException("Home reversale not found!"));
                    reversaleBulks
                            .stream()
                            .map(reversaleBulk -> {
                                try {
                                    return reversaleComponentSession.inizializzaBulkPerModifica(
                                            userContext,
                                            new ReversaleIBulk(reversaleBulk.getCd_cds(), reversaleBulk.getEsercizio(), reversaleBulk.getPg_reversale())
                                    );
                                } catch (ComponentException|RemoteException e) {
                                    throw new DetailedRuntimeException(e);
                                }
                            })
                            .map(ReversaleBulk.class::cast)
                            .forEach(reversaleBulk -> {
                                final Integer numVociBilancio = Optional.ofNullable(reversaleHome.getSiopeBilancio(userContext, reversaleBulk))
                                        .filter(siopeBilancioDTOS -> !siopeBilancioDTOS.isEmpty())
                                        .map(siopeBilancioDTOS -> siopeBilancioDTOS.size()).orElse(0);
                                if (numVociBilancio > numMaxVociBilancio) {
                                    throw new DetailedRuntimeException(
                                            new ApplicationMessageFormatException("Il numero delle voci di Bilancio {0}, è maggiore di quelle previste {1}", numVociBilancio, numMaxVociBilancio)
                                    );
                                }
                                try {
                                    if (Optional.ofNullable(reversaleBulk.getEsitoOperazione())
                                            .filter(s ->
                                                    s.equalsIgnoreCase(EsitoOperazione.ACQUISITO.value())||
                                                    s.equalsIgnoreCase(EsitoOperazione.RISCOSSO.value()) ||
                                                    s.equalsIgnoreCase(EsitoOperazione.REGOLARIZZATO.value())
                                            ).isPresent()) {
                                        Ass_ev_siopeHome assEvSiopeHome = (Ass_ev_siopeHome) getHome(userContext, Ass_ev_siopeBulk.class);

                                        Ass_mandato_reversaleHome assMandatoReversaleHome = (Ass_mandato_reversaleHome)getHome(userContext, Ass_mandato_reversaleBulk.class);
                                        final List<Ass_mandato_reversaleBulk> mandati = assMandatoReversaleHome.findMandati(userContext, reversaleBulk);
                                        reversaleBulk.setToBeUpdated();
                                        if (mandati.isEmpty()) {
                                            reversaleBulk.setStato_trasmissione(ReversaleBulk.STATO_TRASMISSIONE_NON_INSERITO);
                                        }
                                        reversaleBulk.setStatoVarSos(StatoVariazioneSostituzione.VARIAZIONE_DEFINITIVA.value());
                                        final Codici_siopeBulk codiciSiopeBulk = assEvSiopeHome.findByElementoVoce(
                                                userContext,
                                                new Elemento_voceBulk(
                                                        cambiaVocePGiroBulk.getVoceFinale().getCd_elemento_voce(),
                                                        cambiaVocePGiroBulk.getVoceFinale().getEsercizio(),
                                                        cambiaVocePGiroBulk.getVoceFinale().getTi_appartenenza(),
                                                        cambiaVocePGiroBulk.getVoceFinale().getTi_gestione()
                                                )
                                        );

                                        for (Reversale_rigaBulk reversaleRigaBulk : reversaleBulk.getReversale_rigaColl()){
                                            if (reversaleRigaBulk.getReversale_siopeColl().size() > 1) {
                                                throw new ApplicationException("Esistono più righe di dettaglio SIOPE, operazione non possibile!");
                                            }
                                            Reversale_siopeBulk reversaleSiopeBulk = reversaleRigaBulk.getReversale_siopeColl().get(0);
                                            if (!reversaleSiopeBulk.getCodice_siope().equalsByPrimaryKey(codiciSiopeBulk)) {
                                                Reversale_siopeBulk newReversaleSiopeBulk = new Reversale_siopeIBulk();
                                                newReversaleSiopeBulk.setToBeCreated();
                                                newReversaleSiopeBulk.setCodice_siope(codiciSiopeBulk);
                                                newReversaleSiopeBulk.setImporto(reversaleSiopeBulk.getImporto());
                                                reversaleSiopeBulk.setToBeDeleted();
                                                reversaleRigaBulk.removeFromReversale_siopeColl(0);
                                                reversaleRigaBulk.addToReversale_siopeColl(newReversaleSiopeBulk);
                                            }
                                        }
                                        reversaleComponentSession.modificaConBulk(userContext, reversaleBulk);

                                        for (Ass_mandato_reversaleBulk assMandatoReversaleBulk : mandati) {
                                            MandatoBulk mandatoBulk = new MandatoIBulk(
                                                    assMandatoReversaleBulk.getCd_cds_mandato(),
                                                    assMandatoReversaleBulk.getEsercizio_mandato(),
                                                    assMandatoReversaleBulk.getPg_mandato()
                                            );
                                            mandatoBulk = (MandatoBulk) mandatoComponentSession.inizializzaBulkPerModifica(userContext, mandatoBulk);
                                            mandatoBulk.setToBeUpdated();
                                            mandatoBulk.setStato_trasmissione(MandatoBulk.STATO_TRASMISSIONE_NON_INSERITO);
                                            mandatoBulk.setStatoVarSos(StatoVariazioneSostituzione.VARIAZIONE_DEFINITIVA.value());
                                            mandatoComponentSession.modificaConBulk(userContext, mandatoBulk);
                                        }
                                    }
                                } catch (ComponentException | RemoteException | PersistencyException e) {
                                    throw new DetailedRuntimeException(e);
                                }
                            });
                }
            } else {
                final List<ObbligazioneBulk> obbligazioneBulks = cambiaVocePGiroBulk.getDettagliSpesa()
                        .stream()
                        .map(mandatoRigaBulk -> new ObbligazioneBulk(
                                mandatoRigaBulk.getCd_cds(),
                                mandatoRigaBulk.getEsercizio_obbligazione(),
                                mandatoRigaBulk.getEsercizio_ori_obbligazione(),
                                mandatoRigaBulk.getPg_obbligazione()
                        ))
                        .map(obbligazioneBulk -> {
                            try {
                                return findByPrimaryKey(userContext, obbligazioneBulk);
                            } catch (ComponentException e) {
                                throw new DetailedRuntimeException(e);
                            }
                        })
                        .map(ObbligazioneBulk.class::cast)
                        .distinct()
                        .collect(Collectors.toList());
                final List<MandatoBulk> mandatoBulks = cambiaVocePGiroBulk.getDettagliSpesa()
                        .stream()
                        .map(Mandato_rigaBulk::getMandato)
                        .distinct()
                        .collect(Collectors.toList());
                obbligazioneBulks
                        .stream()
                        .forEach(obbligazioneBulk -> {
                            try {
                                final ObbligazioneBulk obbligazioneBulk1 = (ObbligazioneBulk)super.inizializzaBulkPerModifica(userContext, obbligazioneBulk);
                                obbligazioneBulk1.setToBeUpdated();
                                obbligazioneBulk1.setCd_elemento_voce(cambiaVocePGiroBulk.getVoceFinale().getCd_elemento_voce());
                                super.modificaConBulk(userContext, obbligazioneBulk1);
                            } catch (ComponentException e) {
                                throw new DetailedRuntimeException(e);
                            }
                        });
                if (numMaxVociBilancio > 0) {
                    MandatoHome mandatoHome = Optional.ofNullable(getHome(userContext, MandatoIBulk.class))
                            .map(MandatoHome.class::cast)
                            .orElseThrow(() -> new DetailedRuntimeException("Home mandato not found!"));
                    mandatoBulks
                            .stream()
                            .map(mandatoBulk -> {
                                try {
                                    return mandatoComponentSession.inizializzaBulkPerModifica(
                                            userContext,
                                            new MandatoIBulk(mandatoBulk.getCd_cds(), mandatoBulk.getEsercizio(), mandatoBulk.getPg_mandato())
                                    );
                                } catch (ComponentException|RemoteException e) {
                                    throw new DetailedRuntimeException(e);
                                }
                            })
                            .map(MandatoBulk.class::cast)
                            .forEach(mandatoBulk -> {
                                final Integer numVociBilancio = Optional.ofNullable(mandatoHome.getSiopeBilancio(userContext, mandatoBulk))
                                        .filter(siopeBilancioDTOS -> !siopeBilancioDTOS.isEmpty())
                                        .map(siopeBilancioDTOS -> siopeBilancioDTOS.size()).orElse(0);
                                if (numVociBilancio > numMaxVociBilancio) {
                                    throw new DetailedRuntimeException(
                                            new ApplicationMessageFormatException("Il numero delle voci di Bilancio {0}, è maggiore di quelle previste {1}", numVociBilancio, numMaxVociBilancio)
                                    );
                                }
                                try {
                                    if (Optional.ofNullable(mandatoBulk.getEsitoOperazione())
                                            .filter(s ->
                                                    s.equalsIgnoreCase(EsitoOperazione.ACQUISITO.value())||
                                                            s.equalsIgnoreCase(EsitoOperazione.PAGATO.value()) ||
                                                            s.equalsIgnoreCase(EsitoOperazione.REGOLARIZZATO.value())
                                            ).isPresent()) {
                                        Ass_ev_siopeHome assEvSiopeHome = (Ass_ev_siopeHome) getHome(userContext, Ass_ev_siopeBulk.class);

                                        mandatoBulk.setToBeUpdated();
                                        mandatoBulk.setStato_trasmissione(ReversaleBulk.STATO_TRASMISSIONE_NON_INSERITO);
                                        mandatoBulk.setStatoVarSos(StatoVariazioneSostituzione.VARIAZIONE_DEFINITIVA.value());
                                        final Codici_siopeBulk codiciSiopeBulk = assEvSiopeHome.findByElementoVoce(
                                                userContext,
                                                new Elemento_voceBulk(
                                                        cambiaVocePGiroBulk.getVoceFinale().getCd_elemento_voce(),
                                                        cambiaVocePGiroBulk.getVoceFinale().getEsercizio(),
                                                        cambiaVocePGiroBulk.getVoceFinale().getTi_appartenenza(),
                                                        cambiaVocePGiroBulk.getVoceFinale().getTi_gestione()
                                                )
                                        );

                                        for (Mandato_rigaBulk mandatoRigaBulk : mandatoBulk.getMandato_rigaColl()){
                                            if (mandatoRigaBulk.getMandato_siopeColl().size() > 1) {
                                                throw new ApplicationException("Esistono più righe di dettaglio SIOPE, operazione non possibile!");
                                            }
                                            Mandato_siopeBulk mandatoSiopeBulk = mandatoRigaBulk.getMandato_siopeColl().get(0);
                                            if (!mandatoSiopeBulk.getCodice_siope().equalsByPrimaryKey(codiciSiopeBulk)) {
                                                Mandato_siopeBulk newMandatoSiopeBulk = new Mandato_siopeIBulk();
                                                newMandatoSiopeBulk.setToBeCreated();
                                                newMandatoSiopeBulk.setCodice_siope(codiciSiopeBulk);
                                                newMandatoSiopeBulk.setImporto(mandatoSiopeBulk.getImporto());
                                                mandatoSiopeBulk.setToBeDeleted();
                                                mandatoRigaBulk.removeFromMandato_siopeColl(0);
                                                mandatoRigaBulk.addToMandato_siopeColl(newMandatoSiopeBulk);
                                            }
                                        }
                                        mandatoComponentSession.modificaConBulk(userContext, mandatoBulk);
                                    }
                                } catch (ComponentException | RemoteException | PersistencyException e) {
                                    throw new DetailedRuntimeException(e);
                                }
                            });
                }
            }
        } catch (DetailedRuntimeException e) {
            throw handleException(e);
        }
    }
}
