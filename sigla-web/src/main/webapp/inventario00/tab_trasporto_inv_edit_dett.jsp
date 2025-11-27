<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
       it.cnr.jada.action.*,
       java.util.*,
       it.cnr.jada.util.action.*,
       it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk,
       it.cnr.contab.inventario01.bulk.*,
       it.cnr.contab.inventario01.bp.*"
%>

<%
    CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)BusinessProcess.getBusinessProcess(request);
    Doc_trasporto_rientro_dettBulk riga = (Doc_trasporto_rientro_dettBulk)bp.getEditDettController().getModel();
    Inventario_beniBulk bene = null;
    if (riga != null) bene = riga.getBene();
%>

<div class="Group">
    <table>
      <tr>
       <td colspan="4">
         <% bp.getEditDettController().writeHTMLTable(
             pageContext,
             "righeTrasporto",
             bp.isInserting(),
             false,
             bp.isInserting(),
             null,
             "200px",
             true); %>
       </td>
      </tr>
    </table>

    <table>
      <tr>
       <td><% bp.getEditDettController().writeFormLabel(out,"numeroBeneCompleto"); %></td>
       <td colspan="2"><% bp.getEditDettController().writeFormInput(out,"numeroBeneCompleto"); %></td>
      </tr>
      <tr>
       <td><% bp.getEditDettController().writeFormLabel(out,"ds_bene"); %></td>
       <td colspan="2"><% bp.getEditDettController().writeFormInput(out,null,"ds_bene",true,null,null); %></td>
      </tr>
      <tr>
       <td><% bp.getEditDettController().writeFormLabel(out,"ti_istituzionale_commerciale"); %></td>
       <td><% bp.getEditDettController().writeFormInput(out,null,"ti_istituzionale_commerciale",true,null,null); %></td>
      </tr>
      <tr>
       <td><% bp.getEditDettController().writeFormLabel(out,"find_categoria_bene"); %></td>
       <td><% bp.getEditDettController().writeFormInput(out,null,"find_categoria_bene",true,null,null); %></td>
      </tr>
    </table>

    <table>
      <tr>
       <td><% bp.getEditDettController().writeFormLabel(out,"find_ubicazione"); %></td>
       <td colspan="3"><% bp.getEditDettController().writeFormInput(out,null,"find_ubicazione",true,null,null); %></td>
      </tr>
      <tr>
       <td><% bp.getEditDettController().writeFormLabel(out,"find_assegnatario"); %></td>
       <td colspan="3"><% bp.getEditDettController().writeFormInput(out,null,"find_assegnatario",true,null,null); %></td>
      </tr>
      <tr>
       <td><% bp.getEditDettController().writeFormLabel(out,"condizione"); %></td>
       <td colspan="3"><% bp.getEditDettController().writeFormInput(out,null,"condizione",true,null,null); %></td>
      </tr>
    </table>

    <table>
      <tr>
       <td><% bp.getEditDettController().writeFormLabel(out,"quantita"); %></td>
       <td><% bp.getEditDettController().writeFormInput(out,null,"quantita",true,null,null); %></td>
      </tr>
      <tr>
       <td><% bp.getEditDettController().writeFormLabel(out,"data_effettiva_movimentazione"); %></td>
       <td colspan="3"><% bp.getEditDettController().writeFormInput(out,null,"data_effettiva_movimentazione",true,null,null); %></td>
      </tr>
    </table>

</div>