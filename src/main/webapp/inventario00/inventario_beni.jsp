<%@page import="it.cnr.jada.UserContext"%>
<%@page import="it.cnr.contab.util.Utility"%>
<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.inventario00.tabrif.bulk.*,
		it.cnr.contab.inventario00.docs.bulk.*,
		it.cnr.contab.inventario00.bp.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<%
   CRUDInventarioBeniBP bp = (CRUDInventarioBeniBP)BusinessProcess.getBusinessProcess(request);
   Inventario_beniBulk bene = (Inventario_beniBulk)bp.getModel();
   bp.openFormWindow(pageContext);
   UserContext uc = HttpActionContext.getUserContext(session);
%>
<title>Gestione Beni</title>
</head>
<body class="Form">
    <div class="Group card p-2 mb-2">
        <div class="form-row form-group form-inline">
            <div class="col-md-6 mt-3">
                <% bp.getController().writeFormLabel(out,"pg_inventario"); %>
                <% bp.getController().writeFormInput(out,null,"pg_inventario",true,null,null); %>
            </div>
            <% if (bene.getNr_inventario() != null) { %>
                <div class="col-md-4 mt-3 text-end">
                    <span class="FormLabel">Stato del bene:</span>
                    <% if (bene.isTotalmenteScaricato()) { %>
                        <span class="FormLabel" style="color:red">
                        <b>TOTALMENTE SCARICATO</b>
                    <% } else if (Utility.createConfigurazioneCnrComponentSession().isGestioneBeneDismessoInventarioAttivo(uc) &&
                                  (bene.getFl_dismesso()!= null && bene.getFl_dismesso())) { %>
                        <span class="FormLabel" style="color:blue">
                        <b>DISMESSO</b>
                    <% } else { %>
                        <span class="FormLabel" style="color:green">
                        <b>NORMALE</b>
                    <% }
                       if (bene.isMigrato()) { %>
                        (Bene Migrato)
                    <% } %>
                    </span>
                </div>
            <% } %>
        </div>
    <div>
    <%
       // Il BENE selezionato è un bene accessorio: NON viene visualizzato il tab UTILIZZATORI
       if (bene != null && bene.isBeneAccessorio()){
           JSPUtils.tabbed(
                pageContext,
                "tab",
                new String[][] {
                    { "tabInventarioBeniTestata","Bene","/inventario00/tab_inv_bene.jsp" },
                    { "tabInventarioBeniAmmortamento","Ammortamento","/inventario00/tab_inv_bene_ammortamento.jsp" }
                },
                bp.getTab("tab"),
                "center",
                "100%",
                null );
       }
       // Il BENE selezionato NON è un bene accessorio: viene visualizzato il tab UTILIZZATORI
       else {
            JSPUtils.tabbed(
                pageContext,
                "tab",
                new String[][] {
                    { "tabInventarioBeniTestata","Bene","/inventario00/tab_inv_bene.jsp" },
                    { "tabInventarioBeniUtilizzatori","Utilizzatori","/inventario00/tab_inv_bene_utilizzatori.jsp" },
                    { "tabInventarioBeniAmmortamento","Ammortamento","/inventario00/tab_inv_bene_ammortamento.jsp" }
                },
                bp.getTab("tab"),
                "center",
                "100%",
                null );
       }
	   bp.closeFormWindow(pageContext);
    %>
</body>
</html>
