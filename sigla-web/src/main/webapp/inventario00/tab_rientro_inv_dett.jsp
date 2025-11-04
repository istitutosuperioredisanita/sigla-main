<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
        it.cnr.jada.action.*,
        it.cnr.contab.inventario01.bp.*" %>

<% CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP)BusinessProcess.getBusinessProcess(request); %>

<div class="Group card" style="width:100%; margin-top:10px">

    <div style="margin-bottom:10px; padding:10px; background-color:#f0f8ff; border-left:4px solid #2196F3;">
        <strong>Istruzioni:</strong>
        <ul style="margin:5px 0 0 20px; padding:0;">
            <li>Compilare la testata del documento (Tipo Movimento, Data Rientro, ecc.)</li>
            <li>Premere il pulsante <strong>"Aggiungi Beni"</strong> per selezionare i beni da far rientrare</li>
            <li>I beni devono essere presenti in un documento di trasporto <strong>FIRMATO</strong></li>
            <li>Per i beni con accessori, verrà chiesto se includerli nel rientro</li>
        </ul>
    </div>

    <table class="Panel" style="width:100%">
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

    <% if (bp.isBottoneAggiungiBeneEnabled()) { %>
    <div style="margin-top:10px; padding:8px; background-color:#fff3cd; border-left:4px solid #ffc107;">
        <span style="font-size:0.9em; color:#856404;">
            <strong>Nota:</strong> Saranno visualizzati solo i beni presenti in documenti di trasporto firmati
        </span>
    </div>
    <% } %>

</div>
