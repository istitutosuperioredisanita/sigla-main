<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
        it.cnr.jada.action.*,
        it.cnr.contab.pdg00.bp.*,
        it.cnr.contab.pdg00.bulk.*"
%>

<%
    CRUDAccrualMefBP bp = (CRUDAccrualMefBP) BusinessProcess.getBusinessProcess(request);
    AccrualBulk accrual = (AccrualBulk) bp.getModel();
%>

<div class="Group card" style="width:100%">
    <table class="Panel">

        <tr>
            <td>
                <% bp.getController().writeFormLabel(out, "esercizio"); %>
            </td>
            <td>
                <% bp.getController().writeFormInput(out, "esercizio"); %>
            </td>
        </tr>

        <tr>
            <td>
                <% bp.getController().writeFormLabel(out, "stato"); %>
            </td>
            <td>
                <% bp.getController().writeFormInput(out, "stato"); %>
            </td>
        </tr>

        <tr>
            <td>
                <% bp.getController().writeFormLabel(out, "esito"); %>
            </td>
            <td>
                <% bp.getController().writeFormInput(out, "esito"); %>
            </td>
        </tr>

    </table>
</div>