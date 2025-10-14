<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.inventario00.tabrif.bulk.*,
		it.cnr.contab.inventario01.bulk.*,
		it.cnr.contab.docamm00.docs.bulk.Fattura_attiva_rigaIBulk,
		it.cnr.contab.inventario01.bp.*"
%>
<% CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP)BusinessProcess.getBusinessProcess(request);
