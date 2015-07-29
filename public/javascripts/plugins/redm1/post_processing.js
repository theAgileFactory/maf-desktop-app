/*
 * Post processing script for the application app_id="redm1"
 */
function redm1_post_processing(isAuthorized){
	if(!isAuthorized){
		if(document.location.pathname.indexOf("/redmine/login")==-1){
			var matches=getElementsByClassName("logout");
			if(matches.length==1){
				document.location="/redmine/logout";
			}else{
				document.location="/redmine/login";
			}
		}
		return;
	}
	//Hide the account section if logged-in
	var matches=getElementsByClassName("logout");
	if(matches.length==1){
		matches[0].style.display="none";
		var accountSection=document.getElementById("account");
		accountSection.style.display="none";
		var loggedAsSection=document.getElementById("loggedas"); 
		loggedAsSection.style.display="none";
	}else{
		//Hide the register link
		var matches = getElementsByClassName("register","a");
		if(matches.length==1){
			matches[0].style.display="none";
		}

		//Hide the login without cass link
		var matches=getElementsByClassName("login-without-cas","a");
		if(matches.length==1){
			matches[0].style.display="none";
		}

		//If login link is present then redirect to login
		var matches=getElementsByClassName("login","a");
		if(matches.length==1){
			document.location="/redmine/login";
		}
	}
}