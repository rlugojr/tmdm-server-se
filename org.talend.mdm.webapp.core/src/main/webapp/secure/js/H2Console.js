amalto.namespace("amalto.h2console");

amalto.h2console.H2Console = function() {

	function initUIAndData() {
		var baseCurrentUrl = stripQueryStringAndHashFromPath(this.location.toString());
		var theWindow = window.open(baseCurrentUrl + "/../h2console/h2console.html", "H2Console");
		theWindow.focus();
	};

	return {
		init : function() {
			initUIAndData();
		}
	}
	
	function stripQueryStringAndHashFromPath(url) {
		  return url.split("?")[0].split("#")[0];
	}
}();