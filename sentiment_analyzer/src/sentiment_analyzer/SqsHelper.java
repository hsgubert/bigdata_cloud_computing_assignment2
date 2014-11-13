package sentiment_analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

/**
 * Singleton class that centralizes access to SQS. 
 */
/**
 * @author Henrique
 *
 */
public class SqsHelper {

	// Singleton mechanism
	private static SqsHelper sSqsHelper;
	public static SqsHelper getInstance() {
		if (sSqsHelper == null) {
			sSqsHelper = new SqsHelper();
		}
		return sSqsHelper;
	}
	
	private AmazonSQSClient mSqsClient = null;
	
	public SqsHelper() {
		// so amazon refreshes the credentials automatically
		this.mSqsClient = new AmazonSQSClient(new InstanceProfileCredentialsProvider());
//		this.mSqsClient = new AmazonSQSClient(new DefaultAWSCredentialsProviderChain().getCredentials());

		// set region to US East
		mSqsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
	}
	
	/**
	 * Gets the queue URL.
	 * Can be used to check if the specified queue exists (returns null otherwise)
	 */
	public String getQueueUrl(String queueName) {
		try {
			return mSqsClient.getQueueUrl(queueName).getQueueUrl();
		}
		catch (QueueDoesNotExistException e) {
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Creates a queue with the specified name.
	 * Returns the queue URL or null if it fails
	 */
	public String createQueue(String queueName) {
		Map<String,String> queueAttributes = new HashMap<String,String>();
		queueAttributes.put("ReceiveMessageWaitTimeSeconds", "20");
		queueAttributes.put("VisibilityTimeout", "1800");
		
		CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName)
			.withAttributes(queueAttributes);
		
		try {
			return mSqsClient.createQueue(createQueueRequest).getQueueUrl();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Sends message. Returns true if success
	 */
	public boolean sendMessage(String queueURL, String content) {
		try {	
			mSqsClient.sendMessage(queueURL, content);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gets available messages (up to 10)
	 * Returns an empty list otherwise
	 */
	public List<Message> receiveMessage(String queueURL) {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueURL)
        	.withMaxNumberOfMessages(10);
        
        try {
	        List<Message> messages = mSqsClient.receiveMessage(receiveMessageRequest).getMessages();
	        return messages;
        }
		catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<Message>();
		}
	}
	
	/**
	 * Deletes a message returning whether the operation was a success or not
	 */
	public boolean deleteMessage(String queueUrl, String messageReceiptHandle) {
		try {
			mSqsClient.deleteMessage(new DeleteMessageRequest(queueUrl, messageReceiptHandle));
			return true;
        }
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
