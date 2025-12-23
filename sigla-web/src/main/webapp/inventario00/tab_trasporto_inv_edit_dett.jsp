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
    // Seleziona il controller corretto una volta sola
    it.cnr.jada.util.action.RemoteDetailCRUDController controller =
        bp.isEditable() ? bp.getDettBeniController() : bp.getEditDettController();
%>

<div class="Group">
    <table>
      <tr>
       <td colspan="4">
         <% if (bp.isEditable()) { %>
          <% bp.getDettBeniController().writeHTMLTable(
                pageContext,
                "righeTrasporto",
                true,
                false,
                true,
                null,
                "100px",
                true); %>
         <% } else { %>
           <% bp.getEditDettController().writeHTMLTable(
               pageContext,
               "righeTrasporto",
               false,
               false,
               false,
               null,
               "200px",
               true); %>
         <% } %>
       </td>
      </tr>
    </table>

    <table>
      <tr>
       <td><% controller.writeFormLabel(out,"numeroBeneCompleto"); %></td>
       <td colspan="2"><% controller.writeFormInput(out,"numeroBeneCompleto"); %></td>
      </tr>
      <tr>
       <td><% controller.writeFormLabel(out,"ds_bene"); %></td>
       <td colspan="2"><% controller.writeFormInput(out,null,"ds_bene",true,null,null); %></td>
      </tr>
      <tr>
       <td><% controller.writeFormLabel(out,"ti_istituzionale_commerciale"); %></td>
       <td><% controller.writeFormInput(out,null,"ti_istituzionale_commerciale",true,null,null); %></td>
      </tr>
      <tr>
       <td><% controller.writeFormLabel(out,"find_categoria_bene"); %></td>
       <td><% controller.writeFormInput(out,null,"find_categoria_bene",true,null,null); %></td>
      </tr>
    </table>

    <table>
      <tr>
       <td><% controller.writeFormLabel(out,"find_assegnatario"); %></td>
       <td colspan="3"><% controller.writeFormInput(out,null,"find_assegnatario",true,null,null); %></td>
      </tr>
      <tr>
       <td><% controller.writeFormLabel(out,"find_ubicazione"); %></td>
       <td colspan="3"><% controller.writeFormInput(out,null,"find_ubicazione",true,null,null); %></td>
      </tr>
      <tr>
       <td><% controller.writeFormLabel(out,"ds_condizione_bene"); %></td>
       <td colspan="3"><% controller.writeFormInput(out,null,"ds_condizione_bene",true,null,null); %></td>
      </tr>
    </table>

</div>