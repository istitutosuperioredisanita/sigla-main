package it.cnr.contab.inventario01.util;

import it.cnr.contab.inventario01.bulk.AllegatoDocTraspRientroBulk;
import it.cnr.contab.inventario01.bulk.AllegatoDocumentoRientroBulk;
import it.cnr.contab.inventario01.bulk.AllegatoDocumentoTrasportoBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.reports.bulk.Print_spoolerBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.comp.ApplicationException;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.RIENTRO;
import static it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk.TRASPORTO;

/** Funzioni di supporto senza stato per i documenti di Trasporto/Rientro. */
public final class DocTraspRientUtil {

    public static final String REPORT_DOC_TRASPORTO_RIENTRO = "/cnrdocamm/docamm/doc_trasporto_rientro.jasper";

    private DocTraspRientUtil() {
    }

    // ---------------------------------------------------------------- AMBIENTE

    /** True se tra i profili Spring attivi c'è "iss". */
    public static boolean isProfiloIssAttivo() {
        WebApplicationContext springCtx = ContextLoader.getCurrentWebApplicationContext();

        return springCtx != null
                && springCtx.getEnvironment() != null
                && Arrays.stream(springCtx.getEnvironment().getActiveProfiles())
                .filter(Objects::nonNull)
                .map(String::trim)
                .anyMatch("iss"::equalsIgnoreCase);
    }

    // ---------------------------------------------------------------- STAMPA

    /** Richiesta di stampa del report Jasper del documento T/R. */
    public static Print_spoolerBulk creaStampa(Doc_trasporto_rientroBulk doc, String nomeFile, String utente) {
        Print_spoolerBulk print = new Print_spoolerBulk();
        print.setPgStampa(UUID.randomUUID().getLeastSignificantBits());
        print.setFlEmail(false);
        print.setReport(REPORT_DOC_TRASPORTO_RIENTRO);
        print.setNomeFile(nomeFile);
        print.setUtcr(utente);

        print.addParam("esercizio", doc.getEsercizio(), Integer.class);
        print.addParam("pg_inventario", doc.getPgInventario(), Long.class);
        print.addParam("ti_documento", doc.getTiDocumento(), String.class);
        print.addParam("pg_doc_trasporto_rientro", doc.getPgDocTrasportoRientro(), Long.class);
        print.addParam("DIR_IMAGE", "", String.class);
        return print;
    }

    // ---------------------------------------------------------------- TIPO DOCUMENTO

    public static String descrizioneTipo(Doc_trasporto_rientroBulk doc) {
        return descrizioneTipo(doc.getTiDocumento());
    }

    public static String descrizioneTipo(String tiDocumento) {
        return TRASPORTO.equals(tiDocumento) ? "Trasporto" : "Rientro";
    }

    public static String prefisso(Doc_trasporto_rientroBulk doc) {
        if (TRASPORTO.equals(doc.getTiDocumento())) return "DocTrasporto";
        if (RIENTRO.equals(doc.getTiDocumento())) return "DocRientro";
        return "";
    }

    // ---------------------------------------------------------------- NOMI / DESCRIZIONI FILE

    public static String nomeFileDaFirmare(Doc_trasporto_rientroBulk doc) {
        return prefisso(doc) + "_" + doc.getEsercizio() + "_" + doc.getPgDocTrasportoRientro() + "_DAFIRMARE.pdf";
    }

    public static String nomeFileFirmato(Doc_trasporto_rientroBulk doc) {
        return prefisso(doc) + "_" + doc.getEsercizio() + "_" + doc.getPgDocTrasportoRientro() + "_FIRMATO.pdf";
    }

    /** True se il nome è quello del PDF tecnico da firmare. */
    public static boolean isNomeFileDaFirmare(Doc_trasporto_rientroBulk doc, String nome) {
        if (nome == null) return false;
        String n = nome.toUpperCase();
        return nomeFileDaFirmare(doc).equalsIgnoreCase(nome)
                || n.contains("_DAFIRMARE")
                || n.contains("_DA_FIRMARE")
                || n.contains("_DA_FIRMA");
    }

    public static String descrizioneAllegato(Doc_trasporto_rientroBulk doc, String suffisso) {
        if (doc == null) return "Documento " + suffisso;

        String tipo = "";
        if (TRASPORTO.equals(doc.getTiDocumento())) {
            tipo = "Documento di Trasporto " + suffisso;
        } else if (RIENTRO.equals(doc.getTiDocumento())) {
            tipo = "Documento di Rientro " + suffisso;
        }
        return tipo + " - Esercizio " + doc.getEsercizio() + " - Progressivo " + doc.getPgDocTrasportoRientro();
    }

    /** Nome allegato in minuscolo (dal nome o dal file), null se assente. */
    public static String nomeAllegato(AllegatoGenericoBulk allegato) {
        if (allegato == null) return null;
        String nome = allegato.getNome();
        if (nome != null && !nome.isEmpty()) return nome.toLowerCase();
        if (allegato.getFile() != null && allegato.getFile().getName() != null) {
            String fromFile = allegato.parseFilename(allegato.getFile().getName());
            if (fromFile != null && !fromFile.isEmpty()) return fromFile.toLowerCase();
        }
        return null;
    }

    // ---------------------------------------------------------------- ASPECT ALLEGATI

    public static String aspectFirmato(Doc_trasporto_rientroBulk doc) {
        if (doc == null || doc.getTiDocumento() == null) return null;
        if (TRASPORTO.equals(doc.getTiDocumento()))
            return AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO;
        if (RIENTRO.equals(doc.getTiDocumento()))
            return AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_FIRMATO;
        return null;
    }

    public static String aspectDaFirmare(Doc_trasporto_rientroBulk doc) {
        if (doc == null || doc.getTiDocumento() == null) return null;
        if (TRASPORTO.equals(doc.getTiDocumento()))
            return AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_DA_FIRMARE;
        if (RIENTRO.equals(doc.getTiDocumento()))
            return AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_DA_FIRMARE;
        return null;
    }

    /** Aspect dell'allegato (FIRMATO/DA_FIRMARE/ALTRO) tra i secondary type del documentale, null se assente. */
    public static String aspectDaStorage(AllegatoDocTraspRientroBulk allegato, List<String> secondaryTypes) {
        if (allegato == null || secondaryTypes == null) return null;
        for (String tipo : secondaryTypes) {
            if (tipo != null && (tipo.equals(allegato.getAspectFirmatoName())
                    || tipo.equals(allegato.getAspectDaFirmareName())
                    || tipo.equals(allegato.getAspectAltroName()))) {
                return tipo;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- PDF

    /** Verifica che i byte siano un PDF non vuoto. */
    public static void validaPdf(byte[] pdfBytes, String descrizione) throws ApplicationException {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new ApplicationException(descrizione + " non disponibile o vuoto.");
        }
        if (pdfBytes.length < 5) {
            throw new ApplicationException(descrizione + " non valido.");
        }
        if (!new String(pdfBytes, 0, 5).startsWith("%PDF")) {
            throw new ApplicationException(descrizione + " non è un PDF valido.");
        }
    }

    /** Scrive il PDF in un file temporaneo (il documentale lavora su File/InputStream). */
    public static File creaFileTemporaneoPdf(String nomeFile, byte[] pdfBytes)
            throws ApplicationException, IOException {

        validaPdf(pdfBytes, "PDF temporaneo " + nomeFile);

        File dir = new File(System.getProperty("tmp.dir.SIGLAWeb") + "/tmp/");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, nomeFile);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(pdfBytes);
        }
        file.deleteOnExit();
        return file;
    }

    // ---------------------------------------------------------------- EMAIL

    /**
     * Email da cd_utente_uid: se contiene già "@" viene usato così com'è,
     * altrimenti si aggiunge il dominio. Null se non costruibile.
     */
    public static String costruisciEmail(String uid, String dominio) {
        if (uid == null || uid.isBlank()) return null;

        String u = uid.trim();
        if (u.contains("@")) return u;
        if (dominio == null || dominio.isBlank()) return null;

        String d = dominio.trim();
        return u + (d.startsWith("@") ? d : "@" + d);
    }
}
