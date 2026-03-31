<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.cori00.docs.bulk.*,
		it.cnr.contab.cori00.bp.*"
%>

<% CRUDLiquidazioneCORIBP bp = (CRUDLiquidazioneCORIBP)BusinessProcess.getBusinessProcess(request);	
	 Liquid_coriBulk liquid_cori = (Liquid_coriBulk)bp.getModel(); %>

    <div class="Group Panel card border-primary">
        <table class="Panel">
            <tr><% bp.getController().writeFormField(out,"findCds");%></tr>
            <tr><% bp.getController().writeFormField(out,"findUnitaOrganizzativa");%></tr>
            <tr><% bp.getController().writeFormField(out,"esercizio");%></tr>
        </table>
    </div>

	<div class="Group Panel card border-primary">
        <div class="col-md-3 form-check form-check-inline">
            <div class="form-check-input">
                <% bp.getController().writeFormInput(out,null,"da_esercizio_precedente",bp.isCalcolato(),null,null);%>
            </div>
            <div class="form-check-label">
                <% bp.getController().writeFormLabel(out,"da_esercizio_precedente");%>
            </div>
        </div>
        <div class="form-group form-row">
            <div class="col-md-2"><% bp.getController().writeFormField(out,"default","pg_liquidazione"); %></div>
            <div class="col-md-2 offset-md-1"><% bp.getController().writeFormField(out,"default","dacr"); %></div>
            <div class="col-md-2 offset-md-1 h-100"><% bp.getController().writeFormField(out,"default","stato"); %></div>
        </div>
        <div class="form-group form-row">
            <div class="col-md-2">
                <% bp.getController().writeFormLabel(out,"dt_da");%>
                <% bp.getController().writeFormInput(out,null,"dt_da",bp.isCalcolato(),null,null);%>
			</div>
            <div class="col-md-2">
                <% bp.getController().writeFormLabel(out,"dt_a");%>
                <% bp.getController().writeFormInput(out,null,"dt_a",bp.isCalcolato(),null,null);%>
			</div>
        </div>
        <% if (bp.isInserting()){ %>
            <table>
                <tr>
                    <td>
                        <% JSPUtils.button(out,null,null,"Calcola liquidazione","javascript:submitForm('doCalcolaLiquidazione')", !bp.isCalcolato(),bp.getParentRoot().isBootstrap()); %>
                    </td>
                    <% if (bp.isAbilitatoF24()){ %>
                    <td>
                        <% JSPUtils.button(out,null,null,"Seleziona Gruppi F24","javascript:submitForm('doSelezionaF24')",bp.isCalcolato(),bp.getParentRoot().isBootstrap()); %>
                    </td>
                    <td>
                        <% JSPUtils.button(out,null,null,"Seleziona Gruppi F24 Previd.","javascript:submitForm('doSelezionaF24Prev')",bp.isCalcolato(), bp.getParentRoot().isBootstrap()); %>
                    </td>
                    <% } %>
            </tr>
            </table>
        <% } %>
    </div>

<% if ((bp.isCalcolato() || bp.isEditing())|| bp.isViewing()){ %>
    <table class="Group" style="width:100%">
       <tr>
        <td>
          <span class="FormLabel" style="color:blue">Gruppi:</span>
        </td>
       </tr>
       <tr>
         <td>
        <% bp.getGruppi().writeHTMLTable(pageContext,"v_liquid_gruppo",false,false,false,"100%","auto;max-height:50vh"); %>
        </td>
       </tr>

       <% if (bp.isInserting()){ %>
           <tr>
            <td>
                <% JSPUtils.button(out,null,null,"Liquida","javascript:submitForm('doLiquidazione')", !bp.isLiquidato(),bp.getParentRoot().isBootstrap()); %>
            </td>
           </tr>
       <% } %>
    </table>

    <table class="Group" style="width:100%">
       <tr>
        <td>
          <span class="FormLabel" style="color:blue">Dettagli:</span>
        </td>
       </tr>
       <tr>
         <td>
            <% bp.getGruppiDet().writeHTMLTable(pageContext,"v_liquid_gruppo_det",false,false,false,"100%","auto;max-height:30vh"); %>
        </td>
       </tr>
    </table>
<% } %>