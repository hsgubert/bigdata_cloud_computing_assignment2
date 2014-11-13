package models;

import helpers.DynamoHelper;

import java.io.IOException;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class Tweets {

	static final String COMPRESSED_TWEET_TABLE_NAME = "assignment2_tweets_compressed";
	public static final String[] KEYWORDS = new String[] {
		"_none_","ebola","health","justinbieber","thanksgiving","unemployment",
		"theflash","dumbanddumber","alibaba","iphone","obama","climate",
		"parade", "government"
	};
	
	public static JSONObject retrieveClustersForKeyword(String keyword) throws IOException, JSONException {
		DynamoHelper dynamoHelper = DynamoHelper.getInstance();
		
		JSONObject json = new JSONObject();
		
		Map<String, AttributeValue> positive = dynamoHelper.getItemByPrimaryKey(COMPRESSED_TWEET_TABLE_NAME, "keyword", keyword + '+');
		if (positive != null && positive.containsKey("clusters")) {
			JSONArray clusters = new JSONArray(positive.get("clusters").getS());
			json.put("+", clusters);
		} else {
			json.put("+", new JSONArray());
		}
		
		Map<String, AttributeValue> negative = dynamoHelper.getItemByPrimaryKey(COMPRESSED_TWEET_TABLE_NAME, "keyword", keyword + '-');
		if (negative != null && negative.containsKey("clusters")) {
			JSONArray clusters = new JSONArray(negative.get("clusters").getS());
			json.put("-", clusters);
		} else {
			json.put("-", new JSONArray());
		}
		
		return json;
	}
	
}
