package models;

import helpers.DynamoHelper;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;

public class User {
	private static final String TABLE_NAME = "tweet_map_users";
	
	private String mName;
	private String mEmail;
	private String mEncryptedPassword;
	private boolean mActivated;
	private String mCurrentSessionKey;
	
	// attributes that store the decrypted password only if the user is being created now, otherwise are null
	private String mPasswordJustSet; 
	private String mPasswordJustSetConfirmation;
	
	// after calling valid() all the validation errors are stored here
	private ArrayList<String> mValidationErrors;
	
	// indicates whether the user has been created now or has been loaded from Dynamo
	private boolean mNewRecord;
	
	public User() {
		mValidationErrors = new ArrayList<String>();
		mNewRecord = true;
		mActivated = false;
		mCurrentSessionKey = null;
	}
	
	public String getName() {return mName;}
	public void setName(String name) {mName = name;}
	
	public String getEmail() {return mEmail;}
	public void setEmail(String email) {mEmail = email;}
	
	public void setPassword(String password) {
		mPasswordJustSet = password;
		this.setEncryptedPassword(User.encryptPassword(password));
	}
	public String getPassword() { return mPasswordJustSet; }
	public void setPasswordConfirmation(String passwordConfirmation) { mPasswordJustSetConfirmation = passwordConfirmation;	}
	public String getPasswordConfirmation() { return mPasswordJustSetConfirmation; }
	
	public boolean checkPassword(String password) {
		return User.encryptPassword(password).equals(mEncryptedPassword);
	}
	
	public void setEncryptedPassword(String encryptedPassword) { mEncryptedPassword = encryptedPassword; }
	public String getEncryptedPassword() { return mEncryptedPassword; }
	
	public void markAsNotNewRecord() {
		mNewRecord = false;
	}
	
	public void setCurrentSessionKey(String key) { mCurrentSessionKey = key; }
	public String getCurrentSessionKey() { return mCurrentSessionKey; }
	
	public void setActivated(boolean activated) { mActivated = activated; }
	public boolean getActivated() { return mActivated; }
	
	private static final String EMAIL_REGEXP = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String COLUMBIA_EMAIL_REGEXP = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@columbia.edu";
	
	/**
	 * Verifies if the user is valid (i.e. ready to be saved).
	 * If there are errors, they are put in mValidationErrors and can be seen by 
	 * calling getValidationErrors() and getValidationErrorMessages().
	 */
	public boolean valid() {
		mValidationErrors.clear();
		
		// Name should not be empty
		if (mName == null || mName.isEmpty()) {
			mValidationErrors.add("Name should not be empty.");
		}
		
		// Email should be from @columbia.edu and unique
		if (mEmail == null || mEmail.isEmpty()) {
			mValidationErrors.add("Email should not be empty.");
		}
		else if (!mEmail.matches(EMAIL_REGEXP)) {
			mValidationErrors.add("Email is not valid.");
		}
		else if (!mEmail.matches(COLUMBIA_EMAIL_REGEXP)) {
			mValidationErrors.add("Email must be from Columbia.");
		}
		else if (mNewRecord){
			try {
				DynamoHelper dynamoHelper = DynamoHelper.getInstance();
				if (dynamoHelper.getItemByPrimaryKey(TABLE_NAME, "email", mEmail) != null) {
					mValidationErrors.add("This email is already being used by another user.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Password should not be empty
		if (mEncryptedPassword == null) {
			mValidationErrors.add("Must set a password.");
		}
		else if (mNewRecord) {
			// Password confirmation must match
			if (mPasswordJustSet == null || !mPasswordJustSet.equals(mPasswordJustSetConfirmation)) {
				mValidationErrors.add("Password confirmation did not match password.");
			}
			// Password should be at least 6 chars long
			else if (mPasswordJustSet == null || mPasswordJustSet.length() < 6) {
				mValidationErrors.add("Password must be at least 6 characters long.");
			}
		}
		
		// is valid if there are no errors
		return mValidationErrors.size() == 0;
	}
	
	public ArrayList<String> getValidationErrors() {
		return mValidationErrors;
	}
	
	public String getValidationErrorMessage() {
		StringBuffer errorMessage = new StringBuffer();
		
		if (mNewRecord) {
			errorMessage.append("Account could not be created:");
		} else {
			errorMessage.append("Account could not be updated:");
		}
		
		errorMessage.append("<ul>");
		for (String str : getValidationErrors()) {
			errorMessage.append("<li>" + str + "</li>");
		}
		errorMessage.append("</ul>");
		
		return errorMessage.toString();
	}
	
	/**
	 * Saves the user in DynamoDB, but only does that if the user is valid.
	 * Returns true if has successfully saved, otherwise false
	 */
	public boolean save() {
		try {
			if (this.valid()) {
				DynamoHelper dynamoHelper = DynamoHelper.getInstance();
				return dynamoHelper.putItem(TABLE_NAME, this.getAttributeMap());
			}
			else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Map<String, AttributeValue> getAttributeMap() {
		Map<String, AttributeValue> attrMap = new HashMap<String, AttributeValue>();
		attrMap.put("name", new AttributeValue().withS(mName));
		attrMap.put("email", new AttributeValue().withS(mEmail));
		attrMap.put("encrypted_password", new AttributeValue().withS(mEncryptedPassword));
		attrMap.put("activated", new AttributeValue().withBOOL(mActivated));
		if (mCurrentSessionKey != null)
			attrMap.put("current_session_key", new AttributeValue().withS(mCurrentSessionKey));
		return attrMap;
	}
	
	static public User buildFromForm(Map<String, String[]> attrMap) {
		User user = new User();
		if (attrMap.containsKey("name")) { user.setName(attrMap.get("name")[0]); }
		if (attrMap.containsKey("email")) { user.setEmail(attrMap.get("email")[0]); }
		if (attrMap.containsKey("password")) { user.setPassword(attrMap.get("password")[0]); }
		if (attrMap.containsKey("password_confirmation")) { user.setPasswordConfirmation(attrMap.get("password_confirmation")[0]); }
		return user;
	} 
	
	static public User buildFromDynamo(Map<String, AttributeValue> attrMap) {
		User user = new User();
		user.setName(attrMap.get("name").getS());
		user.setEmail(attrMap.get("email").getS());
		user.setEncryptedPassword(attrMap.get("encrypted_password").getS());
		user.setActivated(attrMap.get("activated").getBOOL());
		if (attrMap.containsKey("current_session_key"))
			user.setCurrentSessionKey(attrMap.get("current_session_key").getS());
		user.markAsNotNewRecord();
		return user;
	}
	
	static public User loadFromDynamo(String email) {
		try {
			DynamoHelper dynamoHelper = DynamoHelper.getInstance();
			Map<String, AttributeValue> attrMap = dynamoHelper.getItemByPrimaryKey(TABLE_NAME, "email", email);
			
			if (attrMap != null) {
				return buildFromDynamo(attrMap);
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Checks if the users table exists in Dynamo and if not create the table.
	 * @throws IOException 
	 */
	static public void ensureTableExists() throws IOException {
		DynamoHelper dynamoHelper = DynamoHelper.getInstance();
		
		if (!dynamoHelper.checkIfTableExists(TABLE_NAME)) {
			// create a hash key for the email (string)
			ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
			attributeDefinitions.add(new AttributeDefinition().withAttributeName("email").withAttributeType("S"));
			
			// specify that the key is of type hash
			ArrayList<KeySchemaElement> keySchemaElements = new ArrayList<KeySchemaElement>();
			keySchemaElements.add(new KeySchemaElement().withAttributeName("email").withKeyType(KeyType.HASH));
			
			// create table
			dynamoHelper.createTable(attributeDefinitions, keySchemaElements, 2L, 2L, TABLE_NAME);
		}
	}
	
	private static String encryptPassword(String password) {
		StringBuffer hexString = new StringBuffer();
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(password.getBytes());
			byte[] digest = messageDigest.digest();
			
			for (int i=0; i<digest.length; i++) {
				if ((0xff & digest[i]) < 0x10) {
					hexString.append("0" + Integer.toHexString((0xFF & digest[i])));
				} else {
					hexString.append(Integer.toHexString(0xFF & digest[i]));
				}
			}
			return hexString.toString();
		} 
		catch (NoSuchAlgorithmException e) {
			// if the algorithm is not available does not encrypt
			return password;
		}
	}
	

}
