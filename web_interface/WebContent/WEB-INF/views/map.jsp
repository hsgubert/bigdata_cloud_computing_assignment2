<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<tags:layout activeTab="map">

<div class="row">
	<div class="col-xs-8 col-md-6 col-lg-6">
		<form class="form-inline" role="form">
			<div class="form-group">
				<label for="keyword" class="control-label">Keyword filtering </label>
				<div class="input-group">    
			    	<div class="input-group-addon"><span class="glyphicon glyphicon-filter"></span></div>
			    	<select id="keywords-select" name="keyword" class="form-control">
						<c:forEach var="keyword" items="${keywords}">
							<c:choose>
								<c:when test="${keyword eq '_none_'}">
							   		<option value="_none_">Show all</option>
								</c:when>
								<c:otherwise>
							 		<option>${keyword}</option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</div>
			</div>
		</form>
	</div>
	<div class="col-xs-4 col-md-6 col-lg-6">
		<button id="red" type="button" class="btn btn-danger pull-right">Toogle Red</button>
		<button id="green" type="button" class="btn btn-success pull-right">Toogle Green</button>
	</div>
</div>
<div class="row">
	<div id="map" class="col-xs-12">
		
	</div>
</div>
</tags:layout>

<script>

$(window).load(mapInit);

function mapInit() {
	var heatmap = new Heatmap({containerId: "map"});
	var points = {}; 
	
	function loadPointsForKeyword(keyword, callback) {
		$.get("tweet_points",
			{keyword: keyword},
			function (jsonData) {
				data = JSON.parse(jsonData);
				if (!data.hasOwnProperty("+") && !data.hasOwnProperty("-")) {
					return;
				}
				points[keyword] = data;
				callback(keyword);
			}
		);
	} 
	
	function showPoints(keyword) {
		if (points.hasOwnProperty(keyword)) {
			heatmap.clearNegativeHeatPoints();
			heatmap.clearPositiveHeatPoints();
			heatmap.addNegativeHeatpoints(points[keyword]["-"]);
			heatmap.addPositiveHeatpoints(points[keyword]["+"]);
		}
		else {
			loadPointsForKeyword(keyword, function(keyword) {
				heatmap.clearNegativeHeatPoints();
				heatmap.clearPositiveHeatPoints();
				heatmap.addNegativeHeatpoints(points[keyword]["-"]);
				heatmap.addPositiveHeatpoints(points[keyword]["+"]);
			});	
		}
	}
	
	// load initial points
	showPoints($("#keywords-select").val());
	
	// on select box change
	$("#keywords-select").change(function() {
		var keyword = $(this).val();
		showPoints(keyword);
	});
	
	$("#green").click(function() {
		heatmap.togglePositive();
	});
	
	$("#red").click(function() {
		heatmap.toggleNegative();
	});
	
	var clusterVersion = -1; 
	var intervalID = setInterval(function() {
		$.get("cluster_version",
			{},
			function (jsonData) {
				data = JSON.parse(jsonData);
				if (data.hasOwnProperty("cluster_version")) {
					if (clusterVersion > 0) {
						if (clusterVersion < data["cluster_version"]) {
							location.reload();
						}
					}
					clusterVersion = data["cluster_version"];
				}
			}
		);
	}, 30000);
};

</script>




