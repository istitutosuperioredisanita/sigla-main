<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
       it.cnr.jada.action.*,
       java.util.*,
       it.cnr.jada.util.action.*,
       it.cnr.contab.inventario00.docs.bulk.InventarioDocTRBulk,
       it.cnr.contab.inventario01.bulk.*,
       it.cnr.contab.inventario01.bp.*"
%>

<%
    CRUDRientroBeniInvBP bp =
        (CRUDRientroBeniInvBP) BusinessProcess.getBusinessProcess(request);

    /*
     * IMPORTANTISSIMO:
     * Non usare bp.isEditable() per decidere se i dettagli sono modificabili.
     *
     * Dopo la firma HappySign, isEditable() può risultare true
     * perché serve al framework allegati per consentire la gestione degli allegati.
     *
     * I dettagli invece devono restare bloccati dopo firma.
     */
    boolean dettagliModificabili = bp.isDettagliModificabili();

    RemoteDetailCRUDController controller =
        dettagliModificabili
            ? bp.getDettBeniController()
            : bp.getEditDettController();
%>

<div class="Group">

    <table>
      <tr>
       <td colspan="4">

         <% if (dettagliModificabili) { %>

          <% bp.getDettBeniController().writeHTMLTable(
                pageContext,
                "righeRientro",
                true,
                false,
                true,
                null,
                "100px",
                true); %>

         <% } else { %>

           <% bp.getEditDettController().writeHTMLTable(
               pageContext,
               "righeRientro",
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
       <td><% controller.writeFormLabel(out, "numeroBeneCompleto"); %></td>
       <td colspan="2">
           <% controller.writeFormInput(out, "numeroBeneCompleto"); %>
       </td>
      </tr>

      <tr>
       <td><% controller.writeFormLabel(out, "ds_bene"); %></td>
       <td colspan="2">
           <% controller.writeFormInput(out, null, "ds_bene", true, null, null); %>
       </td>
      </tr>

      <tr>
       <td><% controller.writeFormLabel(out, "ti_istituzionale_commerciale"); %></td>
       <td>
           <% controller.writeFormInput(out, null, "ti_istituzionale_commerciale", true, null, null); %>
       </td>
      </tr>

      <tr>
       <td><% controller.writeFormLabel(out, "find_categoria_bene"); %></td>
       <td>
           <% controller.writeFormInput(out, null, "find_categoria_bene", true, null, null); %>
       </td>
      </tr>
    </table>

    <table>
      <tr>
       <td><% controller.writeFormLabel(out, "find_assegnatario"); %></td>
       <td colspan="3">
           <% controller.writeFormInput(out, null, "find_assegnatario", true, null, null); %>
       </td>
      </tr>

      <tr>
       <td><% controller.writeFormLabel(out, "find_ubicazione"); %></td>
       <td colspan="3">
           <% controller.writeFormInput(out, null, "find_ubicazione", true, null, null); %>
       </td>
      </tr>

      <tr>
       <td><% controller.writeFormLabel(out, "ds_condizione_bene"); %></td>
       <td colspan="3">
           <% controller.writeFormInput(out, null, "ds_condizione_bene", true, null, null); %>
       </td>
      </tr>
    </table>

</div>