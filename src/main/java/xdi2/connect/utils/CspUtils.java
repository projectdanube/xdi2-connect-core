package xdi2.connect.utils;


public class CspUtils {
	
	public static String getCspLogoFromEndpointUri(String cloudEndpoint) {
			
		
		if (cloudEndpoint.contains("danubeclouds.com"))
			return "danube_clouds-logo.png";
		else if (cloudEndpoint.contains("emmettglobal"))
			return "emmett_global-logo.png";
		else if (cloudEndpoint.contains("ownyourinfo"))
			return "bosonweb-logo.png";
		else if (cloudEndpoint.contains("onexus"))
			return "onexus-logo.png";
		else if (cloudEndpoint.contains("paoga"))
			return "paoga-logo.png";
			
		return "selfhosted-logo.png";
		
	}

}
