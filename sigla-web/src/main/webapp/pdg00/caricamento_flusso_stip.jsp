<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.jada.*,
		it.cnr.contab.pdg00.bp.*"
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
            <% bp.getController().writeFormLabel(out, "tipo_rapporto"); %>
            </td>
                <% bp.getController().writeFormInput(
                    out,
                    null,
                    "tipo_rapporto",
                    false,
                    null,
                    null
                );%>
            </td>
        </tr>
    </table>
</div>

<% bp.closeFormWindow(pageContext); %>

</body>
</html>
