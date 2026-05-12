<%@ page pageEncoding="UTF-8"
	import = "it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.ordmag.richieste.bp.CRUDRichiestaUopBP,
		it.cnr.contab.ordmag.richieste.bulk.RichiestaUopRigaBulk,
		it.cnr.contab.ordmag.anag00.*"
%>

<%  
	CRUDRichiestaUopBP bp = (CRUDRichiestaUopBP)BusinessProcess.getBusinessProcess(request);
	RichiestaUopRigaBulk riga = (RichiestaUopRigaBulk)bp.getRighe().getModel();
%>

<div class="Group">
	<table class="card p-2" cellpadding="2">
		<tr>
			<%
				bp.getRighe().writeFormField(out, "findBeneServizio");
			    bp.getRighe().writeFormField(out, "unitaMisuraMinima");
			%>
		</tr>
        <tr>
			<td>
				<% bp.getRighe().writeFormLabel(out,"notaRiga");%>
			</td>      	
			<td>
				<% bp.getRighe().writeFormInput(out,"notaRiga");%>
			</td>
        </tr>
	</table>
	<table class="card mt-1 p-2" cellpadding="2">
		<tr>
			<% bp.getRighe().writeFormField(out, "findUnitaMisura");%>
			<td>
				<% bp.getRighe().writeFormLabel(out,"coefConv");%>
			</td>      	
			<td>
				<%
					if (riga != null){
						bp.getRighe().writeFormInput(out,null,"coefConv",riga.isROCoefConv(),null,"");
			    	} else {
						bp.getRighe().writeFormInput(out,null,"coefConv",false,null,"");
			    	}
			    %>
			</td>
				<%bp.getRighe().writeFormField(out, "quantitaRichiesta"); %>
		</tr>
		<tr>
			<%
				bp.getRighe().writeFormField(out, "findElementoVoce");
			%>
		</tr>
	</table>
	<table class="card mt-1 p-2" cellpadding="2">
		<tr>
			<%
				bp.getRighe().writeFormField(out, "findObbligazione");
			%>
		</tr>
		<tr>
			<%
				bp.getRighe().writeFormField(out, "findCentroResponsabilita");
			%>
		</tr>
		<tr>
			<%
				bp.getRighe().writeFormField(out, "findProgetto");
			%>
		</tr>
		<tr>
			<%
				bp.getRighe().writeFormField(out, "findLineaAttivita");
			%>
		</tr>
	</table>
</div>