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
package it.cnr.contab.inventario01.service;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TelefonoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.ejb.TerzoComponentSession;
import it.cnr.contab.config00.ejb.Unita_organizzativaComponentSession;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.dto.StatoHappySignDto;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.ejb.EJBCommonServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Implementazione HappySign per i documenti Trasporto/Rientro.
 *
 * Questa classe sta nel codice comune e non importa direttamente classi it.iss.si.*.
 * Usa reflection per invocare happysign-client solo se presente nel classpath.
 */
public class DocTraspRientHappySignService implements HappysignDocService {

    private static final Logger log =
            LoggerFactory.getLogger(DocTraspRientHappySignService.class);

    private static final String TERZO_COMPONENT =
            "CNRANAGRAF00_EJB_TerzoComponentSession";

    private static final String UO_COMPONENT =
            "CNRCONFIG00_EJB_Unita_organizzativaComponentSession";

    private static final String CLASS_HAPPYSIGN_SERVICE =
            "it.iss.si.service.HappySignService";

    private static final String CLASS_HAPPYSIGN_FILE =
            "it.iss.si.dto.happysign.base.File";

    @Value("${doc.trasp.rient.happysign.test.enabled:false}")
    private boolean testEnabled;

    @Value("${doc.trasp.rient.happysign.test.signer:}")
    private String testSigner;

    @Value("${flows.templateFirme.1Firma:}")
    private String template1Firma;

    @Value("${flows.templateFirme.2Firme:}")
    private String template2Firme;

    @Value("${flows.templateFirme.3Firme:}")
    private String template3Firma;

    @Value("${flows.templateFirme.4Firme:}")
    private String template4Firme;

    @Value("${flows.templateFirme.5Firme:}")
    private String template5Firme;

    private Object happySignService;

    @Override
    public String inviaDocumentoAdHappySign(
            Doc_trasporto_rientroBulk doc,
            byte[] pdfBytes,
            CNRUserContext userContext)
            throws ComponentException {

        try {
            valida(doc, pdfBytes, userContext);
            caricaResponsabile(doc, userContext);

            List<String> firmatari = firmatari(doc);

            if (testEnabled) {
                firmatari = firmatariTest(firmatari.size());
            }

            firmatari = distinct(firmatari);

            String template = template(firmatari.size());
            Object file = file(doc, pdfBytes);

            log.info(
                    "Invio T/R HappySign - esercizio={}, inventario={}, tipo={}, pg={}, template={}, test={}, firmatari={}",
                    doc.getEsercizio(),
                    doc.getPgInventario(),
                    doc.getTiDocumento(),
                    doc.getPgDocTrasportoRientro(),
                    template,
                    testEnabled,
                    firmatari
            );

            Object service = getHappySignService();

            Method method = service.getClass().getMethod(
                    "startFlowToSignSingleDocument",
                    String.class,
                    List.class,
                    List.class,
                    Class.forName(CLASS_HAPPYSIGN_FILE)
            );

            Object result = method.invoke(
                    service,
                    template,
                    firmatari,
                    Collections.emptyList(),
                    file
            );

            String uuid = result != null ? String.valueOf(result) : null;

            if (blank(uuid)) {
                throw new ApplicationException("HappySign non ha restituito UUID documento.");
            }

            return uuid.trim();

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Errore invio documento T/R ad HappySign", e);
            throw new ComponentException(
                    "Errore invio documento ad HappySign: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public StatoHappySignDto getStatoFlusso(String uuid) throws Exception {
        if (blank(uuid)) {
            throw new IllegalArgumentException("UUID HappySign non presente");
        }

        Object service = getHappySignService();

        Method method = service.getClass().getMethod(
                "getDocumentStatus",
                String.class
        );

        Object stato = method.invoke(service, uuid);

        if (stato == null) {
            return new StatoHappySignDto(StatoHappySignDto.STATO_INVIATO, null);
        }

        String statoRaw = String.valueOf(stato);

        if ("SIGNED".equalsIgnoreCase(statoRaw)) {
            return new StatoHappySignDto(StatoHappySignDto.STATO_FIRMATO, null);
        }

        if ("REFUSED".equalsIgnoreCase(statoRaw)) {
            return new StatoHappySignDto(
                    StatoHappySignDto.STATO_RIFIUTATO,
                    "Documento rifiutato su HappySign"
            );
        }

        return new StatoHappySignDto(StatoHappySignDto.STATO_INVIATO, null);
    }

    @Override
    public byte[] getDocumentoFirmato(String uuid) throws Exception {
        if (blank(uuid)) {
            throw new IllegalArgumentException("UUID HappySign non presente");
        }

        Object service = getHappySignService();

        Method method = service.getClass().getMethod(
                "getDocument",
                String.class
        );

        Object response = method.invoke(service, uuid);

        if (response == null) {
            throw new IllegalStateException("Risposta HappySign getDocument nulla");
        }

        Integer status = (Integer) invokeGetter(response, "getStatus");

        if (status == null || status.intValue() != 0) {
            Object reason = invokeGetter(response, "getReason");
            throw new IllegalStateException(
                    "Errore HappySign getDocument: " + reason
            );
        }

        byte[] document = (byte[]) invokeGetter(response, "getDocument");

        if (document == null || document.length == 0) {
            throw new IllegalStateException(
                    "Documento firmato non presente nella risposta HappySign"
            );
        }

        return document;
    }

    private Object getHappySignService() throws ComponentException {
        if (happySignService != null) {
            return happySignService;
        }

        try {
            Class<?> clazz = Class.forName(CLASS_HAPPYSIGN_SERVICE);
            happySignService = clazz.getDeclaredConstructor().newInstance();
            return happySignService;
        } catch (ClassNotFoundException e) {
            throw new ComponentException(
                    "happysign-client non presente nel classpath. " +
                            "Per usare HappySign compilare e avviare con profilo iss includendo happysign-client nel WAR.",
                    e
            );
        } catch (Exception e) {
            throw new ComponentException(
                    "Errore inizializzazione HappySignService: " + e.getMessage(),
                    e
            );
        }
    }

    private Object file(
            Doc_trasporto_rientroBulk doc,
            byte[] pdf)
            throws Exception {

        Class<?> fileClass = Class.forName(CLASS_HAPPYSIGN_FILE);
        Object file = fileClass.getDeclaredConstructor().newInstance();

        invokeSetter(file, "setFilename", String.class, nomeFile(doc));
        invokeSetter(file, "setPdf", byte[].class, pdf);

        return file;
    }

    private Object invokeGetter(Object target, String methodName)
            throws Exception {

        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    private void invokeSetter(
            Object target,
            String methodName,
            Class<?> parameterType,
            Object value)
            throws Exception {

        Method method = target.getClass().getMethod(methodName, parameterType);
        method.invoke(target, value);
    }

    private void valida(
            Doc_trasporto_rientroBulk doc,
            byte[] pdf,
            CNRUserContext userContext)
            throws ApplicationException {

        if (doc == null) {
            throw new ApplicationException("Documento non presente");
        }

        if (userContext == null) {
            throw new ApplicationException("Contesto utente non presente");
        }

        if (doc.getPgDocTrasportoRientro() == null) {
            throw new ApplicationException("Documento non ancora salvato");
        }

        if (pdf == null || pdf.length == 0) {
            throw new ApplicationException("PDF documento non generato");
        }

        if (doc.getConsegnatario() == null
                || doc.getConsegnatario().getCd_terzo() == null) {
            throw new ApplicationException("Consegnatario non presente");
        }

        if (doc.isRitiroIncaricato()
                && (doc.getTerzoIncRitiro() == null
                || doc.getTerzoIncRitiro().getCd_terzo() == null)) {
            throw new ApplicationException("Dipendente incaricato non presente");
        }

        if (testEnabled && blank(testSigner)) {
            throw new ApplicationException(
                    "Firmatario di test non configurato: doc.trasp.rient.happysign.test.signer"
            );
        }
    }

    private void caricaResponsabile(
            Doc_trasporto_rientroBulk doc,
            CNRUserContext userContext)
            throws ComponentException {

        try {
            String cdUo = cdUoResponsabile(doc, userContext);
            Integer cdResponsabile = cdResponsabileUo(cdUo, userContext);

            if (cdResponsabile == null) {
                throw new ApplicationException(
                        "Responsabile della struttura non trovato per UO: " + cdUo
                );
            }

            TerzoBulk responsabile = terzo(cdResponsabile, userContext);

            doc.setCdTerzoResponsabile(cdResponsabile);
            doc.setTerzoRespDip(responsabile);

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);
        }
    }

    private String cdUoResponsabile(
            Doc_trasporto_rientroBulk doc,
            CNRUserContext userContext)
            throws ApplicationException {

        if (!doc.isRitiroIncaricato()) {
            String cdUo = CNRUserContext.getCd_unita_organizzativa(userContext);

            if (blank(cdUo)) {
                throw new ApplicationException(
                        "Unità organizzativa dell'utente non trovata nel contesto"
                );
            }

            return cdUo;
        }

        TerzoBulk incaricato = doc.getTerzoIncRitiro();
        AnagraficoBulk anagrafico =
                incaricato != null ? incaricato.getAnagrafico() : null;

        if (anagrafico == null || blank(anagrafico.getCd_unita_organizzativa())) {
            throw new ApplicationException(
                    "Unità organizzativa del dipendente incaricato non trovata"
            );
        }

        return anagrafico.getCd_unita_organizzativa();
    }

    private List<String> firmatari(Doc_trasporto_rientroBulk doc)
            throws ApplicationException {

        List<String> lista = new ArrayList<String>();

        lista.add(email("CONSEGNATARIO", doc.getConsegnatario()));
        lista.add(email("RESPONSABILE_STRUTTURA", doc.getTerzoRespDip()));

        if (doc.isRitiroIncaricato()) {
            lista.add(email("DIPENDENTE_INCARICATO", doc.getTerzoIncRitiro()));
        }

        return lista;
    }

    private String email(String ruolo, TerzoBulk terzo)
            throws ApplicationException {

        String email = emailTerzo(terzo);

        if (blank(email)) {
            throw new ApplicationException(
                    "Email firmatario non trovata per ruolo: " + ruolo
            );
        }

        return email.trim();
    }

    private String emailTerzo(TerzoBulk terzo) {
        if (terzo == null) {
            return null;
        }

        String email = primoRiferimento(terzo.getEmail());
        return !blank(email) ? email : primoRiferimento(terzo.getPec());
    }

    private String primoRiferimento(List lista) {
        if (lista == null || lista.isEmpty()) {
            return null;
        }

        Object value = lista.get(0);

        if (value instanceof TelefonoBulk) {
            return ((TelefonoBulk) value).getRiferimento();
        }

        return value != null ? String.valueOf(value) : null;
    }

    private List<String> firmatariTest(int numero) {
        List<String> lista = new ArrayList<String>();

        for (int i = 0; i < numero; i++) {
            lista.add(testSigner.trim());
        }

        log.warn(
                "Modalità test HappySign attiva: {} firmatari sostituiti con {}",
                numero,
                testSigner
        );

        return lista;
    }

    private String template(int numeroFirmatari)
            throws ApplicationException {

        String template;

        switch (numeroFirmatari) {
            case 1:
                template = template1Firma;
                break;
            case 2:
                template = template2Firme;
                break;
            case 3:
                template = template3Firma;
                break;
            case 4:
                template = template4Firme;
                break;
            case 5:
                template = template5Firme;
                break;
            default:
                throw new ApplicationException(
                        "Numero firmatari non gestito per HappySign: " + numeroFirmatari
                );
        }

        if (blank(template)) {
            throw new ApplicationException(
                    "Template HappySign non configurato per numero firmatari: "
                            + numeroFirmatari
            );
        }

        return template.trim();
    }

    private String nomeFile(Doc_trasporto_rientroBulk doc) {
        return (doc.isTrasporto() ? "Trasporto" : "Rientro")
                + "_"
                + doc.getEsercizio()
                + "_"
                + doc.getPgInventario()
                + "_"
                + doc.getPgDocTrasportoRientro()
                + ".pdf";
    }

    private TerzoBulk terzo(
            Integer cdTerzo,
            CNRUserContext userContext)
            throws ComponentException {

        try {
            TerzoComponentSession component =
                    (TerzoComponentSession) EJBCommonServices.createEJB(
                            TERZO_COMPONENT,
                            TerzoComponentSession.class
                    );

            TerzoBulk terzo = new TerzoBulk(cdTerzo);

            terzo = (TerzoBulk) component.inizializzaBulkPerModifica(
                    userContext,
                    terzo
            );

            if (terzo == null) {
                throw new ComponentException("Terzo non trovato: " + cdTerzo);
            }

            return terzo;

        } catch (ComponentException | RemoteException e) {
            throw new ComponentException(
                    "Errore nel recupero terzo: " + e.getMessage(),
                    e
            );
        }
    }

    private Integer cdResponsabileUo(
            String cdUo,
            CNRUserContext userContext)
            throws ComponentException {

        try {
            Unita_organizzativaComponentSession component =
                    (Unita_organizzativaComponentSession) EJBCommonServices.createEJB(
                            UO_COMPONENT,
                            Unita_organizzativaComponentSession.class
                    );

            Unita_organizzativaBulk uo =
                    new Unita_organizzativaBulk(cdUo);

            uo = (Unita_organizzativaBulk) component.inizializzaBulkPerModifica(
                    userContext,
                    uo
            );

            return uo != null ? uo.getCd_responsabile() : null;

        } catch (ComponentException | RemoteException e) {
            throw new ComponentException(
                    "Errore nel recupero responsabile UO: " + e.getMessage(),
                    e
            );
        }
    }

    private List<String> distinct(List<String> lista) {
        return new ArrayList<String>(new LinkedHashSet<String>(lista));
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}