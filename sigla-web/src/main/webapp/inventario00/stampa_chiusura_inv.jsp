<%@ page pageEncoding="UTF-8"
	import="it.cnr.jada.util.jsp.*,
		it.cnr.jada.action.*,
		java.util.*,
		it.cnr.jada.util.action.*,
		it.cnr.contab.inventario00.bp.*,
		it.cnr.contab.ordmag.magazzino.bulk.*,
		it.cnr.contab.logs.bulk.*,
		java.text.*,
        java.lang.*"
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<%
    CRUDChiusuraInventarioBP bp = (CRUDChiusuraInventarioBP)BusinessProcess.getBusinessProcess(request);
 %>

<html>
<head>
<% JSPUtils.printBaseUrl(pageContext); %>
<script language="JavaScript" src="scripts/util.js"></script>
<script language="javascript" src="scripts/css.js"></script>
<title><%=bp.getBulkInfo().getShortDescription()%></title>
</head>
<body class="Form">		

<%
   bp.openFormWindow(pageContext);
 %>

  <div class="Group">
	<table>
		<tr>
		    <td>
                   <% bp.getController().writeFormLabel(out,"anno"); %>
                   <% bp.getController().writeFormInput(out,"anno"); %>
            </td>

        <%if(bp.getChiusuraAnno() != null && bp.getChiusuraAnno().getStato_job() != null){

            java.sql.Timestamp dataCalcAmm = new java.sql.Timestamp(bp.getChiusuraAnno().getDataCalcolo().getTime());
            java.sql.Date dataCalcDate = new java.sql.Date( dataCalcAmm.getTime() );

            String stato = "PROVVISORIO";
            if(bp.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PREDEFINITIVO)){
                stato = "PREDISPOSTO DEFINITIVO";
            }else if(bp.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_DEFINITIVO)){
                stato = "DEFINITIVO";
            }
            String statoJob = "COMPLETATO";

            if(bp.getChiusuraAnno().getStato_job().equals(Batch_log_tstaBulk.STATO_JOB_RUNNING)){
                statoJob = "IN CORSO";
            }else if(bp.getChiusuraAnno().getStato_job().equals(Batch_log_tstaBulk.STATO_JOB_ERROR)){
                statoJob= "IN ERRORE";
            }


            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); %>


                 <td  colspan="2"></td>
                 <td>
                    <span class="FormLabel" style="color:blue">
                        <%if(!bp.getChiusuraAnno().getStato_job().equals(Batch_log_tstaBulk.STATO_JOB_COMPLETE)){
                            if(bp.getChiusuraAnno().getPg_job() != null){%>
                                Stato procedura Ammortamento: <%=statoJob%> (consultare Funzionalità di servizio/Log Applicativi con progressivo job <%=bp.getChiusuraAnno().getPg_job()%>)
                            <%}else{%>
                                Stato procedura Ammortamento: <%=statoJob%>
                            <%}%>
                        <%}else{%>
                            Stato procedura Ammortamento: <%=statoJob%>
                            <br>
                            Data Calcolo Ammortamento:  <%=formatter.format(dataCalcDate) %>
                            <br>
                            Stato Chiusura Inventario: <%=stato%>
                        <%}%>
                    </span>
                </td>



        <%}%>


        </tr>
	</table>
  </div>

</body>
</html>