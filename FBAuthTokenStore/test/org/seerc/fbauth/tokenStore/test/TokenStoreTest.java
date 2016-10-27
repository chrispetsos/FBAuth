package org.seerc.fbauth.tokenStore.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seerc.fbauth.tokenStore.FileTokenStore;
import org.seerc.fbauth.tokenStore.ITokenStore;
import org.seerc.fbauth.tokenStore.MemoryTokenStore;

public class TokenStoreTest {

	ITokenStore its;
	
	String user1Id = "user1Id";
	String user1NewToken = "user1NewToken";
	String user1ExtendedNewToken = "user1ExtendedNewToken";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		its = new FileTokenStore();
	}

	@Test
	public void testCreate() {
		Assert.assertNotNull(its);
	}

	@Test
	public void testStoreNewToken() {
		String userProvidedToken = its.getUserProvidedToken(user1Id);
		String userExtendedToken = its.getUserExtendedToken(user1Id);

		Assert.assertNull(userProvidedToken);
		Assert.assertNull(userExtendedToken);

		its.storeUserToken(user1Id, user1NewToken, user1ExtendedNewToken);
		
		userProvidedToken = its.getUserProvidedToken(user1Id);
		userExtendedToken = its.getUserExtendedToken(user1Id);
		
		Assert.assertEquals(user1NewToken, userProvidedToken);
		Assert.assertEquals(user1ExtendedNewToken, userExtendedToken);
	}

	@Test
	public void testRetrieveNonExistentToken() {
		String nonExistentUserId = "nonExistentUserId";
		
		String nonExistentProvidedToken = its.getUserProvidedToken(nonExistentUserId);
		String nonExistentExtendedToken = its.getUserExtendedToken(nonExistentUserId);
		
		Assert.assertNull(nonExistentProvidedToken);
		Assert.assertNull(nonExistentExtendedToken);
	}

	@Test
	public void testUpdateToken() {
		its.storeUserToken(user1Id, user1NewToken, user1ExtendedNewToken);
		
		String updatedProvidedToken = "updatedToken";
		String updatedExtendedToken = "updatedExtendedToken";

		its.storeUserToken(user1Id, updatedProvidedToken, updatedExtendedToken);
		
		Assert.assertEquals(updatedProvidedToken, its.getUserProvidedToken(user1Id));
		Assert.assertEquals(updatedExtendedToken, its.getUserExtendedToken(user1Id));
	}

	@Test
	public void testDeleteToken() {
		its.storeUserToken(user1Id, user1NewToken, user1ExtendedNewToken);
		
		its.deleteUserToken(user1Id);

		String userProvidedToken = its.getUserProvidedToken(user1Id);
		String userExtendedToken = its.getUserExtendedToken(user1Id);

		Assert.assertNull(userProvidedToken);
		Assert.assertNull(userExtendedToken);
	}

	@After
	public void tearDown() throws Exception {
		its.init();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

}
