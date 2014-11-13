package helpers;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.servlet.http.HttpSession;

import models.User;

public class UserSessionHelper {

	/**
	 * Logs this user in.
	 * This method creates a random login key that is persisted in the database and 
	 * kept in the session to keep the user logged in. Returns whether the operation
	 * was successful.
	 */
	public static boolean login(User user, HttpSession session) {
		String sessionKey = generateSessionKey();
		user.setCurrentSessionKey(sessionKey);
		if (user.save()) {
			session.setAttribute("user_session_email", user.getEmail());
			session.setAttribute("user_session_key", sessionKey);
			return true;
		}
		return false;
	}
	
	public static void logoff(User user, HttpSession session) {
		session.removeAttribute("user_session_email");
		session.removeAttribute("user_session_key");
		
		user.setCurrentSessionKey(null);
		user.save();
	}
	
	/**
	 * Retrieves the current logged in user.
	 * In case some info does not match, ensure the session is cleared.
	 */
	public static User getLoggedUser(HttpSession session) {
		String email = (String) session.getAttribute("user_session_email");
		if (email != null) {
			String key = (String) session.getAttribute("user_session_key");
			if (key != null) {
				User user = User.loadFromDynamo(email);
				if (user != null && user.getCurrentSessionKey().equals(key)) {
					return user;
				}
			}
		}
		session.removeAttribute("user_session_email");
		session.removeAttribute("user_session_key");
		return null;
	}
	
	private static SecureRandom random = new SecureRandom();
	
	private static String generateSessionKey() {
		return new BigInteger(130, random).toString(32);
	}
	
}
