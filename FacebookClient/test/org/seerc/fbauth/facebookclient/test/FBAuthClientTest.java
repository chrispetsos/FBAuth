package org.seerc.fbauth.facebookclient.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.fbauth.facebookclient.FBAuthClient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.FacebookClient.DebugTokenInfo;
import com.restfb.WebRequestor.Response;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.scope.ScopeBuilder;
import com.restfb.scope.UserDataPermissions;
import com.restfb.types.Group;
import com.restfb.types.GroupMember;
import com.restfb.types.Post;
import com.restfb.types.User;

public class FBAuthClientTest {

	private final Logger LOGGER = Logger.getLogger(this.getClass().getPackage().getName());

	/*
	 * This access token should be populated with a user access token created by
	 * the "FBAuth Admin app" Facebook app at the Facebook Graph API Explorer at,
	 * https://developers.facebook.com/tools/explorer/195274567520180
	 * 
	 * Needed permissions:
	 * user_managed_groups, user_posts, public_profile
	 */
	private static String adminAccessToken;
	private static Date adminAccessTokenExpiration;
	private static String accessTokenFile = "accessToken";
	private static String adminAccessTokenExpirationFile = "accessTokenExpiration";
	
	/*
	 * A unique machine ID that identified this client
	 */
	private static String machineId;
	private static String machineIdFile = "machineId";

	/*
	 * This access token should be populated with a user access token created by
	 * the "FBAuth Admin app" Facebook app at the Facebook Graph API Explorer at,
	 * https://developers.facebook.com/tools/explorer/195274567520180
	 * 
	 * Needed permissions:
	 * user_posts
	 */
	private static String userAccessToken = "CAACxmec7D7QBAJaOZAL8vSMyHmGyrri9iBgu8zZBd4iZAXkKN8oOyql33Kl4hzFoGsAU590Dl97B27eZCZBSGUnsL44lPXh9bQ7Yj3geZAVSIw0QPGxKTYDBRDjP9fX2nZBZCYilDGPgDYic8wwx2R0NsVz2IHnAJ4IK7qrvPB3CPDmbeVgybb1GAdVxlFCyYTIZD";
	
	/*
	 * This is the URL that the login dialog will redirect upon user authentication in
	 * testCreateAccessToken() 
	 */
	public static final String redirectUrl = "http://petsosspiti.no-ip.org:8080/FacebookWebTest/FacebookWebTestRest/loginRedirect";

	/*
	 * This is a short term token.
	 * Expires at 4/19/2016, 12:00:00 PM GMT+3:00 DST.
	 */
	String shortTermToken = "CAACxmec7D7QBAPWpKUEvNMIxTUteZBpGhUdVr0p4B85qCgm2rgAZAh0BnQYKjnIY01jyjULUQAj77cXx7KODnZBeHcMZCX9ZC7yAeuawP3ZC8N3QU8eVpje6Vkw10YkJtDmSYwlwSZBT9kZApaFfaZCATPUsCeEuOGZB8RHSwjKA1Uo1SF8RAkQWgQAc4Bnb3qadPiZAcRbblfAkwzu3X5EHzjA";
	
	FBAuthClient adminClient;
	FBAuthClient userClient;

	@BeforeClass
	public static void setUpClass() throws Exception
	{
		adminAccessToken = Files.readAllLines(Paths.get(accessTokenFile), Charset.defaultCharset()).get(0);
		machineId = Files.readAllLines(Paths.get(machineIdFile), Charset.defaultCharset()).get(0);
		String tempTokenExpiration = Files.readAllLines(Paths.get(adminAccessTokenExpirationFile), Charset.defaultCharset()).get(0);
		Long tempTokenExpirationMilliseconds = Long.parseLong(tempTokenExpiration);
		adminAccessTokenExpiration = new Date(tempTokenExpirationMilliseconds);
	}
	
	
	@Before
	public void setUp() throws Exception
	{
		adminClient = new FBAuthClient(adminAccessToken);
		userClient = new FBAuthClient(userAccessToken);
	}
	
	@Test
	public void testCreateFacebookClient() {
		assertNotNull(adminClient);
	}

	@Test
	public void testValidAccessToken() {
		assertTrue(adminClient.isTokenValid());
	}

	@Test
	public void testValidNonAdminToken() {
		assertTrue(userClient.isTokenValid());
	}

	@Test
	public void testGetPosts() {
		List<Post> allPosts = adminClient.getAllPosts();
		assertEquals(allPosts.get(0).getMessage(), "First Post on my wall!");
	}

	@Test
	public void testSecondClient() {
		List<Post> allPosts = userClient.getAllPosts();
		assertNotEquals(allPosts.get(0).getMessage(), "First Post on my wall!");
		assertEquals(allPosts.get(0).getMessage(), "This is my first post!");
	}

	@Test
	public void testGetGroups() {
		List<Group> allGroups = adminClient.getAllGroups();
		assertEquals(allGroups.get(0).getName(), "Test FBAuth");
		
		List<Group> secondGroups = userClient.getAllGroups();
		/*
		 * Note that size here is 0 because the second user doesn't have the
		 * user_managed_groups permission rather than because s/he doesn't actually
		 * manage a Facebook group.  
		 */
		assertEquals(secondGroups.size(), 0);
	}

	@Test
	public void testTokenExpiration() {
		Date adminTokenExpiration = adminClient.getTokenExpiration();
		Date userTokenExpiration = userClient.getTokenExpiration();
		
		Date now = new Date();
		
		assertTrue(adminTokenExpiration.after(now));
		assertTrue(userTokenExpiration.after(now));
	}
	
	/*
	 * Verdict: Long-lived access tokens cannot be further refreshed... NOT(?)
	 */
	@Test
	public void testRefreshLongLivedAccessToken() {
		FacebookClient client = new DefaultFacebookClient(Version.VERSION_2_5);
		String fbredirectUrl = "https://www.facebook.com/connect/login_success.html";
		try {
			// first use currently valid access token to get a code
			Response resp = client.getWebRequestor().executeGet("https://graph.facebook.com/oauth/client_code?access_token=" + adminAccessToken + "&client_secret=" + FBAuthClient.appSecret + "&redirect_uri=" + fbredirectUrl + "&client_id=" + FBAuthClient.appId);
			LOGGER.fine(resp.getBody());
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(resp.getBody()).getAsJsonObject();
			String code = obj.get("code").getAsString();
			LOGGER.fine("Got code: " + code);
			
			// then exchange code for a long-lived access token, at first call machineId is empty
			resp = client.getWebRequestor().executeGet("https://graph.facebook.com/oauth/access_token?code=" + code + "&client_id=" + FBAuthClient.appId + "&redirect_uri=" + fbredirectUrl + "&machine_id=" + machineId);
			AccessToken onTheFlyToken = client.getJsonMapper().toJavaObject(resp.getBody(), AccessToken.class);
			
			// extract machineId and save it
			String machineId = parser.parse(resp.getBody()).getAsJsonObject().get("machine_id").getAsString();
			LOGGER.fine("Got machine_id: " + machineId);
			PrintWriter out = new PrintWriter(machineIdFile);
			out.println(machineId);
			out.close();

			// create a new client with the onTheFlyToken and extend the currently valid access token
			FacebookClient debugClient = new DefaultFacebookClient(onTheFlyToken.getAccessToken(), Version.VERSION_2_5);
			AccessToken extendedAccessToken = debugClient.obtainExtendedAccessToken(FBAuthClient.appId, FBAuthClient.appSecret, onTheFlyToken.getAccessToken());
			
			// get expiration dates of all three tokens
			Date initialAccessTokenExpiration = debugClient.debugToken(adminAccessToken).getExpiresAt();	// the expires of the adminAccessToken after this procedure
			Date onTheFlyTokenExpiration = onTheFlyToken.getExpires();										// the expires of the onTheFlyToken after this procedure
			Date extendedAccessTokenExpiration = extendedAccessToken.getExpires();							// the expires of the extendedAccessToken after this procedure
			
			// if even one of those dates is after the adminAccessTokenExpiration for more that 30 seconds, fail the test to see what happens
			if(this.datesActuallyDiffer(initialAccessTokenExpiration, adminAccessTokenExpiration) || this.datesActuallyDiffer(onTheFlyTokenExpiration, adminAccessTokenExpiration) || this.datesActuallyDiffer(extendedAccessTokenExpiration, adminAccessTokenExpiration))
			{
				fail("The long-lived access token has been extended!");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*public void testRefreshLongLivedAccessToken() {
		FacebookClient client = new DefaultFacebookClient(Version.VERSION_2_5);
		try {
			Response resp = client.getWebRequestor().executeGet("https://graph.facebook.com/oauth/client_code?access_token=" + adminAccessToken + "&client_secret=" + FBAuthClient.appSecret + "&redirect_uri=" + redirectUrl + "&client_id=" + FBAuthClient.appId);
			LOGGER.fine(resp.getBody());
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(resp.getBody()).getAsJsonObject();
			String code = obj.get("code").getAsString();
			LOGGER.fine("Got code: " + code);
			//AccessToken onTheFlyToken = client.obtainUserAccessToken(appId, appSecret, redirectUrl, code);
			resp = client.getWebRequestor().executeGet("https://graph.facebook.com/oauth/access_token?code=" + code + "&client_id=" + FBAuthClient.appId + "&redirect_uri=" + redirectUrl + "&machine_id=" + machineId);
			AccessToken onTheFlyToken = client.getJsonMapper().toJavaObject(resp.getBody(), AccessToken.class);

			String machineId = parser.parse(resp.getBody()).getAsJsonObject().get("machine_id").getAsString();
			LOGGER.fine("Got machine_id: " + machineId);
			PrintWriter out = new PrintWriter(machineIdFile);
			out.println(machineId);
			out.close();
			
			FacebookClient debugClient = new DefaultFacebookClient(onTheFlyToken.getAccessToken(), Version.VERSION_2_5);
			DebugTokenInfo accessTokenInfo = debugClient.debugToken(adminAccessToken);
			AccessToken extendedAccessToken = debugClient.obtainExtendedAccessToken(FBAuthClient.appId, FBAuthClient.appSecret, onTheFlyToken.getAccessToken());
			LOGGER.fine("Got onTheFlyToken: " + onTheFlyToken);
			
			LOGGER.fine("Initial token was:\t\t" + adminAccessToken);
			LOGGER.fine("New token is:\t\t\t" + onTheFlyToken.getAccessToken());
			LOGGER.fine("Extended token is:\t\t" + extendedAccessToken.getAccessToken());
			
			LOGGER.fine("Initial token is expiring at:\t" + accessTokenInfo.getExpiresAt());
			LOGGER.fine("New token is expiring at:\t" + onTheFlyToken.getExpires());
			LOGGER.fine("Extended token is expiring at:\t" + extendedAccessToken.getExpires());
			
			// dates might differ a few secs some times, let them differ 30 secs
			if(Math.abs(accessTokenInfo.getExpiresAt().getTime() - extendedAccessToken.getExpires().getTime()) > 30000)
			{	// initial and extended access tokens are not equal, notify me to see what happened and don't save new token. Will have to handle saving
				// new token if this case comes up some time...
				System.err.println("Initial token is expiring at:\t" + accessTokenInfo.getExpiresAt());
				System.err.println("New token is expiring at:\t" + onTheFlyToken.getExpires());
				System.err.println("Extended token is expiring at:\t" + extendedAccessToken.getExpires());
			}
			else
			{	// they are equal, write them to disk (erroneous)
				out = new PrintWriter(accessTokenFile);
				out.println(extendedAccessToken.getAccessToken());
				out.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	/*@Test
	public void testCreateAccessToken() {
		ScopeBuilder scopeBuilder = new ScopeBuilder();
		scopeBuilder.addPermission(UserDataPermissions.USER_MANAGED_GROUPS);
		scopeBuilder.addPermission(UserDataPermissions.USER_POSTS);
		
		FacebookClient client = new DefaultFacebookClient(Version.VERSION_2_5);
		String loginDialogUrlString = client.getLoginDialogUrl(appId, redirectUrl, scopeBuilder);
		
		System.out.println("Login dialog URL: " + loginDialogUrlString);
		System.out.println("Paste the access token and hit enter...");
		String ontheflyToken = null;
		try {
			byte[] byteArray = new byte[10000]; 
			System.in.read(byteArray);
			ontheflyToken = new String(byteArray, "UTF-8");
			System.out.println("Got access token: " + ontheflyToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FBAuthClient onTheFlyClient = new FBAuthClient(ontheflyToken);
		
		List<Group> allGroups = onTheFlyClient.getAllGroups();
		assertEquals(allGroups.get(0).getName(), "Test FBAuth");
	}*/

	private boolean datesActuallyDiffer(Date date1, Date date2) {
		if(Math.abs(date1.getTime() - date2.getTime()) > 30000)
		{
			return true;
		}
		
		return false;
	}


	@Test
	public void testGetGroupMembers() {
		String groupId = adminClient.getAllGroups().get(0).getId(); // this should be "Test FBAuth"'s Id
		
		List<GroupMember> members = adminClient.getGroupMembers(groupId);
		assertEquals(members.size(), 3);
		
		for(GroupMember member:members)
		{
			if(member.getId().equals(adminClient.getAuthenticatedUser().getId()))
			{	// the admin 
				assertTrue(member.getIsAdmin());
			}
			else
			{	// other non-admin user
				assertFalse(member.getIsAdmin());
			}
		}
	}
	
	@Test
	public void testGetGroupPosts() {
		String groupId = adminClient.getAllGroups().get(0).getId(); // this should be "Test FBAuth"'s Id
		
		List<Post> posts = adminClient.getGroupPosts(groupId);
		assertEquals(posts.size(), 6);
	}
	
	@Test
	public void testGetPostsByMember() {
		String groupId = adminClient.getAllGroups().get(0).getId(); // this should be "Test FBAuth"'s Id
		
		String adminId = adminClient.getAuthenticatedUser().getId();
		String user2Id = userClient.getAuthenticatedUser().getId();

		List<Post> admin1Posts = adminClient.getPostsOfGroupMember(groupId, adminId);
		List<Post> user2Posts = adminClient.getPostsOfGroupMember(groupId, user2Id);
		
		assertEquals(admin1Posts.size(), 3);
		assertEquals(user2Posts.size(), 2);
	}
	
	@Test
	public void testGetGroupPostsByNonAdmin() {
		String groupId = adminClient.getAllGroups().get(0).getId(); // this should be "Test FBAuth"'s Id
		
		boolean failed = false;
		
		try
		{
			List<Post> posts = userClient.getGroupPosts(groupId);
			assertEquals(posts.size(), 0);
		}
		catch(Exception e)
		{
			failed = true;
		}
		
		assertTrue(failed);
	}
	
	@Test
	public void testGetPostsByMemberByNonAdmin() {
		String groupId = adminClient.getAllGroups().get(0).getId(); // this should be "Test FBAuth"'s Id
		
		String adminId = adminClient.getAuthenticatedUser().getId();
		String user2Id = userClient.getAuthenticatedUser().getId();

		boolean failed = false;
		
		try
		{
			List<Post> admin1Posts = userClient.getPostsOfGroupMember(groupId, adminId);
			assertEquals(admin1Posts.size(), 0);
		}
		catch(Exception e)
		{
			failed = true;
		}
		assertTrue(failed);
		
		failed = false;
		
		try
		{
			// User cannot even get posts of its own from group.
			List<Post> user2Posts = userClient.getPostsOfGroupMember(groupId, user2Id);
			assertEquals(user2Posts.size(), 0);
		}
		catch(Exception e)
		{
			failed = true;
		}
		assertTrue(failed);
	}
	
	@Test
	public void testGetPostsByNonExistingMember() {
		String groupId = adminClient.getAllGroups().get(0).getId(); // this should be "Test FBAuth"'s Id
		
		String fictionalId = "45987349857344093";

		List<Post> fictiona1Posts = adminClient.getPostsOfGroupMember(groupId, fictionalId);
		
		assertEquals(fictiona1Posts.size(), 0);
	}

	@Test
	public void testShortTermToken() {
		FBAuthClient shortClient = new FBAuthClient(shortTermToken);
		Boolean tokenValid = shortClient.isTokenValid();
		assertFalse(tokenValid);
	}
	
	@Test
	public void testExtendShortTermToken() {
		FBAuthClient shortClient = new FBAuthClient(adminAccessToken);
		assertEquals(adminAccessToken, shortClient.getCurrentToken());
		Date shortTokenExpiration = shortClient.getTokenExpiration();
		
		String extendedToken = shortClient.extendToken();
		assertNotEquals(extendedToken, adminAccessToken);
		assertEquals(extendedToken, shortClient.getCurrentToken());
		Date extendedTokenExpiration = shortClient.getTokenExpiration();
		
		assertTrue(!extendedTokenExpiration.before(shortTokenExpiration));
	}
	
	@Test
	public void testGetDebugTokenInfo() {
		DebugTokenInfo tokenInfo = adminClient.getDebugTokenInfo();
		assertEquals(FBAuthClient.appId, tokenInfo.getAppId());
	}
	
	@Before
	public void tearDown() throws Exception
	{
		
	}


}
