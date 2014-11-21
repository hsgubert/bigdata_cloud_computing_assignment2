package helpers;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import models.User;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

public class ApplicationHelper implements ServletContextListener {

	// Run on application initialization
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			ApplicationHelper.ensureTablesExist();
			ApplicationHelper.subscribeToReceiveNotifications();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
	}
	
	static public AWSCredentials getCredentials() throws IOException {
		// We use the default credential chain since this will work in the EC2 servers too by getting credentials from the Instance Metadata Service (IMDS)
		return new DefaultAWSCredentialsProviderChain().getCredentials();
	}
	
	static public Region getAmazonRegion() {
		return Region.getRegion(Regions.US_EAST_1);
	}
	
	static public void ensureTablesExist() throws IOException {
		User.ensureTableExists();
//		Tweets.ensureTableExists();
	}

	static final String TWEETS_COMPRESSED_TOPIC_NAME = "assignment2_tweets_compressed";
	static final String ENDPOINT_URL = "http://jhm-assignment.elasticbeanstalk.com/sns_notifications";
	
	private static void subscribeToReceiveNotifications() {
		try {
			SnsHelper snsHelper = SnsHelper.getInstance();
			String topicArn = snsHelper.getTopicArn(TWEETS_COMPRESSED_TOPIC_NAME);
			if (topicArn != null) {
				if (!snsHelper.checkIfSubscribed(topicArn, ENDPOINT_URL)) {
					snsHelper.subscribeToTopicViaHttp(topicArn, ENDPOINT_URL);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
