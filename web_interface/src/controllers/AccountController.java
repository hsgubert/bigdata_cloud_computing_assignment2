package controllers;

import helpers.UserSessionHelper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.User;

@WebServlet("/account")
public class AccountController extends HttpServlet {

	private static final long serialVersionUID = 1L;
    
    public AccountController() {
        super();
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	User user = UserSessionHelper.getLoggedUser(request.getSession());
    	request.setAttribute("user", user);
    	request.getRequestDispatcher("/WEB-INF/views/account.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = UserSessionHelper.getLoggedUser(request.getSession());
		
		String name = request.getParameter("name");
		String currentPassword = request.getParameter("current-password");
		String newPassword = request.getParameter("new-password");
		String newPasswordConfirmation = request.getParameter("new-password-confirmation");
		
		if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !newPasswordConfirmation.isEmpty()) {
			if (currentPassword.isEmpty() || !user.checkPassword(currentPassword)) {
				request.setAttribute("error_message", "Current password is incorrect.");
			}
			else if (newPassword.isEmpty() || newPasswordConfirmation.isEmpty() || !newPassword.equals(newPasswordConfirmation)) {
				request.setAttribute("error_message", "New password confirmation does not match new password.");
			}
			else {
				user.setName(name);
				user.setPassword(newPassword);
				if(user.save()) {
					request.setAttribute("success_message", "Account updated");
				} else {
					request.setAttribute("error_message", user.getValidationErrorMessage());
				}
			}
		}
		else {
			user.setName(name);
			if(user.save()) {
				request.setAttribute("success_message", "Name updated");
			} else {
				request.setAttribute("error_message", user.getValidationErrorMessage());
			}
		}
		request.setAttribute("user", user);
		request.getRequestDispatcher("/WEB-INF/views/account.jsp").forward(request, response);
	}		
						
}
