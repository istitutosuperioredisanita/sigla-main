/*
 * Copyright (C) 2021  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.coepcoan00.comp;

import it.cnr.contab.coepcoan00.core.bulk.*;
import it.cnr.contab.compensi00.docs.bulk.CompensoBulk;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.doccont00.core.bulk.MandatoBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoIBulk;
import it.cnr.contab.doccont00.core.bulk.ReversaleIBulk;
import it.cnr.contab.missioni00.docs.bulk.AnticipoBulk;
import it.cnr.contab.missioni00.docs.bulk.MissioneBulk;
import it.cnr.contab.missioni00.docs.bulk.RimborsoBulk;
import it.cnr.contab.util.Utility;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.*;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.persistency.sql.SQLBuilder;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScritturaPartitaDoppiaFromDocumentoComponent extends CRUDComponent {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ScritturaPartitaDoppiaFromDocumentoComponent.class);

    private Optional<Scrittura_partita_doppiaBulk> getScrittura(UserContext userContext, IDocumentoCogeBulk documentoCogeBulk) throws ComponentException {
        try {
            Optional<Scrittura_partita_doppiaBulk> scritturaOpt = Optional.empty();
            if (Utility.createConfigurazioneCnrComponentSession().isAttivaEconomica(userContext, documentoCogeBulk.getEsercizio())) {
                Scrittura_partita_doppiaHome partitaDoppiaHome = Optional.ofNullable(getHome(userContext, Scrittura_partita_doppiaBulk.class))
                        .filter(Scrittura_partita_doppiaHome.class::isInstance)
                        .map(Scrittura_partita_doppiaHome.class::cast)
                        .orElseThrow(() -> new DetailedRuntimeException("Partita doppia Home not found"));
                scritturaOpt = partitaDoppiaHome.getScrittura(userContext, documentoCogeBulk);
            }
            return scritturaOpt;
        } catch (ComponentException | RemoteException e) {
            throw handleException((OggettoBulk) documentoCogeBulk, e);
        }
    }

    private Optional<Scrittura_analiticaBulk> getScritturaAnalitica(UserContext userContext, IDocumentoCogeBulk documentoCogeBulk) throws ComponentException {
        try {
            Optional<Scrittura_analiticaBulk> scritturaOpt = Optional.empty();
            if (Utility.createConfigurazioneCnrComponentSession().isAttivaAnalitica(userContext, documentoCogeBulk.getEsercizio())) {
                Scrittura_analiticaHome analiticaHome = Optional.ofNullable(getHome(userContext, Scrittura_analiticaBulk.class))
                        .filter(Scrittura_analiticaHome.class::isInstance)
                        .map(Scrittura_analiticaHome.class::cast)
                        .orElseThrow(() -> new DetailedRuntimeException("Scrittura Analitica Home not found"));
                scritturaOpt = analiticaHome.getScrittura(userContext, documentoCogeBulk);
            }
            return scritturaOpt;
        } catch (ComponentException | RemoteException e) {
            throw handleException((OggettoBulk) documentoCogeBulk, e);
        }
    }

    private Optional<Scrittura_partita_doppiaBulk> getScritturaAnnullo(UserContext userContext, IDocumentoCogeBulk documentoCogeBulk) throws ComponentException {
        try {
            Optional<Scrittura_partita_doppiaBulk> scritturaOpt = Optional.empty();
            if (Utility.createConfigurazioneCnrComponentSession().isAttivaEconomica(userContext, documentoCogeBulk.getEsercizio())) {
                Scrittura_partita_doppiaHome partitaDoppiaHome = Optional.ofNullable(getHome(userContext, Scrittura_partita_doppiaBulk.class))
                        .filter(Scrittura_partita_doppiaHome.class::isInstance)
                        .map(Scrittura_partita_doppiaHome.class::cast)
                        .orElseThrow(() -> new DetailedRuntimeException("Partita doppia Home not found"));
                scritturaOpt = partitaDoppiaHome.getScritturaAnnullo(userContext, documentoCogeBulk);
            }
            return scritturaOpt;
        } catch (ComponentException | RemoteException | PersistencyException e) {
            throw handleException((OggettoBulk) documentoCogeBulk, e);
        }
    }

    protected void caricaScrittura(UserContext userContext, IDocumentoCogeBulk documentoCogeBulk) throws ComponentException {
        this.getScrittura(userContext, documentoCogeBulk).ifPresent(documentoCogeBulk::setScrittura_partita_doppia);
        this.getScritturaAnalitica(userContext, documentoCogeBulk).ifPresent(documentoCogeBulk::setScrittura_analitica);
    }

    @Override
    protected OggettoBulk eseguiCreaConBulk(UserContext usercontext, OggettoBulk oggettobulk) throws ComponentException, PersistencyException {
        OggettoBulk bulk = super.eseguiCreaConBulk(usercontext, oggettobulk);
        if (Optional.ofNullable(bulk).filter(IDocumentoCogeBulk.class::isInstance).isPresent()) {
            try {
                ResultScrittureContabili resultScrittureContabili = this.createScrittura(usercontext, (IDocumentoCogeBulk)oggettobulk);
                bulk = (OggettoBulk)resultScrittureContabili.getDocumentoCoge();
            } catch (NoRollbackException ignored) {
            } catch (ApplicationException e) {
                try {
                    if (!Utility.createConfigurazioneCnrComponentSession().isAttivaFinanziaria(usercontext, ((IDocumentoCogeBulk)oggettobulk).getEsercizio()))
                        throw e;
                } catch (RemoteException | ComponentException e2) {
                    throw new DetailedRuntimeException(e2);
                }
            } catch (ComponentException e) {
                throw new DetailedRuntimeException(e);
            }
        }
        return bulk;
    }

    @Override
    protected OggettoBulk eseguiModificaConBulk(UserContext usercontext, OggettoBulk oggettobulk) throws ComponentException, PersistencyException {
        OggettoBulk bulk = super.eseguiModificaConBulk(usercontext, oggettobulk);
        if (Optional.ofNullable(bulk).filter(IDocumentoCogeBulk.class::isInstance).isPresent()) {
            try {
                ResultScrittureContabili resultScrittureContabili = this.createScrittura(usercontext, (IDocumentoCogeBulk) bulk);
                bulk = (OggettoBulk)resultScrittureContabili.getDocumentoCoge();
            } catch (NoRollbackException ignored) {
            } catch (ApplicationException e) {
                try {
                    if (!Utility.createConfigurazioneCnrComponentSession().isAttivaFinanziaria(usercontext, ((IDocumentoCogeBulk) bulk).getEsercizio()))
                        throw e;
                } catch (RemoteException e2) {
                    throw new DetailedRuntimeException(e2);
                }
            }
        }
        return bulk;
    }

    public ResultScrittureContabili createScrittura(UserContext usercontext, IDocumentoCogeBulk documentoCoge) throws ComponentException {
        try {
            final Optional<IDocumentoCogeBulk> optionalIDocumentoCogeBulk = Optional.ofNullable(documentoCoge);
            if (optionalIDocumentoCogeBulk.isPresent()){
                if (Utility.createConfigurazioneCnrComponentSession().isAttivaEconomica(usercontext, documentoCoge.getEsercizio())) {
                    if (Utility.createConfigurazioneCnrComponentSession().isBloccoScrittureProposte(usercontext)) {
                        return this.loadScritturaPatrimoniale(usercontext, optionalIDocumentoCogeBulk.get());
                    } else {
                        final Optional<Scrittura_partita_doppiaBulk> optionalScrittura_partita_doppiaBulk = optionalIDocumentoCogeBulk
                                .map(IDocumentoCogeBulk::getScrittura_partita_doppia);
                        if (optionalScrittura_partita_doppiaBulk.isPresent()) {
                            if (optionalScrittura_partita_doppiaBulk.get().isToBeCreated()) {
                                final Optional<Scrittura_partita_doppiaBulk> optionalScritturaPartitaDoppiaOldBulk = this.getScrittura(usercontext, optionalIDocumentoCogeBulk.get());
                                optionalScritturaPartitaDoppiaOldBulk.ifPresent(oldScrittura->{
                                    //Elimino vecchia scrittura
                                    try {
                                        optionalScrittura_partita_doppiaBulk.ifPresent(prop->prop.setPg_scrittura(oldScrittura.getPg_scrittura()));
                                        this.removeScrittura(usercontext, oldScrittura);
                                    } catch (ComponentException e) {
                                        throw new DetailedRuntimeException(e);
                                    }
                                });
                                makeBulkPersistent(usercontext, optionalScrittura_partita_doppiaBulk.get());
                                return new ResultScrittureContabili(documentoCoge, optionalScrittura_partita_doppiaBulk.get(), null);
                            } else if (optionalScrittura_partita_doppiaBulk.get().isToBeUpdated()) {
                                makeBulkPersistent(usercontext, optionalScrittura_partita_doppiaBulk.get());
                                return new ResultScrittureContabili(documentoCoge, optionalScrittura_partita_doppiaBulk.get(), null);
                            }
                        } else {
                            Optional<Scrittura_partita_doppiaBulk> optionalScritturaPartitaDoppiaBulk;
                            try {
                                ResultScrittureContabili pair = this.proposeScritturaPartitaDoppiaWithSavepoint(usercontext, optionalIDocumentoCogeBulk.get());
                                documentoCoge = pair.getDocumentoCoge();
                                optionalScritturaPartitaDoppiaBulk = Optional.ofNullable(pair.getScritturaPartitaDoppiaBulk());
                            } catch (ScritturaPartitaDoppiaNotRequiredException | ScritturaPartitaDoppiaNotEnabledException e) {
                                optionalScritturaPartitaDoppiaBulk = Optional.empty();
                            }
                            if (optionalScritturaPartitaDoppiaBulk.isPresent()) {
                                makeBulkPersistent(usercontext, optionalScritturaPartitaDoppiaBulk.get());
                                return new ResultScrittureContabili(documentoCoge, optionalScritturaPartitaDoppiaBulk.get(), null);
                            }
                        }
                    }
                }
            }
            return new ResultScrittureContabili(documentoCoge, null, null);
        } catch (RemoteException | PersistencyException | SQLException e) {
            throw handleException(e);
        }
    }

    protected Scrittura_partita_doppiaBulk createScritturaAnnullo(UserContext usercontext, OggettoBulk oggettobulk) throws ComponentException {
        try {
            final Optional<IDocumentoCogeBulk> optionalIDocumentoCogeBulk = Optional.ofNullable(oggettobulk)
                    .filter(IDocumentoCogeBulk.class::isInstance)
                    .map(IDocumentoCogeBulk.class::cast);
            if (optionalIDocumentoCogeBulk.isPresent()){
                if (Utility.createConfigurazioneCnrComponentSession().isAttivaEconomica(usercontext, optionalIDocumentoCogeBulk.get().getEsercizio())) {
                    Optional<Scrittura_partita_doppiaBulk> optionalScritturaPartitaDoppiaPropostaBulk1;
                    try {
                        ResultScrittureContabili result = Utility.createProposeScritturaComponentSession()
                                .proposeScritturaPartitaDoppiaAnnullo(usercontext, optionalIDocumentoCogeBulk.get());
                        optionalScritturaPartitaDoppiaPropostaBulk1 = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk());

                    } catch (ScritturaPartitaDoppiaNotRequiredException | ScritturaPartitaDoppiaNotEnabledException e) {
                        optionalScritturaPartitaDoppiaPropostaBulk1 = Optional.empty();
                    }

                    final Optional<Scrittura_partita_doppiaBulk> optionalScritturaPartitaDoppiaPropostaBulk = optionalScritturaPartitaDoppiaPropostaBulk1;
                    final Optional<Scrittura_partita_doppiaBulk> optionalScritturaPartitaDoppiaOldBulk = this.getScritturaAnnullo(usercontext, optionalIDocumentoCogeBulk.get());
                    optionalScritturaPartitaDoppiaOldBulk.ifPresent(oldScrittura->{
                            //Elimino vecchia scrittura
                            try {
                                optionalScritturaPartitaDoppiaPropostaBulk.ifPresent(prop->prop.setPg_scrittura(oldScrittura.getPg_scrittura()));
                                this.removeScrittura(usercontext, oldScrittura);
                            } catch (ComponentException e) {
                                throw new DetailedRuntimeException(e);
                            }
                    });
                    //Ricreo
                    if (optionalScritturaPartitaDoppiaPropostaBulk.isPresent()) {
                        makeBulkPersistent(usercontext, optionalScritturaPartitaDoppiaPropostaBulk.get());
                        return optionalScritturaPartitaDoppiaPropostaBulk.get();
                    }
                }
            }
            return null;
        } catch (RemoteException | PersistencyException e) {
            throw handleException(e);
        }
    }

    protected Scrittura_partita_doppiaBulk createScritturaAnnullo(UserContext usercontext, IDocumentoCogeBulk documentoCoge, Scrittura_partita_doppiaBulk scritturaPrinc, Timestamp dataStorno) throws ComponentException {
        try {
            final Optional<IDocumentoCogeBulk> optionalIDocumentoCogeBulk = Optional.ofNullable(documentoCoge);

            final Optional<Scrittura_partita_doppiaBulk> optionalScritturaPartitaDoppiaPropostaBulk = Optional.ofNullable(Utility.createProposeScritturaComponentSession()
                        .proposeStornoScritturaPartitaDoppia(usercontext, scritturaPrinc, dataStorno));

            final Optional<Scrittura_partita_doppiaBulk> optionalScritturaPartitaDoppiaOldBulk = this.getScritturaAnnullo(usercontext, optionalIDocumentoCogeBulk.get());
            optionalScritturaPartitaDoppiaOldBulk.ifPresent(oldScrittura->{
                //Elimino vecchia scrittura
                try {
                    optionalScritturaPartitaDoppiaPropostaBulk.ifPresent(prop->prop.setPg_scrittura(oldScrittura.getPg_scrittura()));
                        this.removeScrittura(usercontext, oldScrittura);
                } catch (ComponentException e) {
                        throw new DetailedRuntimeException(e);
                    }
            });
            //Ricreo
            if (optionalScritturaPartitaDoppiaPropostaBulk.isPresent()) {
                makeBulkPersistent(usercontext, optionalScritturaPartitaDoppiaPropostaBulk.get());
                return optionalScritturaPartitaDoppiaPropostaBulk.get();
            }
            return null;
        } catch (RemoteException | PersistencyException e) {
            throw handleException(e);
        }
    }

    public void removeScrittura(UserContext userContext, Scrittura_partita_doppiaBulk scrittura) throws ComponentException {
        try {
            scrittura.getAllMovimentiColl().forEach(movcoge->{
                movcoge.setToBeDeleted();
                try {
                    super.deleteBulk(userContext, movcoge);
                } catch (ComponentException | PersistencyException e) {
                    throw new DetailedRuntimeException(e);
                }
            });
            scrittura.setToBeDeleted();
            super.deleteBulk(userContext, scrittura);
        } catch (PersistencyException e) {
            throw handleException(scrittura, e);
        }
    }

    public void removeScritturaAnalitica(UserContext userContext, Scrittura_analiticaBulk scrittura) throws ComponentException {
        try {
            scrittura.getMovimentiColl().forEach(movcoan->{
                movcoan.setToBeDeleted();
                try {
                    super.deleteBulk(userContext, movcoan);
                } catch (ComponentException | PersistencyException e) {
                    throw new DetailedRuntimeException(e);
                }
            });
            scrittura.setToBeDeleted();
            super.deleteBulk(userContext, scrittura);
        } catch (PersistencyException e) {
            throw handleException(scrittura, e);
        }
    }

    public List<IDocumentoCogeBulk> getAllDocumentiCogeDaContabilizzare(UserContext userContext, Integer esercizio, String cdCds) throws PersistencyException, ComponentException {
        List<IDocumentoCogeBulk> allDocuments = new ArrayList<>();
        {
            PersistentHome anticipoHome = getHome(userContext, AnticipoBulk.class);
            SQLBuilder sql = anticipoHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(anticipoHome.fetchAll(sql));
        }
        {
            PersistentHome missioneHome = getHome(userContext, MissioneBulk.class);
            SQLBuilder sql = missioneHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(missioneHome.fetchAll(sql));
        }
        {
            PersistentHome compensoHome = getHome(userContext, CompensoBulk.class);
            SQLBuilder sql = compensoHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(compensoHome.fetchAll(sql));
        }
        {
            PersistentHome rimborsoHome = getHome(userContext, RimborsoBulk.class);
            SQLBuilder sql = rimborsoHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(rimborsoHome.fetchAll(sql));
        }
        {
            PersistentHome docgenHome = getHome(userContext, Documento_genericoBulk.class);
            SQLBuilder sql = docgenHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(docgenHome.fetchAll(sql));
        }
        {
            PersistentHome fatattHome = getHome(userContext, Fattura_attiva_IBulk.class);
            SQLBuilder sql = fatattHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(fatattHome.fetchAll(fatattHome.createBroker(sql)));
        }
        {
            PersistentHome fatattHome = getHome(userContext, Nota_di_credito_attivaBulk.class);
            SQLBuilder sql = fatattHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(fatattHome.fetchAll(fatattHome.createBroker(sql)));
        }
        {
            PersistentHome fatattHome = getHome(userContext, Nota_di_debito_attivaBulk.class);
            SQLBuilder sql = fatattHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(fatattHome.fetchAll(fatattHome.createBroker(sql)));
        }
        {
            PersistentHome fatpasHome = getHome(userContext, Fattura_passiva_IBulk.class);
            SQLBuilder sql = fatpasHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(fatpasHome.fetchAll(fatpasHome.createBroker(sql)));
        }
        {
            PersistentHome fatpasHome = getHome(userContext, Nota_di_creditoBulk.class);
            SQLBuilder sql = fatpasHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(fatpasHome.fetchAll(fatpasHome.createBroker(sql)));
        }
        {
            PersistentHome fatpasHome = getHome(userContext, Nota_di_debitoBulk.class);
            SQLBuilder sql = fatpasHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(fatpasHome.fetchAll(fatpasHome.createBroker(sql)));
        }
        {
            PersistentHome mandatoHome = getHome(userContext, MandatoIBulk.class);
            SQLBuilder sql = mandatoHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(mandatoHome.fetchAll(sql));
        }
        {
            PersistentHome reversaleHome = getHome(userContext, ReversaleIBulk.class);
            SQLBuilder sql = reversaleHome.createSQLBuilder();
            sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, esercizio);
            Optional.ofNullable(cdCds).ifPresent(el -> sql.addClause(FindClause.AND, "cd_cds", SQLBuilder.EQUALS, el));
            sql.openParenthesis(FindClause.AND);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_N);
            sql.addClause(FindClause.OR, "stato_coge", SQLBuilder.EQUALS, MandatoBulk.STATO_COGE_R);
            sql.closeParenthesis();
            allDocuments.addAll(reversaleHome.fetchAll(sql));
        }
        return allDocuments;
    }

    private ResultScrittureContabili loadScritturaPatrimoniale(UserContext userContext, IDocumentoCogeBulk documentoCoge) throws ComponentException {
        try {
            final Optional<Scrittura_partita_doppiaBulk> optionalScritturaPartitaDoppiaOldBulk = this.getScrittura(userContext, documentoCoge);
            final Optional<Scrittura_analiticaBulk> optionalScritturaAnaliticaOldBulk = this.getScritturaAnalitica(userContext, documentoCoge);

            Optional<Scrittura_partita_doppiaBulk> optionalScritturaPartitaDoppiaPropostaBulk1;
            Optional<Scrittura_analiticaBulk> optionalScritturaAnaliticaPropostaBulk1;
            try {
                if (Utility.createConfigurazioneCnrComponentSession().isAttivaAnalitica(userContext, documentoCoge.getEsercizio())) {
                    ResultScrittureContabili pair = this.proposeScrittureContabiliWithSavepoint(userContext, documentoCoge);
                    documentoCoge = pair.getDocumentoCoge();
                    optionalScritturaPartitaDoppiaPropostaBulk1 = Optional.ofNullable(pair.getScritturaPartitaDoppiaBulk());
                    optionalScritturaAnaliticaPropostaBulk1 = Optional.ofNullable(pair.getScritturaAnaliticaBulk());
                } else {
                    ResultScrittureContabili pair = this.proposeScritturaPartitaDoppiaWithSavepoint(userContext, documentoCoge);
                    documentoCoge = pair.getDocumentoCoge();
                    optionalScritturaPartitaDoppiaPropostaBulk1 = Optional.ofNullable(pair.getScritturaPartitaDoppiaBulk());
                    optionalScritturaAnaliticaPropostaBulk1 = Optional.empty();
                }
                documentoCoge.setStato_coge(Fattura_passivaBulk.NON_REGISTRATO_IN_COGE);
                documentoCoge.setStato_coan(Fattura_passivaBulk.NON_REGISTRATO_IN_COGE);
            } catch (ScritturaPartitaDoppiaNotEnabledException e) {
                return null;
            } catch (ScritturaPartitaDoppiaNotRequiredException e) {
                optionalScritturaPartitaDoppiaPropostaBulk1 = Optional.empty();
                optionalScritturaAnaliticaPropostaBulk1 = Optional.empty();
                documentoCoge.setStato_coge(Fattura_passivaBulk.NON_PROCESSARE_IN_COGE);
                documentoCoge.setStato_coan(Fattura_passivaBulk.NON_PROCESSARE_IN_COAN);
            } catch (RemoteException e) {
                throw new DetailedRuntimeException(e);
            }

            final Optional<Scrittura_analiticaBulk> optionalScritturaAnaliticaPropostaBulk = optionalScritturaAnaliticaPropostaBulk1;

            optionalScritturaAnaliticaOldBulk.ifPresent(oldScrittura->{
                //Elimino vecchia scrittura
                try {
                    optionalScritturaAnaliticaPropostaBulk.ifPresent(prop->prop.setPg_scrittura(oldScrittura.getPg_scrittura()));
                    this.removeScritturaAnalitica(userContext, oldScrittura);
                } catch (ComponentException e) {
                    throw new DetailedRuntimeException(e);
                }
            });

            final Optional<Scrittura_partita_doppiaBulk> optionalScritturaPartitaDoppiaPropostaBulk = optionalScritturaPartitaDoppiaPropostaBulk1;

            optionalScritturaPartitaDoppiaOldBulk.ifPresent(oldScrittura->{
                //Elimino vecchia scrittura
                try {
                    optionalScritturaPartitaDoppiaPropostaBulk.ifPresent(prop->prop.setPg_scrittura(oldScrittura.getPg_scrittura()));
                    this.removeScrittura(userContext, oldScrittura);
                } catch (ComponentException e) {
                    throw new DetailedRuntimeException(e);
                }
            });

            //Ricreo
            if (optionalScritturaPartitaDoppiaPropostaBulk.isPresent()) {
                makeBulkPersistent(userContext, optionalScritturaPartitaDoppiaPropostaBulk.get());
                documentoCoge.setStato_coge(Fattura_passivaBulk.REGISTRATO_IN_COGE);
            }
            if (optionalScritturaAnaliticaPropostaBulk.isPresent()) {
                makeBulkPersistent(userContext, optionalScritturaAnaliticaPropostaBulk.get());
                documentoCoge.setStato_coan(Fattura_passivaBulk.REGISTRATO_IN_COGE);
            }

            ((OggettoBulk)documentoCoge).setToBeUpdated();
            updateBulk(userContext, (OggettoBulk)documentoCoge);
            return new ResultScrittureContabili(documentoCoge, optionalScritturaPartitaDoppiaPropostaBulk.orElse(null), optionalScritturaAnaliticaPropostaBulk.orElse(null));
        } catch (PersistencyException|SQLException e) {
            throw handleException(e);
        }
    }

    private ResultScrittureContabili proposeScritturaPartitaDoppiaWithSavepoint(UserContext userContext, IDocumentoCogeBulk documentoCoge) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException, SQLException{
        try {
            setSavepoint(userContext, "INIT_SCRITTURA_PRIMA_NOTA");
            return Utility.createProposeScritturaComponentSession().proposeScritturaPartitaDoppia(userContext, documentoCoge);
        } catch (ScritturaPartitaDoppiaNotRequiredException | ScritturaPartitaDoppiaNotEnabledException | ApplicationException e) {
            rollbackToSavepoint(userContext, "INIT_SCRITTURA_PRIMA_NOTA");
            throw e;
        } catch (ApplicationRuntimeException e) {
            rollbackToSavepoint(userContext, "INIT_SCRITTURA_PRIMA_NOTA");
            throw new ComponentException(e);
        } catch (RemoteException e) {
            throw handleException(e);
        }
    }

    private ResultScrittureContabili proposeScrittureContabiliWithSavepoint(UserContext userContext, IDocumentoCogeBulk documentoCoge) throws ComponentException, ScritturaPartitaDoppiaNotRequiredException, ScritturaPartitaDoppiaNotEnabledException, SQLException{
        try {
            setSavepoint(userContext, "INIT_SCRITTURE_CONTABILI_PRIMA_NOTA");
            return Utility.createProposeScritturaComponentSession().proposeScrittureContabili(userContext, documentoCoge);
        } catch (ScritturaPartitaDoppiaNotRequiredException | ScritturaPartitaDoppiaNotEnabledException | ApplicationException e) {
            rollbackToSavepoint(userContext, "INIT_SCRITTURE_CONTABILI_PRIMA_NOTA");
            throw e;
        } catch (ApplicationRuntimeException e) {
            rollbackToSavepoint(userContext, "INIT_SCRITTURE_CONTABILI_PRIMA_NOTA");
            throw new ComponentException(e);
        } catch (RemoteException e) {
            throw handleException(e);
        }
    }
}
