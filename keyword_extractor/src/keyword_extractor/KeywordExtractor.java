package keyword_extractor;

import java.io.IOException;
import java.util.List;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class KeywordExtractor {

	static final String FETCHED_TWEETS_QUEUE_NAME = "assignment2_fetched_tweets";
	static final String TAGGED_TWEETS_QUEUE_NAME = "assignment2_tagged_tweets";
	
	static SqsHelper sSqsHelper = null;
	static LogHelper sLogHelper = null;
	
	public static void main(String[] args) throws IOException {
		try {
			sLogHelper = LogHelper.getInstance();
			sSqsHelper = SqsHelper.getInstance();
			
			// get fetched tweets queue url (input)
			String fetchedTweetsQueueUrl = sSqsHelper.getQueueUrl(FETCHED_TWEETS_QUEUE_NAME);
			if (fetchedTweetsQueueUrl == null) {
				sLogHelper.error("There is no fetched tweets queue");
				System.exit(1);
			}
			
			// get tagged tweets queue url (output)
			String taggedTweetsQueueUrl = sSqsHelper.getQueueUrl(TAGGED_TWEETS_QUEUE_NAME);
			if (taggedTweetsQueueUrl == null) {
				taggedTweetsQueueUrl = sSqsHelper.createQueue(TAGGED_TWEETS_QUEUE_NAME);
				if (taggedTweetsQueueUrl == null) {
					sLogHelper.error("Could not create tagged tweets queue");
					System.exit(1);
				}
			}
			
			KeywordHelper keywordHelper = KeywordHelper.getInstance();
			
			while (true) {
				// get fetched tweets
				List<Message> messages = sSqsHelper.receiveMessage(fetchedTweetsQueueUrl);
				for (Message message : messages) {
					try {
						// loads the message and find its keywords
						JSONObject json = new JSONObject(message.getBody());
						List<String> keywords = keywordHelper.getKeywords(json.getString("text"));
						json.put("keywords", keywords);
						sSqsHelper.sendMessage(taggedTweetsQueueUrl, json.toString());
					} 
					catch (JSONException e) {
						sLogHelper.printException(e);
					} 
					finally {
						// deletes the message
						sSqsHelper.deleteMessage(fetchedTweetsQueueUrl, message.getReceiptHandle());
					}
				}
			}
		}
		catch (Exception e) {
			if (sLogHelper != null)
				sLogHelper.printException(e);
		}
	}
}


//List<String> test = new ArrayList<String>();
//test.add("#TheFlash premiere is today with ebola!");
//test.add("No keywords in here");
//test.add("do you like ...!JustinBieber??");
//for (String sentence : test) {
//	List<String> keywords = keywordHelper.getKeywords(sentence);
//	System.out.println(sentence);
//	for (String keyword : keywords) {
//		System.out.println(keyword);
//	}
//	System.out.println();
//}
