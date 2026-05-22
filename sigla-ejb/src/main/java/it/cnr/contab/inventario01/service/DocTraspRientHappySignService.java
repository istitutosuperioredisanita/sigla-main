package it.cnr.contab.inventario01.service;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.dto.StatoHappySignDto;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.iss.si.dto.happysign.base.EnumEsitoFlowDocumentStatus;
import it.iss.si.dto.happysign.base.File;
import it.iss.si.dto.happysign.base.SignersDocumentDetails;
import it.iss.si.dto.happysign.request.GetStatusRequest;
import it.iss.si.dto.happysign.response.GetDocumentDetailResponse;
import it.iss.si.dto.happysign.response.GetDocumentResponse;
import it.iss.si.dto.happysign.response.GetStatusResponse;
import it.iss.si.service.HappySignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class DocTraspRientHappySignService implements HappysignDocService {

    private static final Logger log =
            LoggerFactory.getLogger(DocTraspRientHappySignService.class);

    private final HappySignService happySignService;

    @Value("${flows.templateFirme.1Firma:#{null}}")
    private String template1Firma;

    @Value("${flows.templateFirme.2Firme:#{null}}")
    private String template2Firme;

    @Value("${flows.templateFirme.3Firme:#{null}}")
    private String template3Firme;

    @Value("${flows.templateFirme.4Firme:#{null}}")
    private String template4Firme;

    @Value("${flows.templateFirme.5Firme:#{null}}")
    private String template5Firme;

    @Value("${doc.trasp.rient.happysign.test.enabled:false}")
    private boolean testEnabled;

    @Value("${doc.trasp.rient.happysign.test.signer:#{null}}")
    private String testSigner;

    public DocTraspRientHappySignService(HappySignService happySignService) {
        this.happySignService = happySignService;
    }

    @Override
    public String inviaDocumentoAdHappySign(
            Doc_trasporto_rientroBulk doc,
            byte[] pdfBytes,
            String nomeFile,
            CNRUserContext userContext)
            throws ComponentException {

        try {
            if (doc == null) {
                throw new ApplicationException("Documento T/R non presente.");
            }

            validaPdf(pdfBytes, "PDF da inviare a HappySign");

            if (nomeFile == null || nomeFile.trim().isEmpty()) {
                throw new ApplicationException("Nome file HappySign non valorizzato.");
            }

            List<String> firmatari = getFirmatari(doc);

            if (firmatari == null || firmatari.isEmpty()) {
                throw new ApplicationException("Nessun firmatario trovato per il documento T/R.");
            }

            String templateName = getTemplate(firmatari.size());

            if (templateName == null || templateName.trim().isEmpty()) {
                throw new ApplicationException(
                        "Template HappySign non configurato per "
                                + firmatari.size()
                                + " firmatari."
                );
            }

            File fileToSign = creaFileHappySign(nomeFile, pdfBytes);
            List<String> approvers = new ArrayList<>();

            log.info(
                    "Invio documento T/R a HappySign: doc={}, nomeFile={}, template={}, firmatari={}, testEnabled={}",
                    descriviDoc(doc),
                    nomeFile,
                    templateName,
                    firmatari,
                    testEnabled
            );

            String uuid = happySignService.startFlowToSignSingleDocument(
                    templateName,
                    firmatari,
                    approvers,
                    fileToSign
            );

            if (uuid == null || uuid.trim().isEmpty()) {
                throw new ApplicationException("HappySign non ha restituito UUID.");
            }

            return uuid.trim();

        } catch (ApplicationException e) {
            throw new ComponentException(e.getMessage(), e);

        } catch (ComponentException e) {
            throw e;

        } catch (Exception e) {
            throw new ComponentException(
                    "Errore invio documento a HappySign: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public StatoHappySignDto getStatoFlusso(String uuid) throws Exception {
        if (uuid == null || uuid.trim().isEmpty()) {
            throw new ApplicationException("UUID HappySign non valorizzato.");
        }

        GetDocumentDetailResponse response =
                happySignService.getDocumentDetails(uuid.trim());

        if (response == null) {
            throw new ApplicationException("HappySign non ha restituito il dettaglio documento.");
        }

        if (response.getStatus() != null && response.getStatus() != 0) {
            throw new ApplicationException(
                    "Errore dettaglio documento HappySign: "
                            + firstNotBlank(response.getReason(), response.getDocreason())
            );
        }

        EnumEsitoFlowDocumentStatus statoHappySign =
                EnumEsitoFlowDocumentStatus.esitoForDocStatus(response.getDocstatus());

        if (statoHappySign == null) {
            throw new ApplicationException(
                    "Stato HappySign non riconosciuto: " + response.getDocstatus()
            );
        }

        String statoNormalizzato = normalizzaStato(statoHappySign);

        String motivoRifiuto = null;

        if (StatoHappySignDto.STATO_RIFIUTATO.equals(statoNormalizzato)) {
            motivoRifiuto = firstNotBlank(
                    estraiMotivoRifiutoDaSigners(response.getSigners()),
                    response.getCancelnote(),
                    response.getDocreason(),
                    response.getReason()
            );
        }

        return new StatoHappySignDto(
                statoNormalizzato,
                motivoRifiuto
        );
    }

    private String normalizzaStato(EnumEsitoFlowDocumentStatus statoHappySign)
            throws ApplicationException {

        if (statoHappySign == null) {
            throw new ApplicationException("HappySign non ha restituito lo stato del flusso.");
        }

        return switch (statoHappySign) {
            case TOSIGN -> StatoHappySignDto.STATO_INVIATO;
            case SIGNED -> StatoHappySignDto.STATO_FIRMATO;
            case REFUSED, CANCELED, SEGNAD_AND_CANCELED -> StatoHappySignDto.STATO_RIFIUTATO;
        };
    }

    private String estraiMotivoRifiutoDaSigners(SignersDocumentDetails[] signers) {
        if (signers == null || signers.length == 0) {
            return null;
        }

        for (SignersDocumentDetails signer : signers) {
            if (signer == null) {
                continue;
            }

            String note = signer.getNote();

            if (note != null && !note.trim().isEmpty()) {
                return note.trim();
            }
        }

        return null;
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return null;
    }


    private String firstNotBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }

        if (second != null && !second.trim().isEmpty()) {
            return second.trim();
        }

        return null;
    }

    @Override
    public byte[] getDocumentoFirmato(String uuid) throws Exception {
        if (uuid == null || uuid.trim().isEmpty()) {
            throw new ApplicationException("UUID HappySign non valorizzato.");
        }

        GetDocumentResponse response = happySignService.getDocument(uuid.trim());

        if (response == null) {
            throw new ApplicationException("HappySign non ha restituito il documento firmato.");
        }

        if (response.getStatus() != null && response.getStatus() != 0) {
            throw new ApplicationException(
                    "Errore download documento da HappySign: " + response.getReason()
            );
        }

        byte[] pdfFirmato = response.getDocument();

        validaPdf(pdfFirmato, "PDF firmato ricevuto da HappySign");

        return pdfFirmato;
    }

    private File creaFileHappySign(String nomeFile, byte[] pdfBytes)
            throws ApplicationException {

        if (nomeFile == null || nomeFile.trim().isEmpty()) {
            throw new ApplicationException("Nome file HappySign non valorizzato.");
        }

        validaPdf(pdfBytes, "PDF HappySign");

        File fileToSign = new File();
        fileToSign.setFilename(nomeFile);
        fileToSign.setPdf(pdfBytes);

        return fileToSign;
    }

    private List<String> getFirmatari(Doc_trasporto_rientroBulk doc)
            throws ApplicationException, ComponentException {

        if (doc == null) {
            throw new ApplicationException("Documento T/R non presente.");
        }

        if (testEnabled) {
            List<String> firmatariRealiDaAce = getFirmatariRealiDaAce(doc);

            log.warn(
                    "Modalità TEST HappySign attiva. Firmatari REALI recuperati da ACE che firmerebbero in produzione: {}",
                    firmatariRealiDaAce
            );

            log.warn(
                    "Modalità TEST HappySign attiva. Firmatari inviati realmente a HappySign: [{}, {}]",
                    testSigner,
                    testSigner
            );

            return getFirmatariTest();
        }

        return getFirmatariRealiDaAce(doc);
    }

    private List<String> getFirmatariRealiDaAce(Doc_trasporto_rientroBulk doc)
            throws ApplicationException, ComponentException {

        LinkedHashSet<String> firmatari = new LinkedHashSet<>();

        addEmail(firmatari, doc.getTerzoRespDip(), "Responsabile struttura");
        addEmail(firmatari, doc.getConsegnatario(), "Consegnatario");

        if (doc.isRitiroIncaricato()
                && doc.getTerzoIncRitiro() != null) {
            addEmail(firmatari, doc.getTerzoIncRitiro(), "Incaricato al ritiro");
        }

        return new ArrayList<>(firmatari);
    }

    private void addEmail(
            LinkedHashSet<String> firmatari,
            TerzoBulk terzo,
            String ruolo)
            throws ApplicationException, ComponentException {

        if (terzo == null) {
            throw new ApplicationException("Firmatario non valorizzato: " + ruolo);
        }

        String email = AceEmailLookupServiceFactory
                .get()
                .getEmailByTerzo(terzo);

        if (email == null || email.trim().isEmpty()) {
            throw new ApplicationException(
                    "Email non trovata tramite ACE per il firmatario: " + ruolo
            );
        }

        log.info(
                "Firmatario reale recuperato da ACE: ruolo={}, terzo={}, email={}",
                ruolo,
                terzo.getCd_terzo(),
                email
        );

        firmatari.add(email.trim().toLowerCase());
    }

    private List<String> getFirmatariTest()
            throws ApplicationException {

        if (testSigner == null || testSigner.trim().isEmpty()) {
            throw new ApplicationException(
                    "Modalità test HappySign attiva ma testSigner non configurato."
            );
        }

        List<String> firmatari = new ArrayList<>();

        firmatari.add(testSigner.trim().toLowerCase());
        firmatari.add(testSigner.trim().toLowerCase());

        return firmatari;
    }

    private String getTemplate(int numeroFirmatari) {
        return switch (numeroFirmatari) {
            case 1 -> template1Firma;
            case 2 -> template2Firme;
            case 3 -> template3Firme;
            case 4 -> template4Firme;
            case 5 -> template5Firme;
            default -> null;
        };
    }


    private void validaPdf(byte[] pdfBytes, String label) throws ApplicationException {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new ApplicationException(label + " vuoto.");
        }

        if (pdfBytes.length < 5) {
            throw new ApplicationException(label + " non valido.");
        }

        String header = new String(pdfBytes, 0, 5);

        if (!header.startsWith("%PDF")) {
            throw new ApplicationException(label + " non valido: non inizia con %PDF.");
        }
    }

    private String descriviDoc(Doc_trasporto_rientroBulk doc) {
        if (doc == null) {
            return "null";
        }

        return "esercizio=" + doc.getEsercizio()
                + ", inventario=" + doc.getPgInventario()
                + ", tipo=" + doc.getTiDocumento()
                + ", pg=" + doc.getPgDocTrasportoRientro()
                + ", stato=" + doc.getStato()
                + ", statoFlusso=" + doc.getStatoFlusso()
                + ", uuid=" + doc.getIdFlussoHappysign();
    }

}