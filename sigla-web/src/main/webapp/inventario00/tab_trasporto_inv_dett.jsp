<%@ page pageEncoding="UTF-8"
        import="it.cnr.jada.util.jsp.*,
            it.cnr.jada.action.*,
            java.util.*,
            it.cnr.jada.util.action.*,
            it.cnr.contab.inventario01.bulk.*,
            it.cnr.contab.inventario01.bp.*,
            it.cnr.jada.comp.ApplicationException"
    %>

<%
    CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)BusinessProcess.getBusinessProcess(request);
    Doc_trasporto_rientroBulk docTrasporto = (Doc_trasporto_rientroBulk)bp.getModel();
%>

<div class="Group">
    <table>
      <tr>
        <td colspan="4">
          <% bp.getDettBeniController().writeHTMLTable(
                pageContext,
                "righeTrasporto",
                bp.isInserting(),
                false,
                bp.isInserting(),
                null,
                "100px",
                true); %>
        </td>
      </tr>
    </table>

    <!-- ==================== FORM DETTAGLI BENE ==================== -->

    <table>
      <tr>
        <td><% bp.getDettBeniController().writeFormLabel(out,"codiceCompleto"); %></td>
        <td colspan="2"><% bp.getDettBeniController().writeFormInput(out,"codiceCompleto"); %></td>
      </tr>
    <tr>
       <td><% bp.getDettBeniController().writeFormLabel(out,"ds_bene"); %></td>
       <td colspan="2"><% bp.getDettBeniController().writeFormInput(out,null,"ds_bene",true,null,null); %></td>
    </tr>
      <tr>
        <td><% bp.getDettBeniController().writeFormLabel(out,"ti_istituzionale_commerciale"); %></td>
        <td colspan="2"><% bp.getDettBeniController().writeFormInput(out,null,"ti_istituzionale_commerciale",true,null,null); %></td>
      </tr>
      <tr>
        <td><% bp.getDettBeniController().writeFormLabel(out,"find_categoria_bene"); %></td>
        <td colspan="2"><% bp.getDettBeniController().writeFormInput(out,null,"find_categoria_bene",true,null,null); %></td>
      </tr>

    </table>

    <table>
      <tr>
      <tr>
       <td><% bp.getDettBeniController().writeFormLabel(out,"find_assegnatario"); %></td>
         <td colspan="3"><% bp.getDettBeniController().writeFormInput(out,null,"find_assegnatario",true,null,null); %></td>
      </tr>
      <td><% bp.getDettBeniController().writeFormLabel(out,"find_ubicazione"); %></td>
        <td colspan="3"><% bp.getDettBeniController().writeFormInput(out,null,"find_ubicazione",true,null,null); %></td>
      </tr>
      <tr>
        <td><% bp.getDettBeniController().writeFormLabel(out,"ds_condizione_bene"); %></td>
        <td colspan="3"><% bp.getDettBeniController().writeFormInput(out,null,"ds_condizione_bene",true,null,null); %></td>
      </tr>


    </table>

</div>