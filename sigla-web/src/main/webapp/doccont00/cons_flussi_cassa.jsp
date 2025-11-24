<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		it.cnr.jada.bulk.*,
		it.cnr.jada.action.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.doccont00.bp.*" %>


<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<title>Consultazione Flussi di Cassa</title>
</head>
<body class="Form">

<% 
	BulkBP bp = (BulkBP)BusinessProcess.getBusinessProcess(request);
	bp.openFormWindow(pageContext);
	boolean isFieldReadOnly = true;
%>

	<div class="Group card" style="width:100%">
		<table width="100%">
		
			  <tr>
				<td> <% bp.getController().writeFormLabel(out,"esercizio"); %></td>
    			<td colspan=1> <% bp.getController().writeFormInput(out,null,"esercizio",false,null,"");%></td>
    		 </tr>
    		
    		<tr>
    			<td><% bp.getController().writeFormLabel( out, "find_cds"); %></td>		
				<td colspan=4><% bp.getController().writeFormInput( out, "cds_cd");
								 bp.getController().writeFormInput( out, "cds_ds");
						         bp.getController().writeFormInput( out, "find_cds");%></td>
			 </tr>

 			<tr>
                <td> <% bp.getController().writeFormLabel(out,"trimestre");%></td>
                <td colspan="3"> <% bp.getController().writeFormInput(out,null,"trimestre",false,null,"");%></td>
             </tr>

				
			 <tr>
				<td> <% bp.getController().writeFormLabel(out,"tipoFlusso");%></td>
				<td colspan="3"> <% bp.getController().writeFormInput(out,null,"tipoFlusso",false,null,"");%></td>
			 </tr>
			 <tr><td colspan="3"></td></tr>
			  <tr>
                <td> <% bp.getController().writeFormLabel(out,"livello");%></td>
                <td colspan="3"> <% bp.getController().writeFormInput(out,null,"livello",false,null,"");%></td>
             </tr>

	</table>
		
	</div>
<%	bp.closeFormWindow(pageContext); %>
</body>
</html> 
