package org.seerc.fbauth.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This class will implement the REST API for the FBAuth server side.
 * 
 * @author Chris Petsos (chpetsos@seerc.org)
 *
 */

@Path("/api")
public class FBAuthRESTAPI {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/hello")
	public String sayHello() {
		return "Hello from FBAuthRESTAPI!";
	}
}
