<%@ page pageEncoding="UTF-8"
	import = "it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.ordmag.ordini.bp.CRUDOrdineAcqBP,
		it.cnr.contab.ordmag.ordini.bulk.OrdineAcqRigaBulk,
		it.cnr.contab.ordmag.anag00.*"
%>

<% CRUDOrdineAcqBP bp = (CRUDOrdineAcqBP)BusinessProcess.getBusinessProcess(request);
	bp.getRighe().writeHTMLTable(pageContext,"righeSet",true,false,true,"100%","auto;max-height:50vh"); %>
<div class="mt-2">
	      <%
	      	JSPUtils.tabbed(pageContext, "tabOrdineAcqDettagli",
	      			bp.getTabsDettagli(),
	      			bp.getTab("tabOrdineAcqDettagli"), "left", "100%", null, true);
	      %>
</div>
