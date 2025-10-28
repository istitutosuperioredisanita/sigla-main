<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
        it.cnr.jada.action.*,
        it.cnr.contab.inventario01.bp.*,
        it.cnr.contab.inventario01.bulk.*"
%>
<%
    CRUDTraspRientInventarioBP bp = (CRUDTraspRientInventarioBP)BusinessProcess.getBusinessProcess(request);
    Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk)bp.getModel();
%>

<table>

    <!-- ==================== DATI IDENTIFICATIVI ==================== -->

    <tr>
        <td width="15%"><% bp.getController().writeFormLabel(out,"esercizio"); %></td>
        <td width="35%"><% bp.getController().writeFormInput(out,"esercizio"); %></td>
        <td width="15%"><% bp.getController().writeFormLabel(out,"pgDocTrasportoRientro"); %></td>
        <td width="35%"><% bp.getController().writeFormInput(out,"pgDocTrasportoRientro"); %></td>
    </tr>

    <!-- ==================== DATI INVENTARIO ==================== -->

    <tr>
        <td><% bp.getController().writeFormLabel(out,"cdUoConsegnataria"); %></td>
        <td colspan="3">
            <% bp.getController().writeFormInput(out,"cdUoConsegnataria"); %>
            <% bp.getController().writeFormInput(out,"dsUoConsegnataria"); %>
        </td>
    </tr>

    <tr>
        <td><% bp.getController().writeFormLabel(out,"cdConsegnatario"); %></td>
        <td colspan="3">
            <% bp.getController().writeFormInput(out,"cdConsegnatario"); %>
            <% bp.getController().writeFormInput(out,"cognomeConsegnatario"); %>
        </td>
    </tr>

    <tr>
        <td><% bp.getController().writeFormLabel(out,"cdDelegato"); %></td>
        <td colspan="3">
            <% bp.getController().writeFormInput(out,"cdDelegato"); %>
            <% bp.getController().writeFormInput(out,"cognomeDelegato"); %>
        </td>
    </tr>

    <!-- ==================== INVENTARIO E DATA ==================== -->

    <tr>
        <td><% bp.getController().writeFormLabel(out,"pgInventario"); %></td>
        <td><% bp.getController().writeFormInput(out,"pgInventario"); %></td>
        <td><% bp.getController().writeFormLabel(out,"dataRegistrazione"); %></td>
        <td><% bp.getController().writeFormInput(out,null,"dataRegistrazione",bp.isEditing(),null,null); %></td>
    </tr>

    <!-- ==================== TIPO MOVIMENTO ==================== -->

    <tr>
        <td><% bp.getController().writeFormLabel(out,"tipoMovimento"); %></td>
        <td colspan="3">
            <% bp.getController().writeFormInput(out, null, "tipoMovimento",
                bp.isTipoMovimentoReadOnly(), null,
                bp.isTipoMovimentoReadOnly() ? null : "onChange=\"submitForm('doSelezionaTipoMovimento')\""); %>
        </td>
    </tr>

    <!-- ==================== DESCRIZIONE ==================== -->

    <tr>
        <td><% bp.getController().writeFormLabel(out,"dsDocTrasportoRientro"); %></td>
        <td colspan="3"><% bp.getController().writeFormInput(out,"dsDocTrasportoRientro"); %></td>
    </tr>

    <!-- ==================== SEPARATORE ==================== -->

    <tr>
        <td colspan="4" style="border-top: 2px solid #003d7a; padding-top: 15px;"></td>
    </tr>

    <!-- ==================== TIPO RITIRO ==================== -->

    <tr>
        <td><% bp.getController().writeFormLabel(out,"tipoRitiro"); %></td>
        <td colspan="3">
            <% bp.getController().writeFormInput(out, null, "tipoRitiro",
                bp.isTipoRitiroReadOnly(), null,
                "onClick=\"submitForm('doOnTipoRitiroChange')\""); %>
        </td>
    </tr>

    <!-- ==================== CAMPI CONDIZIONALI ==================== -->

    <% if (bp.isDestinazioneVisible()) { %>

        <tr>
            <td><% bp.getController().writeFormLabel(out,"destinazione"); %></td>
            <td colspan="3"><% bp.getController().writeFormInput(out,"destinazione"); %></td>
        </tr>

        <tr>
            <td valign="top"><% bp.getController().writeFormLabel(out,"indirizzo"); %></td>
            <td colspan="3"><% bp.getController().writeFormInput(out,"indirizzo"); %></td>
        </tr>

    <% } %>

    <!-- ==================== ASSEGNATARIO (INCARICATO) ==================== -->

    <% if (bp.isAssegnatarioVisible()) { %>
        <tr>
            <td><% bp.getController().writeFormLabel(out,"anagDipRitiro"); %></td>
            <td colspan="3">
                <% bp.getController().writeFormInput(out, null, "anagDipRitiro",
                    bp.isAssegnatarioReadOnly(), null,
                    "onChange=\"submitForm('doOnDipendenteChange')\""); %>
            </td>
        </tr>
    <% } %>

    <!-- ==================== NOTE ==================== -->
        <% if (bp.isNoteAbilitate()) { %>
            <tr>
                <td valign="top"><% bp.getController().writeFormLabel(out,"note"); %></td>
                <td colspan="3"><% bp.getController().writeFormInput(out,"note"); %></td>
            </tr>
        <% } %>

</table>