package org.seerc.fbauth.rest.security;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import org.seerc.fbauth.tokenStore.FileTokenStore;
import org.seerc.fbauth.tokenStore.ITokenStore;
import org.seerc.fbauth.tokenStore.MemoryTokenStore;

/**
 * Abstract filter class that extracts token from request. 
 * Holds a token store in hand to be used by extending classes. 
 * 
 * @author Chris Petsos (chpetsos@seerc.org)
 *
 */
public abstract class TokenAuthFilter implements ContainerRequestFilter {

	private final static Pattern tokenPattern = Pattern.compile("Bearer\\s+\\s*(.*)");
	
	protected ITokenStore tokenStore;
	
	public TokenAuthFilter()
	{
		tokenStore = new FileTokenStore();		
	}
	
	/**
	 * Expects to find an Authorization header of the form:
	 * Authorization: Bearer <access_token>
	 */
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String token = this.getToken(requestContext);
		if(token != null && !token.isEmpty())
		{	// we have the token
			this.filterWithToken(requestContext, token);
		}
		else
		{	// no token found
			requestContext.abortWith(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("No authorization token was provided.")
                    .build());
		}
	}
	
	protected abstract void filterWithToken(ContainerRequestContext requestContext, String userProvidedToken);

	/**
	 * Extracts the token (if any) from a request.
	 * @param requestContext The request that this filter is currently handling.
	 * @return The token or null if not found.
	 */
	private String getToken(ContainerRequestContext requestContext) { 
        String authorizationHeader = requestContext.getHeaderString("Authorization");
        if (authorizationHeader!=null && !authorizationHeader.isEmpty()) {
             
            Matcher tokenMatcher = tokenPattern.matcher(authorizationHeader);
            if (tokenMatcher.matches()) {
                return tokenMatcher.group(1);
            }
        }
         
        return null;
    }
}
