package it.cnr.contab.web.rest.model;

import it.cnr.contab.web.rest.local.config00.DocumentoGenericoPassivoLocal;
import it.cnr.contab.web.rest.model.ObbligazioneDto;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

public class DocumentoGenPassivoResource implements DocumentoGenericoPassivoLocal {
    @Override
    public Response insert(HttpServletRequest request, ObbligazioneDto obbligazioneDto) throws Exception {
        return null;
    }

    @Override
    public Response get(String cd_cds, String cd_unita_organizzativa, Integer esercizio, String cd_tipo_documento_amm, Long pg_documento_generico) throws Exception {
        return null;
    }

    @Override
    public Response delete(String cd_cds, String cd_unita_organizzativa, Integer esercizio, String cd_tipo_documento_amm, Long pg_documento_generico) throws Exception {
        return null;
    }

    @Override
    public Response update(String cd_cds, String cd_unita_organizzativa, Integer esercizio, String cd_tipo_documento_amm, Long pg_documento_generico) throws Exception {
        return null;
    }
}
