<%@page import="it.cnr.jada.UserContext"%>
<%@page import="it.cnr.contab.util.Utility"%>
<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.inventario00.tabrif.bulk.*,
		it.cnr.contab.inventario00.docs.bulk.*,
		it.cnr.contab.inventario00.bp.*"
%>

<% CRUDInventarioBeniBP bp = (CRUDInventarioBeniBP)BusinessProcess.getBusinessProcess(request); 
   Inventario_beniBulk bene = (Inventario_beniBulk)bp.getModel();
   UserContext uc = HttpActionContext.getUserContext(session);
%>
<div class="Group card p-2 m-1">
    <div class="form-row">
        <div class="col-md-3"><% bp.getController().writeFormField(out, "etichetta", Boolean.FALSE);%></div>
        <div class="col-md-3"><% bp.getController().writeFormField(out, "nr_inventario", Boolean.FALSE);%></div>
        <div class="col-md-3"><% bp.getController().writeFormField(out, "progressivo", Boolean.FALSE);%></div>
        <div class="col-md-3"><% bp.getController().writeFormField(out, "id_bene_origine", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-3"><% bp.getController().writeFormField(out, "dt_acquisizione", Boolean.FALSE);%></div>
        <div class="col-md-3"><% bp.getController().writeFormField(out, "num_buono", Boolean.FALSE);%></div>
        <% if (bene != null && bene.isDaOrdini()){ %>
        <div class="col-md-3 d-flex align-items-center" style="gap: 1rem;"><% bp.getController().writeFormField(out, "fl_onere", Boolean.FALSE);%></div>
        <%}%>
    </div>
</div>
<div class="Group card p-2 m-1">
    <div class="form-row">
        <div class="col-md-12"><% bp.getController().writeFormField(out, "ds_bene", Boolean.FALSE);%></div>
    </div>
    <div class="form-row">
        <div class="col-md-6">
            <% bp.getController().writeFormLabel(out,"find_categoria_bene"); %>
            <% bp.getController().writeFormInput(out,null,"find_categoria_bene",bp.isInserting(),null,null); %>
        </div>
        <div class="col-md-3">
            <% bp.getController().writeFormLabel(out,"targa"); %>
            <% bp.getController().writeFormInput(out,null,"targa"); %>
        </div>
        <div class="col-md-3">
            <% bp.getController().writeFormLabel(out,"seriale"); %>
            <% bp.getController().writeFormInput(out,null,"seriale",((bene!=null && bene.isROseriale())),null,null); %>
        </div>
    </div>

    <div class="form-row p-2">
        <div class="col-md-3 h-100">
            <% bp.getController().writeFormLabel(out,"condizioni"); %>
            <% bp.getController().writeFormInput(out,null,"condizioni",bp.isInserting(),null,null); %>
        </div>
        <div class="col-md-3 offset-md-1">
            <% bp.getController().writeFormField(out,"cd_barre"); %>
        </div>
        <div class="col-md-3 offset-md-1">
            <% bp.getController().writeFormLabel(out,"ti_istituzionale_commerciale"); %>
            <% bp.getController().writeFormInput(out,null,"ti_istituzionale_commerciale",true,null,null); %>
        </div>
    </div>
</div>
<% if (bene != null && bene.isBeneAccessorio()) { %>
<div class="Group card p-2 m-1">
    <div class="form-row">
        <div class="col-md-12">
            <% bp.getController().writeFormLabel(out,"find_bene_principale"); %>
            <% bp.getController().writeFormInput(out,null,"find_bene_principale",true,null,null); %>
        </div>
    </div>
</div>
<% } %>
<div class="Group card p-2 m-1">
    <div class="form-row">
        <div class="col-md-8">
            <% bp.getController().writeFormLabel(out,"find_ubicazione"); %>
            <% bp.getController().writeFormInput(out,null,"find_ubicazione",(bp.isInserting()||bene.isBeneAccessorio()),null,null); %>
        </div>
        <div class="col-md-4">
            <% bp.getController().writeFormLabel(out,"collocazione"); %>
            <% bp.getController().writeFormInput(out,null,"collocazione",!bene.isPubblicazione(),null,null); %>
        </div>
    </div>
    <div class="form-row">
        <div class="col-md-8">
            <% bp.getController().writeFormLabel(out,"find_assegnatario"); %>
            <% bp.getController().writeFormInput(out,null,"find_assegnatario",(bp.isInserting()||bene.isBeneAccessorio()),null,null); %>
        </div>
        <div class="col-md-4">
            <% bp.getController().writeFormLabel(out,"uo_assegnatario"); %>
            <% bp.getController().writeFormInput(out,null,"uo_assegnatario",true,null,null); %>
        </div>
    </div>
</div>

<div class="Group card p-2 m-1">
    <div class="form-row">
        <div class="col-md-3">
            <% bp.getController().writeFormLabel(out,"valore_iniziale"); %>
            <% bp.getController().writeFormInput(out,null,"valore_unitario",bp.isROvaloreIniziale(),null,null); %>
        </div>
        <div class="col-md-3">
            <% bp.getController().writeFormLabel(out,"variazione_piu"); %>
            <% bp.getController().writeFormInput(out,null,"variazione_piu",bp.isROvaloreIniziale(),null,null); %>
        </div>
        <div class="col-md-3">
            <% bp.getController().writeFormLabel(out,"variazione_meno"); %>
            <% bp.getController().writeFormInput(out,null,"variazione_meno",bp.isROvaloreIniziale(),null,null); %>
        </div>
        <div class="col-md-3">
            <% bp.getController().writeFormLabel(out,"valore_bene"); %>
            <% bp.getController().writeFormInput(out,null,"valore_bene",bp.isInserting(),null,null); %>
        </div>
    </div>
    <div class="form-row">
        <div class="col-md-3 offset-md-3">
            <% bp.getController().writeFormField(out,"imponibile_ammortamento"); %>
        </div>
        <div class="col-md-3">
            <% bp.getController().writeFormField(out,"valore_ammortizzato"); %>
        </div>
        <div class="col-md-3">
            <% bp.getController().writeFormField(out,"valore_residuo_da_ammortizzare"); %>
        </div>
    </div>
</div>

<div class="Group card p-2 m-1">
    <div class="form-row">
        <div class="col-md-3">
            <% bp.getController().writeFormLabel(out,"dataBolla"); %>
            <% bp.getController().writeFormInput(out,null,"dataBolla"); %>
        </div>
        <div class="col-md-3 offset-md-1">
            <% bp.getController().writeFormLabel(out,"numeroBolla"); %>
            <% bp.getController().writeFormInput(out,null,"numeroBolla"); %>
        </div>
        <div class="col-md-3 offset-md-1">
            <% bp.getController().writeFormLabel(out,"estremiOrdine"); %>
            <% bp.getController().writeFormInput(out,null,"estremiOrdine"); %>
        </div>
    </div>
</div>

<% if (bp.isGestioneBeneDismessoInventarioAttivo()) { %>
<div class="Group card p-2 m-1">
    <div class="form-row">
        <div class="col-md-2 d-flex align-items-center" style="gap: 1rem;">
            <% bp.getController().writeFormField(out,"fl_totalmente_scaricato"); %>
        </div>
    </div>
    <div class="form-row">
        <div class="col-md-2 d-flex align-items-center" style="gap: 1rem;">
            <% bp.getController().writeFormLabel(out,"fl_dismesso"); %>
            <% bp.getController().writeFormInput(out,null,"fl_dismesso",bp.isInserting(),null,null); %>
        </div>
        <div class="col-md-2">
            <% bp.getController().writeFormLabel(out,"dt_dismesso"); %>
            <% bp.getController().writeFormInput(out,null,"dt_dismesso",bp.isInserting(),null,null); %>
        </div>
        <div class="col-md-8">
            <% bp.getController().writeFormLabel(out,"causale_dismissione"); %>
            <% bp.getController().writeFormInput(out,null,"causale_dismissione",bp.isInserting(),null,null); %>
        </div>
    </div>
</div>
<% } %>
