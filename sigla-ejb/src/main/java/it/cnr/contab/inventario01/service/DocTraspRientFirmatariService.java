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
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private DocTrasportoRientroComponentSession docTrasportoRientroComponent;

    /**
     * Popola i campi dei firmatari nel documento.
     *
     * @param documento il documento
     * @param userContext il contesto utente
     * @throws ComponentException se si verifica un errore
     */
    public void popolaFirmatari(Doc_trasporto_rientroBulk documento, UserContext userContext)
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
            documento.setTerzoResponsabile(terzoResponsabile);
            documento.setPersonaFisicaResponsabile(personaFisicaResponsabile);

            log.info("Responsabile struttura impostato: cd_terzo = {}", cdTerzoResponsabile);

            // 3. DIPENDENTE INCARICATO - solo verifica se ritiro INCARICATO
            if (documento.isRitiroIncaricato()) {
                if (documento.getAnagDipRitiro() == null ||
                        documento.getAnagDipRitiro().getCd_terzo() == null) {
                    throw new ApplicationException(
                            "Dipendente incaricato non presente nel documento");
                }
                log.info("Dipendente incaricato presente: cd_terzo = {}",
                        documento.getAnagDipRitiro().getCd_terzo());
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
            UserContext userContext) throws ApplicationException {

        if (documento.isRitiroIncaricato()) {
            // Usa la UO dell'incaricato
            TerzoBulk anagDipRitiro = documento.getAnagDipRitiro();
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
            String cdUo = it.cnr.contab.utenze00.bp.CNRUserContext.getCd_unita_organizzativa(userContext);
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
    private TerzoBulk recuperaTerzo(Integer cdTerzo, UserContext userContext)
            throws ComponentException {
        //TODO da decommentare

//        try {
//            return docTrasportoRientroComponent.recuperaTerzo(userContext, cdTerzo);
//        } catch (ComponentException | RemoteException e) {
//            throw new ComponentException("Errore nel recupero terzo: " + e.getMessage(), e);
//        }
        return null;
    }

    /**
     * Recupera la persona fisica dato un codice terzo
     */
    private V_persona_fisicaBulk recuperaPersonaFisica(Integer cdTerzo, UserContext userContext)
            throws ComponentException {
        //TODO da decommentare

//        try {
//            return docTrasportoRientroComponent.recuperaPersonaFisica(userContext, cdTerzo);
//        } catch (ComponentException | RemoteException e) {
//            throw new ComponentException("Errore nel recupero persona fisica: " + e.getMessage(), e);
//        }
        return null;
    }

    /**
     * Recupera il responsabile di un'unità organizzativa
     */
    private Integer recuperaResponsabileUO(String cdUnitaOrganizzativa, UserContext userContext)
            throws ComponentException {
        //TODO da decommentare

//        try {
//            Unita_organizzativaBulk uo = docTrasportoRientroComponent.recuperaUnitaOrganizzativa(
//                    userContext, cdUnitaOrganizzativa);
//
//            if (uo == null) {
//                return null;
//            }
//
//            return uo.getCd_responsabile();
//
//        } catch (ComponentException | RemoteException e) {
//            throw new ComponentException("Errore nel recupero responsabile UO: " + e.getMessage(), e);
//        }
        return 0;
    }
}