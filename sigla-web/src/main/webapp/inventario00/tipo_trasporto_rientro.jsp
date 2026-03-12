<%@ page pageEncoding="UTF-8"
    import="it.cnr.jada.util.jsp.*,
       it.cnr.jada.action.*,
       it.cnr.contab.inventario00.tabrif.bulk.*,
       it.cnr.contab.inventario00.bp.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<title>Tipo Trasporto/Rientro</title>
</head>
<body class="Form">

<% CRUDTipoTrasportoRientroBP bp = (CRUDTipoTrasportoRientroBP)BusinessProcess.getBusinessProcess(request);
     bp.openFormWindow(pageContext);
     Tipo_trasporto_rientroBulk tipoTR = (Tipo_trasporto_rientroBulk)bp.getModel(); %>

    <table class="Panel">
       <tr>
         <td>
          <% bp.getController().writeFormLabel(out,"cdTipoTrasportoRientro");%>
         </td>
         <td>
          <% bp.getController().writeFormInput(out,"cdTipoTrasportoRientro");%>
         </td>
       </tr>
       <tr>
         <td>
          <% bp.getController().writeFormLabel(out,"dsTipoTrasportoRientro");%>
         </td>
         <td>
          <% bp.getController().writeFormInput(out,null,"dsTipoTrasportoRientro",(tipoTR!=null && !tipoTR.isCancellabile()),null,null);%>
         </td>
       </tr>
       <tr>
         <td>
          <% bp.getController().writeFormLabel(out,"tiDocumento");%>
         </td>
         <td>
          <% bp.getController().writeFormInput(out,null,"tiDocumento",bp.isTiDocumentoReadonly(),null,"onClick=\"submitForm('doOnTiDocumentoChange')\"");%>
         </td>
       </tr>
       <tr>
         <td>
          <% bp.getController().writeFormLabel(out,"flAbilitaNote");%>
         </td>
         <td>
          <% bp.getController().writeFormInput(out,null,"flAbilitaNote",bp.isFlAbilitaNoteReadonly(),null,null);%>
         </td>
       </tr>
       <tr>
         <td>
          <% bp.getController().writeFormLabel(out,"dtCancellazione");%>
         </td>
         <td>
            <% bp.getController().writeFormInput(out,null,"dtCancellazione",bp.isDtCancellazioneReadonly(),null,null);%>
         </td>
       </tr>
    </table>
<%  bp.closeFormWindow(pageContext); %>
</body>
</html>