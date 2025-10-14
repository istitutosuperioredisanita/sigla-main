<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk,
		it.cnr.contab.docamm00.tabrif.bulk.*,
		it.cnr.contab.inventario01.bp.*"
%>
<% CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)BusinessProcess.getBusinessProcess(request);
    Doc_trasporto_rientro_dettBulk riga = (Doc_trasporto_rientro_dettBulk)bp.getDettaglio().getModel();
    Inventario_beniBulk bene = null;
    if (riga != null) bene = riga.getBene(); %>
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
             "200px",
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
       <td>
          <% bp.getDettaglio().writeFormLabel(out,"bene.ti_istituzionale_commerciale"); %>
       </td>
       <td>
          <% bp.getDettaglio().writeFormInput(out,null,"bene.ti_istituzionale_commerciale",true,null,null); %>
       </td>
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
       <td colspan="3"><% bp.getDettaglio().writeFormInput(out,null,"note",!bp.isInserting(),null,null); %></td>
      </tr>
    </table>
  </div>