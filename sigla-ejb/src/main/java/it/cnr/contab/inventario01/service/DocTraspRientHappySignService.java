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
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.jada.comp.ComponentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
/*
    private static final Logger log = LoggerFactory.getLogger(DocTraspRientHappySignService.class);

    @Value("${flows.templateFirme.1firma:#{null}}")
    private String happysignTemplate;

    @Autowired(required = false)
    private HappySignService happySignService;

    public String inviaDocumentoAdHappySign(
            Doc_trasporto_rientroBulk documento,
            byte[] pdfBytes) throws ComponentException {

        log.info("Invio documento a HappySign - Doc: {}/{}/{}/{}",
                documento.getEsercizio(),
                documento.getPgInventario(),
                documento.getTiDocumento(),
                documento.getPgDocTrasportoRientro());

        try {
            validaFirmatari(documento);

            // Prepara PDF
            it.iss.si.dto.happysign.base.File pdfFile = new it.iss.si.dto.happysign.base.File();
            pdfFile.setFilename(costruisciNomeDocumento(documento));
            pdfFile.setPdf(pdfBytes);

            // Crea lista firmatari
            List<Signer> signers = creaListaFirmatari(documento);

            // ✅ Estrai email con stream
            List<String> emailFirmatari = signers.stream()
                    .map(Signer::getEmail)
                    .collect(Collectors.toList());

            log.info("Invio a HappySign - Template: {}, Firmatari: {}",
                    happysignTemplate, emailFirmatari);

            // ✅ Invia con lista email
            String uuid = happySignService.startFlowToSignSingleDocument(
                    happysignTemplate,
                    emailFirmatari,      // List<String>
                    new ArrayList<>(),   // Approvatori (vuoto)
                    pdfFile
            );

            if (uuid == null || uuid.trim().isEmpty()) {
                throw new ComponentException("UUID non ricevuto da HappySign");
            }

            log.info("Documento inviato con successo - UUID: {}", uuid);
            return uuid;

        } catch (ComponentException e) {
            log.error("Errore invio documento a HappySign", e);
            throw e;
        } catch (Exception e) {
            log.error("Errore imprevisto invio documento a HappySign", e);
            throw new ComponentException("Errore invio a HappySign: " + e.getMessage(), e);
        }
    }
   //Valida che ci siano tutti i firmatari necessari

    private void validaFirmatari(Doc_trasporto_rientroBulk documento) throws ComponentException {

        // ==================== VERIFICA CONSEGNATARIO ====================
        if (documento.getConsegnatario() == null) {
            throw new ComponentException("Consegnatario non presente nel documento");
        }

        if (documento.getConsegnatario().getCd_terzo() == null) {
            throw new ComponentException("Codice terzo consegnatario non valorizzato");
        }

        AnagraficoBulk anagConsegnatario = documento.getConsegnatario().getAnagrafico();
        if (anagConsegnatario == null) {
            throw new ComponentException("Anagrafica consegnatario non disponibile");
        }

        validaDatiAnagrafica(anagConsegnatario, "Consegnatario");

        // ==================== VERIFICA RESPONSABILE ====================
        if (documento.getCdTerzoResponsabile() == null) {
            throw new ComponentException("Responsabile struttura non configurato");
        }

        if (documento.getPersonaFisicaResponsabile() == null) {
            throw new ComponentException("Dati persona fisica responsabile non disponibili");
        }

        AnagraficoBulk anagResponsabile = documento.getPersonaFisicaResponsabile().getAnagrafico();
        if (anagResponsabile == null) {
            throw new ComponentException("Anagrafica responsabile non disponibile");
        }

        validaDatiAnagrafica(anagResponsabile, "Responsabile");

        // ==================== VERIFICA INCARICATO (se necessario) ====================
        if (documento.isRitiroIncaricato()) {
            if (documento.getTerzoIncRitiro() == null) {
                throw new ComponentException("Dipendente incaricato non presente nel documento");
            }

            if (documento.getTerzoIncRitiro().getCd_terzo() == null) {
                throw new ComponentException("Codice terzo dipendente incaricato non valorizzato");
            }

            AnagraficoBulk anagIncaricato = documento.getTerzoIncRitiro().getAnagrafico();
            if (anagIncaricato == null) {
                throw new ComponentException("Anagrafica dipendente incaricato non disponibile");
            }

            validaDatiAnagrafica(anagIncaricato, "Dipendente incaricato");
        }
    }

    // Valida i dati anagrafici necessari per la firma

    private void validaDatiAnagrafica(AnagraficoBulk anagrafica, String ruolo) throws ComponentException {
        if (anagrafica.getNome() == null || anagrafica.getNome().trim().isEmpty()) {
            throw new ComponentException("Nome non disponibile per " + ruolo);
        }

        if (anagrafica.getCognome() == null || anagrafica.getCognome().trim().isEmpty()) {
            throw new ComponentException("Cognome non disponibile per " + ruolo);
        }

        if (anagrafica.getCodice_fiscale() == null || anagrafica.getCodice_fiscale().trim().isEmpty()) {
            throw new ComponentException("Codice fiscale non disponibile per " + ruolo);
        }
    }

    //Crea la lista dei firmatari per HappySign
         private List<Signer> creaListaFirmatari(Doc_trasporto_rientroBulk documento) throws ComponentException {
        List<Signer> signers = new ArrayList<>();
        int ordine = 1;

        // ==================== 1. CONSEGNATARIO ====================
        AnagraficoBulk anagConsegnatario = documento.getConsegnatario().getAnagrafico();

        String emailConsegnatario = costruisciEmail(
                anagConsegnatario.getNome(),
                anagConsegnatario.getCognome()
        );

        Signer signerConsegnatario = creaSigner(
                anagConsegnatario.getNome(),
                anagConsegnatario.getCognome(),
                emailConsegnatario,
                anagConsegnatario.getCodice_fiscale(),
                ordine++
        );
        signers.add(signerConsegnatario);

        log.debug("Firmatario #{}: Consegnatario - {} {} ({})",
                ordine - 1,
                signerConsegnatario.getSurname(),
                signerConsegnatario.getName(),
                emailConsegnatario);

        // ==================== 2. RESPONSABILE STRUTTURA ====================
        AnagraficoBulk anagResponsabile = documento.getPersonaFisicaResponsabile().getAnagrafico();

        String emailResponsabile = costruisciEmail(
                anagResponsabile.getNome(),
                anagResponsabile.getCognome()
        );

        Signer signerResponsabile = creaSigner(
                anagResponsabile.getNome(),
                anagResponsabile.getCognome(),
                emailResponsabile,
                anagResponsabile.getCodice_fiscale(),
                ordine++
        );
        signers.add(signerResponsabile);

        log.debug("Firmatario #{}: Responsabile - {} {} ({})",
                ordine - 1,
                signerResponsabile.getSurname(),
                signerResponsabile.getName(),
                emailResponsabile);

        // ==================== 3. DIPENDENTE INCARICATO (opzionale) ====================
        if (documento.isRitiroIncaricato()) {
            AnagraficoBulk anagIncaricato = documento.getTerzoIncRitiro().getAnagrafico();

            String emailIncaricato = costruisciEmail(
                    anagIncaricato.getNome(),
                    anagIncaricato.getCognome()
            );

            Signer signerIncaricato = creaSigner(
                    anagIncaricato.getNome(),
                    anagIncaricato.getCognome(),
                    emailIncaricato,
                    anagIncaricato.getCodice_fiscale(),
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

    //
    // Costruisce l'email nel formato nome.cognome@iss.it
    //* Tutto in minuscolo, rimuove spazi e caratteri speciali
    // *
    // * @param nome Nome della persona
    // * @param cognome Cognome della persona
    // * @return Email nel formato nome.cognome@iss.it
    //
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

    //
    // Pulisce una stringa per l'uso nell'email usando normalizzazione Unicode
    //
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

    // Crea un oggetto Signer per HappySign

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

    // Costruisce il nome del documento per HappySign

    private String costruisciNomeDocumento(Doc_trasporto_rientroBulk documento) {
        StringBuilder nome = new StringBuilder();

        // Tipo documento
        if (Doc_trasporto_rientroBulk.TRASPORTO.equals(documento.getTiDocumento())) {
            nome.append("Trasporto");
        } else {
            nome.append("Rientro");
        }

        // Dati identificativi
        nome.append("_");
        nome.append(documento.getEsercizio());
        nome.append("_");
        nome.append(documento.getPgInventario());
        nome.append("_");
        nome.append(documento.getPgDocTrasportoRientro());
        nome.append(".pdf");

        return nome.toString();
    }
    */
}

