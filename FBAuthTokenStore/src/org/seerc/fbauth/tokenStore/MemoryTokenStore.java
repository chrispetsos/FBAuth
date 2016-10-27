package org.seerc.fbauth.tokenStore;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Dummy implementation of ITokenStore that stores tokens in memory.
 * Only used for testing purposes.
 * 
 * @author Chris Petsos (chpetsos@seerc.org)
 *
 */
public class MemoryTokenStore implements ITokenStore {

	protected HashMap<String, String> userProvidedTokens;
	protected HashMap<String, String> userExtendedTokens;
	
	public MemoryTokenStore()
	{
		userProvidedTokens = new HashMap<String, String>();
		userExtendedTokens = new HashMap<String, String>();
	}
	
	@Override
	public void storeUserToken(String userId, String userProvidedToken, String userExtendedToken) 
	{
		userProvidedTokens.put(userId, userProvidedToken);
		userExtendedTokens.put(userId, userExtendedToken);
	}

	@Override
	public String getUserProvidedToken(String userId) 
	{
		return userProvidedTokens.get(userId);
	}

	@Override
	public String getUserExtendedToken(String userId) 
	{
		return userExtendedTokens.get(userId);
	}

	@Override
	public void deleteUserToken(String userId)
	{
		userProvidedTokens.remove(userId);
		userExtendedTokens.remove(userId);
	}

	@Override
	public String getUserOfProvidedToken(String token) {
		if(!userProvidedTokens.containsValue(token))
		{
			return null;
		}
		
		for (Entry<String, String> entry : userProvidedTokens.entrySet()) {
            if (entry.getValue().equals(token)) {
                return entry.getKey();
            }
        }
		
		return null;
	}

	@Override
	public void init() {
		userProvidedTokens = new HashMap<String, String>();
		userExtendedTokens = new HashMap<String, String>();
	}

}
