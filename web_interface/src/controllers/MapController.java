package controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Tweets;
import controllers.utils.FlashMessages;

@WebServlet("/map")
public class MapController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public MapController() {
        super();
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	FlashMessages.extractFlashMessages(request);
    	request.setAttribute("keywords", Tweets.KEYWORDS);
    	request.getRequestDispatcher("/WEB-INF/views/map.jsp").forward(request, response);
	}
	
}
