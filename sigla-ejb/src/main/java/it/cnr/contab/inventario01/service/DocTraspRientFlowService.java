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
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.ejb.EJBCommonServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gestisce le transizioni di stato dei documenti T/R
 * nel flusso firma HappySign.
 */
public class DocTraspRientFlowService implements InitializingBean {

    private static final Logger log =
            LoggerFactory.getLogger(DocTraspRientFlowService.class);

    private static final String DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION =
            "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession";

    private static final String MOTIVO_RIFIUTO_DEFAULT =
            "Respinta da firma su HappySign";

    private DocTrasportoRientroComponentSession docTrasportoRientroComponentSession;

    private DocTrasportoRientroComponentSession creaComponentSession() {
        return Optional.ofNullable(
                        EJBCommonServices.createEJB(
                                DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION
                        )
                )
                .filter(DocTrasportoRientroComponentSession.class::isInstance)
                .map(DocTrasportoRientroComponentSession.class::cast)
                .orElseThrow(() -> new DetailedRuntimeException(
                        "cannot find ejb "
                                + DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION
                ));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.docTrasportoRientroComponentSession = creaComponentSession();

        log.info("EJB inizializzato: {}",
                DOC_TRASPORTO_RIENTRO_COMPONENT_SESSION);
    }

    public DocTrasportoRientroComponentSession
    getDocTrasportoRientroComponentSession() {

        return docTrasportoRientroComponentSession;
    }

    public void setDocTrasportoRientroComponentSession(
            DocTrasportoRientroComponentSession docTrasportoRientroComponentSession) {

        this.docTrasportoRientroComponentSession =
                docTrasportoRientroComponentSession;
    }

    private DocTrasportoRientroComponentSession getComponent() {
        if (docTrasportoRientroComponentSession == null) {
            this.docTrasportoRientroComponentSession = creaComponentSession();
        }

        return docTrasportoRientroComponentSession;
    }


    /**
     * Recupera documenti predisposti alla firma.
     * Effettua deduplicazione per UUID HappySign e gestisce errori backend.
     */
    public List<Doc_trasporto_rientroBulk>
    getDocumentiPredispostiAllaFirma(UserContext userContext) {

        try {

            List<Doc_trasporto_rientroBulk> documenti =
                    getComponent().getDocumentiPredispostiAllaFirma(userContext);

            if (documenti == null || documenti.isEmpty()) {

                log.info("Nessun documento predisposto alla firma");
                return Collections.emptyList();
            }

            List<Doc_trasporto_rientroBulk> distintiPerUuid =
                    filtraDocumentiDistintiPerUuidHappySign(documenti);

            if (documenti.size() != distintiPerUuid.size()) {

                log.warn(
                        "Documenti predisposti: {}, distinti per UUID: {}",
                        documenti.size(),
                        distintiPerUuid.size()
                );

            } else {

                log.info(
                        "Documenti predisposti distinti: {}",
                        distintiPerUuid.size()
                );
            }

            return distintiPerUuid;

        } catch (ComponentException | RemoteException e) {

            log.error("Errore recupero documenti predisposti", e);
            return Collections.emptyList();

        } catch (RuntimeException e) {

            log.error("Errore runtime recupero documenti predisposti", e);
            return Collections.emptyList();
        }
    }


    /**
     * Aggiorna il documento come firmato delegando al componente EJB.
     * La logica completa (stato, allegati, validazione) è centralizzata lato component.
     */
    public Doc_trasporto_rientroBulk aggiornaDocumentoFirmato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            byte[] pdfFirmato) {

        try {
            return getComponent().aggiornaDocumentoFirmatoDaHappySign(
                    userContext,
                    doc,
                    pdfFirmato
            );

        } catch (ComponentException | RemoteException e) {
            throw new RuntimeException("Errore aggiornamento documento firmato", e);

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Errore aggiornamento documento firmato", e);
        }
    }

    /**
     * Aggiorna il documento come rifiutato.
     */
    public Doc_trasporto_rientroBulk aggiornaDocumentoRifiutato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            String motivoRifiuto) {

        try {
            String motivoNormalizzato = normalizzaMotivoRifiuto(motivoRifiuto);

            return getComponent().aggiornaDocumentoRifiutatoDaHappySign(
                    userContext,
                    doc,
                    motivoNormalizzato
            );

        } catch (ComponentException | RemoteException e) {
            throw new RuntimeException("Errore aggiornamento documento rifiutato", e);

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Errore aggiornamento documento rifiutato", e);
        }
    }

    /**
     * Normalizza il motivo rifiuto prima del salvataggio.
     *
     * Evita di salvare valori tecnici come:
     * OK, REFUSED, SIGNED, TOSIGN, ecc.
     */
    private String normalizzaMotivoRifiuto(String motivoRifiuto) {

        String motivo = motivoRifiuto;

        if (motivo == null || motivo.trim().isEmpty()) {
            motivo = MOTIVO_RIFIUTO_DEFAULT;
        }

        motivo = motivo.trim();

        if (isValoreTecnicoNonMotivo(motivo)) {
            motivo = MOTIVO_RIFIUTO_DEFAULT;
        }

        if (motivo.length() > 4000) {
            motivo = motivo.substring(0, 4000);
        }

        return motivo;
    }

    private boolean isValoreTecnicoNonMotivo(String valore) {
        if (valore == null) {
            return true;
        }

        String v = valore.trim();

        return "OK".equalsIgnoreCase(v)
                || "REFUSED".equalsIgnoreCase(v)
                || "CANCELED".equalsIgnoreCase(v)
                || "SIGNED".equalsIgnoreCase(v)
                || "TOSIGN".equalsIgnoreCase(v)
                || "SUCCESS".equalsIgnoreCase(v)
                || "200".equalsIgnoreCase(v);
    }


    /**
     * Deduplica documenti usando UUID HappySign come chiave.
     * Mantiene anche documenti senza UUID per evitare perdita informazione.
     */
    private List<Doc_trasporto_rientroBulk>
    filtraDocumentiDistintiPerUuidHappySign(
            List<Doc_trasporto_rientroBulk> documenti) {

        if (documenti == null || documenti.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Doc_trasporto_rientroBulk> perUuid =
                new LinkedHashMap<>();

        List<Doc_trasporto_rientroBulk> senzaUuid =
                new ArrayList<>();

        for (Doc_trasporto_rientroBulk doc : documenti) {

            if (doc == null) {
                continue;
            }

            String uuid = doc.getIdFlussoHappysign();

            if (uuid == null || uuid.trim().isEmpty()) {

                log.warn(
                        "Documento senza UUID HappySign: {}",
                        descriviDoc(doc)
                );

                senzaUuid.add(doc);

                continue;
            }

            String uuidNormalizzato = uuid.trim();

            if (perUuid.containsKey(uuidNormalizzato)) {

                Doc_trasporto_rientroBulk giaPresente =
                        perUuid.get(uuidNormalizzato);

                log.warn(
                        "UUID duplicato {} -> tenuto {}, scartato {}",
                        uuidNormalizzato,
                        descriviDoc(giaPresente),
                        descriviDoc(doc)
                );

            } else {

                perUuid.put(uuidNormalizzato, doc);
            }
        }

        List<Doc_trasporto_rientroBulk> result =
                new ArrayList<>(perUuid.values());

        result.addAll(senzaUuid);

        return result;
    }

    private String descriviDoc(
            Doc_trasporto_rientroBulk doc) {

        if (doc == null) {
            return "null";
        }

        return String.format(
                "[es=%s, inv=%s, tipo=%s, pg=%s, uuid=%s]",
                doc.getEsercizio(),
                doc.getPgInventario(),
                doc.getTiDocumento(),
                doc.getPgDocTrasportoRientro(),
                doc.getIdFlussoHappysign()
        );
    }
}