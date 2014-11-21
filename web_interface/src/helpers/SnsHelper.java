package helpers;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;

public class SnsHelper {
	// Singleton mechanism
	private static SnsHelper sSnsHelper;
	public static SnsHelper getInstance() throws IOException {
		if (sSnsHelper == null) {
			sSnsHelper = new SnsHelper();
		}
		return sSnsHelper;
	}
	
	private AmazonSNSClient mSnsClient = null;
	
	public SnsHelper() throws IOException {
		try {
			// so amazon refreshes the credentials automatically
			this.mSnsClient = new AmazonSNSClient(new InstanceProfileCredentialsProvider());
			// test if the credentials work
			mSnsClient.listTopics();
		}
		catch (AmazonClientException e) {
			// probably is not in an EC2 instance, then look for the credentials in the default chain
			// (credentials will expire)
			this.mSnsClient = new AmazonSNSClient(new DefaultAWSCredentialsProviderChain().getCredentials());
		}

		// set region to US East
		mSnsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
	}
	
	/**
	 * Returns the topic ARN or null if it does not exist
	 */
	public String getTopicArn(String topicName) {
		try {
			ListTopicsResult result = mSnsClient.listTopics();
			Pattern pattern = Pattern.compile(".+:" + topicName);
			for (Topic topic : result.getTopics()) {
				if (pattern.matcher(topic.getTopicArn()).matches()) {
					return topic.getTopicArn();
				}
			}
		}
		catch (Exception e) {}
		return null;
	}
	
	/**
	 * Creates a topic and returns the topic ARN (unique identifier) or null if there was an error
	 */
	public String createTopic(String topicName) {
		try {
			CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
			CreateTopicResult createTopicResult = mSnsClient.createTopic(createTopicRequest);
			
			return createTopicResult.getTopicArn();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean checkIfSubscribed(String topicArn, String httpURL) {
		try {
			List<Subscription> subscriptions = mSnsClient.listSubscriptionsByTopic(topicArn).getSubscriptions();
			for (Subscription subscription : subscriptions) {
				if (subscription.getEndpoint().equals(httpURL)) {
					return true;
				}
			}
		}
		catch (Exception e) {
			System.out.println("Failed to subscribe to topic " + topicArn);
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Subscribes to a topic to receive notifications via HTTP. Returns true if success
	 */
	public boolean subscribeToTopicViaHttp(String topicArn, String httpURL) {
		try {
			SubscribeRequest subRequest = new SubscribeRequest(topicArn, "http", httpURL);
			mSnsClient.subscribe(subRequest);
			return true;
		}
		catch (Exception e) {
			System.out.println("Failed to subscribe to topic " + topicArn);
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Subscribes to a topic to receive notifications via HTTP. Returns true if success
	 */
	public boolean publishToTopic(String topicArn, String message) {
		try {
			PublishRequest publishRequest = new PublishRequest(topicArn, message);
			mSnsClient.publish(publishRequest);
			return true;
		}
		catch (Exception e) {
			System.out.println("Failed to publish to topic " + topicArn);
			e.printStackTrace();
			return false;
		}
	}
	
}
