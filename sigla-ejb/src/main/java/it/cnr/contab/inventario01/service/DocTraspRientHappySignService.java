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

import it.cnr.contab.anagraf00.core.bulk.V_persona_fisicaBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.jada.comp.ComponentException;
import it.iss.si.dto.happysign.base.Signer;
import it.iss.si.dto.happysign.request.UploadToComplexRequest;
import it.iss.si.dto.happysign.response.UploadToComplexResponse;
import it.iss.si.service.HappySign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Service per l'invio dei documenti a HappySign per la firma digitale
 *
 * FIRMATARI OBBLIGATORI (minimo 2):
 * 1. Consegnatario (sempre)
 * 2. Responsabile struttura (sempre)
 * 3. Dipendente incaricato (solo se ritiro INCARICATO)
 */
@Service
public class DocTraspRientHappySignService {

    private static final Logger log = LoggerFactory.getLogger(DocTraspRientHappySignService.class);

    @Value("${flows.templateFirme.docTrasportoRientro:Missioni_doppia_firma}")
    private String happysignTemplate;

    @Autowired
    private HappySign happySignClient;

    /**
     * Invia un documento a HappySign per la firma digitale
     *
     * @param documento il documento da firmare
     * @param pdfBytes il contenuto del PDF da firmare
     * @return l'UUID del documento su HappySign
     * @throws ComponentException se si verifica un errore
     */
    public String inviaDocumentoAdHappySign(
            Doc_trasporto_rientroBulk documento,
            byte[] pdfBytes) throws ComponentException {

        log.info("Invio documento a HappySign - Esercizio: {}, Inventario: {}, Tipo: {}, Progressivo: {}",
                documento.getEsercizio(),
                documento.getPgInventario(),
                documento.getTiDocumento(),
                documento.getPgDocTrasportoRientro());

        try {
            // Validazione firmatari
            validaFirmatari(documento);

            int numeroFirmatari = documento.getNumeroFirmatariRichiesti();
            log.info("Firmatari configurati: {}", numeroFirmatari);

            // Prepara la richiesta per HappySign
            UploadToComplexRequest request = new UploadToComplexRequest();
            request.setNametemplate(happysignTemplate);

            // Aggiungi il PDF
            it.iss.si.dto.happysign.base.File pdfFile = new it.iss.si.dto.happysign.base.File();
            pdfFile.setFilename(costruisciNomeDocumento(documento));
            pdfFile.setPdf(pdfBytes);
            request.addPdf(pdfFile);

            // Aggiungi i firmatari
            List<Signer> signers = creaListaFirmatari(documento);
            for (Signer signer : signers) {
                request.addSigner(signer);
            }

            // Invia a HappySign
            UploadToComplexResponse response = happySignClient.uploadToComplexTemplate(request);

            if (response != null && response.getListiddocument() != null &&
                    !response.getListiddocument().isEmpty()) {

                String uuid = response.getListiddocument().get(0).getUuid();
                log.info("Documento inviato con successo a HappySign - UUID: {}", uuid);
                return uuid;

            } else {
                throw new ComponentException("Risposta non valida da HappySign");
            }

        } catch (ComponentException e) {
            log.error("Errore durante l'invio del documento a HappySign", e);
            throw e;
        } catch (Exception e) {
            log.error("Errore imprevisto durante l'invio del documento a HappySign", e);
            throw new ComponentException("Errore durante l'invio a HappySign: " + e.getMessage(), e);
        }
    }

    /**
     * Valida che ci siano tutti i firmatari necessari
     */
    private void validaFirmatari(Doc_trasporto_rientroBulk documento) throws ComponentException {
        // Verifica consegnatario
        if (documento.getConsegnatario() == null || documento.getConsegnatario().getCd_terzo() == null) {
            throw new ComponentException("Consegnatario non configurato");
        }

        // Verifica responsabile
        if (documento.getCdTerzoResponsabile() == null) {
            throw new ComponentException("Responsabile struttura non configurato");
        }

        // Se ritiro incaricato, verifica incaricato
        if (documento.isRitiroIncaricato()) {
            if (documento.getAnagDipRitiro() == null || documento.getAnagDipRitiro().getCd_terzo() == null) {
                throw new ComponentException("Dipendente incaricato non configurato");
            }
        }
    }

    /**
     * Crea la lista dei firmatari per HappySign
     */
    private List<Signer> creaListaFirmatari(Doc_trasporto_rientroBulk documento) throws ComponentException {
        List<Signer> signers = new ArrayList<>();
        int ordine = 1;

        // 1. CONSEGNATARIO
        String emailConsegnatario = costruisciEmail(
                documento.getConsegnatario().getAnagrafico().getNome(),
                documento.getConsegnatario().getAnagrafico().getCognome()
        );

        Signer signerConsegnatario = creaSigner(
                documento.getConsegnatario().getAnagrafico().getNome(),
                documento.getConsegnatario().getAnagrafico().getCognome(),
                emailConsegnatario,
                documento.getConsegnatario().getAnagrafico().getCodice_fiscale(),
                ordine++
        );
        signers.add(signerConsegnatario);
        log.debug("Firmatario #{}: Consegnatario - {} {} ({})",
                ordine - 1,
                signerConsegnatario.getSurname(),
                signerConsegnatario.getName(),
                emailConsegnatario);

        // 2. RESPONSABILE STRUTTURA
        V_persona_fisicaBulk personaResponsabile = documento.getPersonaFisicaResponsabile();
        if (personaResponsabile == null || personaResponsabile.getAnagrafico() == null) {
            throw new ComponentException("Dati persona fisica responsabile non disponibili");
        }

        String emailResponsabile = costruisciEmail(
                personaResponsabile.getAnagrafico().getNome(),
                personaResponsabile.getAnagrafico().getCognome()
        );

        Signer signerResponsabile = creaSigner(
                personaResponsabile.getAnagrafico().getNome(),
                personaResponsabile.getAnagrafico().getCognome(),
                emailResponsabile,
                personaResponsabile.getAnagrafico().getCodice_fiscale(),
                ordine++
        );
        signers.add(signerResponsabile);
        log.debug("Firmatario #{}: Responsabile - {} {} ({})",
                ordine - 1,
                signerResponsabile.getSurname(),
                signerResponsabile.getName(),
                emailResponsabile);

        // 3. DIPENDENTE INCARICATO (solo se ritiro INCARICATO)
        if (documento.isRitiroIncaricato()) {
            String emailIncaricato = costruisciEmail(
                    documento.getAnagDipRitiro().getAnagrafico().getNome(),
                    documento.getAnagDipRitiro().getAnagrafico().getCognome()
            );

            Signer signerIncaricato = creaSigner(
                    documento.getAnagDipRitiro().getAnagrafico().getNome(),
                    documento.getAnagDipRitiro().getAnagrafico().getCognome(),
                    emailIncaricato,
                    documento.getAnagDipRitiro().getAnagrafico().getCodice_fiscale(),
                    ordine++
            );
            signers.add(signerIncaricato);
            log.debug("Firmatario #{}: Dipendente incaricato - {} {} ({})",
                    ordine - 1,
                    signerIncaricato.getSurname(),
                    signerIncaricato.getName(),
                    emailIncaricato);
        }

        return signers;
    }

    /**
     * Costruisce l'email nel formato nome.cognome@iss.it
     * Tutto in minuscolo, rimuove spazi e caratteri speciali
     *
     * @param nome Nome della persona
     * @param cognome Cognome della persona
     * @return Email nel formato nome.cognome@iss.it
     */
    private String costruisciEmail(String nome, String cognome) throws ComponentException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ComponentException("Nome non disponibile per costruire l'email");
        }
        if (cognome == null || cognome.trim().isEmpty()) {
            throw new ComponentException("Cognome non disponibile per costruire l'email");
        }

        // Pulisce e normalizza nome e cognome
        String nomePulito = pulisciStringa(nome);
        String cognomePulito = pulisciStringa(cognome);

        // Costruisce l'email
        String email = nomePulito + "." + cognomePulito + "@iss.it";

        log.debug("Email costruita: {} (da: {} {})", email, nome, cognome);

        return email;
    }

    /**
     * Pulisce una stringa per l'uso nell'email usando normalizzazione Unicode
     */
    private String pulisciStringa(String stringa) {
        if (stringa == null || stringa.trim().isEmpty()) {
            return "";
        }

        // Converte in minuscolo
        String risultato = stringa.toLowerCase().trim();
        // Rimuove spazi e trattini
        risultato = risultato.replace(" ", "").replace("-", "");
        // Normalizza caratteri Unicode (rimuove accenti)
        risultato = Normalizer.normalize(risultato, Normalizer.Form.NFD);
        risultato = risultato.replaceAll("\\p{M}", ""); // Rimuove i segni diacritici
        // Rimuove tutti i caratteri non alfanumerici
        risultato = risultato.replaceAll("[^a-z0-9]", "");
        return risultato;
    }

    /**
     * Crea un oggetto Signer per HappySign
     */
    private Signer creaSigner(String nome, String cognome, String email,
                              String codiceFiscale, int ordine) {
        Signer signer = new Signer();
        signer.setName(nome);
        signer.setSurname(cognome);
        signer.setEmail(email);
        signer.setTaxcode(codiceFiscale);
        signer.setOrder(ordine);
        return signer;
    }

    /**
     * Costruisce il nome del documento per HappySign
     */
    private String costruisciNomeDocumento(Doc_trasporto_rientroBulk documento) {
        StringBuilder nome = new StringBuilder();

        if (Doc_trasporto_rientroBulk.TRASPORTO.equals(documento.getTiDocumento())) {
            nome.append("Trasporto");
        } else {
            nome.append("Rientro");
        }

        nome.append("_");
        nome.append(documento.getEsercizio());
        nome.append("_");
        nome.append(documento.getPgInventario());
        nome.append("_");
        nome.append(documento.getPgDocTrasportoRientro());
        nome.append(".pdf");

        return nome.toString();
    }
}
