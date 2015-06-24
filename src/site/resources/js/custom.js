window.onload = function() {
	var a = document.getElementById("toggleGuessed");
	a.onclick = function() {
		$('.guessed').toggle();
		var caption = $('#toggleGuessed').html();
		if (caption == "Show other parameters") {
			$('#toggleGuessed').html("Hide other parameters");
		} else {
			$('#toggleGuessed').html("Show other parameters");
		}
	}
}
