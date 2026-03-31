<!--
 ?ResourceName "TemplateForm.jsp"
 ?ResourceTimestamp "08/11/00 16.43.22"
 ?ResourceEdition "1.0"
-->

<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
	        it.cnr.jada.action.*,
	        java.util.*,
	        it.cnr.jada.util.action.*,
	        it.cnr.contab.doccont00.bp.*,
	        it.cnr.contab.doccont00.core.bulk.*"
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
	<head>
		<script language="JavaScript" src="scripts/util.js"></script>
		<% JSPUtils.printBaseUrl(pageContext);%>
	</head>
	<script language="JavaScript" src="scripts/util.js"></script>
	<script language="javascript" src="scripts/css.js"></script>
	<title>Mandato automatico</title>
	<body class="Form">
<%
		MandatoAutomaticoWizardBP bp = (MandatoAutomaticoWizardBP)BusinessProcess.getBusinessProcess(request);
		it.cnr.contab.doccont00.core.bulk.MandatoAutomaticoWizardBulk mandatoAutomatico = (it.cnr.contab.doccont00.core.bulk.MandatoAutomaticoWizardBulk)bp.getModel();
		String collapseCriteriRicercaIconClass = bp.isCriteriRicercaCollapse() ? "fa-angle-down" : "fa-angle-up";
		String collapseDettDocumentiIconClass = bp.isDettDocumentoCollapse() ? "fa-angle-down" : "fa-angle-up";
%>
<% 	bp.openFormWindow(pageContext); %>
    <div class="card">
        <div class="card-header d-flex">
            <a onclick="submitForm('doToggle(criteriRicerca)')" class="text-primary d-flex w-100">
                <span class="h4 mb-0">Criteri di Ricerca</span>
                <i aria-hidden="true" class="ml-auto fa fa-2x <%=collapseCriteriRicercaIconClass%>"></i>
            </a>
        </div>
        <% if (!bp.isCriteriRicercaCollapse()) { %>
            <div class="card-block p-2">
                <div class="Group">
                    <table cellpadding="5px">
                        <tr>
                            <td><% bp.getController().writeFormLabel( out, "find_cd_terzo"); %></td>
                            <td><% bp.getController().writeFormInput( out, "find_cd_terzo"); %></td>
                            <td><% bp.getController().writeFormLabel( out, "find_cd_precedente"); %></td>
                            <td><% bp.getController().writeFormInput( out, "find_cd_precedente"); %></td>
                        </tr>
                        <tr>
                            <td><% bp.getController().writeFormLabel( out, "find_cognome"); %></td>
                            <td><% bp.getController().writeFormInput( out, "find_cognome"); %></td>
                            <td><% bp.getController().writeFormLabel( out, "find_nome"); %></td>
                            <td><% bp.getController().writeFormInput( out, "find_nome"); %></td>
                        </tr>
                        <tr>
                            <td><% bp.getController().writeFormLabel( out, "find_partita_iva"); %></td>
                            <td><% bp.getController().writeFormInput( out, "find_partita_iva"); %></td>
                            <td><% bp.getController().writeFormLabel( out, "find_codice_fiscale"); %></td>
                            <td><% bp.getController().writeFormInput( out, "find_codice_fiscale"); %></td>
                        </tr>
                        <tr>
                            <td><% bp.getController().writeFormLabel( out, "find_ragione_sociale"); %></td>
                            <td colspan=3><% bp.getController().writeFormInput( out, "find_ragione_sociale"); %></td>
                        </tr>
                        <tr>
                            <td colspan=4 align="center"><% bp.getController().writeFormInput( out, "find_doc_passivi"); %></td>
                        </tr>
                    </table>
                </div>
                <div class="Group">
                <table border="0" cellspacing="0" cellpadding="2">
                        <td><% bp.getController().writeFormLabel( out, "ti_impegni"); %></td>
                        <td><% bp.getController().writeFormInput( out, "ti_impegni"); %></td>
                </table>
                </div>
                <div class="Group">
                <table border="0" cellspacing="0" cellpadding="2">
                    <tr>
                        <td><% bp.getController().writeFormLabel( out, "find_nr_fattura_fornitore"); %></td>
                        <td><% bp.getController().writeFormInput( out, "find_nr_fattura_fornitore"); %></td>
                        <td><% bp.getController().writeFormLabel( out, "find_pg_doc_passivo"); %></td>
                        <td><% bp.getController().writeFormInput( out, "find_pg_doc_passivo"); %></td>
                    </tr>
                    <tr>
                        <td><% bp.getController().writeFormLabel( out, "find_cd_tipo_documento_amm"); %></td>
                        <td><% bp.getController().writeFormInput( out, "find_cd_tipo_documento_amm"); %></td>
                        <td></td>
                        <td></td>
                    </tr>
                    <tr>
                        <td><% bp.getController().writeFormLabel( out, "find_pg_obbligazione"); %></td>
                        <td><% bp.getController().writeFormInput( out, "find_pg_obbligazione"); %></td>
                        <td><% bp.getController().writeFormLabel( out, "find_dt_scadenza"); %></td>
                        <td><% bp.getController().writeFormInput( out, "find_dt_scadenza"); %></td>
                    </tr>
                    <tr>
                        <td><% bp.getController().writeFormLabel( out, "find_im_scadenza"); %></td>
                        <td><% bp.getController().writeFormInput( out, "find_im_scadenza"); %></td>
                        <td><% bp.getController().writeFormLabel( out, "find_ti_pagamento"); %></td>
                        <td><% bp.getController().writeFormInput( out, "find_ti_pagamento"); %></td>
                    </tr>
                </table>
                </div>
                <table border="0" cellspacing="0" cellpadding="2"  align="center">
                    <tr>
                        <td colspan=2>
                        	<%JSPUtils.button(out,
                        			bp.getParentRoot().isBootstrap() ? "fa fa-external-link faa-shake" : "img/find16.gif",
                        			bp.getParentRoot().isBootstrap() ? "fa fa-external-link faa-shake" : "img/find16.gif",
                   					bp.getParentRoot().isBootstrap() ? "Ricerca Impegni":"Ricerca<BR>Impegni",
                        					"submitForm('doCercaImpegni')",
                        					"btn-outline-primary btn-title faa-parent animated-hover",
                        					true,
                        					bp.getParentRoot().isBootstrap());%>
                        </td>
                        <td colspan=2>
                        	<%JSPUtils.button(out,
                        			bp.getParentRoot().isBootstrap() ? "fa fa-external-link faa-shake" : "img/find16.gif",
                        			bp.getParentRoot().isBootstrap() ? "fa fa-external-link faa-shake" : "img/find16.gif",
                   					bp.getParentRoot().isBootstrap() ? "Ricerca Documenti":"Ricerca<BR>Documenti",
                        					"submitForm('doCercaDocPassivi')",
                        					"btn-outline-primary btn-title faa-parent animated-hover",
                        					true,
                        					bp.getParentRoot().isBootstrap());%>
                        </td>
                    </tr>
                </table>
            </div>
        <% } %>
    </div>

    <% if (bp.isCriteriRicercaCollapse()) { %>
        <div class="Group card p-3" width="100%">
            <div class="Group">
            <table class="Panel">
                <tr>
                    <td><% bp.getController().writeFormLabel( out, "esercizio"); %></td>
                    <td><% bp.getController().writeFormInput( out, "esercizio"); %></td>
                    <td colspan="4" align="right"><% bp.getController().writeFormLabel( out, "dt_emissione"); %>
                        <% bp.getController().writeFormInput( out, "dt_emissione"); %></td>
                </tr>
                <% if (mandatoAutomatico.isAutomatismoDaImpegni()) { %>
                <tr>
                    <td><% bp.getController().writeFormLabel( out, "terzo_cd_terzo"); %></td>
                    <td colspan="3"><% bp.getController().writeFormInput( out, "terzo_cd_terzo"); %>
                                    <% bp.getController().writeFormInput( out, "terzo_ds_terzo"); %> </td>
                    <td colspan="2" align="right"><% bp.getController().writeFormLabel( out, "ti_istituz_commerc"); %>
                                                  <% bp.getController().writeFormInput( out, "ti_istituz_commerc"); %></td>
                </tr>
                <tr>
                    <td><% bp.getController().writeFormLabel( out, "cd_modalita_pag"); %></td>
                    <td colspan= "5"><% bp.getController().writeFormInput( out,"default", "cd_modalita_pag", false, null,"onchange=\"submitForm('doCambiaModalitaPagamento')\"" ); %>
                                     <% bp.getController().writeFormLabel( out, "banca"); %>
                                     <% bp.getController().writeFormInput( out, "banca"); %></td>
                </tr>
                <tr>
                    <td><% bp.getController().writeFormLabel( out, "dt_da_competenza_coge"); %></td>
                    <td colspan= "5"><% bp.getController().writeFormInput( out, "dt_da_competenza_coge" ); %>
                                     <% bp.getController().writeFormLabel( out, "dt_a_competenza_coge"); %>
                                     <% bp.getController().writeFormInput( out, "dt_a_competenza_coge"); %></td>
                </tr>
                <% } %>
                <tr>
                    <td><% bp.getController().writeFormLabel( out, "terzo_tipo_bollo"); %></td>
                    <td colspan= "5"><% bp.getController().writeFormInput( out, "terzo_tipo_bollo" ); %></td>
                </tr>
            </table>
            </div>
            <table class="Panel">
                <% if (mandatoAutomatico.isAutomatismoDaImpegni()) { %>
                    <tr>
                        <td><b ALIGN="CENTER">Impegni</b></td>
                        <% if (bp.isFlCalcoloAutomaticoCheckboxVisible()) { %>
                          <td align="right"><% bp.getController().writeFormInput(out,null,"fl_imputazione_manuale",!bp.isFlCalcoloAutomaticoCheckboxEnabled(),null,"onclick=\"submitForm('doCambiaFl_imputazione_manuale')\""); %>
                                            <% bp.getController().writeFormLabel( out, "fl_imputazione_manuale"); %></td>
                        <% } %>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <% if ( mandatoAutomatico.isFl_imputazione_manuale() )
                                    bp.getImpegni().writeHTMLTable(pageContext,"impegni_man",false,false,false,"100%","100px", false);
                               else
                                    bp.getImpegni().writeHTMLTable(pageContext,"impegni_auto",false,false,false,"100%","100px", false);
                            %>
                        </td>
                    </tr>
                <% } else if (mandatoAutomatico.isAutomatismoDaDocumentiPassivi()) { %>
                    <tr>
                        <td colspan=2>
                              <b ALIGN="CENTER"><font size=2>Documenti passivi disponibili</font></b>
                              <% if ( mandatoAutomatico.isMandatiCreati() )
                                    bp.getDocumentiPassivi().writeHTMLTable(pageContext,"mandatoAutomatico",false,false,false,"100%","200px", true);
                                 else
                                    bp.getDocumentiPassivi().writeHTMLTable(pageContext,null,false,false,false,"100%","200px", true);
                              %>
                        </td>
                    </tr>
                <% } %>
                <tr>
                <td colspan="2">
                    <span class="pt-1">
                        <div class="card">
                            <div class="card-header d-flex">
                                <a onclick="submitForm('doToggle(dettDocumento)')" class="text-primary d-flex w-100">
                                    <span class="h4 mb-0">Dettagli Riga Documento</span>
                                    <i aria-hidden="true" class="ml-auto fa fa-2x <%=collapseDettDocumentiIconClass%>"></i>
                                </a>
                            </div>
                            <% if (!bp.isDettDocumentoCollapse()) { %>
                            <div class="card-block p-2">
                                <div>
                                    <div class="form-row">
                                        <div class="form-group col-md-4">
                                            <% bp.getDocumentiPassivi().writeFormField( out, "importoRigaMandatoWizard", Boolean.FALSE); %>
                                        </div>
                                        <% if (bp.getDocumentiPassivi().getModel()!=null &&
                                                ((V_doc_passivo_obbligazione_wizardBulk)bp.getDocumentiPassivi().getModel()).isFatturaPassiva()) { %>
                                            <div class="form-group col-md-4">
                                                <% bp.getDocumentiPassivi().writeFormField( out, "imponibileRigaMandatoWizard", Boolean.FALSE); %>
                                            </div>
                                            <div class="form-group col-md-4">
                                                <% bp.getDocumentiPassivi().writeFormField( out, "impostaRigaMandatoWizard", Boolean.FALSE); %>
                                            </div>
                                        <% } %>
                                    </div>
                                    <div class="form-row">
                                        <div class="form-group col-md-4 h-100">
                                            <% bp.getDocumentiPassivi().writeFormField( out,"default", "modalitaPagamentoRigaDocumentoWizard", Boolean.FALSE); %>
                                        </div>
                                        <div class="form-group col-md-8 h-100">
                                            <% bp.getDocumentiPassivi().writeFormField( out, "bancaRigaDocumentoWizard", Boolean.FALSE); %>
                                        </div>
                                    </div>
                                    <% if (bp.getDocumentiPassivi().getModel()!=null &&
                                       ((V_doc_passivo_obbligazione_wizardBulk)bp.getDocumentiPassivi().getModel()).getCd_terzo_cessionario()!=null) { %>
                                        <div class="form-row">
                                            <div class="form-group col-md-4 h-100">
                                                <% bp.getDocumentiPassivi().writeFormField( out, "cdTerzoDelegatoRigaDocumentoWizard", Boolean.FALSE); %>
                                            </div>
                                        </div>
                                    <% } %>
                                </div>
                            </div>
                            <% } %>
                        </div>
                    </span>
                </td></tr>

                <tr>
                <td colspan="2" align="center">
                    <% JSPUtils.button(out,
                            bp.getParentRoot().isBootstrap() ? "fa fa-fw fa-floppy-o text-primary" : "img/save24.gif",
                            bp.getParentRoot().isBootstrap() ? "fa fa-fw fa-floppy-o text-primary" : "img/save24.gif",
                            "Genera Mandati",
                            "javascript:submitForm('doEmettiMandato')",
                            "btn-outline-secondary btn-title",
                            bp.isEmettiMandatoButtonEnabled(),
                            bp.getParentRoot().isBootstrap());
                    %>
                </tr>

                <% if (mandatoAutomatico.getMandatiColl().size()>0) { %>
                    <tr>
                        <td colspan="2"><b>Mandati Generati</b></td>
                    </tr>

                    <tr>
                        <td colspan="2">
                            <% bp.getMandati().writeHTMLTable(pageContext,"mandatoAutomatico",false,false,false,"100%","100px", true);%>
                       </td>
                    </tr>

                    <tr>
                       <td colspan="2" align="center"><% JSPUtils.button(out,bp.encodePath("img/edit24.gif"),bp.encodePath("img/edit24.gif"),"Visualizza", "javascript:submitForm('doVisualizzaMandato')", bp.isVisualizzaMandatoButtonEnabled(),bp.getParentRoot().isBootstrap()); %></td>
                    </tr>
                <% } %>
            </table>
        </div>
    <% } %>
<%	bp.closeFormWindow(pageContext); %>
</body>
</html>