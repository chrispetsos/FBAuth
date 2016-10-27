package org.seerc.fbauth.rest.security;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.seerc.fbauth.facebookclient.FBAuthClient;

import com.restfb.FacebookClient.DebugTokenInfo;

/**
 * This is the filter that will authenticate users by token on every request.
 * if the token is valid it stores it in token store.
 * 
 * @author Chris Petsos (chpetsos@seerc.org)
 *
 */

// Uncomment the following two lines to enable filter
/*@Provider
@Priority(javax.ws.rs.Priorities.AUTHENTICATION + 1)*/
public class ContinuousAuthFilter extends TokenAuthFilter {

	@Override
	protected void filterWithToken(ContainerRequestContext requestContext, String userProvidedToken) {
		/*
		 * Now check if it is valid.
		 * If it is, extend it and store a relation user-providedToken-extendedToken
		 */
		FBAuthClient fbClient = new FBAuthClient(userProvidedToken);
		DebugTokenInfo tokenInfo = fbClient.getDebugTokenInfo();
		if(!tokenInfo.isValid())
		{	// invalid token
			requestContext.abortWith(Response
					.status(Response.Status.UNAUTHORIZED)
					.entity("The provided authorization token is invalid.")
					.build());
		}
		else
		{	// valid token
			// get user that provided this token
			String userId = tokenStore.getUserOfProvidedToken(userProvidedToken);
			// if this is a new user-provided token
			if(userId == null)
			{
				// extend it first
				String extendedToken = fbClient.extendToken();

				// get user ID from token info
				userId = tokenInfo.getUserId();

				// then store it in store
				tokenStore.storeUserToken(userId, userProvidedToken, extendedToken);
			}
		}
	}
}
