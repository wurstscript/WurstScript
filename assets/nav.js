(function ($) {
	





	var debug = $("<div>").appendTo("#nav");

	var headers = new Array();
	var headerLinks = new Array();
	function createNav() {
		$("h1, h2, h3, h4, h5, h6").each(function() {
			var elem = $(this);
			//alert(elem.attr('id'));
			if (elem.attr('id')) {
				headers.push(elem);
			}
		});

		var menus = new Array();
		menus[1] = $("<ul>");
		var lastLevel = 1;
		for (var i=0; i<headers.length; i++) {
			(function(i) {
			var header = headers[i];
			var level = parseInt(header.prop('tagName').substr(1));
			if (level > lastLevel) {
				for(var j=lastLevel+1; j<=level; j++) {
					menus[j] = $("<ul>").addClass('submenu');
					var li = $("<li>").appendTo(menus[j-1]);
					menus[j].appendTo(li);
				}
			}
			var li = $('<li>');
			headerLinks[i] = $('<a href="#'+$(header).attr('id')+'">'+
				$(header).html() + 
				'</a>'
				).appendTo(li);
				/*.click(function(e) {
					$(window).scrollTop(header.offset().top + 1 - $(window).height()/2);
					e.preventDefault();
					return false;
				});*/
			li.appendTo(menus[level]);
			
			lastLevel = level;
			})(i);
		}
		menus[1].appendTo("#nav");

	}


	var lastHeader = -1;
			
	function updateScroll() {
		var scroll = $(window).scrollTop() + $(window).height()*0.5;
		var currentHeader = -1;
		for (var i=0; i<headers.length; i++) {
			if (headers[i].offset().top > scroll) {
				break;
			}
			currentHeader = i;
		}
		if (currentHeader != lastHeader) {
			if (lastHeader >= 0) {
				headerLinks[lastHeader].removeClass('current');
			}
			if (currentHeader >= 0) {
				headerLinks[currentHeader].addClass('current');
			}
			lastHeader = currentHeader;
			// first hide all nested elements
			var hideset = new Array();
			var showset = new Array();
			$("#nav ul.submenu").each(function () {hideset.push(this)});
			// now show all the childs and parents
			headerLinks[currentHeader].parents("ul.submenu").each(function() { showset.push(this)});
			headerLinks[currentHeader].parent().next().children("ul.submenu").each(function() { showset.push(this)});
			
			hideset.forEach(function(h) {
				if (!showset.some(function (s) {return s == h;})) {
					$(h).hide(500);
				}
			});
			showset.forEach(function(s) {
				$(s).show(500);
			});
		}
	
	}
	$(window).scroll(updateScroll);

	createNav();
	$("#nav ul.submenu").hide();
	updateScroll();
				

})(jQuery);
