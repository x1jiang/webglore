<html>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" %>
<%@ page import="java.sql.*,java.util.*,java.io.*,org.joda.time.DateTime,org.joda.time.DateTimeComparator,org.joda.time.format.DateTimeFormatter,org.joda.time.format.DateTimeFormat,java.sql.*,java.text.SimpleDateFormat;" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<head>

		<title>WebGLORE</title>
		<link href="./css/styles.css" rel="stylesheet" type="text/css">
		</script><script src="./js/jquery-1.8.2.min.js" language="JavaScript" type="text/javascript"></script>
		<script src="./js/utility.js" language="JavaScript" type="text/javascript"></script>
</head>

<body topmargin="0" leftmargin="0" rightmargin="0" bottommargin="0">
		<div id="main_container">

			<div id="left_container">
				<!-- Logo Start -->
				<div id="title">

					<div id="logo">
						<h1>GLORE</h1>
					</div>

					<div id="name">
						<h3>Web-based Grid binary LOgistic REgression (WebGLORE)</h3>
					</div>

				</div>
				<!-- Logo End -->
				<!-- Navigation Start-->
				<div id="navigation" class="border rounded-upper">

					<div id="nav_header" class="bottom-border">
						<h3>Navigation</h3></div>

					<a id="nav_login" class="nav bottom-border" href="./login.html">
						<div class="nav_icon">
						</div>
						<div class="nav_text">
							<h2 class="">Login</h2>
							<p>Log into the GLORE system</p>
						</div>
					</a>
					
					<a id="nav_home" class="nav bottom-border" href="./index.html">
						<div class="nav_icon">
						</div>
						<div class="nav_text">
							<h2>Home</h2>
							<p>View your GLORE profile page</p>
						</div>
					</a>

					<a id="nav_instructions" class="nav bottom-border" href="./instructions.html">
						<div class="nav_icon">
						</div>
						<div class="nav_text">
							<h2 class="">Instructions</h2>
							<p>Learn the fundamentals of using GLORE</p>
						</div>
					</a>

					<a id="nav_registration" class="nav bottom-border" href="./registration.html">
						<div class="nav_icon">
						</div>
						<div class="nav_text">
							<h2>Registration</h2>
							<p>Register an account in GLORE</p>
						</div>
					</a>

					<a id="nav_createTask" class="nav bottom-border" href="./createTask.html">
						<div class="nav_icon">
						</div>
						<div class="nav_text">
							<h2>Create Task</h2>
							<p>Create a new GLORE task</p>
						</div>
					</a>
					
					<a id="nav_wait" class="nav bottom-border" href="./WaitForParticipants.html">
						<div class="nav_icon">
						</div>
						<div class="nav_text">
							<h2>WaitForParticipants</h2>
							<p>Wait for other participants</p>
						</div>
					</a>

					<a id="nav_compute" class="nav bottom-border" href="./computation.jsp">
						<div class="nav_icon">
						</div>
						<div class="nav_text">
							<h2>Computation</h2>
							<p>Computation process</p>
						</div>
					</a>
					
					<a id="nav_test" class="nav bottom-border" href="./Test.jsp">
						<div class="nav_icon">
						</div>
						<div class="nav_text">
							<h2 class="hightlighted">Test Data</h2>
							<p>Test your local data</p>
						</div>
					</a>
					
					<a id="team" class="nav bottom-border" href="./team.html">
						<div class="nav_icon">
						</div>
						<div class="nav_text">
							<h2>Team</h2>
							<p>Team members</p>
						</div>
					</a>
					
				</div>
			</div>
				<!-- Navigation End-->
				
			<div id="right_container" class="border rounded-upper">

				<div id="header" class="bottom-border">
					<h2>Test Data</h2>
				</div>

				<!-- <div id="error_display">

				</div>-->

				<div id="full_content">


<table width="1024" align="center" valign="middle" bgcolor="">

<tr><td>
<table align="center" width="800">
<tr>
<td>
<!--  <form action="creategloretaskservlet" method = "post" enctype="application/x-www-form-urlencoded">-->

<table width="800" align="left" valign="left" >
<tr>

<td align="center"> </td>
<tr>
	<td>
		<table width = 800 class="content">
			<tr>
				<td>Test GLORE performance using local data</td>
			</tr>
<%
	Properties properties = new Properties();
	InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/config.properties");
	properties.load(is);
	String taskName = (String)request.getSession().getAttribute("taskName");
	String betaString = null;
	
	String dbconnection = properties.getProperty("dbconnection");
	request.getSession().setAttribute("dbconnection_property", dbconnection);
	String dbuser = properties.getProperty("dbusername");
	request.getSession().setAttribute("dbusername_property", dbuser);
	String dbpwd = properties.getProperty("dbpassword");
	request.getSession().setAttribute("dbpassword_property", dbpwd);
	String root = properties.getProperty("root");
	request.getSession().setAttribute("root_property", root);
	
	Class.forName("com.mysql.jdbc.Driver");
    Connection conn = DriverManager.getConnection(dbconnection, dbuser, dbpwd);
    Statement stat = conn.createStatement();
	  String sql = "select beta from tempresult where taskName='" + taskName + "';";
  	ResultSet rs = stat.executeQuery(sql);
  	if(rs.first()){
		betaString = rs.getString(1);
		System.out.println("betaString" + betaString);
	}
  
	String showFilePath = "showFilePath";
	if (request.getSession().getAttribute("showFilePath") != null)
	{
		showFilePath = (String)request.getSession().getAttribute("showFilePath");
	}
	
	
%>
	
<td>
<applet code="glore.LocalTestApplet.class" archive="WebGlore.jar" width=500 height=55" MAYSCRIPT>
<!-- The parameter of applet is added by jwc -->
<param name="showFilePath" value="<%=showFilePath %>">
<param name="betaString" value="<%=betaString %>">
</applet>
</td>
</tr>
</table>

		</table>
	</td>
</tr>
<script language="Javascript">

function accessAppletResult()
{
	var fn = "TestResult";
	//alert("in accessAppletMethod");
 //   fn = document.applets[0].getAttributes();
 //   document.getElementById('showProperty').value = fn;
    fn = document.applets[0].getResults();
    document.getElementById('TestResult').innerHTML = fn;
    //document.getElementById('sameAsApplet').style.display='none';
}
</script>
<tr>
<td><input type="hidden" name="showFilePath" id='showFilePath'  value='<%=showFilePath %>' size="50"/></td>
</tr>


 <tr>
 <td>
<div id="TestResult" align="center" valign="middle">Here to show the test result!</div></td>
</tr>

</tr>
</table>
</form>
</td>

</table>

				</div>

			</div>

		</div>
</body>
</html>
