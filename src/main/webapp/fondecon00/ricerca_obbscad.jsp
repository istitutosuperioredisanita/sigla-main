<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.fondecon00.core.bulk.*,
		it.cnr.contab.fondecon00.bp.*"
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="javascript" src="scripts/css.js"></script>
<script language="JavaScript" src="scripts/util.js"></script>
<title>Ricerca impegni per contabilizzazione</title>

</head>
<body class="Form">
<%	RicercaObbScadBP bp = (RicercaObbScadBP)BusinessProcess.getBusinessProcess(request);
	Filtro_ricerca_obbligazioniVBulk filtro = (Filtro_ricerca_obbligazioniVBulk)bp.getModel();
	it.cnr.contab.anagraf00.core.bulk.TerzoBulk fornitore = filtro.getFornitore();

	bp.openFormWindow(pageContext); %>

	<div class="Group Panel card border-primary">
    	<div class="card-body">
            <div class="form-row">
                <div class="col-md-6">
                   <% bp.getController().writeFormLabel(out,"fl_fornitore");%>
                   <% bp.getController().writeFormInput(out,null,"fl_fornitore",false,null,"onClick=\"submitForm('doOnFlFornitoreChange')\"");%>
				</div>
            </div>
            <div class="form-row">
                <div class="col-md-12">
                	<% bp.getController().writeFormLabel(out,"cd_fornitore");%>
                    <% bp.getController().writeFormInput(out,"fornitore"); %>
                </div>
            </div>
      		<%
      		    if (fornitore != null && fornitore.getAnagrafico() != null) {
      		%>
          		<%
	    			if ((fornitore.getAnagrafico().isStrutturaCNR() || fornitore.getAnagrafico().isPersonaGiuridica() || fornitore.getAnagrafico().isDittaIndividuale()) &&
    					fornitore.getAnagrafico().getRagione_sociale() != null && fornitore.getAnagrafico().getRagione_sociale().length() > 0) {
	    		%>
                    <div class="form-row">
                        <div class="col-md-12">
                            <%	if (fornitore.getAnagrafico().isStrutturaCNR()) { %>
                                <b>Nome</b>
                            <%	} else { %>
                                <%bp.getController().writeFormLabel(out,"ragione_sociale");%>
                            <%  } %>
                            <%bp.getController().writeFormInput(out,"ragione_sociale");%>
                        </div>
                    </div>
                <%	}
    				if (fornitore.getAnagrafico().isPersonaFisica()) {
		        %>
                    <div class="form-row">
                        <div class="col-md-6"><% bp.getController().writeFormField(out,"cognome");%></div>
                        <div class="col-md-6"><% bp.getController().writeFormField(out,"nome");%></div>
                    </div>
                <%	} %>
                    <div class="form-row">
                        <div class="col-md-12"><% bp.getController().writeFormField(out,"denominazione_sede");%></div>
                    </div>
                <%	if (!fornitore.getAnagrafico().isStrutturaCNR()) { %>
                    <div class="form-row">
                        <% 	if (fornitore.getAnagrafico().isPersonaGiuridica() ||
                                fornitore.getAnagrafico().isDittaIndividuale()) { %>
                            <div class="col-md-6"><% bp.getController().writeFormField(out,"partita_iva");%></div>
                        <%	} %>
                        <div class="col-md-6"><% bp.getController().writeFormField(out,"codice_fiscale");%></div>
                    </div>
                <%	}
				} else { %>
                    <div class="form-row">
                        <div class="col-md-12"><% bp.getController().writeFormField(out,"ragione_sociale");%></div>
                    </div>
                    <div class="form-row">
                        <div class="col-md-6"><% bp.getController().writeFormField(out,"cognome");%></div>
                        <div class="col-md-6"><% bp.getController().writeFormField(out,"nome");%></div>
                    </div>
                    <div class="form-row">
                        <div class="col-md-6"><% bp.getController().writeFormField(out,"codice_fiscale");%></div>
                        <div class="col-md-6"><% bp.getController().writeFormField(out,"partita_iva");%></div>
                    </div>
			<%	} %>
	    </div>
	</div>

	<div class="Group Panel card border-primary">
    	<div class="card-body">
            <div class="form-row">
                <div class="col-md-6">
					<% bp.getController().writeFormLabel(out,"fl_data_scadenziario");%>
					<% bp.getController().writeFormInput(out,null,"fl_data_scadenziario",false,null,"onClick=\"submitForm('doOnFlDataScadenziarioChange')\"");%>
				</div>
            </div>
            <div class="form-row">
                <div class="col-md-12">
        			<% 	bp.getController().writeFormField(out,"data_scadenziario"); %>
        		</div>
        	</div>
       	</div>
	</div>

	<div class="Group Panel card border-primary">
    	<div class="card-body">
            <div class="form-row">
                <div class="col-md-6">
					<% bp.getController().writeFormLabel(out,"fl_importo");%>
					<% bp.getController().writeFormInput(out,null,"fl_importo",false,null,"onClick=\"submitForm('doOnFlImportoChange')\"");%>
				</div>
            </div>
            <div class="form-row">
                <div class="col-md-12">
        			<% 	bp.getController().writeFormField(out,"im_importo"); %>
        		</div>
        	</div>
       	</div>
	</div>

	<div class="Group Panel card border-primary">
    	<div class="card-body">
            <div class="form-row">
                <div class="col-md-6">
    				<% bp.getController().writeFormField(out,"fl_associate"); %>
    			</div>
        	</div>
       	</div>
	</div>

	<div class="Group Panel card border-primary">
    	<div class="card-body">
            <div class="form-row">
                <div class="col-md-6">
					<% bp.getController().writeFormLabel(out,"fl_nr_obbligazione");%>
					<% bp.getController().writeFormInput(out,null,"fl_nr_obbligazione",false,null,"onClick=\"submitForm('doOnFlNrObbligazioneChange')\"");%>
				</div>
            </div>
            <div class="form-row">
                <div class="col-md-4 h-100"><% bp.getController().writeFormField(out,"tipo_obbligazione"); %></div>
                <div class="col-md-4"><% bp.getController().writeFormField(out,"nr_obbligazione"); %></div>
                <div class="col-md-4"><% bp.getController().writeFormField(out,"nr_scadenza"); %></div>
            </div>
       	</div>
	</div>

	<% bp.closeFormWindow(pageContext); %>
</body>