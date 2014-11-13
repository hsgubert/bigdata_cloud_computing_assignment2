package controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Tweets;

import com.amazonaws.util.json.JSONException;

@WebServlet("/tweet_points")
public class TweetPointsController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public TweetPointsController() {
        super();
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	if (request.getParameter("keyword") != null) {
			String keyword = (String) request.getParameter("keyword");
			try {
				response.getWriter().write(Tweets.retrieveClustersForKeyword(keyword).toString());
			} catch (JSONException e) {
				e.printStackTrace();
				response.getWriter().write("{}");
			}
    	}
    	else {
    		response.getWriter().write("{}");
    	}
    	
	}
	
}
