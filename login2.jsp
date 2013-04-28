<html>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.sql.*,java.util.*,java.io.*,org.joda.time.DateTime,org.joda.time.DateTimeComparator,org.joda.time.format.DateTimeFormatter,org.joda.time.format.DateTimeFormat,java.sql.*,java.text.SimpleDateFormat;" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
		<title>GLORE</title>
		<link href="./css/styles.css" rel="stylesheet" type="text/css">
		</script><script src="./js/jquery-1.8.2.min.js" language="JavaScript" type="text/javascript"></script>
		<script src="./js/utility.js" language="JavaScript" type="text/javascript"></script>

		<script src="./js/utility_login.js" language="JavaScript" type="text/javascript"></script>
	</head>

	<body>

		<div id="main_container">

			<div id="left_container">

				<!-- Logo Start -->
				<div id="title">

					<div id="logo">
						<h1>GLORE</h1>
					</div>

					<div id="name">
						<h3>Grid Binary Logistic Regression</h3>
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
							<h2 >Login</h2>
							<p>Login GLORE system</p>
						</div>
					</a>
					
					<a id="nav_home" class="nav bottom-border" href="./home.html">
						<div class="nav_icon">
						</div>
						<div class="nav_text">
							<h2 class="">Home</h2>
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
							<h2 class="">Registration</h2>
							<p>Register an account with GLORE</p>
						</div>
					</a>

					<a id="nav_createTask" class="nav bottom-border" href="./createTask.html" onClick="getEmailList();">
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
							<p>Wait for participants</p>
						</div>
					</a>
					
					<a id="nav_compute" class="nav " href="./computation.jsp">
						<div class="nav_icon">
						</div>
						<div class="nav_text">
							<h2 class="hightlighted">Computation</h2>
							<p>Computation process</p>
						</div>
					</a>
				</div>
				<!-- Navigation End-->

			</div>

			<div id="right_container" class="border rounded-upper">

				<div id="header" class="bottom-border">
					<h2>Computation</h2>
				</div>

				<div id="error_display">

				</div>

				<div id="content">



				</div>

			</div>

		</div>

	

</body></html>