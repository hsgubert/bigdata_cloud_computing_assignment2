<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Tweet Map</title>
		
		<link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css" />
		<link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap-theme.min.css" /> <!-- Optional theme -->
		<link rel="stylesheet" type="text/css" href="resources/stylesheets/main.css" />

		<script type="text/javascript" src="resources/javascripts/jquery-1.11.1.min.js"></script>
		<script type="text/javascript" src="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
	</head>
  	<body>
  		<nav class="navbar navbar-inverse navbar-static-top" role="navigation">
  			<div class="navbar-header">
		      <a class="navbar-brand" href="#">Tweet Map</a>
		    </div>
		</nav>
		<div class="container">
	    	
	    	<div class="row">
		    	<c:if test="${not empty success_message}">
					<div class="alert alert-dismissible alert-success col-xs-12" role="alert">
						<button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
						${success_message}
					</div>	
				</c:if>
				<c:if test="${not empty error_message}">
					<div class="alert alert-dismissible alert-danger col-xs-12" role="alert">
						<button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
						${error_message}
					</div>	
				</c:if>
			</div>
			
	    	<div class="row">
				<div id="home_jumbotron" class="jumbotron col-xs-12">
					<h1>Tweet Map</h1>
					<p>This is a class project for E6998 Big Data &amp; Cloud Computing course at Columbia University.</p>
					<p>Students: <span class="link-blue" data-toggle="tooltip" data-placement="right" data-html="true" title="hs2807 - please login using my Columbia Mail and my UNI as password">Henrique Gubert</span>, Juliana Louback, Radu Moldoveanu</p>
					<p><a class="btn btn-lg btn-primary" href="#" role="button" id="login_button" data-toggle="collapse" data-target="#login_form" data-parent="#login_signup_forms">Login now!</a></p>
				</div>	    	
	    	</div>
	    		    	
	    	<div class="panel-group" id="login_signup_forms">
				<div class="row panel">
					<div id="login_form" class="col-xs-4 collapse panel-collapse login_signup_form ${(login_user eq null ? '' : 'in')}">
						<form role="form" action="home" method="post">
						  <div class="form-group">
						    <label for="login_email">Email</label>
						    <input type="email" class="form-control" name="login_email" placeholder="Enter email" value="${login_user.email}" autofocus>
						  </div>
						  <div class="form-group">
						    <label for="login_password">Password</label>
						    <input type="password" class="form-control" name="login_password" placeholder="Password" value="${signup_user.password}">
						  </div>
						  <input type="hidden" name="form_name" value="login"/>
						  <button type="submit" class="btn btn-primary">Login</button>
						  <a class="btn btn-link pull-right" href="#" role="button" data-toggle="collapse" data-target="#signup_form" data-parent="#login_signup_forms">Don't have an account?</a> 
						</form>
					</div>	    	
		    	</div>
		    	<div class="row panel">
					<div id="signup_form" class="col-xs-4 collapse panel-collapse login_signup_form ${(signup_user eq null ? '' : 'in')}">
						<form role="form" action="home" method="post">
						  <div class="form-group">
						    <label for="name">Name</label>
						    <input type="text" class="form-control" name="name" placeholder="Enter name" value="${signup_user.name}">
						  </div>
						  <div class="form-group">
						    <label for="email">Email</label>
						    <input type="email" class="form-control" name="email" placeholder="Enter email" value="${signup_user.email}">
						  </div>
						  <div class="form-group">
						    <label for="password">Password</label>
						    <input type="password" class="form-control" name="password" placeholder="Password" value="${signup_user.password}">
						  </div>
						  <div class="form-group">
						    <label for="password_confirmation">Confirm password</label>
						    <input type="password" class="form-control" name="password_confirmation" placeholder="Confirm password" value="${signup_user.passwordConfirmation}">
						  </div>
						  <input type="hidden" name="form_name" value="signup"/>
						  <button type="submit" class="btn btn-primary">Create account</button> 
						  <a class="btn btn-link pull-right" href="#" role="button" data-toggle="collapse" data-target="#login_form" data-parent="#login_signup_forms">I already have an account!</a>
						</form>
					</div>	    	
		    	</div>
		    </div>
	    </div>
	</body>
</html>

<script>
$(function() {
	$("[data-toggle=\"tooltip\"]").tooltip();
});
</script>





