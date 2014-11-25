package controllers;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

@SuppressWarnings("deprecation")
@WebServlet("/sns_notifications")
public class SnsNotificationsController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public SnsNotificationsController() {
        super();
    }

    @SuppressWarnings({ "unused", "resource" })
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String messageType = request.getHeader("x-amz-sns-message-type");
    	
    	// if message doesn't have the message type header, don't process it.
		if (messageType == null)
			return;
    	
    	if (messageType.equals("SubscriptionConfirmation")) {
    		try {
	    		JSONObject json = new JSONObject(readBody(request));
	    		String url = json.getString("SubscribeURL");
    		
	    		HttpGet get = new HttpGet(url);
	    		HttpClient httpClient = new DefaultHttpClient();
	    		HttpResponse getResponse = httpClient.execute(get);
    		} catch (IOException | JSONException e) {
    			e.printStackTrace();
    		}
    	}
    	else if (messageType.equals("Notification")) {
    		System.out.println("Received notification!!!");
    		// We have to do something here!!! Perhaps websockets to update browsers???
    		ClusterVersionController.clusterVersion += 1;
    	}
    }
	
    private String readBody(HttpServletRequest request) throws IOException {
    	StringBuilder buffer = new StringBuilder();
	    BufferedReader reader = request.getReader();
	    String line;
	    while ((line = reader.readLine()) != null) {
	        buffer.append(line);
	    }
	    String data = buffer.toString();
	    return data;
    }
}

