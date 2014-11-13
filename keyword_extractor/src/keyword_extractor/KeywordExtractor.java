package keyword_extractor;

import java.io.IOException;
import java.util.List;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class KeywordExtractor {

	static final String FETCHED_TWEETS_QUEUE_NAME = "assignment2_fetched_tweets";
	static final String TAGGED_TWEETS_QUEUE_NAME = "assignment2_tagged_tweets";
	
	public static void main(String[] args) throws IOException {
		SqsHelper sqsHelper = SqsHelper.getInstance();
		String fetchedTweetsQueueUrl = sqsHelper.getQueueUrl(FETCHED_TWEETS_QUEUE_NAME);
		if (fetchedTweetsQueueUrl == null) {
			System.out.println("There is no fetched tweets queue");
			System.exit(1);
		}
		
		String taggedTweetsQueueUrl = sqsHelper.getQueueUrl(TAGGED_TWEETS_QUEUE_NAME);
		if (taggedTweetsQueueUrl == null) {
			taggedTweetsQueueUrl = sqsHelper.createQueue(TAGGED_TWEETS_QUEUE_NAME);
			if (taggedTweetsQueueUrl == null) {
				System.out.println("Could not create tagged tweets queue");
				System.exit(1);
			}
		}
		
		KeywordHelper keywordHelper = KeywordHelper.getInstance();
		
		while (true) {
			// get fetched tweets
			List<Message> messages = sqsHelper.receiveMessage(fetchedTweetsQueueUrl);
			for (Message message : messages) {
				try {
					// loads the message and find its keywords
					JSONObject json = new JSONObject(message.getBody());
					List<String> keywords = keywordHelper.getKeywords(json.getString("text"));
					json.put("keywords", keywords);
					sqsHelper.sendMessage(taggedTweetsQueueUrl, json.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				} finally {
					// deletes the message
					sqsHelper.deleteMessage(fetchedTweetsQueueUrl, message.getReceiptHandle());
				}
			}
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
