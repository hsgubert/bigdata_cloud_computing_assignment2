

var Heatmap = function(params) {
	this.map = new google.maps.Map(document.getElementById(params.containerId), {
	    center: params.center || new google.maps.LatLng(40.164971, -96.077127),
	    zoom: params.zoom || 4,
	    disableDefaultUI: null,
	    mapTypeControl: true,
	    styles: Heatmap.getMapStyles()
	});
}

Heatmap.getMapStyles = function generateGetMapStyles() {
	var styles = [
		{
	   		featureType: "poi",
	   		elementType: "labels",
	   		stylers: [ { visibility: "off" } ]
	   	},
	   	{
	   		featureType: "transit",
	   		stylers: [ { visibility: "off" } ]
	   	}
	];
  return function() {
    return styles;
  };
}();

Heatmap.getPositiveGradient = function generateGetPositiveGradient() {
	var gradient = [
	    'rgba(0, 0, 0, 0)',
	    'rgba(0, 255, 0, 0.4)',
	    'rgba(0, 255, 0, 0.6)',
	    'rgba(0, 255, 0, 0.8)',
	    'rgba(0, 255, 0, 0.9)',
	    'rgba(0, 255, 0, 1.0)',
	];
	return function() {
		return gradient;
	};
}();

Heatmap.getNegativeGradient = function generateGetNegativeGradient() {
	var gradient = [
	    'rgba(0, 0, 0, 0)',
	    'rgba(255, 0, 0, 0.4)',
	    'rgba(255, 0, 0, 0.6)',
	    'rgba(255, 0, 0, 0.8)',
	    'rgba(255, 0, 0, 0.9)',
	    'rgba(255, 0, 0, 1.0)',
	];
	return function() {
		return gradient;
	};
}();
	  
///////////////////////////////////////////////////////////////////////////////
/// Instance definitions
///////////////////////////////////////////////////////////////////////////////

Heatmap.prototype.map = null;
Heatmap.prototype.positiveHeatmapLayer = null;
Heatmap.prototype.negativeHeatmapLayer = null;

Heatmap.prototype.addPositiveHeatpoints = function(points) {
	var pointCount = points.length;
	var heatMapData = [];
	
	for (i=0; i<pointCount; i++) {
		heatMapData.push({
			location: new google.maps.LatLng(points[i][0], points[i][1]), 
			weight: points[i][2]
		});
	}
	  
	if (this.positiveHeatmapLayer == null) {
		this.positiveHeatmapLayer = new google.maps.visualization.HeatmapLayer({
			data: heatMapData
		});
		this.positiveHeatmapLayer.set('gradient', Heatmap.getPositiveGradient());
		this.positiveHeatmapLayer.set('dissipating', false);
		this.positiveHeatmapLayer.set('radius', 1.5);
		this.positiveHeatmapLayer.setMap(this.map);
	} 
	else {
		var datapoints = this.positiveHeatmapLayer.getData().getArray();
		datapoints = datapoints.concat(heatMapData);
		this.positiveHeatmapLayer.setData(datapoints);
	}
}

Heatmap.prototype.clearPositiveHeatPoints = function() {
	if (this.positiveHeatmapLayer != null && this.positiveHeatmapLayer.getData().length > 0) {
		this.positiveHeatmapLayer.setData([]);
	} 
}

Heatmap.prototype.addNegativeHeatpoints = function(points) {
	var pointCount = points.length;
	var heatMapData = [];
	
	for (i=0; i<pointCount; i++) {
		heatMapData.push({
			location: new google.maps.LatLng(points[i][0], points[i][1]), 
			weight: points[i][2]
		});
	}
	  
	if (this.negativeHeatmapLayer == null) {
		this.negativeHeatmapLayer = new google.maps.visualization.HeatmapLayer({
			data: heatMapData
		});
		this.negativeHeatmapLayer.set('gradient', Heatmap.getNegativeGradient());
		this.negativeHeatmapLayer.set('dissipating', false);
		this.negativeHeatmapLayer.set('radius', 1.5);
		this.negativeHeatmapLayer.setMap(this.map);
	} 
	else {
		var datapoints = this.negativeHeatmapLayer.getData().getArray();
		datapoints = datapoints.concat(heatMapData);
		this.negativeHeatmapLayer.setData(datapoints);
	}
}

Heatmap.prototype.clearNegativeHeatPoints = function() {
	if (this.negativeHeatmapLayer != null && this.negativeHeatmapLayer.getData().length > 0) {
		this.negativeHeatmapLayer.setData([]);
	} 
}

