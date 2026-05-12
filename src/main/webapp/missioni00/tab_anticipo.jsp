<%@ page pageEncoding="UTF-8"  import = "it.cnr.jada.util.jsp.*,it.cnr.jada.action.*,java.util.*, it.cnr.jada.util.action.*, it.cnr.contab.missioni00.bp.*, it.cnr.contab.missioni00.docs.bulk.*, it.cnr.jada.bulk.*, it.cnr.contab.docamm00.bp.*"%>

<%  
	CRUDAnticipoBP bp = (CRUDAnticipoBP)BusinessProcess.getBusinessProcess(request);
	AnticipoBulk anticipo = (AnticipoBulk) bp.getModel();

	if(anticipo == null)
		anticipo = new AnticipoBulk();
%>

<% if (bp.isAttivaEconomica() || bp.isAttivaAnalitica()) {
  JSPUtils.tabbed(
				pageContext,
				"tabAnticipo",
				bp.getTabsTabAnticipo(),
				bp.getTab("tabAnticipo"),
				"center",
				"100%",
				null);
 } else { %>
<jsp:include page="/missioni00/tab_anticipo_detail.jsp" />
<% } %>