<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.docamm00.tabrif.bulk.*,
		it.cnr.jada.*,
		it.cnr.contab.docamm00.docs.bulk.*,
		it.cnr.contab.docamm00.bp.*,
		it.cnr.contab.anagraf00.tabrif.bulk.*"
%>
<%	CRUDAutofatturaBP bp = (CRUDAutofatturaBP)BusinessProcess.getBusinessProcess(request);
	AutofatturaBulk autofattura = (AutofatturaBulk)bp.getModel();
	UserContext uc = HttpActionContext.getUserContext(session);
%>
   <div class="Group card">
	 <table>

	  <tr>
	  <td>
	 	<% bp.getController().writeFormLabel(out,"esercizio");%>
	   </td>
	   <td>
	   	<% bp.getController().writeFormInput(out,null,"esercizio",false,null,"");%>
	   </td>
	   <td>   
	 	<% bp.getController().writeFormLabel(out,"pg_autofattura");%>
	 	</td><td>
	 	<% bp.getController().writeFormInput(out,null,"pg_autofattura",false,null,"");%>
	   </td>
	 </tr>
	 <tr>
       		<td>
           		<% bp.getController().writeFormLabel(out,"dt_registrazione");%>
      		</td><td>
      			<% bp.getController().writeFormInput(out,null,"dt_registrazione",false,null,"submitForm('doOnDataRegistrazioneChange')\"");%>
           	</td>
     		<td>
           		<% bp.getController().writeFormLabel(out,"data_esigibilita_iva");%>
     	 	</td><td>
      			<% bp.getController().writeFormInput(out,null,"data_esigibilita_iva",false,null,"");%>
           	</td>
          </tr>
     <tr>
     		<td>
           		<% bp.getController().writeFormLabel(out,"protocollo_iva");%>
           	</td><td>
           		<% bp.getController().writeFormInput(out,null,"protocollo_iva",false,null,"");%>
           	</td>
     		<td>
           		<% bp.getController().writeFormLabel(out,"protocollo_iva_generale");%>
           	</td>
           	<td>
           		<% bp.getController().writeFormInput(out,null,"protocollo_iva_generale",false,null,"");%>
           	</td>
      </tr>
        <tr>
            <td>
                    <% bp.getController().writeFormLabel(out,"stato_cofi");%>
                       	</td>
                       	<td>
                       		<% bp.getController().writeFormInput(out,null,"stato_cofi",false,null,"");%>
                       	</td>

            <td >
                  			<% bp.getController().writeFormLabel(out,"fl_liquidazione_differita");%>
                  			<% bp.getController().writeFormInput(out,null,"fl_liquidazione_differita",true,null,"onClick=\"submitForm('doOnLiquidazioneDifferitaChange')\"");%>
            </td>
        <tr>
	</table>
	  <div class="Group card">
      	<table>
    		<tr >
    			<% if (!bp.isSearching()) { %>
    		     	<td >
    		      		<% bp.getController().writeFormLabel(out,"ti_istituz_commerc");%>
    		      	</td>
    		     	<td colspan="3">
    		      		<% bp.getController().writeFormInput(out,null,"ti_istituz_commerc",false,null,"onChange=\"submitForm('doOnIstituzionaleCommercialeChange')\"");%>
    		      	</td>
    			<% } else { %>
    		     	<td>
    		      		<% bp.getController().writeFormLabel(out,"ti_istituz_commercSearch");%>
    		      	</td>
    		     	<td colspan="10">
    		      		<% bp.getController().writeFormInput(out,null,"ti_istituz_commercSearch",false,null,"onChange=\"submitForm('doOnIstituzionaleCommercialeChange')\"");%>
    		      	</td>
    			<% } %>
          </tr>
	 <% if (bp.isSearching()) { %>
         <tr>
            <td>
                <% bp.getController().writeFormLabel(out, "sezionaliFlagsRadioGroup");%>
            </td>
            <td colspan="3">
                <% bp.getController().writeFormInput(out, null, "sezionaliFlagsRadioGroup", false, null, "onClick=\"submitForm('doOnSezionaliFlagsChange')\"");%>
            </td>
         </tr>
         <% } else { %>
         	      <tr>
         	     	<td>
         	      		<% bp.getController().writeFormLabel(out,"fl_intra_ue");%>
         	      	</td>
         	     	<td>
         	      		<% bp.getController().writeFormInput(out,null,"fl_intra_ue",false,null,"");%>
         	      	</td>
         	     	<td width="100">&nbsp;</td>
         	     	<td>
         	      		<% bp.getController().writeFormLabel(out,"fl_extra_ue");%>
         	      	</td>
         	     	<td>
         	      		<% bp.getController().writeFormInput(out,null,"fl_extra_ue",false,null,"");%>
         	      	</td>
         			<td width="100">&nbsp;</td>
         	      	<td>
         	      		<% bp.getController().writeFormLabel(out,"fl_split_payment");%>
         	      	</td>
         	     	<td>
         	     		<% bp.getController().writeFormInput(out,"fl_split_payment");%>
         	     	</td>
         	      </tr>
         	      <tr>
         	      	<td>
         	      		<% bp.getController().writeFormLabel(out,"fl_san_marino_con_iva");%>
         	      	</td>
         	     	<td>
         	     		<% bp.getController().writeFormInput(out,null,"fl_san_marino_con_iva",false,null,"");%>
         	     	</td>
         	     	<td width="100">&nbsp;</td>
         	      	<td>
         	      		<% bp.getController().writeFormLabel(out,"fl_san_marino_senza_iva");%>
         	      	</td>
         	     	<td>
         	     		<% bp.getController().writeFormInput(out,null,"fl_san_marino_senza_iva",false,null,"");%>
         	     	</td>

         	      </tr>
         	     <% } %>
         <tr>
               	<td>
               		<% bp.getController().writeFormLabel(out,"sezionale");%>
               	</td>
              	<td colspan="10">
              		<% bp.getController().writeFormInput(out,null,"sezionale",false,null,"");%>
              	</td>
               </tr>


     </table>
   </div>

