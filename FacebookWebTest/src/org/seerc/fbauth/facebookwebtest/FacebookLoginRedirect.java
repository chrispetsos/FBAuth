package org.seerc.fbauth.facebookwebtest;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.seerc.fbauth.facebookclient.FBAuthClient;
import org.seerc.fbauth.facebookclient.test.FBAuthClientTest;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.FacebookClient.AccessToken;
//import com.restfb.WebRequestor.Response;

@Path("/loginRedirect")
public class FacebookLoginRedirect {

	/*
	 * Open this link in a browser and you will go through FB login dialog to your app
	 * https://www.facebook.com/dialog/oauth?client_id=195274567520180&redirect_uri=http%3A%2F%2Fpetsosspiti.no-ip.org%3A8080%2FFacebookWebTest%2FFacebookWebTestRest%2FloginRedirect%3FmyApp%3Dhttp%3A%2F%2Fpetsosspiti.no-ip.org%3A8080%2FFacebookWebTest%2F&scope=public_profile%2Cuser_managed_groups%2Cuser_posts
	 * needs:
	 * client_id: the app id
	 * redirect_uri: contains the URL to this REST method that has a parameter "myApp" that points to where this will redirect upon login. This parameter should be encoded!
	 * scope: the permissions that will be granted.
	 * 
	 * This link should be generated by another REST method that will automate the whole process.
	 */
	@GET
	public Response receiveRedirection(@Context HttpServletRequest request, @QueryParam("code") String code, @QueryParam("myApp") String myApp) {
		System.out.println("Received code: " + code);
		System.out.println("Received myApp: " + myApp);
		
		String requestURL = request.getRequestURL().append('?').append(request.getQueryString()).toString();
		System.out.println("Received requestURL: " + requestURL);
		
		FacebookClient client = new DefaultFacebookClient(Version.VERSION_2_5);
		AccessToken ontheflyToken = client.obtainUserAccessToken(FBAuthClient.appId, FBAuthClient.appSecret, requestURL, code);
		
		System.out.println("Got access token: " + ontheflyToken);
		
		java.net.URI location = null;
		try {
			location = new java.net.URI(myApp);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		System.out.println("Redirecting to your app: " + location.toASCIIString());
		
	    return Response.temporaryRedirect(location).build();
		//return ontheflyToken.toString();
	}
}