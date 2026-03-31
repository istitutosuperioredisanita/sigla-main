/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.inventario01.service;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.V_persona_fisicaBulk;
import it.cnr.contab.anagraf00.ejb.TerzoComponentSession;
import it.cnr.contab.config00.ejb.Unita_organizzativaComponentSession;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.ejb.EJBCommonServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;

/**
 * Service per popolare i firmatari nel documento (campi diretti)
 *
 * FIRMATARI OBBLIGATORI:
 * 1. Consegnatario (già presente nel documento)
 * 2. Responsabile struttura (recuperato dalla UO)
 * 3. Dipendente incaricato (solo se ritiro INCARICATO, già presente)
 */
@Service
public class DocTraspRientFirmatariService {

    private static final Logger log = LoggerFactory.getLogger(DocTraspRientFirmatariService.class);

    // Nomi JNDI dei component session
    private static final String TERZO_COMPONENT_SESSION = "CNRANAGRAF00_EJB_TerzoComponentSession";
    private static final String UO_COMPONENT_SESSION = "CNRCONFIG00_EJB_Unita_organizzativaComponentSession";

    /**
     * Popola i campi dei firmatari nel documento.
     *
     * @param documento il documento
     * @param userContext il contesto utente
     * @throws ComponentException se si verifica un errore
     */
    public void popolaFirmatari(Doc_trasporto_rientroBulk documento, CNRUserContext userContext)
            throws ComponentException {

        log.info("Inizio popolazione firmatari per documento - Esercizio: {}, Inventario: {}, Tipo: {}, Progressivo: {}",
                documento.getEsercizio(),
                documento.getPgInventario(),
                documento.getTiDocumento(),
                documento.getPgDocTrasportoRientro());

        try {
            // 1. CONSEGNATARIO - già presente, solo verifica
            if (documento.getConsegnatario() == null || documento.getConsegnatario().getCd_terzo() == null) {
                throw new ApplicationException("Consegnatario non presente nel documento");
            }
            log.info("Consegnatario presente: cd_terzo = {}", documento.getConsegnatario().getCd_terzo());

            // 2. RESPONSABILE DELLA STRUTTURA
            String cdUnitaOrganizzativa = determinaUnitaOrganizzativa(documento, userContext);

            Integer cdTerzoResponsabile = recuperaResponsabileUO(cdUnitaOrganizzativa, userContext);
            if (cdTerzoResponsabile == null) {
                throw new ApplicationException(
                        "Responsabile della struttura non trovato per UO: " + cdUnitaOrganizzativa);
            }

            // Recupera il terzo e la persona fisica del responsabile
            TerzoBulk terzoResponsabile = recuperaTerzo(cdTerzoResponsabile, userContext);
            V_persona_fisicaBulk personaFisicaResponsabile = recuperaPersonaFisica(cdTerzoResponsabile, userContext);

            documento.setCdTerzoResponsabile(cdTerzoResponsabile);
            documento.setTerzoRespDip(terzoResponsabile);
            documento.setPersonaFisicaResponsabile(personaFisicaResponsabile);

            log.info("Responsabile struttura impostato: cd_terzo = {}", cdTerzoResponsabile);

            // 3. DIPENDENTE INCARICATO - solo verifica se ritiro INCARICATO
            if (documento.isRitiroIncaricato()) {
                if (documento.getTerzoIncRitiro() == null ||
                        documento.getTerzoIncRitiro().getCd_terzo() == null) {
                    throw new ApplicationException(
                            "Dipendente incaricato non presente nel documento");
                }
                log.info("Dipendente incaricato presente: cd_terzo = {}",
                        documento.getTerzoIncRitiro().getCd_terzo());
            }

            log.info("Popolazione firmatari completata con successo");

        } catch (ApplicationException e) {
            log.error("Errore nella popolazione dei firmatari", e);
            throw new ComponentException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Errore imprevisto nella popolazione dei firmatari", e);
            throw new ComponentException("Errore nella popolazione dei firmatari: " + e.getMessage(), e);
        }
    }

    /**
     * Determina l'unità organizzativa da usare per il responsabile
     */
    private String determinaUnitaOrganizzativa(
            Doc_trasporto_rientroBulk documento,
            CNRUserContext userContext) throws ApplicationException {

        if (documento.isRitiroIncaricato()) {
            // Usa la UO dell'incaricato
            TerzoBulk anagDipRitiro = documento.getTerzoIncRitiro();
            if (anagDipRitiro == null) {
                throw new ApplicationException("Dipendente incaricato non valorizzato");
            }

            AnagraficoBulk anagrafico = anagDipRitiro.getAnagrafico();
            if (anagrafico == null || anagrafico.getCd_unita_organizzativa() == null) {
                throw new ApplicationException(
                        "Unità organizzativa del dipendente incaricato non trovata");
            }

            return anagrafico.getCd_unita_organizzativa();

        } else {
            // Usa la UO dell'utente dal contesto
            String cdUo = CNRUserContext.getCd_unita_organizzativa(userContext);
            if (cdUo == null) {
                throw new ApplicationException(
                        "Unità organizzativa dell'utente non trovata nel contesto");
            }

            return cdUo;
        }
    }

    /**
     * Recupera il terzo dato un codice terzo
     */
    private TerzoBulk recuperaTerzo(Integer cdTerzo, CNRUserContext userContext)
            throws ComponentException {
        try {
            // Crea il component session
            TerzoComponentSession component = (TerzoComponentSession)
                    EJBCommonServices.createEJB(TERZO_COMPONENT_SESSION, TerzoComponentSession.class);

            // Cerca il terzo
            TerzoBulk terzo = new TerzoBulk(cdTerzo);
            terzo = (TerzoBulk) component.inizializzaBulkPerModifica(userContext, terzo);

            if (terzo == null) {
                throw new ComponentException("Terzo non trovato: " + cdTerzo);
            }

            return terzo;

        } catch (ComponentException | RemoteException e) {
            log.error("Errore nel recupero terzo: {}", cdTerzo, e);
            throw new ComponentException("Errore nel recupero terzo: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera la persona fisica dato un codice terzo
     */
    private V_persona_fisicaBulk recuperaPersonaFisica(Integer cdTerzo, CNRUserContext userContext)
            throws ComponentException {
        try {
            // Crea il component session
            TerzoComponentSession component = (TerzoComponentSession)
                    EJBCommonServices.createEJB(TERZO_COMPONENT_SESSION, TerzoComponentSession.class);

            // Cerca la persona fisica
            V_persona_fisicaBulk personaFisica = new V_persona_fisicaBulk();
            personaFisica.setCd_terzo(cdTerzo);

            personaFisica = (V_persona_fisicaBulk) component.inizializzaBulkPerModifica(
                    userContext,
                    personaFisica
            );

            if (personaFisica == null) {
                throw new ComponentException("Persona fisica non trovata per terzo: " + cdTerzo);
            }

            return personaFisica;

        } catch (ComponentException | RemoteException e) {
            log.error("Errore nel recupero persona fisica per terzo: {}", cdTerzo, e);
            throw new ComponentException("Errore nel recupero persona fisica: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera il responsabile di un'unità organizzativa
     */
    private Integer recuperaResponsabileUO(String cdUnitaOrganizzativa, CNRUserContext userContext)
            throws ComponentException {
        try {
            // Crea il component session
            Unita_organizzativaComponentSession component = (Unita_organizzativaComponentSession)
                    EJBCommonServices.createEJB(UO_COMPONENT_SESSION, Unita_organizzativaComponentSession.class);

            // Cerca la UO
            Unita_organizzativaBulk uo = new Unita_organizzativaBulk();
            uo.setCd_unita_organizzativa(cdUnitaOrganizzativa);

            uo = (Unita_organizzativaBulk) component.inizializzaBulkPerModifica(userContext, uo);

            if (uo == null) {
                log.warn("Unità organizzativa non trovata: {}", cdUnitaOrganizzativa);
                return null;
            }

            Integer cdResponsabile = uo.getCd_responsabile();

            if (cdResponsabile == null) {
                log.warn("Responsabile non definito per UO: {}", cdUnitaOrganizzativa);
            }

            return cdResponsabile;

        } catch (ComponentException | RemoteException e) {
            log.error("Errore nel recupero responsabile UO: {}", cdUnitaOrganizzativa, e);
            throw new ComponentException("Errore nel recupero responsabile UO: " + e.getMessage(), e);
        }
    }
}