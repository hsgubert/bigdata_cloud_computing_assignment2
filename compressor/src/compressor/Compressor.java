package compressor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.weka.WekaClusterer;
import weka.clusterers.XMeans;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.util.json.JSONArray;

public class Compressor {

	static final String TWEET_TABLE_NAME = "assignment2_tweets";
	static final String COMPRESSED_TWEET_TABLE_NAME = "assignment2_tweets_compressed";
	
	private static final String[] KEYWORDS = new String[] {
		"ebola","health","justinbieber","thanksgiving","unemployment",
		"theflash","dumbanddumber","alibaba","iphone","obama","climate",
		"parade", "government", "_none_"
	};
	
	private static final String[] SIGNS = new String[] { "+","-" };
	
	public static void main(String[] args) throws Exception {
		// Checks if the DynamoDb table does not exist, and creates it
		DynamoHelper dynamoHelper = DynamoHelper.getInstance();
		if (!dynamoHelper.checkIfTableExists(COMPRESSED_TWEET_TABLE_NAME)) {
			// create a hash key
			ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
			attributeDefinitions.add(new AttributeDefinition().withAttributeName("keyword").withAttributeType("S"));
			
			// specify that the key is of type hash
			ArrayList<KeySchemaElement> keySchemaElements = new ArrayList<KeySchemaElement>();
			keySchemaElements.add(new KeySchemaElement().withAttributeName("keyword").withKeyType(KeyType.HASH));
			
			// create table
			dynamoHelper.createTable(attributeDefinitions, keySchemaElements, 3L, 3L, COMPRESSED_TWEET_TABLE_NAME);
		}
		
		System.out.println();
		System.out.println("Starting...");
		for (String unsignedKeyword : KEYWORDS) {
			for (String sign : SIGNS) {
				long lastProcessedTweetSavedAt = 0;
				String keyword = unsignedKeyword + sign;
				System.out.println("Processing " + keyword);
				Dataset dataset = new DefaultDataset();
				JSONArray clusters = null;
				
				// first, read the last tweet_id considered to create the last cluster
				Map<String, AttributeValue> clusterAttrMap = dynamoHelper.getItemByPrimaryKey(COMPRESSED_TWEET_TABLE_NAME, "keyword", keyword);
				if (clusterAttrMap != null && clusterAttrMap.containsKey("last_processed_tweet_saved_at")) {
					lastProcessedTweetSavedAt = Long.valueOf(clusterAttrMap.get("last_processed_tweet_saved_at").getN());
					
					// Since we already have a cluster, lets populate our dataset with that
					clusters = new JSONArray(clusterAttrMap.get("clusters").getS());
					for (int i=0; i<clusters.length(); i++) {
						JSONArray cluster = (JSONArray) clusters.get(i);
						double[] coordinates = new double[] { cluster.getDouble(0), cluster.getDouble(1) };
						Instance instance = new DenseInstance(coordinates, Double.valueOf(cluster.getDouble(2)));
						dataset.add(instance);
					}
				} 
				
				// Read all new entries of the keyword
				List<Map<String, AttributeValue>> tweets = dynamoHelper.queryByPrimaryKeyAndIndex(TWEET_TABLE_NAME, "keyword", keyword, "saved_at", String.valueOf(lastProcessedTweetSavedAt+1), "saved_at_index");
				
				// If there is nothing new, return
				if (tweets.size() == 0) {
					continue;
				}
				
				// If new + clusters < 400 points, then just add the new points as clusters (it's not worth the clustering)
				if ((clusters == null && tweets.size() < 400) || (clusters != null && (tweets.size() + clusters.length() < 400))) {
					if (clusters == null)
						clusters = new JSONArray();
					
					long maxSavedAt = 0;
					for (Map<String, AttributeValue> tweet : tweets) {
						JSONArray point = new JSONArray();
						point.put(0, Double.valueOf(tweet.get("latitude").getN()));
						point.put(1, Double.valueOf(tweet.get("longitude").getN()));
						point.put(2, Math.abs(Double.valueOf(tweet.get("sentiment_score").getN())));

						long SavedAt = Long.valueOf(tweet.get("saved_at").getN());
						if (SavedAt > maxSavedAt)
							maxSavedAt = SavedAt;
						
						clusters.put(point);
					}
					Map<String, AttributeValue> attrMap = new HashMap<String, AttributeValue>();
					attrMap.put("keyword", new AttributeValue().withS(keyword));
					attrMap.put("last_processed_tweet_saved_at", new AttributeValue().withN(String.valueOf(maxSavedAt)));
					attrMap.put("clusters", new AttributeValue().withS(clusters.toString()));
					attrMap.put("saved_at", new AttributeValue().withN(String.valueOf(System.currentTimeMillis())));
					dynamoHelper.putItem(COMPRESSED_TWEET_TABLE_NAME, attrMap);
					
					continue;
				}
				
				// If none of the cases above, we will have to do clustering
				
				// Populate dataset with new entries
				long maxSavedAt = 0;
				for (Map<String, AttributeValue> tweet : tweets) {
					long savedAt = Long.valueOf(tweet.get("saved_at").getN());
					if (savedAt > maxSavedAt)
						maxSavedAt = savedAt;
					
					double[] features = new double[] { Double.valueOf(tweet.get("latitude").getN()), Double.valueOf(tweet.get("longitude").getN()) };
					Instance instance = new DenseInstance(features, Double.valueOf(tweet.get("sentiment_score").getN()));
					dataset.add(instance);
				}
				
				// Call K-means clustering
				XMeans xm = new XMeans();
				xm.setMaxNumClusters(300);
				xm.setMinNumClusters(200);
				xm.setMaxIterations(20);
				Clusterer clusterer = new WekaClusterer(xm);
				Dataset[] clustersDataset = clusterer.cluster(dataset);
				
				// For each cluster lets calculate the weighted centroid
				clusters = new JSONArray();
				for (Dataset clusterDataset : clustersDataset) {
					double weightedLatSum = 0;
					double weightedLongSum = 0;
					double weightsSum = 0;
					
					for (int i=0; i<clusterDataset.size(); i++) {
						Instance instance = clusterDataset.get(i);
						double weight = Math.abs((Double) instance.classValue());
						if (weight < 0.001)
							weight = 0.001;
						weightedLatSum += instance.value(0) * weight;
						weightedLongSum += instance.value(1) * weight;
						weightsSum += weight;
					}
					
					if (weightsSum > 0) {
						JSONArray point = new JSONArray();
						point.put(0, weightedLatSum / weightsSum);
						point.put(1, weightedLongSum / weightsSum);
						point.put(2, weightsSum);
						clusters.put(point);
					}
				}
				
				// Now lets save the clusters into DynamoDB
				Map<String, AttributeValue> attrMap = new HashMap<String, AttributeValue>();
				attrMap.put("keyword", new AttributeValue().withS(keyword));
				attrMap.put("last_processed_tweet_saved_at", new AttributeValue().withN(String.valueOf(maxSavedAt)));
				attrMap.put("clusters", new AttributeValue().withS(clusters.toString()));
				attrMap.put("saved_at", new AttributeValue().withN(String.valueOf(System.currentTimeMillis())));
				dynamoHelper.putItem(COMPRESSED_TWEET_TABLE_NAME, attrMap);
			}
		}
	}
}
