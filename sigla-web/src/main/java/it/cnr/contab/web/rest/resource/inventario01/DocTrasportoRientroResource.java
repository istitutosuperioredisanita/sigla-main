package it.cnr.contab.web.rest.resource.inventario01;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.*;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.inventario01.DocTrasportoRientroLocal;
import it.cnr.contab.web.rest.model.AttachmentDocTrasportoRientro;
import it.cnr.contab.web.rest.model.DocTrasportoRientroDTOBulk;
import it.cnr.contab.web.rest.model.DocTrasportoRientroDettDTOBulk;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.si.spring.storage.StorageException;
import it.cnr.si.spring.storage.StoreService;
import liquibase.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.*;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static it.cnr.jada.bulk.OggettoBulk.isNullOrEmpty;

@Stateless
public class DocTrasportoRientroResource implements DocTrasportoRientroLocal {

    private final Logger log = LoggerFactory.getLogger(DocTrasportoRientroResource.class);

    @Context
    SecurityContext securityContext;

    @EJB
    DocTrasportoRientroComponentSession componenteDocTR;

    /**
     * Inserimento documento: gestisce PG temporaneo/definitivo, dettagli, allegati e stato.
     */
    @Override
    public Response insertDocTrasportoRientro(HttpServletRequest request,
                                              DocTrasportoRientroDTOBulk dto) throws Exception {

        final CNRUserContext ctx = recuperaContestoUtente(request, dto);
        validaDto(dto, ctx);

        try {
            // 1) Creazione bulk e inizializzazione
            Doc_trasporto_rientroBulk bulk = inizializzaBulkDaDTO(dto, ctx);

            // 2) Creazione documento senza dettagli
            Doc_trasporto_rientroBulk creato =
                    (Doc_trasporto_rientroBulk) componenteDocTR.creaConBulk(ctx, bulk);

            final boolean pgTemporaneo = isPgTemporaneo(creato);

            // 3) Aggiunta dettagli
            if (dto.getDettagli() != null && !dto.getDettagli().isEmpty()) {
                final List<Doc_trasporto_rientro_dettBulk> dettagli = costruisciDettagliBeni(dto, creato);
                creato = componenteDocTR.aggiungiBeniDaDTO(ctx, creato, dettagli);
            }

            // 4) Se PG temporaneo → conferma per ottenere PG definitivo evitando inconsistenze cache
            if (pgTemporaneo) {
                creato = componenteDocTR.confermaDocumentoTemporaneo(ctx, creato);
            }

            // 5) Allegati solo se richiesto stato definitivo (DEF)
            if (isDefinitivo(dto)) {
                gestisciAllegati(dto, creato, ctx);
            }

            final Long pgDocDefinitivo = creato.getPgDocTrasportoRientro();

            // 6) Salvataggio a definitivo se stato richiesto = DEF
            if (isDefinitivo(dto)) {
                creato = componenteDocTR.salvaDefinitivo(ctx, creato);
            }

            // Ritorno sempre il PG definitivo certo
            dto.setPgDocTrasportoRientro(pgDocDefinitivo);
            dto.setStato(creato.getStato());

            return Response.status(Response.Status.CREATED).entity(dto).build();

        } catch (ApplicationException e) {
            throw new RestException(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // ---------------------------------------
    // Allegati → CMIS
    // ---------------------------------------
    private void gestisciAllegati(DocTrasportoRientroDTOBulk dto,
                                  Doc_trasporto_rientroBulk doc,
                                  CNRUserContext ctx) {
        if (dto.getAttachments() == null || dto.getAttachments().isEmpty()) return;

        final BulkList<AllegatoGenericoBulk> allegati = preparaAllegati(dto, doc);
        doc.setArchivioAllegati(allegati);
        archiviaAllegatiSuCmis(dto, doc, ctx);
    }

    /** Archivia gli allegati su CMIS dopo conferma documento. */
    private void archiviaAllegatiSuCmis(DocTrasportoRientroDTOBulk dto,
                                        Doc_trasporto_rientroBulk docCreato,
                                        CNRUserContext uc) {

        final StoreService storeService = SpringUtil.getBean("storeService", StoreService.class);
        final boolean isTrasporto = docCreato instanceof DocumentoTrasportoBulk;
        final String storePath = docCreato.getStorePath().get(0);

        for (AttachmentDocTrasportoRientro attDto : dto.getAttachments()) {
            final File temp = creaFileTemporaneo(attDto.getNomeFile(), attDto.getBytes());

            final AllegatoDocTraspRientroBulk allegato = isTrasporto
                    ? new AllegatoDocumentoTrasportoBulk()
                    : new AllegatoDocumentoRientroBulk();

            allegato.setAspectName(attDto.getTypeAttachment());
            allegato.setNome(attDto.getNomeFile());
            allegato.setTitolo(attDto.getNomeFile());
            allegato.setContentType(attDto.getMimeTypes().mimetype());
            allegato.setFile(temp);
            allegato.complete(uc);

            try (FileInputStream fis = new FileInputStream(temp)) {
                storeService.storeSimpleDocument(allegato, fis, allegato.getContentType(),
                        allegato.getNome(), storePath);
            } catch (StorageException e) {
                if (e.getType() != null && e.getType().name().contains("CONSTRAINT")) {
                    throw new RestException(Response.Status.CONFLICT,
                            "File già presente: " + allegato.getNome());
                }
                throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Errore storage allegato: " + allegato.getNome());
            } catch (IOException e) {
                throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,
                        "File temporaneo non trovato: " + allegato.getNome());
            }
        }
    }

    // ---------------------------------------
    // Dettagli
    // ---------------------------------------
    /** Costruisce i dettagli dei beni da DTO per il bulk. */
    private List<Doc_trasporto_rientro_dettBulk> costruisciDettagliBeni(
            DocTrasportoRientroDTOBulk dto,
            Doc_trasporto_rientroBulk docBulk) {

        if (dto.getDettagli() == null || dto.getDettagli().isEmpty()) return Collections.emptyList();

        final boolean isTrasporto = Doc_trasporto_rientroBulk.TRASPORTO.equals(dto.getTiDocumento());

        return dto.getDettagli().stream().map(detDto -> {
            final Doc_trasporto_rientro_dettBulk dett =
                    isTrasporto ? new DocumentoTrasportoDettBulk() : new DocumentoRientroDettBulk();

            final Long pgInv = Optional.ofNullable(detDto.getPgInventario())
                    .orElse(docBulk.getPgInventario());
            final int progr = Optional.ofNullable(detDto.getProgressivo()).orElse(0);

            dett.setBene(new Inventario_beniBulk(detDto.getNrInventario(), pgInv, (long) progr));

            Optional.ofNullable(detDto.getCdTerzoAssegnatario())
                    .ifPresent(cd -> dett.setCdTerzoAssegnatario(Math.toIntExact(cd)));

            dett.setPgInventarioRif(detDto.getPgInventarioRif());
            dett.setTiDocumentoRif(detDto.getTiDocumentoRif());
            dett.setEsercizioRif(detDto.getEsercizioRif());
            dett.setPgDocTrasportoRientroRif(detDto.getPgDocTrasportoRientroRif());
            dett.setNrInventarioRif(detDto.getNrInventarioRif());
            dett.setProgressivoRif(detDto.getProgressivoRif());
            return dett;
        }).collect(Collectors.toList());
    }

    // ---------------------------------------
    // Validazione DTO
    // ---------------------------------------
    private void validaDto(DocTrasportoRientroDTOBulk dto, CNRUserContext ctx) {

        final String ti = dto.getTiDocumento();
        if (ti == null || !(Doc_trasporto_rientroBulk.TRASPORTO.equals(ti)
                || Doc_trasporto_rientroBulk.RIENTRO.equals(ti))) {
            throw new RestException(Response.Status.BAD_REQUEST,
                    "tiDocumento deve essere 'T' o 'R'");
        }

        if (dto.getEsercizio() == null)
            throw new RestException(Response.Status.BAD_REQUEST, "esercizio obbligatorio");

        if (!ctx.getEsercizio().equals(dto.getEsercizio()))
            throw new RestException(Response.Status.BAD_REQUEST, "Esercizio diverso dal contesto");

        if (isNullOrEmpty(dto.getDsDocTrasportoRientro()))
            throw new RestException(Response.Status.BAD_REQUEST, "Descrizione obbligatoria");

        if (dto.getCdTipoTrasportoRientro() == null)
            throw new RestException(Response.Status.BAD_REQUEST, "cdTipoTrasportoRientro obbligatorio");

        final boolean incaricato = Boolean.TRUE.equals(dto.getFlIncaricato());
        final boolean vettore = Boolean.TRUE.equals(dto.getFlVettore());

        if (incaricato && vettore)
            throw new RestException(Response.Status.BAD_REQUEST,
                    "flIncaricato e flVettore non possono essere entrambi true");

        if (incaricato && dto.getCdAnagIncaricato() == null)
            throw new RestException(Response.Status.BAD_REQUEST, "cdAnagIncaricato obbligatorio");

        if (vettore && isNullOrEmpty(dto.getNominativoVettore()))
            throw new RestException(Response.Status.BAD_REQUEST, "nominativoVettore obbligatorio");

        if (dto.getAttachments() != null) {
            int pos = 0;
            for (AttachmentDocTrasportoRientro att : dto.getAttachments()) {
                if (isNullOrEmpty(att.getNomeFile()))
                    throw new RestException(Response.Status.BAD_REQUEST,
                            "Allegato " + pos + " senza nome");
                if (att.getBytes() == null || att.getBytes().length == 0)
                    throw new RestException(Response.Status.BAD_REQUEST,
                            "Allegato '" + att.getNomeFile() + "' vuoto");
                if (att.getMimeTypes() == null)
                    throw new RestException(Response.Status.BAD_REQUEST,
                            "MimeType mancante per '" + att.getNomeFile() + "'");
                if (isNullOrEmpty(att.getTypeAttachment()))
                    throw new RestException(Response.Status.BAD_REQUEST,
                            "typeAttachment mancante per '" + att.getNomeFile() + "'");
                pos++;
            }
        }
    }

    // ---------------------------------------
    // Creazione/Applicazione Bulk
    // ---------------------------------------
    private Doc_trasporto_rientroBulk inizializzaBulkDaDTO(DocTrasportoRientroDTOBulk dto, CNRUserContext ctx) {
        final Doc_trasporto_rientroBulk base = creaScheletroBulk(dto);
        final Doc_trasporto_rientroBulk bulk;
        try {
            bulk = (Doc_trasporto_rientroBulk) componenteDocTR.inizializzaBulkPerInserimento(ctx, base);
        } catch (ComponentException | RemoteException e) {
            throw new RuntimeException(e);
        }
        applicaTestataDtoSuBulk(dto, bulk);
        return bulk;
    }

    /** Crea struttura base bulk a seconda del tipo documento */
    private Doc_trasporto_rientroBulk creaScheletroBulk(DocTrasportoRientroDTOBulk dto) {
        if (Doc_trasporto_rientroBulk.TRASPORTO.equals(dto.getTiDocumento())) {
            final DocumentoTrasportoBulk doc = new DocumentoTrasportoBulk();
            doc.setEsercizio(dto.getEsercizio());
            return doc;
        }
        final DocumentoRientroBulk doc = new DocumentoRientroBulk();
        doc.setEsercizio(dto.getEsercizio());
        return doc;
    }

    /** Applica i dati della testata DTO sul bulk */
    private void applicaTestataDtoSuBulk(DocTrasportoRientroDTOBulk dto,
                                         Doc_trasporto_rientroBulk doc) {

        doc.setDsDocTrasportoRientro(dto.getDsDocTrasportoRientro());
        doc.setCdTipoTrasportoRientro(dto.getCdTipoTrasportoRientro());

        if (doc.getTipoMovimento() == null) {
            doc.setTipoMovimento(new Tipo_trasporto_rientroBulk(dto.getCdTipoTrasportoRientro()));
        }

        Optional.ofNullable(dto.getDataRegistrazione()).ifPresent(doc::setDataRegistrazione);
        Optional.ofNullable(dto.getDestinazione()).ifPresent(doc::setDestinazione);
        Optional.ofNullable(dto.getIndirizzo()).ifPresent(doc::setIndirizzo);
        Optional.ofNullable(dto.getNote()).ifPresent(doc::setNote);
        Optional.ofNullable(dto.getNoteRitiro()).ifPresent(doc::setNoteRitiro);

        doc.setFlIncaricato(Boolean.TRUE.equals(dto.getFlIncaricato()));
        doc.setFlVettore(Boolean.TRUE.equals(dto.getFlVettore()));

        if (Boolean.TRUE.equals(dto.getFlVettore())) {
            Optional.ofNullable(dto.getNominativoVettore()).ifPresent(doc::setNominativoVettore);
        }

        Optional.ofNullable(dto.getCdAnagIncaricato()).ifPresent(cd -> {
            final AnagraficoBulk a = new AnagraficoBulk();
            a.setCd_anag(Integer.valueOf(cd));
            doc.setAnagIncRitiro(a);
        });

        Optional.ofNullable(dto.getCdAnagSmartworking()).ifPresent(cd -> {
            final AnagraficoBulk a = new AnagraficoBulk();
            a.setCd_anag(Integer.valueOf(cd));
            doc.setAnagSmartworking(a);
        });

        Optional.ofNullable(dto.getPgInventario()).ifPresent(doc::setPgInventario);

        doc.setToBeCreated();
    }

    /** Prepara allegati associandoli al bulk */
    private BulkList<AllegatoGenericoBulk> preparaAllegati(DocTrasportoRientroDTOBulk dto,
                                                           Doc_trasporto_rientroBulk docBulk) {

        final boolean isTrasporto = docBulk instanceof DocumentoTrasportoBulk;
        final BulkList<AllegatoGenericoBulk> allegati = new BulkList<>();

        for (AttachmentDocTrasportoRientro attDto : dto.getAttachments()) {
            final File temp = creaFileTemporaneo(attDto.getNomeFile(), attDto.getBytes());

            final AllegatoDocTraspRientroBulk allegato = isTrasporto
                    ? new AllegatoDocumentoTrasportoBulk()
                    : new AllegatoDocumentoRientroBulk();

            allegato.setAspectName(attDto.getTypeAttachment());
            allegato.setNome(attDto.getNomeFile());
            allegato.setTitolo(attDto.getNomeFile());
            allegato.setContentType(attDto.getMimeTypes().mimetype());
            allegato.setFile(temp);
            allegato.setCrudStatus(OggettoBulk.TO_BE_CREATED);

            allegati.add(allegato);
        }

        return allegati;
    }

    // ---------------------------------------
    // Util
    // ---------------------------------------
    private File creaFileTemporaneo(String nome, byte[] bytes) {
        try {
            String pref = nome.contains(".") ? nome.substring(0, nome.lastIndexOf(".")) : nome;
            String suff = nome.contains(".") ? nome.substring(nome.lastIndexOf(".")) : ".tmp";
            if (pref.length() < 3) pref = pref + "___";

            final File tmp = File.createTempFile(pref, suff);
            tmp.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                fos.write(bytes);
            }
            return tmp;

        } catch (IOException e) {
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,
                    "Errore creazione file temporaneo per " + nome);
        }
    }

    private CNRUserContext recuperaContestoUtente(HttpServletRequest req,
                                                  DocTrasportoRientroDTOBulk dto) {

        final Principal p = securityContext.getUserPrincipal();
        if (p == null) return creaContestoAnonimo(req, dto);

        if (!(p instanceof CNRUserContext))
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, "Principal non valido");

        return (CNRUserContext) p;
    }

    private CNRUserContext creaContestoAnonimo(HttpServletRequest req, DocTrasportoRientroDTOBulk dto) {

        final CNRUserContext ctx = new CNRUserContext();
        ctx.setUser("REST_ANONIMO");

        final String cds = req.getHeader(SIGLASecurityContext.X_SIGLA_CD_CDS);
        final String uo = req.getHeader(SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA);
        final String esercizio = req.getHeader(SIGLASecurityContext.X_SIGLA_ESERCIZIO);

        ctx.setCd_cds(cds != null ? cds : "000");
        ctx.setCd_unita_organizzativa(uo != null ? uo : "000.000");
        ctx.setEsercizio(esercizio != null ? Integer.valueOf(esercizio) : dto.getEsercizio());
        return ctx;
    }

    private boolean isPgTemporaneo(Doc_trasporto_rientroBulk b) {
        return b.getPgDocTrasportoRientro() != null && b.getPgDocTrasportoRientro() < 0;
    }

    private boolean isDefinitivo(DocTrasportoRientroDTOBulk dto) {
        return Doc_trasporto_rientroBulk.STATO_DEFINITIVO.equals(dto.getStato());
    }


}