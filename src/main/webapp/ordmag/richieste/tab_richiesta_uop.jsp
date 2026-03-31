<%@ page pageEncoding="UTF-8"
	import = "it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.ordmag.richieste.bp.CRUDRichiestaUopBP,
		it.cnr.contab.ordmag.richieste.bulk.RichiestaUopBulk,
		it.cnr.contab.ordmag.anag00.*"
%>

<% CRUDBP bp = (CRUDRichiestaUopBP)BusinessProcess.getBusinessProcess(request);
   RichiestaUopBulk richiesta = (RichiestaUopBulk)bp.getModel();
%>

<div class="Group">
	<table class="card p-2" cellpadding="2">
		<tr>
			<td><%bp.getController().writeFormLabel(out, "findUnitaOperativaOrd");%></td>
            <td colspan="9"><%bp.getController().writeFormInput(out, "findUnitaOperativaOrd");%></td>
		</tr>
		<tr>
			<td><%bp.getController().writeFormLabel(out, "findNumerazioneOrd");%></td>
			<td colspan="9"><%bp.getController().writeFormInput(out, "findNumerazioneOrd");%></td>
		</tr>
		<tr>
			<%
				bp.getController().writeFormField(out, "esercizio");
			%>
			<%
				bp.getController().writeFormField(out, "cdNumeratore");
			%>
			<%
				bp.getController().writeFormField(out, "numero");
			%>
			<%
				bp.getController().writeFormField(out, "dataRichiesta");
			%>
			<% if (!bp.isSearching()) {
				 bp.getController().writeFormField(out, "stato");
			   } else {
			     bp.getController().writeFormField(out, "statoKeysForSearch");
			   } %>
		</tr>
	</table>
    <table class="card mt-1 p-2" cellpadding="2">
      <tr>      	
			<td>
				<% bp.getController().writeFormLabel(out,"dsRichiesta");%>
			</td>      	
			<td colspan="4">
				<% bp.getController().writeFormInput(out,"dsRichiesta");%>
			</td>
      </tr>
      <tr>      	
			<td>
				<% bp.getController().writeFormLabel(out,"nota");%>
			</td>      	
			<td colspan="4">
				<% bp.getController().writeFormInput(out,"nota");%>
			</td>
      </tr>
		<tr>
			<%
			bp.getController().writeFormField(out, "findUnitaOperativaOrdDest");
			%>
		</tr>
	</table>
</div>
