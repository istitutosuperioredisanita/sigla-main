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

/**
 * Contratto comune per l'integrazione HappySign dei documenti Trasporto/Rientro.
 *
 * Questa interfaccia non importa classi di happysign-client e può quindi
 * essere compilata anche senza il profilo Maven "iss".
 */
public interface HappysignDocService {

    String inviaDocumentoAdHappySign(
            Doc_trasporto_rientroBulk doc,
            byte[] pdfBytes,
            CNRUserContext userContext)
            throws ComponentException;

    StatoHappySignDto getStatoFlusso(String uuid) throws Exception;

    byte[] getDocumentoFirmato(String uuid) throws Exception;
}