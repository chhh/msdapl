<%@page import="org.yeastrc.ms.domain.search.Program"%>
<%@page import="org.yeastrc.ms.domain.general.MsInstrument"%>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

						
<!-- SEARCHES FOR THE EXPERIMENT -->
<logic:notEmpty name="experiment" property="searches">
	<logic:iterate name="experiment" property="searches" id="search">
		<div style="background-color: #FFFFE0; margin:5 5 5 5; padding:5; border: 1px dashed gray;" >
		<table width="100%">
			<tr>
				<td width="33%"><b>Program: </b>&nbsp;
				<b><bean:write name="search" property="searchProgram"/>
				&nbsp;
				<bean:write name="search" property="searchProgramVersion"/></b></td>
				
				
				<!-- !!!!!! SEQUEST !!!!!! -->
				<logic:equal name="search" property="searchProgram" value="<%=Program.SEQUEST.toString() %>">
				<td width="33%">
					<b>
					<html:link action="viewSequestResults.do" 
								paramId="ID" 
								paramName="search" paramProperty="id">[View Results]</html:link>
					<!-- <html:link action="percolatorPepXmlDownloadForm.do" 
								paramId="ID" 
								paramName="search" paramProperty="id">[PepXML]</html:link> -->
					</b>
				</td>
				</logic:equal>
				
				<!-- !!!!!! MASCOT !!!!!! -->
				<logic:equal name="search" property="searchProgram" value="<%=Program.MASCOT.toString() %>">
				<td width="33%">
				<b>
					<html:link action="viewMascotResults.do" 
								paramId="ID" 
								paramName="search" paramProperty="id">[View Results]</html:link>
				</b>
				</td>
				</logic:equal>
				
				<!-- !!!!!! XTANDEM !!!!!! -->
				<logic:equal name="search" property="searchProgram" value="<%=Program.XTANDEM.toString() %>">
				<td width="33%">
				<b>
					<html:link action="viewXtandemResults.do" 
								paramId="ID" 
								paramName="search" paramProperty="id">[View Results]</html:link>
				</b>
				</td>
				</logic:equal>
				
				
				<td width="33%"><b>Search Date: </b>&nbsp;
				<bean:write name="search" property="searchDate"/></td>
				
			</tr>
			<tr>
				<td><b>Search Database: </b></td>
				<td><bean:write name="search" property="searchDatabase"/></td>
			</tr>
			<tr>
				<td><b>Enzyme: </b></td>
				<td><bean:write name="search" property="enzymes"/></td>
			</tr>
			<tr>
				<td valign="top"><b>Residue Modifications: </b></td>
				<td width="33%" valign="top"><b>Static: </b>
				<bean:write name="search" property="staticResidueModifications"/></td>
				<td width="33%" valign="top"><b>Dynamic: </b>
				<bean:write name="search" property="dynamicResidueModifications"/></td>
			</tr>
			<tr>
				<td valign="top"><b>Terminal Modifications: </b></td>
				<td width="33%" valign="top"><b>Static: </b>
				<bean:write name="search" property="staticTerminalModifications"/></td>
				<td width="33%" valign="top"><b>Dynamic: </b>
				<bean:write name="search" property="dynamicTerminalModifications"/></td>
			</tr>
		</table>
		</div>	
		
		
		<logic:equal name="experiment" property="analysisProgramName" value="<%=Program.PERCOLATOR.displayName() %>">
			<div>
			&nbsp;&nbsp;
			<html:link action="percolatorUploadForm.do" paramId="experimentId" paramName="experiment" paramProperty="id"><span style="color:red; font-weight:bold;">[Add Percolator Results]</span></html:link>
			&nbsp;&nbsp;
			<html:link action="percolatorRunForm.do" paramId="searchId" paramName="search" paramProperty="id"><span style="color:red; font-weight:bold;">[Run Percolator]</span></html:link>
			</div>
		</logic:equal>

	</logic:iterate>
</logic:notEmpty>

<!-- SEARCH ANALYSES FOR THE EXPERIMENT -->
<logic:notEmpty name="experiment" property="analyses">

<logic:equal name="writeAccess" value="true">	
<div>

</div>
</logic:equal>

	<!-- !!!!!! PERCOLATOR !!!!!! -->
	<logic:equal name="experiment" property="analysisProgramName" value="<%=Program.PERCOLATOR.displayName() %>">
		
			<logic:iterate name="experiment" property="analyses" id="analysis" indexId="analysis_idx">
			<div style="background-color: #F0FFF0; margin:5 5 5 5; padding:5; border: 1px dashed gray;" >
			<table width="100%">
			<tbody>
			<tr>
				<td valign="middle" width="1%"><b><nobr>ID <bean:write name="analysis" property="id"/></nobr></b></td>
				<td  valign="middle" width="10%"><b>Percolator <bean:write name="analysis" property="analysisProgramVersionShort"/></b></td>
				<td valign="middle">
					<span id="analysis_<bean:write name='analysis' property='id'/>_text"><bean:write name="analysis" property="comments"/></span>
					<logic:equal name="writeAccess" value="true">
					<logic:notEmpty name="analysis" property="comments">
						<span class="underline clickable small_font editableComment" style="color:red;"
						id="analysis_<bean:write name='analysis' property='id'/>"
						title="expt_<bean:write name='experiment' property='id'/>" >[Edit]</span>
					</logic:notEmpty>
					<logic:empty name="analysis" property="comments">
						<span class="underline clickable small_font editableComment" style="color:red;"
						id="analysis_<bean:write name='analysis' property='id'/>"
						title="expt_<bean:write name='experiment' property='id'/>" >[Add Comments]</span>
					</logic:empty>
					</logic:equal>
				</td>
				<logic:equal name="analysis" property="complete" value="true">
					<td valign="middle">
						<b><html:link action="viewPercolatorResults.do" paramId="ID" paramName="analysis" paramProperty="id">[View Results]</html:link></b>
					</td>
					<td>
						<b><a href="<yrcwww:link path='viewQCPlots.do?'/>analysisId=<bean:write name='analysis' property='id' />&experimentId=<bean:write name='experiment' property='id'/>"> 
						[Statistics]</a></b>
					</td>
					<td>
						<b><a href="<yrcwww:link path='newPercolatorProteinInference.do?'/>searchAnalysisId=<bean:write name='analysis' property='id' />&projectId=<bean:write name='experiment' property='projectId'/>"> 
						[Infer Proteins]</a></b>
						<a href="" onclick="openInformationPopup('<yrcwww:link path='pages/internal/docs/proteinInference.jsp'/>'); return false;">
	   					<img src="<yrcwww:link path='images/info_16.png'/>" align="bottom" border="0"/></a>
					</td>
				</logic:equal>
				
				<logic:equal name="analysis" property="complete" value="false">
					<td>
					<!-- Job FAILED -->
					<logic:equal name="analysis" property="job.failed" value="true">
						 <a href="<yrcwww:link path='viewUploadJob.do?'/>id=<bean:write name='analysis' property='job.id'/>">
							<b><font color="red"><bean:write name="analysis" property="job.statusDescription"/></font></b>
						</a>
					</logic:equal>
					<!-- Job RUNNING -->
					<logic:equal name="analysis" property="job.running" value="true">
						<a href="<yrcwww:link path='viewUploadJob.do?'/>id=<bean:write name='analysis' property='job.id'/>">
						<b><font color="#000000"><bean:write name="analysis" property="job.statusDescription"/></font></b>
						</a>
					</logic:equal>
					</td>
			</logic:equal>
				
			</tr>
			
			<tr>
				<td colspan="10" valign="top">
				<div id="analysis_<bean:write name='analysis' property='id'/>_edit" align="center"
			     style="display:none;">
			     <textarea rows="5" cols="60" class="edit_text"></textarea>
			     <br>
			     <button class="saveAnalysisComments"
			     		id="<bean:write name='analysis' property='id'/>"
			     		title="expt_<bean:write name='experiment' property='id'/>">Save</button>
			     <button class="cancelAnalysisComments"
			     		id="<bean:write name='analysis' property='id'/>"
			     		title="expt_<bean:write name='experiment' property='id'/>">Cancel</button>
				</div>
				</td>
			</tr>
			
			</tbody>
		</table>
		</div>
		
		
		<!-- DTASELECT RUN -- only for the first Percolator run -->
		<logic:equal name="analysis_idx" value="0">
		<logic:equal name="experiment" property="hasProtInferResults" value="true" >
			<logic:present name="experiment" property="dtaSelect">
				<div style="background-color: #FFFFF0; margin:5 5 5 5; padding:5; border: 1px dashed gray;" > 
	
				<table width="90%">
				<tr>
					<td width="33%"><b>Program: </b>&nbsp;
					<b>DTASelect</b>
					&nbsp;</td>
					<td width="33%">
						<b><html:link action="viewYatesRun.do" paramId="id" paramName="experiment" paramProperty="dtaSelect.id">[View Results]</html:link></b>
					</td>
					<td width="33%">&nbsp;
					</td>
				</tr>
				</table>
			</div>
		</logic:present>
		</logic:equal>
		</logic:equal>

		<!-- PROTEIN INFERENCE RUNS FOR THIS ANALYSIS -->
		<logic:notEmpty name="analysis" property="protInferRuns">
			<%@ include file="protInferDetails.jsp" %>
		</logic:notEmpty>


		</logic:iterate>
	</logic:equal>
	
	<!-- !!!!!! PEPTIDE PROPHET !!!!!! -->
	<logic:equal name="experiment" property="analysisProgramName" value="<%=Program.PEPTIDE_PROPHET.displayName() %>">
	<!-- NAME OF THE ANALYSIS PROGRAM -->
	<div style="background-color: #F0FFF0; margin:5 5 5 5; padding:5; border: 1px dashed gray;" >
	<div><b><bean:write name="experiment" property="analysisProgramName"/> Results </b></div> 
	<table width="100%">
		<thead>
		<tr align="left">
			<th valign="top">ID</th>
			<th valign="top">Version</th>
			<th valign="top">File</th>
			<th valign="top"></th>
		</thead>
		<tbody>
		<logic:iterate name="experiment" property="analyses" id="analysis">
			<tr>
			<td><bean:write name="analysis" property="id"/></td>
			<td><bean:write name="analysis" property="analysisProgramVersionShort"/></td>
			<td valign="top">
				<b><bean:write name="analysis" property="analysisName" /></b>
			</td>
			<td valign="top">
				<b>
					<html:link action="viewPeptideProphetResults.do" paramId="ID" paramName="analysis" paramProperty="id">[View Results]</html:link>
				</b>
			</td>
		</tr>
		</logic:iterate>
		</table>
		</div>
  </logic:equal>
</logic:notEmpty>

<!-- PROTEIN INFERENCE RESULTS FOR THE EXPERIMENT -->
<logic:equal name="experiment" property="hasProtInferResults" value="true" >

<logic:notEmpty name="experiment" property="proteinProphetRuns">
	<%@ include file="proteinProphetDetails.jsp" %>
	<div style="margin:5 5 5 5; padding:5; border: 1px dashed gray;" >
	<table width="100%">
	<tr>
			<td colspan="9" align="right">
				<input type="checkbox" id="grpProts" value="group" checked="checked" />Group Indistinguishable Proteins
				&nbsp;
				<span class="clickable" style="text-decoration:underline;" onclick="javascript:compareSelectedProtInferAndMore();"><b>[Compare More]</b></span>
				&nbsp;
				<span class="clickable" style="text-decoration:underline;" onclick="javascript:compareSelectedProtInfer();"><b>[Compare]</b></span>
				<br><br>
				<span class="clickable" style="text-decoration:underline;" onclick="javascript:clearSelectedProtInfer();">[Clear Selected]</span>
			</td>
		</tr>
	</table>
	</div>
</logic:notEmpty>

</logic:equal>


<!-- FILES FOR THE EXPERIMENT (Placeholder)-->
<div align="center" style="width:100%;">
<span
	id="listfileslink_<bean:write name='experiment' property='id'/>"  
	class="clickable" style="font-weight:bold; color:#D74D2D;" 
	onclick="javascript:toggleFilesForExperiment(<bean:write name='experiment' property='id'/>);">[List Files]</span>
</div>
<div style="background-color: #FFFFFF; margin:5 5 5 5; padding:0;" id="listfileslink_<bean:write name='experiment' property='id'/>_target"></div>
