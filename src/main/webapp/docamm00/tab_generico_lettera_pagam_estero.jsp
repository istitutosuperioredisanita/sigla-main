<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.docamm00.tabrif.bulk.*,
		it.cnr.contab.docamm00.docs.bulk.*,
		it.cnr.contab.doccont00.core.bulk.*,
		it.cnr.contab.docamm00.bp.*"
%>

<%	
	CRUDDocumentoGenericoPassivoBP bp = (CRUDDocumentoGenericoPassivoBP)BusinessProcess.getBusinessProcess(request);
	Documento_genericoBulk documento = (Documento_genericoBulk)bp.getModel();
%>
<div class="Group card p-3" style="width:100%">
	<table width="100%">
		<tr>
			<% bp.writeFormFieldDoc1210(out,"creaLettera");%>
			<% bp.writeFormFieldDoc1210(out,"cancellaLettera");%>
			<% bp.writeFormFieldDoc1210(out,"disassociaLettera");%>
		</tr>
	</table>
</div>


<div class="Group card p-3" style="width:100%">
	<div class="GroupLabel h3 text-primary"><% bp.writeFormInput(out, "displayStatoTrasmissione");%></div>

	<%if(documento.getLettera_pagamento_estero()!=null && documento.getLettera_pagamento_estero().getLettera_vers().equals(Lettera_pagam_esteroBulk.PRIMA_VERSIONE) ) {%>

        <table width="100%">
            <tr>
                <% bp.writeFormFieldDoc1210(out,"esercizio_lettera");%>
                <% bp.writeFormFieldDoc1210(out,"pg_lettera");%>
            </tr>
            <tr>
                <% bp.writeFormFieldDoc1210(out,"dt_registrazione_lettera");%>
            </tr>
            <tr>
                <% bp.writeFormFieldDoc1210(out,"im_pagamento");%>
            </tr>
            <tr>
                <% bp.writeFormFieldDoc1210(out,"im_commissioni_lettera");%>
            </tr>
            <tr>
                <td colspan="4">
                    <div class="GroupLabel h3 text-primary">Stampa documento</div>
                    <div class="Group card p-3" style="width:90%">
                        <table>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "divisa");%>
                            </tr>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "beneficiario");%>
                            </tr>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "paese_beneficiario");%>
                            </tr>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "indirizzo_beneficiario");%>
                            </tr>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "iban");%>
                            </tr>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "indirizzo");%>
                            </tr>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "indirizzo_swift");%>
                            </tr>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "bic_banca_intermediaria");%>
                            </tr>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "motivo_pag");%>
                            </tr>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "commissioni_spese");%>
                            </tr>
                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <div class="GroupLabel h3 text-primary">Istruzioni Speciali</div>
                    <div class="Group card" style="width:100%">
                        <table width="100%">
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "istruzioni_speciali_1");%>
                            </tr>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "istruzioni_speciali_2");%>
                            </tr>
                            <tr>
                                <% bp.writeFormFieldDoc1210(out, "istruzioni_speciali_3");%>
                            </tr>
                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <div class="GroupLabel h3 text-primary">Sospeso</div>
                    <div class="Group" style="width:100%">
                        <% bp.writeFormInput(out,null, "sospeso",(documento.getLettera_pagamento_estero()!=null && documento.getLettera_pagamento_estero().getStato_trasmissione().compareTo(MandatoBulk.STATO_TRASMISSIONE_TRASMESSO)!=0),null,""); %>
                    </div>
                </td>
            </tr>
        </table>
	<%}else{%>

	    <div class="Group card p-2 mb-2">
            <div class="form-row">
                <div class="col-md-4"><% bp.writeFormFieldDoc1210(out,"esercizio_lettera");%></div>
                <div class="col-md-4"> <% bp.writeFormFieldDoc1210(out,"pg_lettera");%></div>
                 <div class="col-md-4"> <% bp.writeFormFieldDoc1210(out,"dt_registrazione_lettera");%></div>
            </div>
            <div class="form-row">
                <div class="col-md-4"> <% bp.writeFormFieldDoc1210(out,"im_pagamento");%></div>
                <div class="col-md-4"> <% bp.writeFormFieldDoc1210(out,"im_commissioni_lettera");%></div>
            </div>

        </div>
        <div class="GroupLabel h3 text-primary">Stampa documento</div>
        <div class="Group card p-2 mb-2">
            <div class="form-row">
                <div class="col-md-2 h-100"><% bp.writeFormFieldDoc1210(out, "divisa");%></div>
                <div class="col-md-2 h-100"><% bp.writeFormFieldDoc1210(out, "cod_divisa_accr");%></div>
             </div>
             <div class="form-row">
                <div class="col-md-12><% bp.writeFormFieldDoc1210(out, "iban");%></div>
             </div>
             <div class="form-row">
                <!-- COD SWIFT -->
                <div class="col-md-2"> <% bp.writeFormFieldDoc1210(out, "indirizzo_swift_v2");%></div>
                <!-- BANCA SWIFT -->
                <div class="col-md-10">  <% bp.writeFormFieldDoc1210(out, "indirizzo_v2");%> </div>
             </div>
              <div class="form-row">
                 <div class="col-md-2"> <% bp.writeFormFieldDoc1210(out, "cod_aba_routing");%></div>
                 <div class="col-md-10">  <% bp.writeFormFieldDoc1210(out, "banca_aba_routing");%> </div>
              </div>
               <div class="form-row">
                   <div class="col-md-2"> <% bp.writeFormFieldDoc1210(out, "cod_swift_tramite");%></div>
                   <div class="col-md-10">  <% bp.writeFormFieldDoc1210(out, "banca_tramite");%> </div>
               </div>
               <div class="form-row">
                  <div class="col-md-2"> <% bp.writeFormFieldDoc1210(out, "notice_agreed");%></div>
                  <div class="col-md-4">  <% bp.writeFormFieldDoc1210(out, "purpose_pay");%> </div>
               </div>
               <div class="form-row">
                    <div class="col-md-12"><% bp.writeFormFieldDoc1210(out, "segnalazioni_v2");%></div>
               </div>
               <div class="GroupLabel h3 text-primary"></div>
               <div class="form-row">
                    <div  class="col-md-8"><% bp.writeFormFieldDoc1210(out, "beneficiario");%></div>
                    <div  class="col-md-4"><% bp.writeFormFieldDoc1210(out, "cod_lei_beneficiario");%></div>
               </div>
                <div class="form-row">
                    <div  class="col-md-10"><% bp.writeFormFieldDoc1210(out, "indirizzo_beneficiario");%></div>
                    <div  class="col-md-2"><% bp.writeFormFieldDoc1210(out, "civico_beneficiario");%></div>
                </div>
                <div class="form-row">
                    <div  class="col-md-4"><% bp.writeFormFieldDoc1210(out, "cap_beneficiario");%></div>
                    <div  class="col-md-8"><% bp.writeFormFieldDoc1210(out, "paese_beneficiario");%></div>
                </div>
                <div class="GroupLabel h3 text-primary"></div>
                <div class="form-row">
                    <div  class="col-md-8"><% bp.writeFormFieldDoc1210(out, "beneficiario_eff");%></div>
                    <div  class="col-md-4"><% bp.writeFormFieldDoc1210(out, "cod_lei_ben_eff");%></div>
               </div>
                <div class="form-row">
                    <div  class="col-md-10"><% bp.writeFormFieldDoc1210(out, "indirizzio_ben_eff");%></div>
                    <div  class="col-md-2"><% bp.writeFormFieldDoc1210(out, "civico_ben_eff");%></div>
                </div>
                <div class="form-row">
                    <div  class="col-md-4"><% bp.writeFormFieldDoc1210(out, "cap_ben_eff");%></div>
                    <div  class="col-md-8"><% bp.writeFormFieldDoc1210(out, "paese_ben_eff");%></div>
                </div>
                <div class="GroupLabel h3 text-primary"></div>
                <div class="form-row">
                     <div  class="col-md-12"><% bp.writeFormFieldDoc1210(out, "motivo_pag");%></div>
                </div>
                <div class="form-row">
                    <div  class="col-md-12"><% bp.writeFormFieldDoc1210(out, "commissioni_spese");%></div>
                </div>
                <div class="form-row">
                  <div  class="col-md-12"><% bp.writeFormFieldDoc1210(out, "conto_commissioni");%></div>
                </div>
                <div class="form-row">
                    <div  class="col-md-12"><% bp.writeFormFieldDoc1210(out, "ordinante");%></div>
                </div>
        </div>

        <div class="GroupLabel h3 text-primary">Sospeso</div>
            <div class="Group" style="width:100%">
                <% bp.writeFormInput(out,null, "sospeso",(documento.getLettera_pagamento_estero()!=null && documento.getLettera_pagamento_estero().getStato_trasmissione().compareTo(MandatoBulk.STATO_TRASMISSIONE_TRASMESSO)!=0),null,""); %>
            </div>
	<%}%>
</div>