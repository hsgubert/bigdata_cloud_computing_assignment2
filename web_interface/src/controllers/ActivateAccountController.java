package controllers;

import helpers.UserSessionHelper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controllers.utils.FlashMessages;
import models.User;

@WebServlet("/activate_account")
public class ActivateAccountController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public ActivateAccountController() {
        super();
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = request.getParameter("email");
		String key = request.getParameter("key");
    	
		User user = User.loadFromDynamo(email);
		if (user == null || user.getActivated() || !user.getEncryptedPassword().equals(key)) {
			FlashMessages.addErrorMessage(request, "Invalid activation request!");
        	response.sendRedirect("home");
		} else {
			// activate user
			user.setActivated(true);
			
			if (user.save()) {
				if (UserSessionHelper.login(user, request.getSession())) {
					FlashMessages.addSuccessMessage(request, "Account activated!");
					response.sendRedirect("map");
				}
				else {
					FlashMessages.addSuccessMessage(request, "Account activated!");
					response.sendRedirect("home");
				}
			} 
			else {
				FlashMessages.addErrorMessage(request, "An error has occured during activation!");
	        	response.sendRedirect("home");
			}
		}
	}

}
