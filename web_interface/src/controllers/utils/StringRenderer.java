package controllers.utils;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * An object of this class should wrap the response of a servelet in case we
 * want to render a jsp to string:
 * 
 * StringRenderer stringRenderer = new StringRenderer(response);
 * request.getRequestDispatcher("path_to_my.jsp").forward(request, stringRenderer);
 * stringRenderer.getOutput()
 */
public class StringRenderer extends HttpServletResponseWrapper {
		
	private final CharArrayWriter charArray = new CharArrayWriter();
 
	public StringRenderer(HttpServletResponse response) {
		super(response);
	}
 
	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(charArray);	
	}
 
	public String getOutput() {
		return charArray.toString();
	}
}
