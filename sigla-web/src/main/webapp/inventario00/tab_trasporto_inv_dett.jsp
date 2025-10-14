<%@ page pageEncoding="UTF-8"
    import=
       "it.cnr.jada.action.*,
       it.cnr.jada.util.jsp.*,
       it.cnr.contab.inventario01.bp.*,
       it.cnr.contab.docamm00.tabrif.bulk.*"
%>
<%@page import="it.cnr.jada.UserContext"%>
<%@page import="it.cnr.contab.util.Utility"%>

<% CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)BusinessProcess.getBusinessProcess(request);
   Doc_trasporto_rientroBulk docTrasporto = (Doc_trasporto_rientroBulk)bp.getModel();
   UserContext uc = HttpActionContext.getUserContext(session);%>
  <div class="Group">
	<table>
	  <tr>
	  	<td colspan="4">
		  <% bp.getDettaglio().writeHTMLTable(
				pageContext,
				"righeDaTrasporto",
				bp.isCRUDAddButtonEnabled(),
				false,
				bp.isCRUDDeleteButtonEnabled(),
				null,
				"100px",
				true); %>
		</td>
	  </tr>
	</table>

	<table>
	  <tr>
		<td><% bp.getDettaglio().writeFormLabel(out,"bene.codiceCompleto"); %></td>
		<td colspan="2"><% bp.getDettaglio().writeFormInput(out,"bene.codiceCompleto"); %></td>
	  </tr>
	  <tr>
		<td><% bp.getDettaglio().writeFormLabel(out,"bene.ds_bene"); %></td>
		<td colspan="2"><% bp.getDettaglio().writeFormInput(out,null,"bene.ds_bene",!bp.isInserting(),null,null); %></td>
	  </tr>
	  <tr>
		<td><% bp.getDettaglio().writeFormLabel(out,"bene.ti_istituzionale_commerciale"); %></td>
		<td colspan="2"><% bp.getDettaglio().writeFormInput(out,null,"bene.ti_istituzionale_commerciale",true,null,null); %></td>
	  </tr>
	  <tr>
		<td><% bp.getDettaglio().writeFormLabel(out,"bene.find_categoria_bene"); %></td>
		<td><% bp.getDettaglio().writeFormInput(out,null,"bene.find_categoria_bene",!bp.isInserting(),null,null); %></td>
	  </tr>
	</table>

	<table>
	  <tr>
		<td><% bp.getDettaglio().writeFormLabel(out,"bene.ubicazione"); %></td>
		<td colspan="3"><% bp.getDettaglio().writeFormInput(out,null,"bene.ubicazione",!bp.isInserting(),null,null); %></td>
	  </tr>
	  <tr>
		<td><% bp.getDettaglio().writeFormLabel(out,"bene.condizioneBene"); %></td>
		<td colspan="3"><% bp.getDettaglio().writeFormInput(out,null,"bene.condizioneBene",!bp.isInserting(),null,null); %></td>
	  </tr>
	  <tr>
		<td><% bp.getDettaglio().writeFormLabel(out,"quantita"); %></td>
		<td><% bp.getDettaglio().writeFormInput(out,null,"quantita",!bp.isQuantitaEnabled(),null,null); %></td>
		<td><% bp.getDettaglio().writeFormLabel(out,"statoTrasporto"); %></td>
		<td><% bp.getDettaglio().writeFormInput(out,null,"statoTrasporto",true,null,null); %></td>
	  </tr>
	  <tr>
		<td><% bp.getDettaglio().writeFormLabel(out,"note"); %></td>
		<td colspan="3"><% bp.getDettaglio().writeFormInput(out,null,"note",false,null,null); %></td>
	  </tr>
	</table>
  </div>