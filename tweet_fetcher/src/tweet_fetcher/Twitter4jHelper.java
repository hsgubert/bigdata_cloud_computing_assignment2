package tweet_fetcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
 
public class Twitter4jHelper {
 
	// Open communication with Tweet API 
	public static TwitterStream getTwitterClient() throws IOException {
		LogHelper.getInstance().info("Connecting to Twitter API");
		
		Properties properties = Twitter4jHelper.getProperties();
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(properties.getProperty("OAuthConsumerKey"))
		  .setOAuthConsumerSecret(properties.getProperty("OAuthConsumerSecret"))
		  .setOAuthAccessToken(properties.getProperty("OAuthAccessToken"))
		  .setOAuthAccessTokenSecret(properties.getProperty("OAuthAccessTokenSecret"));
		
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		return twitterStream;
	}
	
	private static Properties getProperties() throws IOException { 
		Properties properties = new Properties();
		String propertiesFilename = "twitter.properties";
 
		File file = new File(propertiesFilename);
		InputStream inputStream = new FileInputStream(file);
		properties.load(inputStream);
 
		return properties;
	}
}
