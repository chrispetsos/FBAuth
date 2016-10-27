package org.seerc.fbauth.facebookclient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.FacebookClient.DebugTokenInfo;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.Group;
import com.restfb.types.GroupMember;
import com.restfb.types.Post;
import com.restfb.types.User;

/**
 * 
 * This class will be responsible for fetching needed data from Facebook.
 * 
 * @author Chris Petsos (chpetsos@seerc.org)
 */
public class FBAuthClient {

	private final Logger LOGGER = Logger.getLogger(this.getClass().getPackage().getName());

	/*
	 * App ID and App Secret of the "FBAuth Admin app" Facebook app.
	 * 
	 * CAUTION!!!
	 * 
	 * DO NOT EXPOSE THIS DATA PUBLICLY!!!
	 */
	public final static String appId = "xxxxxxxxxxxxxxxxxxx";
	public final static String appSecret = "yyyyyyyyyyyyyyyyyyyyyyyyyyy";
	
	private FacebookClient fbClient;
	
	// the current access token
	private String accessToken;
	
	/**
	 * FBAuthClient is instantiated with an accessToken
	 * 
	 * @param accessToken The Facebook access token.
	 */
	public FBAuthClient(String accessToken)
	{
		fbClient = new DefaultFacebookClient(accessToken, Version.VERSION_2_5);
		
		this.accessToken = accessToken;
	}

	/**
	 * Gets all Posts of a user.
	 * 
	 * @return A List with all user's Posts.
	 */
	public List<Post> getAllPosts()
	{
		return getListFromFBEndpoint("me/feed", Post.class);
	}

	/**
	 * Gets all Groups managed by a user.
	 * @return A List with all Groups managed by the user. 
	 */
	public List<Group> getAllGroups()
	{
		return getListFromFBEndpoint("me/groups", Group.class);
	}

	/**
	 * Gets all the items existing in a single Facebook endpoint for a user. 
	 * This operation can be lengthy if user has a lot of items at that endpoint,
	 * because it iterates over all paged items and returns them in a single List.
	 * 
	 * @param fbEndpoint The Facebook endpoint to hit (e.g. "me/feed").
	 * @param objectType The kind of RestFB object types to return in the list.
	 * @param parameters Optional parameters to add when hitting FB endpoint. 
	 * @return A list containing all the items existing at the given endpoint for the user.
	 */
	private <T> List<T> getListFromFBEndpoint(String fbEndpoint, Class<T> objectType, Parameter... parameters)
	{
		List<T> resultList = new ArrayList<T>();
		Connection<T> myList = fbClient.fetchConnection(fbEndpoint, objectType, parameters);
		
		for (List<T> myConnectionPage : myList)
		{
			for (T item : myConnectionPage)
			{
				resultList.add(item);
			}
		}
		
		return resultList;		
	}

	/**
	 * Gets all members of a group
	 * @param groupId The Id of the group
	 * @return a List with all the members of the group
	 */
	public List<GroupMember> getGroupMembers(String groupId)
	{
		return getListFromFBEndpoint(groupId + "/members", GroupMember.class);
	}

	/**
	 * Gets the currently authenticated User
	 * @return The currently authenticated User
	 */
	public User getAuthenticatedUser() {
		return fbClient.fetchObject("me", User.class);
	}

	/**
	 * Returns the posts that a user has made to a group.
	 * @param groupId The group to look in.
	 * @param userId The user to look for.
	 * @return Lists of Posts of the user in the group.
	 */
	public List<Post> getPostsOfGroupMember(String groupId, String userId)
	{
		List<Post> allPosts = getListFromFBEndpoint(groupId + "/feed", Post.class, Parameter.with("fields", "from"));
		List<Post> userPosts = new ArrayList<Post>();
		
		for(Post post:allPosts)
		{
			if(post.getFrom().getId().equals(userId))
			{
				userPosts.add(post);
			}
		}
		
		return userPosts;
	}

	/**
	 * Returns all the posts in a group.
	 * @param groupId The group to look in.
	 * @return Lists of Posts in the group.
	 */
	public List<Post> getGroupPosts(String groupId)
	{
		return getListFromFBEndpoint(groupId + "/feed", Post.class);
	}

	/**
	 * Checks if the current client's token is valid.
	 * Uses an app token to query Graph API.
	 * @return Whether the current client's token is valid.
	 */
	public Boolean isTokenValid()
	{
		DefaultFacebookClient appClient = new DefaultFacebookClient(appId + "|" + appSecret, Version.VERSION_2_5);
		DebugTokenInfo tokenInfo = appClient.debugToken(accessToken);
		return tokenInfo.isValid();
	}

	/**
	 * Fetches the expiration date of the current token.
	 * @return The expiration date of the current token
	 */
	public Date getTokenExpiration() {
		DefaultFacebookClient appClient = new DefaultFacebookClient(appId + "|" + appSecret, Version.VERSION_2_5);
		DebugTokenInfo tokenInfo = appClient.debugToken(accessToken);
		return tokenInfo.getExpiresAt();
	}

	/**
	 * Returns the access token of the currently authenticated user.
	 * @return The access token of the currently authenticated user.
	 */
	public String getCurrentToken()
	{
		return this.accessToken;
	}

	/**
	 * Extends the current access token of the authenticated user. May not be able
	 * to extend it if it is already extended, and may not return the same access token. 
	 * @return The extended access token
	 */
	public String extendToken()
	{
		AccessToken extendedAccessToken = fbClient.obtainExtendedAccessToken(appId, appSecret, this.getCurrentToken());

		fbClient = new DefaultFacebookClient(extendedAccessToken.getAccessToken(), Version.VERSION_2_5);
		this.accessToken = extendedAccessToken.getAccessToken();
		
		return extendedAccessToken.getAccessToken();
	}

	/**
	 * Fetches the info needed to debug the current token such as if it valid, expiration time etc.
	 * @return The token's debug info.
	 */
	public DebugTokenInfo getDebugTokenInfo()
	{
		DefaultFacebookClient appClient = new DefaultFacebookClient(appId + "|" + appSecret, Version.VERSION_2_5);
		DebugTokenInfo tokenInfo = appClient.debugToken(accessToken);
		return tokenInfo;
	}
}
