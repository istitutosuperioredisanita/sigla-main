<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		it.cnr.contab.inventario00.docs.bulk.*,
		it.cnr.contab.inventario00.bp.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<title>Associa Righe Fattura - Beni</title>
</head>
<body class="Form">

<% AssBeneFatturaBP bp = (AssBeneFatturaBP)BusinessProcess.getBusinessProcess(request); 
   Ass_inv_bene_fatturaBulk associaz_bene_fatt = (Ass_inv_bene_fatturaBulk)bp.getModel();
   bp.openFormWindow(pageContext); %>
  
  <% bp.getDettagliDocumento().writeHTMLTable(pageContext,"inventarioSet",false,false,false,"100%","200px"); %> 
  <div class="Group">
	<table>

	  <tr>
	  	<td colspan = "4">
		  <% bp.getRigheDaDocumento().writeHTMLTable(
				pageContext,
				null,
				true,
				false,
				true,
				"100%","140px",
				true); %>
		</td>
	  </tr>
	</table>
	<table>	
	  <tr>
		<td>
			<% bp.getRigheDaDocumento().writeFormLabel(out,"codiceCompleto"); %>
		</td>
		<td>
			<% bp.getRigheDaDocumento().writeFormInput(out,null,"codiceCompleto",true,null,null); %>
		</td>
	  </tr>	
	  <tr>
		<td>
			<% bp.getRigheDaDocumento().writeFormLabel(out,"ds_bene"); %>
		</td>
		<td>
			<% bp.getRigheDaDocumento().writeFormInput(out,null,"ds_bene",true,null,null); %>
		</td>
	  </tr>
   </table>
   <table>
	  <tr>
		<td>
			<% bp.getRigheDaDocumento().writeFormLabel(out,"find_categoria_bene"); %>
		</td>
		<td>
			<% bp.getRigheDaDocumento().writeFormInput(out,null,"find_categoria_bene",true,null,null); %>
		</td>
	  </tr>	
	  <tr>
		<td>
			<% bp.getRigheDaDocumento().writeFormLabel(out,"ds_assegnatario"); %>
		</td>
		<td colspan="4">
			<% bp.getRigheDaDocumento().writeFormInput(out,null,"cd_assegnatario",true,null,null); %>
		
			<% bp.getRigheDaDocumento().writeFormInput(out,null,"ds_assegnatario",true,null,null); %>
		</td>
	  </tr>
	  <tr>
		<td>
			<% bp.getRigheDaDocumento().writeFormLabel(out,"find_ubicazione"); %>
		</td>
		<td>
			<% bp.getRigheDaDocumento().writeFormInput(out,null,"find_ubicazione",true,null,null); %>
		</td>
	  </tr>
   </table>
   <table>
	  <tr>
		<td>
			<% bp.getRigheDaDocumento().writeFormLabel(out,"collocazione"); %>
		</td>
		<td>
			<% bp.getRigheDaDocumento().writeFormInput(out,null,"collocazione",true,null,null); %>
		</td>
	  </tr>	 
	</table>
 </div>
<% bp.closeFormWindow(pageContext); %>	
</body>
</html>