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

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.dto.StatoHappySignDto;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.comp.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementazione NoOp di HappySign.
 *
 * Viene usata quando happysign-client non è presente nel classpath.
 */
public class HappysignDocServiceNoOp implements HappysignDocService {

    private static final Logger log =
            LoggerFactory.getLogger(HappysignDocServiceNoOp.class);

    private static final String MSG_DISABLED =
            "HappySign non attivo: happysign-client non presente nel classpath.";

    @Override
    public String inviaDocumentoAdHappySign(
            Doc_trasporto_rientroBulk doc,
            byte[] pdfBytes,
            CNRUserContext userContext)
            throws ComponentException {

        log.warn(
                "{} Invio documento ignorato. Documento T/R: esercizio={}, inventario={}, tipo={}, pg={}",
                MSG_DISABLED,
                doc != null ? doc.getEsercizio() : null,
                doc != null ? doc.getPgInventario() : null,
                doc != null ? doc.getTiDocumento() : null,
                doc != null ? doc.getPgDocTrasportoRientro() : null
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
}