window.onload = function() {
	var a = document.getElementById("toggleGuessed");
	if (a != null) {
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
}

var clipboard = new Clipboard('.data-btn-command-line');

clipboard.on('success', function(e) {
	var alertMsg = '<br /><span class="label label-info">Command line copied to clipboard!</span>';
	var elem = e.trigger;

	$(elem).parent().find('.label').remove();
	$(elem).parent().find('br').remove();
	$(alertMsg).fadeIn(1000).appendTo($(elem).parent()).delay(1000).fadeOut(1000);
});

var clipboard2 = new Clipboard('.data-btn-text');

clipboard2.on('success', function(e) {
	var alertMsg = '<br /><span class="label label-info">Config copied to clipboard!</span>';
	var elem = e.trigger;

	$(elem).parent().find('.label').remove();
	$(elem).parent().find('br').remove();
	$(alertMsg).fadeIn(1000).appendTo($(elem).parent()).delay(1000).fadeOut(1000);
});
