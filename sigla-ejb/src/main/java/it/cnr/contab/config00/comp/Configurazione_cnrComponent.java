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

package it.cnr.contab.config00.comp;

import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrHome;
import it.cnr.contab.config00.bulk.Configurazione_cnrKey;
import it.cnr.contab.utente00.ejb.RuoloComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.enumeration.TipoRapportoTesoreriaEnum;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyError;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.ejb.EJBCommonServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.EJBException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

public class Configurazione_cnrComponent extends it.cnr.jada.comp.CRUDDetailComponent implements IConfigurazione_cnrMgr, Cloneable, Serializable {

    public static final String ASTERISCO = "*";

    private final static Logger logger = LoggerFactory.getLogger(Configurazione_cnrComponent.class);

    public Configurazione_cnrComponent() {

        /*Default constructor*/


    }

    /**
     * esercizio nullo
     * PreCondition:
     * esercizio = null
     * PostCondition:
     * Effettua la ricerca usando la condizione SQL esercizio = '*'
     * unita funzionale nulla
     * PreCondition:
     * unita_funzionale = null
     * PostCondition:
     * Effettua la ricerca con la codizione SQL cd_unita_funzionale = '*'
     * chiave secondaria nulla
     * PreCondition:
     * chiave_secondaria = null
     * PostCondition:
     * Effettua la ricerca usando la clausola SQL chiave_secondaria = '*'
     * Normale
     * PreCondition:
     * Viene richiesto un istanza di Configurazioe_cnrBulk dalle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito un'istanza di Configurazione_cnrBulk o null se la ricerca non restituisce nulla
     */
    public Configurazione_cnrBulk getConfigurazione(UserContext userContext, Integer esercizio, String unita_funzionale, String chiave_primaria, String chiave_secondaria) throws ComponentException {
        try {
            if (esercizio == null) esercizio = 0;
            if (unita_funzionale == null) unita_funzionale = ASTERISCO;
            if (chiave_secondaria == null) chiave_secondaria = ASTERISCO;
            return (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, chiave_secondaria, unita_funzionale, esercizio));
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * esercizio nullo
     * PreCondition:
     * esercizio = null
     * PostCondition:
     * Effettua la ricerca usando la condizione SQL esercizio = '*'
     * unita funzionale nulla
     * PreCondition:
     * unita_funzionale = null
     * PostCondition:
     * Effettua la ricerca con la codizione SQL cd_unita_funzionale = '*'
     * chiave secondaria nulla
     * PreCondition:
     * chiave_secondaria = null
     * PostCondition:
     * Effettua la ricerca usando la clausola SQL chiave_secondaria = '*'
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public Timestamp getDt01(UserContext userContext, Integer esercizio, String unita_funzionale, String chiave_primaria, String chiave_secondaria) throws ComponentException {
        try {
            if (esercizio == null) esercizio = 0;
            if (unita_funzionale == null) unita_funzionale = ASTERISCO;
            if (chiave_secondaria == null) chiave_secondaria = ASTERISCO;
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, chiave_secondaria, unita_funzionale, esercizio));
            if (bulk == null) return null;
            return bulk.getDt01();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public Timestamp getDt01(UserContext userContext, String chiave_primaria) throws ComponentException {
        try {
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, ASTERISCO, ASTERISCO, 0));
            if (bulk == null) return null;
            return bulk.getDt01();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * esercizio nullo
     * PreCondition:
     * esercizio = null
     * PostCondition:
     * Effettua la ricerca usando la condizione SQL esercizio = '*'
     * unita funzionale nulla
     * PreCondition:
     * unita_funzionale = null
     * PostCondition:
     * Effettua la ricerca con la codizione SQL cd_unita_funzionale = '*'
     * chiave secondaria nulla
     * PreCondition:
     * chiave_secondaria = null
     * PostCondition:
     * Effettua la ricerca usando la clausola SQL chiave_secondaria = '*'
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public Timestamp getDt02(UserContext userContext, Integer esercizio, String unita_funzionale, String chiave_primaria, String chiave_secondaria) throws ComponentException {
        try {
            if (esercizio == null) esercizio = 0;
            if (unita_funzionale == null) unita_funzionale = ASTERISCO;
            if (chiave_secondaria == null) chiave_secondaria = ASTERISCO;
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, chiave_secondaria, unita_funzionale, esercizio));
            if (bulk == null) return null;
            return bulk.getDt02();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public Timestamp getDt02(UserContext userContext, String chiave_primaria) throws ComponentException {
        try {
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, ASTERISCO, ASTERISCO, 0));
            if (bulk == null) return null;
            return bulk.getDt02();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * esercizio nullo
     * PreCondition:
     * esercizio = null
     * PostCondition:
     * Effettua la ricerca usando la condizione SQL esercizio = '*'
     * unita funzionale nulla
     * PreCondition:
     * unita_funzionale = null
     * PostCondition:
     * Effettua la ricerca con la codizione SQL cd_unita_funzionale = '*'
     * chiave secondaria nulla
     * PreCondition:
     * chiave_secondaria = null
     * PostCondition:
     * Effettua la ricerca usando la clausola SQL chiave_secondaria = '*'
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public BigDecimal getIm01(UserContext userContext, Integer esercizio, String unita_funzionale, String chiave_primaria, String chiave_secondaria) throws ComponentException {
        try {
            if (esercizio == null) esercizio = 0;
            if (unita_funzionale == null) unita_funzionale = ASTERISCO;
            if (chiave_secondaria == null) chiave_secondaria = ASTERISCO;
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, chiave_secondaria, unita_funzionale, esercizio));
            if (bulk == null) return null;
            return bulk.getIm01();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public BigDecimal getIm01(UserContext userContext, String chiave_primaria) throws ComponentException {
        try {
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, ASTERISCO, ASTERISCO, 0));
            if (bulk == null) return null;
            return bulk.getIm01();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * esercizio nullo
     * PreCondition:
     * esercizio = null
     * PostCondition:
     * Effettua la ricerca usando la condizione SQL esercizio = '*'
     * unita funzionale nulla
     * PreCondition:
     * unita_funzionale = null
     * PostCondition:
     * Effettua la ricerca con la codizione SQL cd_unita_funzionale = '*'
     * chiave secondaria nulla
     * PreCondition:
     * chiave_secondaria = null
     * PostCondition:
     * Effettua la ricerca usando la clausola SQL chiave_secondaria = '*'
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public BigDecimal getIm02(UserContext userContext, Integer esercizio, String unita_funzionale, String chiave_primaria, String chiave_secondaria) throws ComponentException {
        try {
            if (esercizio == null) esercizio = 0;
            if (unita_funzionale == null) unita_funzionale = ASTERISCO;
            if (chiave_secondaria == null) chiave_secondaria = ASTERISCO;
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, chiave_secondaria, unita_funzionale, esercizio));
            if (bulk == null) return null;
            return bulk.getIm02();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public BigDecimal getIm02(UserContext userContext, String chiave_primaria) throws ComponentException {
        try {
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, ASTERISCO, ASTERISCO, 0));
            if (bulk == null) return null;
            return bulk.getIm02();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * esercizio nullo
     * PreCondition:
     * esercizio = null
     * PostCondition:
     * Effettua la ricerca usando la condizione SQL esercizio = '*'
     * unita funzionale nulla
     * PreCondition:
     * unita_funzionale = null
     * PostCondition:
     * Effettua la ricerca con la codizione SQL cd_unita_funzionale = '*'
     * chiave secondaria nulla
     * PreCondition:
     * chiave_secondaria = null
     * PostCondition:
     * Effettua la ricerca usando la clausola SQL chiave_secondaria = '*'
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public String getVal01(UserContext userContext, Integer esercizio, String unita_funzionale, String chiave_primaria, String chiave_secondaria) throws ComponentException {
        try {
            if (esercizio == null) esercizio = 0;
            if (unita_funzionale == null) unita_funzionale = ASTERISCO;
            if (chiave_secondaria == null) chiave_secondaria = ASTERISCO;
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, chiave_secondaria, unita_funzionale, esercizio));
            if (bulk == null) return null;
            return bulk.getVal01();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public String getVal01(UserContext userContext, String chiave_primaria) throws ComponentException {
        try {
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, ASTERISCO, ASTERISCO, 0));
            if (bulk == null) return null;
            return bulk.getVal01();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * esercizio nullo
     * PreCondition:
     * esercizio = null
     * PostCondition:
     * Effettua la ricerca usando la condizione SQL esercizio = '*'
     * unita funzionale nulla
     * PreCondition:
     * unita_funzionale = null
     * PostCondition:
     * Effettua la ricerca con la codizione SQL cd_unita_funzionale = '*'
     * chiave secondaria nulla
     * PreCondition:
     * chiave_secondaria = null
     * PostCondition:
     * Effettua la ricerca usando la clausola SQL chiave_secondaria = '*'
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public String getVal02(UserContext userContext, Integer esercizio, String unita_funzionale, String chiave_primaria, String chiave_secondaria) throws ComponentException {
        try {
            if (esercizio == null) esercizio = 0;
            if (unita_funzionale == null) unita_funzionale = ASTERISCO;
            if (chiave_secondaria == null) chiave_secondaria = ASTERISCO;
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, chiave_secondaria, unita_funzionale, esercizio));
            if (bulk == null) return null;
            return bulk.getVal02();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public String getVal02(UserContext userContext, String chiave_primaria) throws ComponentException {
        try {
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, ASTERISCO, ASTERISCO, 0));
            if (bulk == null) return null;
            return bulk.getVal02();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * esercizio nullo
     * PreCondition:
     * esercizio = null
     * PostCondition:
     * Effettua la ricerca usando la condizione SQL esercizio = '*'
     * unita funzionale nulla
     * PreCondition:
     * unita_funzionale = null
     * PostCondition:
     * Effettua la ricerca con la codizione SQL cd_unita_funzionale = '*'
     * chiave secondaria nulla
     * PreCondition:
     * chiave_secondaria = null
     * PostCondition:
     * Effettua la ricerca usando la clausola SQL chiave_secondaria = '*'
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public String getVal03(UserContext userContext, Integer esercizio, String unita_funzionale, String chiave_primaria, String chiave_secondaria) throws ComponentException {
        try {
            if (esercizio == null) esercizio = 0;
            if (unita_funzionale == null) unita_funzionale = ASTERISCO;
            if (chiave_secondaria == null) chiave_secondaria = ASTERISCO;
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, chiave_secondaria, unita_funzionale, esercizio));
            if (bulk == null) return null;
            return bulk.getVal03();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public String getVal03(UserContext userContext, String chiave_primaria) throws ComponentException {
        try {
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, ASTERISCO, ASTERISCO, 0));
            if (bulk == null) return null;
            return bulk.getVal03();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * esercizio nullo
     * PreCondition:
     * esercizio = null
     * PostCondition:
     * Effettua la ricerca usando la condizione SQL esercizio = '*'
     * unita funzionale nulla
     * PreCondition:
     * unita_funzionale = null
     * PostCondition:
     * Effettua la ricerca con la codizione SQL cd_unita_funzionale = '*'
     * chiave secondaria nulla
     * PreCondition:
     * chiave_secondaria = null
     * PostCondition:
     * Effettua la ricerca usando la clausola SQL chiave_secondaria = '*'
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public String getVal04(UserContext userContext, Integer esercizio, String unita_funzionale, String chiave_primaria, String chiave_secondaria) throws ComponentException {
        try {
            if (esercizio == null) esercizio = 0;
            if (unita_funzionale == null) unita_funzionale = ASTERISCO;
            if (chiave_secondaria == null) chiave_secondaria = ASTERISCO;
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, chiave_secondaria, unita_funzionale, esercizio));
            if (bulk == null) return null;
            return bulk.getVal04();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Normale
     * PreCondition:
     * Viene richiesto il valore del campo val01 delle costanti cnr
     * PostCondition:
     * Viene effettuata una ricerca sulla tabella CONFIGURAZIONE_CNR con le chiavi specificate e viene restituito il valore del campo; se non viene trovato nessun record restituisce null
     */
    public String getVal04(UserContext userContext, String chiave_primaria) throws ComponentException {
        try {
            Configurazione_cnrBulk bulk = (Configurazione_cnrBulk) getHome(userContext, Configurazione_cnrBulk.class).findByPrimaryKey(new Configurazione_cnrKey(chiave_primaria, ASTERISCO, ASTERISCO, 0));
            if (bulk == null) return null;
            return bulk.getVal04();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Boolean isAttivoOrdini(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_ORDINI,
                    Configurazione_cnrBulk.SK_GESTIONE_ORDINI,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Boolean hasGestioneImportiFlussiFinanziari(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_GESTIONE_CONTRATTI,
                    Configurazione_cnrBulk.SK_CIG_IMP_SENZA_FLUSSI_FINANZ,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }
    public Boolean propostaFatturaDaOrdini(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_ORDINI,
                    Configurazione_cnrBulk.SK_GESTIONE_ORDINI,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val02YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val02YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Boolean isEconomicaPatrimonialeAttivaImputazioneManuale(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_ECONOMICO_PATRIMONIALE,
                    Configurazione_cnrBulk.SK_IMPUTAZIONE_MANUALE,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    private Optional<Boolean> val01YesNo(UserContext userContext, Configurazione_cnrKey configurazioneCnrKey) throws PersistencyException, ComponentException {
        final BulkHome home = getHome(userContext, Configurazione_cnrBulk.class);
        return Optional.ofNullable(home.findByPrimaryKey(configurazioneCnrKey))
                .filter(Configurazione_cnrBulk.class::isInstance)
                .map(Configurazione_cnrBulk.class::cast)
                .map(bulk -> Optional.ofNullable(bulk.getVal01()).filter(val -> val.equals("Y") || val.equals("S")).isPresent());
    }
    private Optional<Boolean> val02YesNo(UserContext userContext, Configurazione_cnrKey configurazioneCnrKey) throws PersistencyException, ComponentException {
        final BulkHome home = getHome(userContext, Configurazione_cnrBulk.class);
        return Optional.ofNullable(home.findByPrimaryKey(configurazioneCnrKey))
                .filter(Configurazione_cnrBulk.class::isInstance)
                .map(Configurazione_cnrBulk.class::cast)
                .map(bulk -> Optional.ofNullable(bulk.getVal02()).filter(val -> val.equals("Y") || val.equals("S")).isPresent());
    }

    /**
     * Ritorna il codice uo della Ragioneria
     * <p><b>chiave_primaria: UO_SPECIALE</b>
     * <p><b>chiave_secondaria: UO_RAGIONERIA</b>
     *
     * @param esercizio l'esercizio di ricerca - se non esistono configurazioni per l'esercizio indicato viene cercata la configurazione con esercizio=0
     * @return String - il codice uo della Ragioneria
     * @throws ComponentException, PersistencyException
     */
    public String getUoRagioneria(UserContext userContext, Integer esercizio) throws ComponentException {
        try {
            return ((Configurazione_cnrHome) getHome(userContext, Configurazione_cnrBulk.class)).getUoRagioneria(esercizio);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Ritorna il conto corrente ENTE
     * <p><b>chiave_primaria: CONTO_CORRENTE_SPECIALE</b>
     * <p><b>chiave_secondaria: ENTE</b>
     *
     * @param esercizio l'esercizio di ricerca - se non esistono configurazioni per l'esercizio indicato viene cercata la configurazione con esercizio=0
     * @return String - il codice uo della Ragioneria
     * @throws ComponentException, PersistencyException
     */
    public String getContoCorrenteEnte(UserContext userContext, Integer esercizio) throws ComponentException {
        try {
            return ((Configurazione_cnrHome) getHome(userContext, Configurazione_cnrBulk.class)).getContoCorrenteEnte(esercizio);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Ritorna il codice cdr del personale
     * <p><b>chiave_primaria: ELEMENTO_VOCE_SPECIALE</b>
     * <p><b>chiave_secondaria: TEMPO_IND_SU_PROGETTI_FINANZIATI</b>
     *
     * @param esercizio l'esercizio di ricerca - se non esistono configurazioni per l'esercizio indicato viene cercata la configurazione con esercizio=0
     * @return String - il codice cdr del personale
     * @throws ComponentException
     */
    public String getCdrPersonale(UserContext userContext, Integer esercizio) throws ComponentException {
        try {
            return ((Configurazione_cnrHome) getHome(userContext, Configurazione_cnrBulk.class)).getCdrPersonale(esercizio);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Ritorna il codice uo distinta tutta sac
     * <p><b>chiave_primaria: UO_SPECIALE</b>
     * <p><b>chiave_secondaria: UO_DISTINTA_TUTTA_SAC</b>
     *
     * @param esercizio l'esercizio di ricerca - se non esistono configurazioni per l'esercizio indicato viene cercata la configurazione con esercizio=0
     * @return String - il codice codice uo distinta tutta sac
     * @throws PersistencyException
     */
    public String getUoDistintaTuttaSac(UserContext userContext, Integer esercizio) throws ComponentException {
        try {
            return ((Configurazione_cnrHome) getHome(userContext, Configurazione_cnrBulk.class)).getUoDistintaTuttaSac(esercizio);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Indica se la uo indicata è proprio quella speciale tutta sac
     *
     * @param esercizio            l'esercizio di ricerca - Lasciare vuoto per ricercare il parametro generale (esercizio=0).
     * @param cdUnitaOrganizzativa l'unità organizzativa di cui si chiede se si tratta della Uo Speciale Tutta SAC
     * @return Boolean
     * @throws PersistencyException
     */
    public Boolean isUOSpecialeDistintaTuttaSAC(UserContext userContext, Integer esercizio, String cdUnitaOrganizzativa) throws ComponentException {
        try {
            return ((Configurazione_cnrHome) getHome(userContext, Configurazione_cnrBulk.class)).isUOSpecialeDistintaTuttaSAC(esercizio, cdUnitaOrganizzativa);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Indica il cds sac
     *
     * @param esercizio l'esercizio di ricerca - Lasciare vuoto per ricercare il parametro generale (esercizio=0).
     * @throws PersistencyException
     */
    public String getCdsSAC(UserContext userContext, Integer esercizio) throws ComponentException {
        try {
            return ((Configurazione_cnrHome) getHome(userContext, Configurazione_cnrBulk.class)).getCdsSAC(esercizio);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public void shutdowHook(UserContext userContext) throws ComponentException {
        logger.info("shutdow hook");
        final BulkHome home = getHome(userContext, Configurazione_cnrBulk.class);
        try {
            Configurazione_cnrBulk configurazione_cnrBulk = new Configurazione_cnrBulk(
                    Configurazione_cnrBulk.PK_EMAIL_PEC,
                    Configurazione_cnrBulk.SK_SDI,
                    ASTERISCO,
                    0);
            Optional.ofNullable(home.findByPrimaryKey(configurazione_cnrBulk))
                    .filter(Configurazione_cnrBulk.class::isInstance)
                    .map(Configurazione_cnrBulk.class::cast)
                    .filter(bulk -> bulk.getVal04().equalsIgnoreCase("Y"))
                    .ifPresent(bulk -> {
                        bulk.setVal04("N");
                        bulk.setToBeUpdated();
                        try {
                            home.update(bulk, userContext);
                        } catch (PersistencyException e) {
                            throw new RuntimeException(e);
                        }
                    });
            Configurazione_cnrBulk flussoOrdinativi = new Configurazione_cnrBulk(
                    Configurazione_cnrBulk.PK_FLUSSO_ORDINATIVI,
                    Configurazione_cnrBulk.SK_ATTIVO_SIOPEPLUS,
                    ASTERISCO,
                    LocalDateTime.now().getYear());
            Optional.ofNullable(home.findByPrimaryKey(flussoOrdinativi))
                    .filter(Configurazione_cnrBulk.class::isInstance)
                    .map(Configurazione_cnrBulk.class::cast)
                    .filter(bulk -> bulk.getVal04().equalsIgnoreCase("Y"))
                    .ifPresent(bulk -> {
                        bulk.setVal04("N");
                        bulk.setToBeUpdated();
                        try {
                            home.update(bulk, userContext);
                        } catch (PersistencyException e) {
                            throw new RuntimeException(e);
                        }
                    });

        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Boolean getGestioneImpegnoChiusuraForzataCompetenza(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_AGGIORNAMENTO_IMPEGNO_DA_ORDINE,
                    Configurazione_cnrBulk.IMPEGNO_CHIUSURA_FORZATA_A_COMPETENZA,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        return Boolean.FALSE;
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Boolean getGestioneImpegnoChiusuraForzataResiduo(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_AGGIORNAMENTO_IMPEGNO_DA_ORDINE,
                    Configurazione_cnrBulk.IMPEGNO_CHIUSURA_FORZATA_A_RESIDUO,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        return Boolean.FALSE;
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * @param userContext
     * @return É attiva la gestione della finanziaria
     * @throws PersistencyException
     */
    public boolean isAttivaFinanziaria(UserContext userContext, int esercizio) throws ComponentException {
        try {
            return !Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
                    .filter(Configurazione_cnrHome.class::isInstance)
                    .map(Configurazione_cnrHome.class::cast)
                    .orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
                    .isAttivaEconomicaPura(esercizio);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * @param userContext
     * @return É attiva la gestione dell'economico patrimoniale (parallela o pura)
     * @throws PersistencyException
     */
    public boolean isAttivaEconomica(UserContext userContext, int esercizio) throws ComponentException {
        try {
            return Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
                    .filter(Configurazione_cnrHome.class::isInstance)
                    .map(Configurazione_cnrHome.class::cast)
                    .orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
                    .isAttivaEconomica(esercizio);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * @param userContext
     * @return É attiva la gestione dell'analitica
     * @throws PersistencyException
     */
    public boolean isAttivaAnalitica(UserContext userContext, int esercizio) throws ComponentException {
        try {
            return Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
                    .filter(Configurazione_cnrHome.class::isInstance)
                    .map(Configurazione_cnrHome.class::cast)
                    .orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
                    .isAttivaAnalitica(esercizio);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * @param userContext
     * @return É attivo il blocco delle scritture di economica
     * @throws PersistencyException
     */
    public boolean isBloccoScrittureProposte(UserContext userContext) throws ComponentException {
        try {
            return Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
                    .filter(Configurazione_cnrHome.class::isInstance)
                    .map(Configurazione_cnrHome.class::cast)
                    .orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
                    .isBloccoScrittureProposte(userContext);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * @param userContext
     * @return É attiva la gestione che, in fase di assunzione impegno, consente di creare in automatico sull'impegno di spesa
     * una variazione di storno bilancio con cui vengono in automatico spostate le somme dalla GAE selezionata sull'impegno ad una GAE alternativa
     * indicata direttamente dall'utente
     * @throws PersistencyException
     */
    public boolean isVariazioneAutomaticaSpesa(UserContext userContext) throws ComponentException {
        try {
            return Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
                    .filter(Configurazione_cnrHome.class::isInstance)
                    .map(Configurazione_cnrHome.class::cast)
                    .orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
                    .isVariazioneAutomaticaSpesa(userContext);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Integer getCdTerzoDiversiStipendi(UserContext userContext) throws ComponentException {
        try {
            return Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
                    .filter(Configurazione_cnrHome.class::isInstance)
                    .map(Configurazione_cnrHome.class::cast)
                    .orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
                    .getCdTerzoDiversiStipendi();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Integer getCdTerzoDiversiCollaboratori(UserContext userContext) throws ComponentException {
        try {
            return Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
                    .filter(Configurazione_cnrHome.class::isInstance)
                    .map(Configurazione_cnrHome.class::cast)
                    .orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
                    .getCdTerzoDiversiCollaboratori();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Timestamp getDataFineValiditaCaricoFamiliare(UserContext userContext, String tiPersona) throws ComponentException {
        try {
            return Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
                    .filter(Configurazione_cnrHome.class::isInstance)
                    .map(Configurazione_cnrHome.class::cast)
                    .orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
                    .getDataFineValiditaCaricoFamiliare(tiPersona);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }
    public Boolean isAssPrgAnagraficoAttiva(UserContext userContext) throws ComponentException{
        try{
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_GESTIONE_PROGETTI,
                    Configurazione_cnrBulk.SK_ASS_PROGETTI_ANGAGRAFICO,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }
    /**
     *
     * @param userContext
     * @return É attiva la gestione che, in fase di assunzione impegno, consente di creare in automatico sull'impegno di spesa
     * una variazione di storno bilancio con cui vengono in automatico spostate le somme dalla GAE selezionata sull'impegno ad una GAE alternativa
     * indicata direttamente dall'utente
     * @throws PersistencyException
     */
    public Boolean isAttachRestContrStoredFromSigla(UserContext userContext) throws ComponentException, RemoteException {
        try{
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_GESTIONE_CONTRATTI,
                    Configurazione_cnrBulk.SK_ATT_REST_STORED_FROM_SIGLA,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }


    public Boolean isAccertamentoPluriennaleAttivo(UserContext userContext) throws ComponentException {
        final Configurazione_cnrBulk configurazione =
                Optional.ofNullable(
                        getConfigurazione(
                                userContext,
                                CNRUserContext.getEsercizio(userContext),
                                ASTERISCO,
                                Configurazione_cnrBulk.PK_ACCERTAMENTI,
                                Configurazione_cnrBulk.SK_ACCERTAMENTI_PLURIENNALI)
                ).orElseGet(() -> {
                            try {
                                return getConfigurazione(
                                        userContext,
                                        0,
                                        ASTERISCO,
                                        Configurazione_cnrBulk.PK_ACCERTAMENTI,
                                        Configurazione_cnrBulk.SK_ACCERTAMENTI_PLURIENNALI);
                            } catch (ComponentException e) {
                                throw new DetailedRuntimeException(e);
                            }
                        }
                );
        final boolean accertamentoPluriennaleAttivo = Optional.ofNullable(configurazione)
                .flatMap(configurazione_cnrBulk -> Optional.ofNullable(configurazione_cnrBulk.getVal01()))
                .filter(val -> val.equals("Y"))
                .isPresent();
        final Optional<String> ruolo = Optional.ofNullable(configurazione.getVal02());
        if (accertamentoPluriennaleAttivo && ruolo.isPresent()){
            try {
                return ((RuoloComponentSession) EJBCommonServices.createEJB("CNRUTENZE00_EJB_RuoloComponentSession"))
                        .controlloAbilitazione(userContext, ruolo.get());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        return accertamentoPluriennaleAttivo;
    }

    public Boolean isImpegnoPluriennaleAttivo(UserContext userContext) throws ComponentException {
        final Configurazione_cnrBulk configurazione =
                Optional.ofNullable(
                        getConfigurazione(
                                userContext,
                                CNRUserContext.getEsercizio(userContext),
                                ASTERISCO,
                                Configurazione_cnrBulk.PK_IMPEGNI,
                                Configurazione_cnrBulk.SK_IMPEGNI_PLURIENNALI)
                ).orElseGet(() -> {
                            try {
                                return getConfigurazione(
                                        userContext,
                                        0,
                                        ASTERISCO,
                                        Configurazione_cnrBulk.PK_IMPEGNI,
                                        Configurazione_cnrBulk.SK_IMPEGNI_PLURIENNALI);
                            } catch (ComponentException e) {
                                throw new DetailedRuntimeException(e);
                            }
                        }
                );
        final boolean impegnoPluriennaleAttivo = Optional.ofNullable(configurazione)
                .flatMap(configurazione_cnrBulk -> Optional.ofNullable(configurazione_cnrBulk.getVal01()))
                .filter(val -> val.equals("Y"))
                .isPresent();
        final Optional<String> ruolo = Optional.ofNullable(configurazione.getVal02());
        if (impegnoPluriennaleAttivo && ruolo.isPresent()){
            try {
                return ((RuoloComponentSession) EJBCommonServices.createEJB("CNRUTENZE00_EJB_RuoloComponentSession"))
                        .controlloAbilitazione(userContext, ruolo.get());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        return impegnoPluriennaleAttivo;
    }

    public Boolean isGestioneImportoInventarioReadOnly(UserContext userContext) throws ComponentException {
        final Configurazione_cnrBulk configurazione =
                Optional.ofNullable(
                        getConfigurazione(
                                userContext,
                                CNRUserContext.getEsercizio(userContext),
                                ASTERISCO,
                                Configurazione_cnrBulk.PK_INVENTARIO,
                                Configurazione_cnrBulk.SK_GESTIONE_IMPORTO_RO_INVENTARIO)
                ).orElseGet(() -> {
                            try {
                                return getConfigurazione(
                                        userContext,
                                        0,
                                        ASTERISCO,
                                        Configurazione_cnrBulk.PK_INVENTARIO,
                                        Configurazione_cnrBulk.SK_GESTIONE_IMPORTO_RO_INVENTARIO);
                            } catch (ComponentException e) {
                                throw new DetailedRuntimeException(e);
                            }
                        }
                );
        return Optional.ofNullable(configurazione)
                .flatMap(configurazione_cnrBulk -> Optional.ofNullable(configurazione_cnrBulk.getVal01()))
                .filter(val -> val.equals("Y"))
                .isPresent();
    }


    public Boolean isGestioneEtichettaInventarioBeneAttivo(UserContext userContext) throws ComponentException {
        final Configurazione_cnrBulk configurazione =
                Optional.ofNullable(
                        getConfigurazione(
                                userContext,
                                CNRUserContext.getEsercizio(userContext),
                                ASTERISCO,
                                Configurazione_cnrBulk.PK_INVENTARIO,
                                Configurazione_cnrBulk.SK_GESTIONE_ETICHETTA_BENE)
                ).orElseGet(() -> {
                            try {
                                return getConfigurazione(
                                        userContext,
                                        0,
                                        ASTERISCO,
                                        Configurazione_cnrBulk.PK_INVENTARIO,
                                        Configurazione_cnrBulk.SK_GESTIONE_ETICHETTA_BENE);
                            } catch (ComponentException e) {
                                throw new DetailedRuntimeException(e);
                            }
                        }
                );
        final boolean gestioneEtichettaInventarioAttiva = Optional.ofNullable(configurazione)
                .flatMap(configurazione_cnrBulk -> Optional.ofNullable(configurazione_cnrBulk.getVal01()))
                .filter(val -> val.equals("Y"))
                .isPresent();
        final Optional<String> ruolo = Optional.ofNullable(configurazione.getVal02());
        if (gestioneEtichettaInventarioAttiva && ruolo.isPresent()){
            try {
                return ((RuoloComponentSession) EJBCommonServices.createEJB("CNRUTENZE00_EJB_RuoloComponentSession"))
                        .controlloAbilitazione(userContext, ruolo.get());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        return gestioneEtichettaInventarioAttiva;
    }

    public String getTipoStanziamentoLiquidazioneIva(UserContext userContext) throws ComponentException {
        try {
            return Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
                    .filter(Configurazione_cnrHome.class::isInstance)
                    .map(Configurazione_cnrHome.class::cast)
                    .orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
                    .getLiquidazioneIvaTipoStanziamento();
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Boolean isGestioneBeneDismessoInventarioAttivo(UserContext userContext) throws ComponentException {
        final Configurazione_cnrBulk configurazione =
                Optional.ofNullable(
                        getConfigurazione(
                                userContext,
                                CNRUserContext.getEsercizio(userContext),
                                ASTERISCO,
                                Configurazione_cnrBulk.PK_INVENTARIO,
                                Configurazione_cnrBulk.SK_GESTIONE_BENE_DISMESSO_INVENTARIO)
                ).orElseGet(() -> {
                            try {
                                return getConfigurazione(
                                        userContext,
                                        0,
                                        ASTERISCO,
                                        Configurazione_cnrBulk.PK_INVENTARIO,
                                        Configurazione_cnrBulk.SK_GESTIONE_BENE_DISMESSO_INVENTARIO);
                            } catch (ComponentException e) {
                                throw new DetailedRuntimeException(e);
                            }
                        }
                );
        final boolean gestioneBeneDismessoInventarioAttiva = Optional.ofNullable(configurazione)
                .flatMap(configurazione_cnrBulk -> Optional.ofNullable(configurazione_cnrBulk.getVal01()))
                .filter(val -> val.equals("Y"))
                .isPresent();
        final Optional<String> ruolo = Optional.ofNullable(configurazione.getVal02());
        if (gestioneBeneDismessoInventarioAttiva && ruolo.isPresent()){
            try {
                return ((RuoloComponentSession) EJBCommonServices.createEJB("CNRUTENZE00_EJB_RuoloComponentSession"))
                        .controlloAbilitazione(userContext, ruolo.get());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        return gestioneBeneDismessoInventarioAttiva;
    }
    public Boolean isPagamentoEsteroISSAttivo(UserContext userContext) throws ComponentException {
        final Configurazione_cnrBulk configurazione =
                Optional.ofNullable(
                        getConfigurazione(
                                userContext,
                                CNRUserContext.getEsercizio(userContext),
                                ASTERISCO,
                                Configurazione_cnrBulk.PK_PAGAMENTO_ESTERO,
                                Configurazione_cnrBulk.SK_PAGAMENTO_ESTERO_ISS)
                ).orElseGet(() -> {
                            try {
                                return getConfigurazione(
                                        userContext,
                                        0,
                                        ASTERISCO,
                                        Configurazione_cnrBulk.PK_PAGAMENTO_ESTERO,
                                        Configurazione_cnrBulk.SK_PAGAMENTO_ESTERO_ISS);
                            } catch (ComponentException e) {
                                throw new DetailedRuntimeException(e);
                            }
                        }
                );
        final boolean gestioneModuloPagamentoEsteroIssAttiva = Optional.ofNullable(configurazione)
                .flatMap(configurazione_cnrBulk -> Optional.ofNullable(configurazione_cnrBulk.getVal01()))
                .filter(val -> val.equals("Y"))
                .isPresent();
        final Optional<String> ruolo = Optional.ofNullable(configurazione.getVal02());
        if (gestioneModuloPagamentoEsteroIssAttiva && ruolo.isPresent()){
            try {
                return ((RuoloComponentSession) EJBCommonServices.createEJB("CNRUTENZE00_EJB_RuoloComponentSession"))
                        .controlloAbilitazione(userContext, ruolo.get());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        return gestioneModuloPagamentoEsteroIssAttiva;
    }

    public TipoRapportoTesoreriaEnum getTipoRapportoTesoreria(UserContext userContext) throws ComponentException {
        final Configurazione_cnrBulk configurazione =
                getConfigurazione(
                        userContext,
                        CNRUserContext.getEsercizio(userContext),
                        ASTERISCO,
                        Configurazione_cnrBulk.PK_FLUSSO_ORDINATIVI,
                        Configurazione_cnrBulk.SK_TIPO_RAPPORTO_TESORERIA);
        return TipoRapportoTesoreriaEnum.valueOf(configurazione.getVal01());
    }

    public Boolean isGestioneStatoInizialeSospesiAttivo(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_SOSPESI,
                    Configurazione_cnrBulk.SK_GESTIONE_STATO_INIZIALE,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }
    public Boolean isAttivoInventariaDocumenti(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_INVENTARIO,
                    Configurazione_cnrBulk.SK_GESTIONE_INVENTARIA_DA_DOCUMENTI,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public void eliminaSTEP_FINE_ANNOConBulk(UserContext usercontext, OggettoBulk oggettoBulk) throws ComponentException {
        final BulkHome home = getHome(usercontext, Configurazione_cnrBulk.class);
        final SQLBuilder sqlBuilder = home.createSQLBuilder();
        sqlBuilder.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(usercontext));
        sqlBuilder.addClause(FindClause.AND, "cd_chiave_primaria", SQLBuilder.EQUALS, Configurazione_cnrBulk.PK_STEP_FINE_ANNO);
        try {
            home.fetchAll(sqlBuilder)
                    .stream()
                    .forEach(o -> {
                        OggettoBulk bulk = (OggettoBulk)o;
                        bulk.setToBeDeleted();
                        try {
                            eliminaConBulk(usercontext, bulk);
                        } catch (ComponentException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Boolean isAttivoRegitrazioneFattAnnoPrec(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_FATTURA_PASSIVA,
                    Configurazione_cnrBulk.SK_REGISTAZIONE_ANNO_PREC,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }
    public Boolean isAttivoGestModPagDipendenti(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_DIPENDENTI,
                    Configurazione_cnrBulk.SK_MODALITA_PAGAMENTO,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }
    public Boolean isCheckImpIntrastatFattAttiva(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_FATTURA_ATTIVA,
                    Configurazione_cnrBulk.SK_CHECK_IMP_INTRASTAT,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }
    public Boolean isCheckImpIntrastatFattPassiva(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_FATTURA_PASSIVA,
                    Configurazione_cnrBulk.SK_CHECK_IMP_INTRASTAT,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Boolean isAttivoGestFlIrregistrabile(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_FATTURA_PASSIVA,
                    Configurazione_cnrBulk.SK_GEST_IRREGISTABILE,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }


    public Boolean isLiqIvaAnticipataFattAttiva(UserContext userContext, Timestamp dataFattura) throws ComponentException {
        Date dataInizio;
        Date dataFine;
        boolean isLiqIvaAnticipata;

        try {
            // Controllo dello stato "val01" per FATTURA_ATTIVA e LIQ_IVA_ANTICIPATA
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_FATTURA_ATTIVA,
                    Configurazione_cnrBulk.SK_LIQ_IVA_ANTICIPATA,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            isLiqIvaAnticipata = val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException | ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });

            // Recupero delle date di validità
            dataInizio = getDt01(
                    userContext,
                    CNRUserContext.getEsercizio(userContext),
                    null,
                    Configurazione_cnrBulk.PK_FATTURA_ATTIVA,
                    Configurazione_cnrBulk.SK_LIQ_IVA_ANTICIPATA
            );
            dataFine = getDt02(
                    userContext,
                    CNRUserContext.getEsercizio(userContext),
                    null,
                    Configurazione_cnrBulk.PK_FATTURA_ATTIVA,
                    Configurazione_cnrBulk.SK_LIQ_IVA_ANTICIPATA
            );
        } catch (PersistencyException | ComponentException | EJBException e) {
            throw new it.cnr.jada.comp.ApplicationException(e.getMessage());
        }

        // Verifica delle date di validità
        return isLiqIvaAnticipata && dataFattura != null && dataInizio != null && dataFine != null &&
                (dataFattura.compareTo(dataInizio)>=0 &&  dataFattura.compareTo(dataFine)<=0);
    }

    public Boolean isLiqIvaAnticipataFattPassiva(UserContext userContext, Timestamp dataFattura) throws ComponentException {
        Date dataInizio;
        Date dataFine;
        boolean isLiqIvaAnticipata;

        try {
            // Controllo dello stato "val01" per FATTURA_PASSIVA e LIQ_IVA_ANTICIPATA
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_FATTURA_PASSIVA,
                    Configurazione_cnrBulk.SK_LIQ_IVA_ANTICIPATA,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            isLiqIvaAnticipata = val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException | ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });

            // Recupero delle date di validità
            dataInizio = getDt01(
                    userContext,
                    CNRUserContext.getEsercizio(userContext),
                    null,
                    Configurazione_cnrBulk.PK_FATTURA_PASSIVA,
                    Configurazione_cnrBulk.SK_LIQ_IVA_ANTICIPATA
            );
            dataFine = getDt02(
                    userContext,
                    CNRUserContext.getEsercizio(userContext),
                    null,
                    Configurazione_cnrBulk.PK_FATTURA_PASSIVA,
                    Configurazione_cnrBulk.SK_LIQ_IVA_ANTICIPATA
            );
        } catch (PersistencyException | ComponentException | EJBException e) {
            throw new it.cnr.jada.comp.ApplicationException(e.getMessage());
        }

        // Verifica delle date di validità
        return isLiqIvaAnticipata && dataFattura != null && dataInizio != null && dataFine != null &&
                (dataFattura.compareTo(dataInizio)>=0 &&  dataFattura.compareTo(dataFine)<=0);
    }
    public Timestamp getFineRegFattPass(UserContext userContext, Integer esercizio) throws ComponentException {
        try {
            return Optional.ofNullable(getHome(userContext, Configurazione_cnrBulk.class))
                    .filter(Configurazione_cnrHome.class::isInstance)
                    .map(Configurazione_cnrHome.class::cast)
                    .orElseThrow(() -> new DetailedRuntimeException("Configurazione Home not found"))
                    .getFineRegFattPass(userContext, esercizio);
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }
    public Boolean isAttivoGestFirmatariCont(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_GESTIONE_CONTRATTI,
                    Configurazione_cnrBulk.SK_GEST_FIRMATARIO_CTR,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public Boolean isAttivoLiqFattOrdineCheckInv(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_INVENTARIO,
                    Configurazione_cnrBulk.SK_GEST_LIQ_FATT_ORDINE_CHECK_INV,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }
    public Boolean is1210BonificoEsteroEuro(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_FLUSSO_ORDINATIVI,
                    Configurazione_cnrBulk.SK_BONIFICO_ESTERO_EURO_1210,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }
    public Boolean isEnabledPartGiroInVarizione(UserContext userContext) throws ComponentException {
        try {
            Configurazione_cnrKey configurazioneCnrKey = new Configurazione_cnrKey(
                    Configurazione_cnrBulk.PK_PDG_VARIAZIONE,
                    Configurazione_cnrBulk.SK_VARIAZIONE_FL_PGIRO,
                    ASTERISCO,
                    CNRUserContext.getEsercizio(userContext));
            return val01YesNo(userContext, configurazioneCnrKey)
                    .orElseGet(() -> {
                        try {
                            return val01YesNo(userContext, configurazioneCnrKey.esercizio(0))
                                    .orElse(Boolean.FALSE);
                        } catch (PersistencyException|ComponentException e) {
                            throw new PersistencyError(e);
                        }
                    });
        } catch (PersistencyException e) {
            throw handleException(e);
        }
    }

    public void ribaltaProgetti(UserContext userContext, int esercizio) throws ComponentException {
        try {
            Optional<Configurazione_cnrBulk> confApertura =
                    Optional.ofNullable(
                            getConfigurazione(
                                    userContext,
                                    esercizio,
                                    ASTERISCO,
                                    Configurazione_cnrBulk.PK_STEP_FINE_ANNO,
                                    Configurazione_cnrBulk.StepFineAnno.APERTURA_PREVISIONE.value())
                    );

            confApertura.flatMap(el->Optional.ofNullable(el.getVal02()))
                    .filter(el->el.equals("Y"))
                    .orElseThrow(() -> new ApplicationException("Esercizio "+esercizio+" non aperto. Aggiornamento Progetti non possibile."));

            Optional<Configurazione_cnrBulk> confChiusura =
                    Optional.ofNullable(
                            getConfigurazione(
                                    userContext,
                                    esercizio,
                                    ASTERISCO,
                                    Configurazione_cnrBulk.PK_STEP_FINE_ANNO,
                                    Configurazione_cnrBulk.StepFineAnno.CHIUSURA_DEFINITIVA.value())
                    );

            confChiusura.filter(el->Optional.ofNullable(el.getVal02()).map(el2->!el2.equals("Y")).orElse(Boolean.TRUE))
                    .orElseThrow(() -> new ApplicationException("Esercizio "+esercizio+" chiuso. Aggiornamento Progetti non possibile."));

            confChiusura =
                    Optional.ofNullable(
                            getConfigurazione(
                                    userContext,
                                    esercizio,
                                    ASTERISCO,
                                    Configurazione_cnrBulk.PK_STEP_FINE_ANNO,
                                    Configurazione_cnrBulk.StepFineAnno.CHIUSURA_PROVVISORIA.value())
                    );

            confChiusura.filter(el->Optional.ofNullable(el.getVal02()).map(el2->!el2.equals("Y")).orElse(Boolean.TRUE))
                    .orElseThrow(() -> new ApplicationException("Esercizio "+esercizio+" chiuso in modalità provvisoria. Aggiornamento Progetti non possibile."));

            LoggableStatement cs = new LoggableStatement(getConnection(userContext),
                    "{  call " +
                            it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() +
                            "CNRMIG100.RIBALTA_PROGETTI(?, ?)}",false,this.getClass());
            try {
                cs.setInt( 1, esercizio );
                cs.setString( 2, CNRUserContext.getUser(userContext) );
                cs.execute();
            } finally {
                cs.close();
            }
        } catch (java.sql.SQLException e) {
            throw handleException(e);
        }
    }

}