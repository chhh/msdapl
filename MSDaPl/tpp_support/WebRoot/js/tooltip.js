//
// Tooltip script 
// powered by jQuery (http://www.jquery.com)
// 
// written by Alen Grakalic (http://cssglobe.com)
// 
// for more info visit http://cssglobe.com/post/1695/easiest-tooltip-and-image-preview-using-jquery
//
//

//starting the script on page load


this.tooltip = function() {
	//CONFIG		
	var xOffset = 10;
	var yOffset = 20;		
	// these 2 variable determine popup's distance from the cursor
	// you might want to adjust to get the right result		
	//END CONFIG		
	$(".tooltip").hover(function(e){
		this.t = this.title;
		this.title = "";									  
		$("body").append("<p id='tooltip'>"+ this.t +"</p>");
		$("#tooltip").css("top",(e.pageY - xOffset) + "px").css("left",(e.pageX + yOffset) + "px").fadeIn("fast");		
   		},
		function(){
			this.title = this.t;		
			$("#tooltip").remove();
   	});	
	$(".tooltip").mousemove(function(e){
		$("#tooltip").css("top",(e.pageY - xOffset) + "px").css("left",(e.pageX + yOffset) + "px");
	});	
}

