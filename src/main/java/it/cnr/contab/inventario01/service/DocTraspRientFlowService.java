package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.naming.InitialContext;
import java.rmi.RemoteException;
import java.util.List;

@Service
@Profile("iss")
public class DocTraspRientFlowService {

    private static final String DOC_TRASP_RIENT_JNDI =
            "java:global/sigla/CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession!"
                    + "it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession";

    private DocTrasportoRientroComponentSession docTrasportoRientroComponentSession;

    private DocTrasportoRientroComponentSession getSession() throws ComponentException {
        if (docTrasportoRientroComponentSession != null) {
            return docTrasportoRientroComponentSession;
        }
        try {
            docTrasportoRientroComponentSession =
                    (DocTrasportoRientroComponentSession)
                            new InitialContext().lookup(DOC_TRASP_RIENT_JNDI);
            return docTrasportoRientroComponentSession;
        } catch (Exception e) {
            throw new ComponentException(
                    "Errore lookup JNDI EJB DocTrasportoRientroComponentSession: "
                            + DOC_TRASP_RIENT_JNDI,
                    e);
        }
    }

    public List<Doc_trasporto_rientroBulk> getDocumentiPredispostiAllaFirma(
            UserContext userContext) throws ComponentException {
        try {
            return getSession().getDocumentiPredispostiAllaFirma(userContext);
        } catch (RemoteException e) {
            throw new ComponentException(
                    "Errore remoto nel recupero documenti predisposti alla firma", e);
        }
    }

    public Doc_trasporto_rientroBulk aggiornaDocumentoFirmato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            byte[] pdfFirmato) throws ComponentException {
        try {
            return getSession().aggiornaDocumentoFirmato(userContext, doc, pdfFirmato);
        } catch (RemoteException e) {
            throw new ComponentException("Errore remoto aggiornamento documento firmato", e);
        }
    }

    public Doc_trasporto_rientroBulk aggiornaDocumentoRifiutato(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc,
            String motivoRifiuto) throws ComponentException {
        try {
            return getSession().aggiornaDocumentoRifiutato(userContext, doc, motivoRifiuto);
        } catch (RemoteException e) {
            throw new ComponentException("Errore remoto aggiornamento documento rifiutato", e);
        }
    }

    /** Email del creatore del documento (null se non risolvibile), risolta nel component. */
    public String getEmailCreatore(
            UserContext userContext,
            Doc_trasporto_rientroBulk doc) throws ComponentException {
        try {
            return getSession().getEmailCreatore(userContext, doc);
        } catch (RemoteException e) {
            throw new ComponentException("Errore remoto nel recupero email creatore", e);
        }
    }

    /** Invio tramite component: nel contesto EJB SendMail trova la mail session SIGLA. */
    public void inviaMail(UserContext userContext, String subject, String body,
                          java.util.List<String> destinatari) throws Exception {
        getSession().inviaNotifica(userContext, subject, body, destinatari);
    }
}