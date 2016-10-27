package org.seerc.fbauth.tokenStore;

/**
 * Interface to token store classes. Stores a triple of:
 * - userId
 * - access token provided by user
 * - extended access token  
 * 
 * @author Chris Petsos (chpetsos@seerc.org)
 *
 */
public interface ITokenStore {

	/**
	 * Store token triple in store.
	 * 
	 * @param userId The id of the user.
	 * @param userProvidedToken The access token that the user provided.
	 * @param userExtendedToken The extended access token to be stored.
	 */
	void storeUserToken(String userId, String userProvidedToken, String userExtendedToken);

	/**
	 * Get access token provided by a user.
	 * 
	 * @param userId The id of the user.
	 * @return The access token provided by a user.
	 */
	String getUserProvidedToken(String userId);

	/**
	 * Get extended access token of a user.
	 * 
	 * @param userId The id of the user.
	 * @return The extended access token of a user.
	 */
	String getUserExtendedToken(String userId);

	/**
	 * Delete the auth token of a user.
	 * 
	 * @param userId The id of the user.
	 */
	void deleteUserToken(String userId);

	/**
	 * @param token The token to look for.
	 * @return The user that provided this token or null if token was not found.
	 */
	String getUserOfProvidedToken(String token);

	/**
	 * Initializes the token store
	 */
	void init();

}
