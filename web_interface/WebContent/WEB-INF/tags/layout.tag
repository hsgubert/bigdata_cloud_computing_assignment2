<%@tag description="Generic Layout" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="activeTab" required="true"%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Tweet Map</title>
		
		<link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css" />
		<link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap-theme.min.css" /> <!-- Optional theme -->
		<link rel="stylesheet" type="text/css" href="resources/stylesheets/main.css" />

		<script src="resources/javascripts/jquery-1.11.1.min.js"></script>
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
		<script type="text/javascript"
			src="http://maps.googleapis.com/maps/api/js?libraries=visualization&key=AIzaSyBTCbY_Kgc3UPMtGzq8XfqyaBZfaf4sX9U&sensor=false&language=en-us">
		</script>
		<script src="resources/javascripts/heatmap.js"></script>
		
	</head>
  	<body>
	  	<!-- Fixed navbar -->
	    <div class="navbar navbar-default navbar-static-top" role="navigation">
	      <div class="container">
	        <div class="navbar-header">
	          <a class="navbar-brand" href="map">Tweet Map</a>
	        </div>
	        <div class="navbar-collapse collapse">
	          <ul class="nav navbar-nav">
	            <li class="${activeTab eq 'map' ? 'active' : ''}"><a href="map"><span class="glyphicon glyphicon-globe"></span> Map</a></li>
	          </ul>
	          <ul class="nav navbar-nav navbar-right">
	          	<li class="${activeTab eq 'account' ? 'active' : ''}"><a href="account"><span class="glyphicon glyphicon-user"></span> Account</a></li>
	            <li><a href="logout"><span class="glyphicon glyphicon-off"></span> Logout</a></li>
	          </ul>
	        </div>
	      </div>
	    </div>
	
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
			
	    	<jsp:doBody/>
	    </div>
  	</body>
</html>