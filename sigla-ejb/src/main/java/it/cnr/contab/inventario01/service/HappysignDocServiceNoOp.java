package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.dto.StatoHappySignDto;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.comp.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HappysignDocServiceNoOp implements HappysignDocService {

    private static final Logger log =
            LoggerFactory.getLogger(HappysignDocServiceNoOp.class);

    private static final String MSG_DISABLED =
            "HappySign non attivo: happysign-client non presente nel classpath.";

    @Override
    public String inviaDocumentoAdHappySign(
            Doc_trasporto_rientroBulk doc,
            byte[] pdfBytes,
            String nomeFile,
            CNRUserContext userContext)
            throws ComponentException {

        log.warn(
                "{} Invio documento ignorato. Documento T/R: {}, nomeFile={}, pdfBytes={}",
                MSG_DISABLED,
                descriviDoc(doc),
                nomeFile,
                pdfBytes != null ? pdfBytes.length : null
        );

        throw new ComponentException(
                "HappySign non disponibile. Verificare che il profilo iss sia attivo e che happysign-client sia nel WAR."
        );
    }

    @Override
    public StatoHappySignDto getStatoFlusso(String uuid) {
        log.warn("{} Verifica stato ignorata. uuid={}", MSG_DISABLED, uuid);
        return new StatoHappySignDto(StatoHappySignDto.STATO_INVIATO, null);
    }

    @Override
    public byte[] getDocumentoFirmato(String uuid) {
        log.warn("{} Download documento firmato ignorato. uuid={}", MSG_DISABLED, uuid);
        return null;
    }

    private String descriviDoc(Doc_trasporto_rientroBulk doc) {
        if (doc == null) {
            return "null";
        }

        return String.format(
                "esercizio=%s, inventario=%s, tipo=%s, pg=%s, stato=%s, statoFlusso=%s, uuid=%s",
                doc.getEsercizio(),
                doc.getPgInventario(),
                doc.getTiDocumento(),
                doc.getPgDocTrasportoRientro(),
                doc.getStato(),
                doc.getStatoFlusso(),
                doc.getIdFlussoHappysign()
        );
    }
}