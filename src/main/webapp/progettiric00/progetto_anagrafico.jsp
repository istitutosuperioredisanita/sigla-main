<%@ page pageEncoding="UTF-8"
        import="it.cnr.jada.action.*,
                it.cnr.jada.bulk.*,
                it.cnr.jada.util.action.*,
                it.cnr.jada.util.jsp.*,
                it.cnr.contab.progettiric00.bp.*,
                it.cnr.contab.progettiric00.core.bulk.*"
%>

<%
    TestataProgettiRicercaBP bp = (TestataProgettiRicercaBP) BusinessProcess.getBusinessProcess(request);
    bp.getCrudProgetto_anagrafico().writeHTMLTable(pageContext,bp.getAnagraficheProgettoFormName(),true,false,true,"100%","150px");
%>

<div class="Group card p-2 mb-2">

        <table class="w-100">
             <% bp.getCrudProgetto_anagrafico().writeForm(out, bp.getAnagraficheProgettoFormName());  %>

        </table>
</div>
