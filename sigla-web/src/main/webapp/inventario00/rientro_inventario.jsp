<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
        it.cnr.jada.action.*,
        it.cnr.contab.inventario01.bp.*"
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<title>Documento di Rientro</title>
</head>
<body class="Form">

<% CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP)BusinessProcess.getBusinessProcess(request);
    bp.openFormWindow(pageContext);

    if (bp.isInserting()){
        JSPUtils.tabbed(
                        pageContext,
                        "tab",
                        new String[][] {
                            { "tabRientroTestata","Testata","/inventario00/tab_testata_doc_r.jsp" },
                            { "tabRientroDettaglio","Dettaglio","/inventario00/tab_rientro_inv_dett.jsp"},
                        },
                        bp.getTab("tab"),
                        "center",
                        "100%",
                        null );
    } else {
        JSPUtils.tabbed(
                        pageContext,
                        "tab",
                        new String[][] {
                            { "tabRientroTestata","Testata","/inventario00/tab_testata_doc_r.jsp" },
                            { "tabRientroDettaglio","Dettaglio","/inventario00/tab_rientro_inv_edit_dett.jsp"},
                        },
                        bp.getTab("tab"),
                        "center",
                        "100%",
                        null );
    }

    bp.closeFormWindow(pageContext); %>

</body>
</html>