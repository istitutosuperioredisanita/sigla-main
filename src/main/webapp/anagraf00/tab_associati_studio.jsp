<!-- 
 ?ResourceName ""
 ?ResourceTimestamp ""
 ?ResourceEdition ""
-->

<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.action.*,
		    it.cnr.contab.anagraf00.bp.*,
            it.cnr.contab.anagraf00.core.bulk.Anagrafico_terzoBulk"
%>

<%
  CRUDAnagraficaBP bp = (CRUDAnagraficaBP)BusinessProcess.getBusinessProcess(request);
  bp.getCrudAssociatiStudio().writeHTMLTable(pageContext,null,true,false,true,"100%","auto;max-height:30vh");
%>
<div class="Group card p-2 mt-1">
    <div class="form-row">
        <div class="col-md-8"><% bp.getCrudAssociatiStudio().writeFormField(out, "find_terzo", Boolean.FALSE);%></div>
        <div class="col-md-4"><% bp.getCrudAssociatiStudio().writeFormField(out, "dt_canc", Boolean.FALSE);%></div>
    </div>
</div>