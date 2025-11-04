<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
        it.cnr.jada.action.*,
        it.cnr.contab.inventario01.bp.*" %>

<% CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP)BusinessProcess.getBusinessProcess(request); %>

<div class="Group card" style="width:100%; margin-top:10px">

    <div style="margin-bottom:10px; padding:10px; background-color:#e7f3ff; border-left:4px solid #2196F3;">
        <strong>Modalità Modifica:</strong>
        <ul style="margin:5px 0 0 20px; padding:0;">
            <li>Visualizzazione dei beni rientrati nel documento</li>
            <li>Possibilità di modificare quantità e data effettiva di movimentazione</li>
            <li>Non è possibile aggiungere/rimuovere beni dopo il salvataggio</li>
        </ul>
    </div>

    <table class="Panel" style="width:100%">
        <tr>
            <td>
                <% bp.getEditDettController().writeHTMLTable(
                    pageContext,
                    "100%",
                    "300px"); %>
            </td>
        </tr>
    </table>

</div>