package sentiment_analyzer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class SentimentAnalyzer {

	static final String TWEET_TABLE_NAME = "assignment2_tweets";
	static final String TAGGED_TWEETS_QUEUE_NAME = "assignment2_tagged_tweets";
	
	static LogHelper sLogHelper = null;
	static DynamoHelper sDynamoHelper = null;
	static SqsHelper sSqsHelper = null;
	
	public static void main(String[] args) throws IOException {
		try {
			sLogHelper = LogHelper.getInstance();
			sDynamoHelper = DynamoHelper.getInstance();
			
			// Checks if the DynamoDb table does not exist, and creates it
			if (!sDynamoHelper.checkIfTableExists(TWEET_TABLE_NAME)) {
				sLogHelper.info("Table " + TWEET_TABLE_NAME + " does not exist, creating it now");
				
				// create a hash key for the email (string)
				ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
				attributeDefinitions.add(new AttributeDefinition().withAttributeName("keyword").withAttributeType("S"));
				attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType("N"));
				attributeDefinitions.add(new AttributeDefinition().withAttributeName("saved_at").withAttributeType("N"));
				
				// specify that the key is of type hash and range
				ArrayList<KeySchemaElement> keySchemaElements = new ArrayList<KeySchemaElement>();
				keySchemaElements.add(new KeySchemaElement().withAttributeName("keyword").withKeyType(KeyType.HASH));
				keySchemaElements.add(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.RANGE));
				
				// create local secondary index to access tweets by saved_at
				ArrayList<KeySchemaElement> indexKeySchema = new ArrayList<KeySchemaElement>();
				indexKeySchema.add(new KeySchemaElement().withAttributeName("keyword").withKeyType(KeyType.HASH));
				indexKeySchema.add(new KeySchemaElement().withAttributeName("saved_at").withKeyType(KeyType.RANGE));
				
				Projection projection = new Projection().withProjectionType(ProjectionType.INCLUDE);
				ArrayList<String> nonKeyAttributes = new ArrayList<String>();
				nonKeyAttributes.add("latitude");
				nonKeyAttributes.add("longitude");
				nonKeyAttributes.add("sentiment_score");
				projection.setNonKeyAttributes(nonKeyAttributes);
				
				LocalSecondaryIndex localSecondaryIndex = new LocalSecondaryIndex()
				.withIndexName("saved_at_index").
				withKeySchema(indexKeySchema).
				withProjection(projection);
				
				ArrayList<LocalSecondaryIndex> localSecondaryIndexes = new ArrayList<LocalSecondaryIndex>();
				localSecondaryIndexes.add(localSecondaryIndex);
				
				// create table
				sDynamoHelper.createTable(attributeDefinitions, keySchemaElements, 10L, 10L, TWEET_TABLE_NAME, localSecondaryIndexes);
			}
			
			// Checks if the queue is accessible
			sSqsHelper = SqsHelper.getInstance();
			String taggedTweetsQueueUrl = sSqsHelper.getQueueUrl(TAGGED_TWEETS_QUEUE_NAME);
			if (taggedTweetsQueueUrl == null) {
				sLogHelper.error("There is no tagged tweets queue");
				System.exit(1);
			}
			
			SentimentHelper sentimentHelper = SentimentHelper.getInstance();
			
			while (true) {
				// get tagged tweets
				List<Message> messages = sSqsHelper.receiveMessage(taggedTweetsQueueUrl);
				for (Message message : messages) {
					try {
						// loads the message and find out its sentiment
						JSONObject json = new JSONObject(message.getBody());
						double score = sentimentHelper.getSentiment(json.getString("text"));
						
						if (score != 0.0) {
							// prepare to input to dynamo
							Map<String, AttributeValue> attrMap = new HashMap<String, AttributeValue>();
							attrMap.put("id", new AttributeValue().withN(String.valueOf(json.getLong("id"))));
							attrMap.put("created_at", new AttributeValue().withN(String.valueOf(json.getLong("created_at"))));
							attrMap.put("text", new AttributeValue().withS(String.valueOf(json.get("text"))));
							attrMap.put("country_code", new AttributeValue().withS(String.valueOf(json.get("country_code"))));
							attrMap.put("latitude", new AttributeValue().withN(String.valueOf(json.getDouble("latitude"))));
							attrMap.put("longitude", new AttributeValue().withN(String.valueOf(json.getDouble("longitude"))));
							attrMap.put("sentiment_score", new AttributeValue().withN(String.valueOf(score)));
							
							JSONArray keywords = json.getJSONArray("keywords");
							String symbol = (score > 0 ? "+" : "-");
							
							// for each keyword, put the tweet in dynamo
							for (int i=0; i<keywords.length(); i++) {
								attrMap.put("keyword", new AttributeValue().withS(keywords.getString(i) + symbol));
								attrMap.put("saved_at", new AttributeValue().withN(String.valueOf(System.currentTimeMillis())));
								sDynamoHelper.putItem(TWEET_TABLE_NAME, attrMap);
							}
						}
						
						// deletes the message
						sSqsHelper.deleteMessage(taggedTweetsQueueUrl, message.getReceiptHandle());
					} 
					catch (IOException | JSONException e) {
						// Don't do anything: message will be retried later
						sLogHelper.printException(e);
					} 
					catch (URISyntaxException e) {
						// deletes the message
						sLogHelper.warning("Could not get sentiment due to URISyntaxException. Just deleting the message");
						sLogHelper.printException(e);
						sSqsHelper.deleteMessage(taggedTweetsQueueUrl, message.getReceiptHandle());
					}
				}
			}
		}
		catch (Exception e) {
			if (sLogHelper != null) {
				sLogHelper.printException(e);
			}
		}
	}
}


//String[] KEYWORDS = new String[] {
//	"ebola","health","justinbieber","thanksgiving","unemployment",
//	"theflash","dumbanddumber","alibaba","iphone","obama","climate",
//	"parade", "government"
//};
//	
//for (String str : KEYWORDS) {
//	List<Map<String, AttributeValue>> tweets = dynamoHelper.queryByPrimaryKey(TWEET_TABLE_NAME, "keyword", str);
//	for (Map<String, AttributeValue> tweet : tweets) {
//		if (Double.valueOf(tweet.get("sentiment_score").getN()) > 0) {
//			tweet.put("keyword", new AttributeValue().withS(str + "+"));
//			dynamoHelper.putItem(TWEET_TABLE_NAME, tweet);
//		} 
//		else if (Double.valueOf(tweet.get("sentiment_score").getN()) < 0){
//			tweet.put("keyword", new AttributeValue().withS(str + "-"));
//			dynamoHelper.putItem(TWEET_TABLE_NAME, tweet);
//		}
//		dynamoHelper.deleteItem(TWEET_TABLE_NAME, "keyword", str, "id", tweet.get("id").getN());
//	}
//}
