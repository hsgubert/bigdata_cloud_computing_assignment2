<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tags:layout activeTab="account">

<div class="row">
	<div class="col-xs-12 col-md-6 col-lg-6">
		<form role="form" action="account" method="post">
		  <h3>Edit Name</h3>
		  <div class="form-group">
		    <label for="name">Name</label>
		    <input type="text" class="form-control" name="name" value="${user.name}">
		  </div>
		  
		  <h3>Change password</h3>
		  <div class="form-group">
		    <label for="name">Current password</label>
		    <input type="password" class="form-control" name="current-password" placeholder="Enter current password">
		  </div>
		  <div class="form-group">
		    <label for="name">New password</label>
		    <input type="password" class="form-control" name="new-password" placeholder="Enter new password">
		  </div>
		  <div class="form-group">
		    <label for="name">New password confirmation</label>
		    <input type="password" class="form-control" name="new-password-confirmation" placeholder="Enter password confirmation">
		  </div>
		  <button type="submit" class="btn btn-default">Submit</button>
		</form>
	</div>
</div>
</tags:layout>

<script>

$(function() {
	var heatmap = new Heatmap({containerId: "map"});
	var points = {};
	
	function loadPointsForKeyword(keyword, callback) {
		$.get("tweet_points",
			{keyword: keyword},
			function (jsonData) {
				data = JSON.parse(jsonData);
				points[keyword] = data;
				callback(keyword);
			}
		);
	} 
	
	function showPointsForOne(keyword) {
		if (points.hasOwnProperty(keyword)) {
			heatmap.clearHeatPoints();
			heatmap.addHeatPoints(points[keyword].latitudes, points[keyword].longitudes);	
		}
		else {
			loadPointsForKeyword(keyword, function(keyword) {
				heatmap.clearHeatPoints();
				heatmap.addHeatPoints(points[keyword].latitudes, points[keyword].longitudes);
			});	
		}
	}
	
	function showPointsForAll() {
		var requestsMade = 0;
		heatmap.clearHeatPoints();
		$.each($("#keywords-select")[0].options, function(i, option) {
			var keyword = $(option).val();
			if (keyword != "Show all") {
				if (points.hasOwnProperty(keyword)) {
					heatmap.addHeatPoints(points[keyword].latitudes, points[keyword].longitudes);		
				}
				else {
					loadPointsForKeyword(keyword, function(keyword) { 
						heatmap.addHeatPoints(points[keyword].latitudes, points[keyword].longitudes);
					});
				}
			}
		});
	}
	
	// load initial points
	loadPointsForKeyword($("#keywords-select").val(), function(keyword) {
		heatmap.addHeatPoints(points[keyword].latitudes, points[keyword].longitudes);
	});

	// on select box change
	$("#keywords-select").change(function() {
		var keyword = $(this).val();
		if (keyword == "Show all") {
			showPointsForAll();
		}
		else {
			showPointsForOne(keyword);
		}
	});
		
});

</script>




