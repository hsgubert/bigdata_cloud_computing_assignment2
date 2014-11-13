package controllers.utils;

import javax.servlet.http.HttpServletRequest;

public class FlashMessages {

	static public void extractFlashMessages(HttpServletRequest request) {
		request.setAttribute("error_message", request.getSession().getAttribute("error_message"));
		request.getSession().removeAttribute("error_message");
		request.setAttribute("success_message", request.getSession().getAttribute("error_message"));
		request.getSession().removeAttribute("success_message");
	}
	
	static public void addSuccessMessage(HttpServletRequest request, String message) {
		request.getSession().setAttribute("success_message", message);
	}

	static public void addErrorMessage(HttpServletRequest request, String message) {
		request.getSession().setAttribute("error_message", message);
	}
}
