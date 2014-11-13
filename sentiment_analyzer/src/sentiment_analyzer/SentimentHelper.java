package sentiment_analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

@SuppressWarnings("deprecation")
public class SentimentHelper {

	// Singleton mechanism
	private static SentimentHelper sSentimentHelper;
	public static SentimentHelper getInstance() throws IOException {
		if (sSentimentHelper == null) {
			sSentimentHelper = new SentimentHelper();
		}
		return sSentimentHelper;
	}
	
	HttpClient mHttpClient;
	String mMashapeKey;
	
	public SentimentHelper() throws IOException {
		mHttpClient = new DefaultHttpClient();
		mMashapeKey = getProperties().getProperty("api_key");
	}
	
	/**
	 * Returns a value [-1,1]
	 * @throws URISyntaxException 
	 * @throws JSONException 
	 * @throws ParseException 
	 */
	public double getSentiment(String text) throws IOException, URISyntaxException, JSONException, ParseException {
		// https://www.mashape.com/twinword/sentiment-analysis-free#!documentation
		URI uri = new URIBuilder("https://twinword-sentiment-analysis.p.mashape.com/analyze/")
			.addParameter("text", text)
			.build();
		
		HttpGet request = new HttpGet(uri);
		request.addHeader("X-Mashape-Key", mMashapeKey);

		HttpResponse response = mHttpClient.execute(request);
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			JSONObject json = new JSONObject(EntityUtils.toString(entity));
			if (json.has("score")) {
				return json.getDouble("score");
			} else {
				throw new IOException("API did not return score.");
			}
		}
		
		throw new IOException("HTTP returned error code: " + String.valueOf(response.getStatusLine().getStatusCode()));
	}
	
	private static Properties getProperties() throws IOException { 
		Properties properties = new Properties();
		String propertiesFilename = "mashape.properties";
 
		File file = new File(propertiesFilename);
		InputStream inputStream = new FileInputStream(file);
		properties.load(inputStream);
 
		return properties;
	}
}
