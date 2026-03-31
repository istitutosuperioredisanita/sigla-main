<%@ page pageEncoding="UTF-8"
	import=
		"it.cnr.jada.action.*,
		it.cnr.jada.util.jsp.*,
		it.cnr.contab.inventario01.bp.*,
		it.cnr.contab.inventario01.bulk.*"
%>
<%@page import="it.cnr.jada.UserContext"%>
<%@page import="it.cnr.contab.util.Utility"%>

<% CRUDScaricoInventarioBP bp = (CRUDScaricoInventarioBP)BusinessProcess.getBusinessProcess(request); 
   Buono_carico_scaricoBulk buonoCarico = (Buono_carico_scaricoBulk)bp.getModel();
    UserContext uc = HttpActionContext.getUserContext(session);%>
  <div class="Group">
	<table>
	  <tr>
	  	<td colspan = "4">
		  <% bp.getDettController().writeHTMLTable(
				pageContext,
				"righeDaScarico",
				bp.isInserting(),
				false,
				bp.isInserting(),
				null,
				"100px",
				true); %>
		</td>
	  </tr>
	</table>
	<%if (bp.isAmministratore() &&!( (buonoCarico!=null && buonoCarico.getTipoMovimento()!=null && buonoCarico.getTipoMovimento().getFl_vendita().booleanValue()))){ %>
	<table>
	<tr>
		<td colspan="1">
		</td >
		<td colspan="4">
				<% JSPUtils.button(out,null,"Scarica Tutti definitivamente","javascript:submitForm('doScaricaTutti')", bp.getParentRoot().isBootstrap()); %>
		</td>
	</tr>
	</table>
	<%} %>
	<table>
	  <tr>
		<td><% bp.getDettController().writeFormLabel(out,"codiceCompleto"); %></td>
		<td colspan="2"><% bp.getDettController().writeFormInput(out,"codiceCompleto"); %></td>
	  </tr>
	  <tr>
		<td><% bp.getDettController().writeFormLabel(out,"ds_bene"); %></td>
		<td colspan=2><% bp.getDettController().writeFormInput(out,null,"ds_bene",true,null,null); %></td>
	  </tr>
	  <tr>
		<td><% bp.getDettController().writeFormLabel(out,"ti_istituzionale_commerciale"); %></td>
		<td colspan=2><% bp.getDettController().writeFormInput(out,null,"ti_istituzionale_commerciale",true,null,null); %></td>
	  </tr>
	  <tr>
		<td><% bp.getDettController().writeFormLabel(out,"find_categoria_bene"); %></td>
		<td><% bp.getDettController().writeFormInput(out,null,"find_categoria_bene",true,null,null); %></td>
	  </tr>	  
	</table>

	<table>	
	  <tr>
		<td><% bp.getDettController().writeFormLabel(out,"valore_iniziale"); %></td>
		<td><% bp.getDettController().writeFormInput(out,null,"valore_iniziale",true,null,null); %></td>
		<td><% bp.getDettController().writeFormLabel(out,"valore_bene"); %></td>
		<td><% bp.getDettController().writeFormInput(out,"valore_bene"); %></td>
	  </tr>
	  <%if (buonoCarico!=null && buonoCarico.getTipoMovimento()!=null && buonoCarico.getTipoMovimento().getFl_vendita().booleanValue()){ %>
	  <tr>
	  	<td><% bp.getDettController().writeFormLabel(out,"variazione_meno"); %></td>
		<td><% bp.getDettController().writeFormInput(out,null,"variazione_meno",true,null,null); %></td>
		<td><% bp.getDettController().writeFormLabel(out,"fl_totalmente_scaricato"); %></td>
		<td><% bp.getDettController().writeFormInput(out,null,"fl_totalmente_scaricato",true,null,"checked"); %></td>
	</tr>
	<tr>
		<td><% bp.getDettController().writeFormLabel(out,"valore_alienazione_apg"); %></td>
		<td colspan="3"><% bp.getDettController().writeFormInput(out,null,"valore_alienazione_apg",bp.isEditing()||!bp.isBottoneAggiungiBeneEnabled(),null,null); %></td>
	  </tr>
	  <% }else{ %>
	  <tr>
		<td><% bp.getDettController().writeFormLabel(out,"valore_unitario"); %></td>
		<td><% bp.getDettController().writeFormInput(out,null,"valore_unitario",bp.isValoreScaricatoRO(),null,null); %></td>
		<td><% bp.getDettController().writeFormLabel(out,"fl_totalmente_scaricato"); %></td>
		<td><% bp.getDettController().writeFormInput(out,null,"fl_totalmente_scaricato",bp.isFlagScaricoTotaleRO(),null,"onClick=\"submitForm('doClickFlagScaricoTotale')\""); %></td>
	  </tr>
	  <%} %>
	</table>
	<%if(Utility.createConfigurazioneCnrComponentSession().isGestioneBeneDismessoInventarioAttivo(uc)){%>
	    <table>
          <tr>
            <td><% bp.getDettController().writeFormLabel(out,"fl_dismesso"); %></td>
            <td><% bp.getDettController().writeFormInput(out,null,"fl_dismesso",true,null,null); %></td>
            <td><% bp.getDettController().writeFormLabel(out,"dt_dismesso"); %></td>
            <td><% bp.getDettController().writeFormInput(out,null,"dt_dismesso",true,null,null); %></td>
          </tr>
          <tr>
              <td><% bp.getDettController().writeFormLabel(out,"causale_dismissione"); %></td>
              <td><% bp.getDettController().writeFormInput(out,null,"causale_dismissione",true,null,null); %></td>

          </tr>
        </table>
        <table>
            <tr>
                <td>
                    <% bp.getController().writeFormLabel(out,"fl_dismesso"); %>
                </td>
                <td>
                    <% bp.getController().writeFormInput(out,null,"fl_dismesso",bp.isInserting(),null,null); %>
                </td>
                <td>
                    <% bp.getController().writeFormLabel(out,"dt_dismesso"); %>
                </td>
                <td>
                    <% bp.getController().writeFormInput(out,null,"dt_dismesso",bp.isInserting(),null,null); %>
                </td>
            </tr>
            <tr>
                <td>
                    <% bp.getController().writeFormLabel(out,"causale_dismissione"); %>
                </td>
                <td>
                    <% bp.getController().writeFormInput(out,null,"causale_dismissione",bp.isInserting(),null,null); %>

                </td>
            </tr>
        </table>
	<%}%>
  </div>