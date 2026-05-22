package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.dto.StatoHappySignDto;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.comp.ComponentException;

public interface HappysignDocService {

    String inviaDocumentoAdHappySign(
            Doc_trasporto_rientroBulk doc,
            byte[] pdfBytes,
            String nomeFile,
            CNRUserContext userContext)
            throws ComponentException;

    StatoHappySignDto getStatoFlusso(String uuid) throws Exception;

    byte[] getDocumentoFirmato(String uuid) throws Exception;
}