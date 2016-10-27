package org.seerc.fbauth.tokenStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class FileTokenStore extends MemoryTokenStore {

	private static final String providedFilePath = "~/fbauth.provided";
	private static final String extendedFilePath = "~/fbauth.extended";

	public FileTokenStore()
	{
		super();
		userProvidedTokens = this.readHashMapFromFile(providedFilePath);
		userExtendedTokens = this.readHashMapFromFile(extendedFilePath);
	}

	@Override
	public void storeUserToken(String userId, String userProvidedToken, String userExtendedToken) {
		super.storeUserToken(userId, userProvidedToken, userExtendedToken);
		this.writeHashMapToFile(userProvidedTokens, providedFilePath);
		this.writeHashMapToFile(userExtendedTokens, extendedFilePath);
	}

	@Override
	public String getUserProvidedToken(String userId) 
	{
		userProvidedTokens = this.readHashMapFromFile(providedFilePath);
		return userProvidedTokens.get(userId);
	}

	@Override
	public String getUserExtendedToken(String userId) 
	{
		userExtendedTokens = this.readHashMapFromFile(extendedFilePath);
		return userExtendedTokens.get(userId);
	}

	@Override
	public void deleteUserToken(String userId) {
		super.deleteUserToken(userId);
		this.writeHashMapToFile(userProvidedTokens, providedFilePath);
		this.writeHashMapToFile(userExtendedTokens, extendedFilePath);
	}

	@Override
	public String getUserOfProvidedToken(String token) {
		userProvidedTokens = this.readHashMapFromFile(providedFilePath);
		return super.getUserOfProvidedToken(token);
	}
	
	@Override
	public void init() {
		super.init();
		this.writeHashMapToFile(userProvidedTokens, providedFilePath);
		this.writeHashMapToFile(userExtendedTokens, extendedFilePath);
	}
	
	private HashMap<String, String> readHashMapFromFile(String filePath) 
	{
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try{
			File toRead=new File(filePath);
			fis=new FileInputStream(toRead);
			ois=new ObjectInputStream(fis);

			HashMap<String,String> mapInFile=(HashMap<String,String>)ois.readObject();

			return mapInFile;
		}catch(Exception e){}
		finally {
			if(ois != null)
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return new HashMap<String, String>();
	}

	private void writeHashMapToFile(HashMap<String, String> hashMap, String filePath) {
		try{
			File fileOne=new File(filePath);
			FileOutputStream fos=new FileOutputStream(fileOne);
			ObjectOutputStream oos=new ObjectOutputStream(fos);

			oos.writeObject(hashMap);
			oos.flush();
			oos.close();
			fos.close();
		}catch(Exception e){}
	}

}
