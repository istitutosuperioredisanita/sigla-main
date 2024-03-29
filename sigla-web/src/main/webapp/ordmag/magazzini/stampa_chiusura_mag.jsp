<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
			it.cnr.jada.action.*,
			java.util.*,
			it.cnr.jada.util.action.*,
			it.cnr.contab.ordmag.magazzino.bp.*,
			it.cnr.contab.ordmag.magazzino.bulk.*,
			 java.text.*,
			 java.lang.*"
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<%	StampaChiusuraMagazzinoBP bp = (StampaChiusuraMagazzinoBP)BusinessProcess.getBusinessProcess(request);%>

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext);%>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
</head>
<title><%=bp.getBulkInfo().getShortDescription()%></title>

<body class="Form">

<%	bp.openFormWindow(pageContext);%>

<table>
  <tr>

    <table >

        <%if(bp.getChiusuraAnno() != null){
             java.sql.Timestamp duva = new java.sql.Timestamp(bp.getChiusuraAnno().getDuva().getTime());
             java.sql.Timestamp dataCalc = new java.sql.Timestamp(bp.getChiusuraAnno().getDataCalcolo().getTime());
             java.sql.Date duvaDate = new java.sql.Date( duva.getTime() );
             java.sql.Date dataCalcDate = new java.sql.Date( dataCalc.getTime() );

              String stato = "PROVVISORIO";
              if(bp.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PREDEFINITIVO)){
                  stato = "PREDISPOSTO DEFINITIVO";
              }else if(bp.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_DEFINITIVO)){
                 stato = "DEFINITIVO";
              }

             SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");%>


                <tr>
                     <td colspan="5"></td>
                     <td>
                        <span class="FormLabel" style="color:blue">
                            Calcolo rimanenze aggiornato al :  <%=formatter.format(duvaDate)%>
                            <br>
                            Data Calcolo :  <%=formatter.format(dataCalcDate) %>
                            <br>
                            Stato : <%=stato%>
                        </span>
                    </td>

                </tr>

        	<%}%>
        <tr>
            <td><% bp.getController().writeFormLabel(out,"esercizio"); %></td>
            <td><% bp.getController().writeFormInput(out,"esercizio"); %></td>


         </tr>
         <tr>
             <td><% bp.getController().writeFormLabel(out,"dataInventarioInizio"); %></td>
             <td><% bp.getController().writeFormInput(out,"dataInventarioInizio"); %></td>
             <td><% bp.getController().writeFormLabel(out,"dataInventario"); %></td>
             <td colspan="5"><% bp.getController().writeFormInput(out,"dataInventario"); %></td>

         </tr>

       <tr>
            <td><% bp.getController().writeFormLabel(out,"findRaggrMagazzinoRim"); %></td>
            <td colspan="5"><% bp.getController().writeFormInput(out,"findRaggrMagazzinoRim"); %></td>
       </tr>
       <tr>
            <td><% bp.getController().writeFormLabel(out,"findCatGrp"); %></td>
            <td colspan="5"><% bp.getController().writeFormInput(out,"findCatGrp"); %></td>
        </tr>

        <tr>
           <td><% bp.getController().writeFormLabel(out,"ti_raggr_report"); %></td>
           <td><% bp.getController().writeFormInput(out,"ti_raggr_report"); %></td>
        </tr>

    </table>

	<td></td>
	 <td>
     </td>
	<td></td>
  </tr>
  </table>


</body>