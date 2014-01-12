(function ($) {



		//var debug = $("<div>").appendTo("#tableofcontents");

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
			
			function Node(pheader) {
				this.header = pheader
				this.children = new Array();
				
			}

			
			var currentNodes = new Array();
			var currentLevel = 0;
			
			currentNodes[0] = new Node(null);
			
			
			
			for (var i in headers) {
				var header = headers[i];
				var node = new Node(header);
				//alert("header = " + header);
				var level = parseInt(header.prop('tagName').substr(1));
				//alert("level = " + level);
				
				for (; currentLevel<level;currentLevel++) {
					var node2 =  new Node(null)
					currentNodes[currentLevel+1] = node2;
					currentNodes[currentLevel].children.push(node2);
				}
				
				currentNodes[level] = node;
				currentNodes[level-1].children.push(node);
			}


			function makeHtml(node) {
				var li = $("<li>");
				if (node.header != null) {
					li.html('<a href="#'+$(node.header).attr('id')+'">'+
					$(node.header).html() + 
					'</a>');
				} else if (node.children.length == 0) {
					//return $("<p>null</p>","<p>abc</p>");
					return null;
				}
				var ul = $("<ul>").appendTo(li);
				for (var n in node.children) {
					var c = makeHtml(node.children[n]);
					if (c != null) {
						c.appendTo(ul)
					}
				}
				return li;
			}

			$("#tableofcontents").html("<h2>Table of contents:</h2>");
			makeHtml(currentNodes[0]).children().appendTo("#tableofcontents");

		}


		

		createNav();


		for (var i in headers) {
			var header = headers[i];
			
			var id = header.attr('id');
			var link = $('<a class="anchorlink" href="#'+id+'">#</a>')
			link.appendTo($(header));
		}
				

})(jQuery);
