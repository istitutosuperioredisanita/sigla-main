<!-- 
 ?ResourceName "liquidazione_definitiva_iva.jsp"
 ?ResourceTimestamp "09/08/01 16.54.00"
 ?ResourceEdition "1.0"
-->

<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.gestiva00.bp.*,
		it.cnr.contab.gestiva00.core.bulk.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>

<%	LiquidazioneDefinitivaIvaBP bp = (LiquidazioneDefinitivaIvaBP)BusinessProcess.getBusinessProcess(request);
    Liquidazione_definitiva_ivaVBulk liquidazione = (Liquidazione_definitiva_ivaVBulk)bp.getModel();
	boolean richiamaChiusuraMode = bp instanceof VisualizzaLiquidazioneDefinitivaIvaBP; %>
<title><%=((richiamaChiusuraMode)?"Richiama chiusura mensile dell'IVA": "Liquidazione definitiva IVA") %></title>
</head>
<body class="Form">

	<% bp.openFormWindow(pageContext); %>

	<div class="Group card p-2" style="width:100%">
		<table width="100%">
			<tr>
				<td>
					<% bp.getController().writeFormLabel(out,"tipoSezionaleFlag");%>
				</td>
				<td>
					<% bp.getController().writeFormInput(out,null,"tipoSezionaleFlag",false,null,"onChange=\"submitForm('doOnTipoChange')\"");%>
				</td>
				<% if (bp.isAggiornaIvaDaVersareEnable()) { %>
						<td><% bp.getController().writeFormLabel(out,"debitoLastLiquidazioneProvvisoria"); %> </td>
						<td><% bp.getController().writeFormInput(out,"debitoLastLiquidazioneProvvisoria"); %>
                        <%JSPUtils.button(out,
                            bp.getParentRoot().isBootstrap() ? "fa fa-fw fa-refresh" : "img/refresh16.gif",
                            "Aggiorna Iva da versare",
                            "if (disableDblClick()) javascript:submitForm('doAggiornaIvaDaVersare')",
                            "btn-outline-info btn-title",
                            bp.getParentRoot().isBootstrap());%>
						</td>
				 <% } %>
			</tr>     	
			<% if (!richiamaChiusuraMode) { %>
				<tr>
					<td>
						<% bp.getController().writeFormLabel(out,"mese");%>
					</td>
					<td>
						<% bp.getController().writeFormInput(out,null,"mese",false,null,"onChange=\"submitForm('doOnMeseChange')\"");%>
					</td>
					<% if (bp.isAggiornaIvaDaVersareEnable() && liquidazione!=null && liquidazione.getDataAggiornamentoLastLiquidazioneProvvisoria()!=null) { %>
						<td colspan="2">
							<% bp.getController().writeFormLabel(out,"dataAggiornamentoLastLiquidazioneProvvisoria"); %> 
							<% bp.getController().writeFormInput(out,"dataAggiornamentoLastLiquidazioneProvvisoria"); %> 
						</td>
					 <% } %>
				</tr>
				<tr>
					<td>
						<% bp.getController().writeFormLabel(out,"data_da");%>
					</td>
					<td>
						<% bp.getController().writeFormInput(out,null,"data_da",false,null,"");%>
					</td>
					<td>
						<% bp.getController().writeFormLabel(out,"data_a");%>
					</td>
					<td>
						<% bp.getController().writeFormInput(out,null,"data_a",false,null,"");%>
					</td>
				</tr>
			<%	} %>
			<tr>
				<% bp.getController().writeFormField(out,"pageNumber");%>
			</tr>
		</table>
	</div>
    <table class="mt-2">
        <%JSPUtils.tabbed(
                pageContext,
                "tabLiquidazione",
                bp.getTabsLiquidazione(),
                bp.getTab("tabLiquidazione"),
                "center",
                "100%",
                null );%>
    </table>
	<div class="Group card mt-1 p-2" style="width:100%">
		<% if (!richiamaChiusuraMode) { %>
			<table>
			<%JSPUtils.tabbed(
					pageContext,
					"tab",
					bp.getTabs(),
					bp.getTab("tab"),
					"center",
					"100%",
					null );%>
			</table>
		<%	} %>
<%	bp.closeFormWindow(pageContext); %>
</body>
</html>