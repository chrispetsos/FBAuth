package org.seerc.fbauth.rest.security;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

//Uncomment the following two lines to enable filter
/*@Provider
@Priority(javax.ws.rs.Priorities.AUTHENTICATION + 2)*/
public class UserLoginFilter extends TokenAuthFilter {

	@Override
	protected void filterWithToken(ContainerRequestContext requestContext, String userProvidedToken) {
		// get user that provided this token
		String userId = tokenStore.getUserOfProvidedToken(userProvidedToken);
		// if the id is not found
		if(userId == null)
		{	// not logged in, abort
			requestContext.abortWith(Response
					.status(Response.Status.UNAUTHORIZED)
					.entity("Provided token does not correspond to a logged user. Please login first.")
					.build());
		}
		else
		{	// user logged in, let in
			
		}
	}

}
