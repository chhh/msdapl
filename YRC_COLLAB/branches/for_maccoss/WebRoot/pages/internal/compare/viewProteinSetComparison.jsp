<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<script src="<yrcwww:link path='js/jquery.ui-1.6rc2/jquery-1.2.6.js'/>"></script>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>


<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<CENTER>

<br>

<table cellspacing="2">
<tr>
<td></td>
<logic:iterate name="comparison" property="datasets" id="dataset">
<td><bean:write name="dataset" property="datasetId"/></td>
</logic:iterate>
</tr>

<logic:iterate name="comparison" property="datasets" id="dataset">
<tr>
<td><bean:write name="dataset" property="datasetId"/></td>
</tr>
</logic:iterate>

</table>

<img src="<bean:write name='chart' />" align="top" alt="Comparison" style="padding-right:20px;"></img>

</CENTER>

<%@ include file="/includes/footer.jsp" %>