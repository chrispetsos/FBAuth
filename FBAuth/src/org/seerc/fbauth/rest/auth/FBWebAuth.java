package org.seerc.fbauth.rest.auth;

import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.seerc.fbauth.facebookclient.FBAuthClient;
import org.seerc.fbauth.tokenStore.FileTokenStore;
import org.seerc.fbauth.tokenStore.ITokenStore;
import org.seerc.fbauth.tokenStore.MemoryTokenStore;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.scope.ScopeBuilder;
import com.restfb.scope.UserDataPermissions;

/**
 * This class is responsible of performing auth tasks when user uses a
 * web client to auth. It extends and stores the generated token upon successful login.
 * 
 * @author Chris Petsos (chpetsos@seerc.org)
 *
 */
@Path("/web")
public class FBWebAuth {

	private final Logger LOGGER = Logger.getLogger(this.getClass().getPackage().getName());

	ITokenStore tokenStore;
	
	public FBWebAuth()
	{
		tokenStore = new FileTokenStore();
	}
	
	/**
	 * Redirects to the FB login page for the user to let access to this app.
	 * 
	 * @param request The current request's context.
	 * @param myApp The app to redirect to upon successful authorization.
	 * 
	 * @return A redirect response to the Facebook login page.
	 */
	@GET
	@Path("/login")
	public Response login(@Context HttpServletRequest request, @QueryParam("myApp") String myApp) {
		LOGGER.fine("Received myApp: " + myApp);

		/*
		 * We need to build the something like the following URL:
		 * https://www.facebook.com/dialog/oauth?client_id=195274567520180&redirect_uri=http%3A%2F%2Fpetsosspiti.no-ip.org%3A8080%2FFacebookWebTest%2FFacebookWebTestRest%2FloginRedirect%3FmyApp%3Dhttp%3A%2F%2Fpetsosspiti.no-ip.org%3A8080%2FFacebookWebTest%2F&scope=public_profile%2Cuser_managed_groups%2Cuser_posts 
		 */
		ScopeBuilder scopeBuilder = new ScopeBuilder();
		scopeBuilder.addPermission(UserDataPermissions.USER_MANAGED_GROUPS);
		scopeBuilder.addPermission(UserDataPermissions.USER_POSTS);
		
		FacebookClient client = new DefaultFacebookClient(Version.VERSION_2_5);
		String redirectUrl = request.getRequestURL().append("/../loginRedirect").toString();
		if(myApp != null && !myApp.isEmpty())
		{
			redirectUrl += "?myApp=" + myApp;
		}
		String loginDialogUrlString = client.getLoginDialogUrl(FBAuthClient.appId, redirectUrl, scopeBuilder);

		java.net.URI loginRedirectLocation = null;
		try {
			loginRedirectLocation = new java.net.URI(loginDialogUrlString);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return Response.temporaryRedirect(loginRedirectLocation).build();
	}

	/**
	 * This will be invoked by the FB server upon login redirection. It will contain the code
	 * generated by FB and the app that this will redirect to upon access token creation.
	 * @param request The current request's context.
	 * @param code The code generated by the FB server.
	 * @param myApp The user app that this will redirect to.
	 * @return A redirect response to the user's app.
	 */
	@GET
	@Path("/loginRedirect")
	public Response receiveRedirection(@Context HttpServletRequest request, @QueryParam("code") String code, @QueryParam("myApp") String myApp) {
		// TODO: Do not log code for security reasons
		LOGGER.fine("Received code: " + code);
		LOGGER.fine("Received myApp: " + myApp);
		
		if(myApp == null)
		{
			LOGGER.fine("No myApp provided, will default to : /../../../");
			myApp = "/../../../";
		}
		
		String requestURL = request.getRequestURL().append('?').append(request.getQueryString()).toString();
		LOGGER.fine("Received requestURL: " + requestURL);
		
		FacebookClient client = new DefaultFacebookClient(Version.VERSION_2_5);
		AccessToken ontheflyToken = client.obtainUserAccessToken(FBAuthClient.appId, FBAuthClient.appSecret, requestURL, code);
		
		// TODO: Do not log access token for security reasons
		LOGGER.fine("Got access token: " + ontheflyToken);
		
		java.net.URI appLocation = null;
		try {
			appLocation = new java.net.URI(myApp + "#" + ontheflyToken.getAccessToken()); // append token to URL as fragment

			if(!appLocation.isAbsolute())
			{	// relative myApp URL, append it to current request context
				appLocation = new java.net.URI(request.getRequestURL().append(appLocation.toASCIIString()).toString());
			}
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		FBAuthClient fbClient = new FBAuthClient(ontheflyToken.getAccessToken());
		// extend ontheflyToken
		String extendedToken = fbClient.extendToken();

		LOGGER.fine("Storing user ID and token in token store.");
		// no need to check if the token is valid here. Should be...
		String userId = fbClient.getAuthenticatedUser().getId();
		tokenStore.storeUserToken(userId, ontheflyToken.getAccessToken(), extendedToken);
		
		LOGGER.fine("Redirecting to your app: " + appLocation.toASCIIString());
		
	    return Response.temporaryRedirect(appLocation).build();
	}
	
	@GET
	@Path("/logout")
	public Response logout(@Context HttpServletRequest request, @QueryParam("token") String token, @QueryParam("loginURL") String loginURL) {
		tokenStore.deleteUserToken(tokenStore.getUserOfProvidedToken(token));
		
		java.net.URI loginLocation = null;
		try {
			loginLocation = new java.net.URI(loginURL); // append token to URL as fragment

			if(!loginLocation.isAbsolute())
			{	// relative myApp URL, append it to current request context
				loginLocation = new java.net.URI(request.getRequestURL().append(loginLocation.toASCIIString()).toString());
			}
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return Response.temporaryRedirect(loginLocation).build();
	}
}
