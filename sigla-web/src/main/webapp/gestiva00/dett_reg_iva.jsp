<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
        it.cnr.jada.action.*,
        java.util.*,
        it.cnr.jada.util.action.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<title>Risultato ricerca</title>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
</head>

<body class="Form">

<%
    BulkBP bp = (BulkBP)BusinessProcess.getBusinessProcess(request);
    bp.openFormWindow(pageContext);
%>

<div class="Group card" style="width:100%">
    <table width="100%">
        <tr>
            <td>
            <% bp.getController().writeFormLabel(out, "tipo_sezionale"); %>
            </td>
            <td>
                <% bp.getController().writeFormInput(
                    out,
                    null,
                    "tipo_sezionale",
                    false,
                    null,
                    "onChange=\"submitForm('doOnTipoSezionaleChange')\""
                );%>
            </td>
        </tr>
    </table>
</div>

<% bp.closeFormWindow(pageContext); %>

</body>
</html>