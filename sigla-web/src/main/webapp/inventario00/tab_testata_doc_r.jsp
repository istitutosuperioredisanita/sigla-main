<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
        it.cnr.jada.action.*,
        it.cnr.contab.inventario01.bp.*,
        it.cnr.contab.inventario01.bulk.*" %>

<script language="JavaScript">
function doStampaDocTrasporto() {
    doPrint('<%=JSPUtils.getAppRoot(request)%>genericdownload/stampaDocTrasportoRientro.html?methodName=stampaDocTrasportoRientro&it.cnr.jada.action.BusinessProcess=<%=bp.getPath()%>',
            'Documento di Trasporto Rientro',
            'toolbar=no, location=no, directories=no, status=no, menubar=no,resizable,scrollbars,width=800,height=600').focus() ;
}
</script>

<% CRUDRientroBeniInvBP bp = (CRUDRientroBeniInvBP)BusinessProcess.getBusinessProcess(request);
   Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk)bp.getModel(); %>

<div class="Group card" style="width:100%">
<table class="Panel">

    <tr>
      <td><% bp.getController().writeFormLabel( out, "pgInventario"); %></td>
      <td colspan="3">
        <% bp.getController().writeFormInput( out, null, "pgInventario", true, null, ""); %>
        <% bp.getController().writeFormInput( out, null, "descrizioneInventario", true, null, ""); %>
      </td>
    </tr>

    <tr>
      <td><% bp.getController().writeFormLabel( out, "esercizio"); %></td>
      <td><% bp.getController().writeFormInput( out, null, "esercizio", true, null, ""); %></td>

      <td><% bp.getController().writeFormLabel( out, "pgDocTrasportoRientro"); %></td>
      <td><% bp.getController().writeFormInput( out, null, "pgDocTrasportoRientro", !bp.isInserting(), null, ""); %></td>
    </tr>

    <tr>
      <td><% bp.getController().writeFormLabel(out,"tipoMovimento"); %></td>
      <td colspan="3">
        <% bp.getController().writeFormInput(out, null, "tipoMovimento",
            bp.isTipoMovimentoReadOnly(), null,
            bp.isTipoMovimentoReadOnly() ? null : "onChange=\"submitForm('doSelezionaTipoMovimento')\""); %>
      </td>
    </tr>

    <tr>
      <td><% bp.getController().writeFormLabel( out, "dsDocTrasportoRientro"); %></td>
      <td colspan="3">
        <% bp.getController().writeFormInput(
            out,
            null,
            "dsDocTrasportoRientro",
            bp.isInputReadonly(),
            null,
            "style='width:100%'"); %>
      </td>
    </tr>

    <!-- ==================== TIPO RITIRO ==================== -->
    <tr>
      <td><% bp.getController().writeFormLabel(out,"tipoRitiro"); %></td>
      <td colspan="3">
        <% bp.getController().writeFormInput(
            out,
            null,
            "tipoRitiro",
            bp.isTipoRitiroReadOnly(),
            null,
            null
        ); %>
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
        <td><% bp.getController().writeFormLabel(out,"find_assegnatario"); %></td>
        <td colspan="3">
          <% bp.getController().writeFormInput(out, null, "find_assegnatario",
              bp.isAssegnatarioReadOnly(), null,
              "onChange=\"submitForm('doOnDipendenteChange')\""); %>
        </td>
      </tr>
    <% } %>

    <!-- ==================== NOMINATIVO VETTORE ==================== -->
    <% if (bp.isNominativoVettoreVisible()) { %>
      <tr>
        <td><% bp.getController().writeFormLabel(out,"nominativoVettore"); %></td>
        <td colspan="3">
          <% bp.getController().writeFormInput(out, null, "nominativoVettore",
              bp.isNominativoVettoreReadOnly(), null, null); %>
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
</div>