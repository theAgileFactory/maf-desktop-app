/*
 * Post processing script for the application app_id="jenk1"
 */
function jenk1_post_processing(isAuthorized){

    /*
    if(isAuthorized){
        var matches=getElementsByClassName("login");
        if(matches.length==1){
            matches[0].style.display="none";
        }
        var breadcrumbBar=document.getElementById("breadcrumbBar");
        if(breadcrumbBar){
            breadcrumbBar.childNodes[2].style.display="none";
            breadcrumbBar.childNodes[3].removeAttribute("style");
        }
    }
    */
    
    if(isAuthorized){
        
        /*var breadcrumbBar = document.getElementById("breadcrumbBar");
        if (breadcrumbBar) {
            breadcrumbBar.parentNode.removeChild(breadcrumbBar);
        }*/
    	
    	var breadcrumbBar = document.getElementById("breadcrumbBar");
    	var breadcrumbBarParent = document.getElementById("breadcrumbBar").parentNode;
    	
    	breadcrumbBar.removeChild(breadcrumbBar.getElementsByTagName("script")[0]);
    	breadcrumbBar.removeChild(breadcrumbBar.getElementsByTagName("div")[0]);
    	breadcrumbBar.getElementsByClassName("top-sticker")[0].style = null;
    	breadcrumbBar.getElementsByClassName("top-sticker")[0].style.position = '';
    	breadcrumbBar.getElementsByClassName("top-sticker")[0].style.left = '';
    	breadcrumbBar.getElementsByClassName("top-sticker")[0].style.top = '';

    	var breadcrumbBarClone = breadcrumbBar.cloneNode(true);
    	
    	breadcrumbBarParent.insertBefore(breadcrumbBarClone, breadcrumbBar);
    	breadcrumbBarParent.removeChild(breadcrumbBar);

        var header = document.getElementById("header");
        if (header) {
            header.parentNode.removeChild(header);
        }
    }
}