/*
 *  Copyright (C) 2023  Consiglio Nazionale delle Ricerche
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_respintoBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_respintoHome;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.persistency.PersistencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

@Service
public class DocTraspRientRespintoService {

    @Autowired
    private CRUDComponentSession crudServiceBean;

    @Autowired
    private DataSource dataSource;

    @Context
    SecurityContext securityContext;

    /**
     * Metodo unificato per inserire un respingimento di documento Trasporto/Rientro
     * Determina automaticamente il tipo operazione dal tiDocumento del bulk
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void inserisciDocumentoRespinto(Doc_trasporto_rientroBulk documento,
                                           String tipoFaseRespingi,
                                           String motivoRespingi) throws ComponentException, RemoteException {
        CNRUserContext userContext = getUserContext();

        Doc_trasporto_rientro_respintoBulk respinto = new Doc_trasporto_rientro_respintoBulk();
        respinto.setPgInventario(documento.getPgInventario());
        respinto.setTiDocumento(documento.getTiDocumento());
        respinto.setEsercizio(documento.getEsercizio());
        respinto.setPgDocTrasportoRientro(documento.getPgDocTrasportoRientro());
        respinto.setDataInserimento(new Timestamp(new Date().getTime()));
        respinto.setTipoFaseRespingi(tipoFaseRespingi);
        respinto.setMotivoRespingi(motivoRespingi);
        respinto.setUidInsert(userContext.getUser());

        // Determina automaticamente il tipo operazione dal tiDocumento
        respinto.setTipoOperazioneDoc(mapTipoOperazione(documento.getTiDocumento()));

        respinto.setToBeCreated();
        crudServiceBean.creaConBulk(userContext, respinto);
    }

    /**
     * Overload con fase di default (RIFIUTO_FIRMA)
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void inserisciDocumentoRespinto(Doc_trasporto_rientroBulk documento,
                                           String motivoRespingi) throws ComponentException, RemoteException {
        inserisciDocumentoRespinto(documento,
                Doc_trasporto_rientro_respintoBulk.FASE_RIFIUTO_FIRMA,
                motivoRespingi);
    }

    @Transactional(readOnly = true)
    public Collection getCronologiaRespingimentiDocumento(Long pgInventario,
                                                          String tiDocumento,
                                                          Integer esercizio,
                                                          Long pgDocTrasportoRientro)
            throws PersistencyException, SQLException {
        CNRUserContext userContext = getUserContext();

        try (Connection conn = dataSource.getConnection()) {
            Doc_trasporto_rientro_respintoHome home = new Doc_trasporto_rientro_respintoHome(conn);
            return home.getRespingimenti(userContext, pgInventario, tiDocumento, esercizio, pgDocTrasportoRientro);
        }
    }

    @Transactional(readOnly = true)
    public Collection getRespingimentiPerTipoOperazione(String tipoOperazioneDoc)
            throws PersistencyException, SQLException {
        CNRUserContext userContext = getUserContext();

        try (Connection conn = dataSource.getConnection()) {
            Doc_trasporto_rientro_respintoHome home = new Doc_trasporto_rientro_respintoHome(conn);
            return home.getRespingimentiPerTipoOperazione(userContext, tipoOperazioneDoc);
        }
    }

    @Transactional(readOnly = true)
    public Collection getRespingimentiPerFase(String tipoFaseRespingi)
            throws PersistencyException, SQLException {
        CNRUserContext userContext = getUserContext();

        try (Connection conn = dataSource.getConnection()) {
            Doc_trasporto_rientro_respintoHome home = new Doc_trasporto_rientro_respintoHome(conn);
            return home.getRespingimentiPerFase(userContext, tipoFaseRespingi);
        }
    }

    @Transactional(readOnly = true)
    public Collection getRespingimentiPerUtente(String uidInsert)
            throws PersistencyException, SQLException {
        CNRUserContext userContext = getUserContext();

        try (Connection conn = dataSource.getConnection()) {
            Doc_trasporto_rientro_respintoHome home = new Doc_trasporto_rientro_respintoHome(conn);
            return home.getRespingimentiPerUtente(userContext, uidInsert);
        }
    }

    /**
     * Mappa il tiDocumento (T/R) al tipo operazione (TR/RI)
     */
    private String mapTipoOperazione(String tiDocumento) {
        if ("T".equals(tiDocumento)) {
            return Doc_trasporto_rientro_respintoBulk.OPERAZIONE_TRASPORTO;
        } else if ("R".equals(tiDocumento)) {
            return Doc_trasporto_rientro_respintoBulk.OPERAZIONE_RIENTRO;
        }
        return tiDocumento;
    }

    /**
     * Ottiene il CNRUserContext dal SecurityContext
     */
    private CNRUserContext getUserContext() {
        if (securityContext != null && securityContext.getUserPrincipal() instanceof CNRUserContext) {
            return (CNRUserContext) securityContext.getUserPrincipal();
        }
        throw new IllegalStateException("CNRUserContext non disponibile nel SecurityContext");
    }
}
