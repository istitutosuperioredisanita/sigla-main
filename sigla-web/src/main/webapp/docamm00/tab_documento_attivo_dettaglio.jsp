<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.docamm00.tabrif.bulk.*,
		it.cnr.contab.docamm00.docs.bulk.*,
		it.cnr.contab.docamm00.bp.*,
		it.cnr.contab.anagraf00.tabrif.bulk.*"
%>

<% CRUDDocumentoGenericoAttivoBP bp = (CRUDDocumentoGenericoAttivoBP)BusinessProcess.getBusinessProcess(request);
   Documento_generico_rigaBulk riga = (Documento_generico_rigaBulk)bp.getDettaglio().getModel();
   bp.getDettaglio().writeHTMLTable(pageContext,"righiSet",true,false,true,"100%","auto;max-height:50vh;"); %>

<% if (bp.isAttivaEconomica() || bp.isAttivaAnalitica()) {
  JSPUtils.tabbed(
				pageContext,
				"tabDocumentoAttivoDettaglio",
				new String[][] {
						{ "tabDocumentoAttivoDettaglioDetail1","Dettaglio","/docamm00/tab_documento_attivo_dettaglio_detail.jsp" },
						{ "tabDocumentoAttivoDettaglioDetail2","Dati Coge/Coan","/coepcoan00/tab_doc_detail_eco_coge.jsp" } },
				bp.getTab("tabDocumentoAttivoDettaglio"),
				"center",
				"100%",
				null);
 } else { %>
<jsp:include page="/docamm00/tab_documento_attivo_dettaglio_detail.jsp" />
<% } %>