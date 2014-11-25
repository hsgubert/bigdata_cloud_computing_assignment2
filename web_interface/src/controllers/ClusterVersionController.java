package controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/cluster_version")
public class ClusterVersionController extends HttpServlet {

	private static final long serialVersionUID = 1L;
    public static long clusterVersion = 102;
	
    public ClusterVersionController() {
        super();
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.getWriter().write("{\"cluster_version\":" + String.valueOf(clusterVersion) + "}");
	}
}
