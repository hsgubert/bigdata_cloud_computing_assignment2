package helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;

/**
 * Singleton class that centralizes access to DynamoDB. 
 * This class should not be used in controllers directly, but rather through models.
 */
public class DynamoHelper {
	private static DynamoHelper sDynamoHelper;
	public static DynamoHelper getInstance() throws IOException {
		if (sDynamoHelper == null) {
			sDynamoHelper = new DynamoHelper(ApplicationHelper.getCredentials());
		}
		return sDynamoHelper;
	}

	private AmazonDynamoDBClient mDynamoDBClient = null;	
	
	public DynamoHelper(AWSCredentials awsCredentials) {
		this.mDynamoDBClient = new AmazonDynamoDBClient(awsCredentials);
		// so amazon refreshes the credentials automatically
//		this.mDynamoDBClient = new AmazonDynamoDBClient(new InstanceProfileCredentialsProvider());

		// set region to US East
		mDynamoDBClient.setRegion(ApplicationHelper.getAmazonRegion());
	}
	
	/**
	 * Checks if the specified table exists in Amazon AWS.
	 * This method is used at startup to create tables that are missing
	 */
	public boolean checkIfTableExists(String table_name) {
		// checking if the table already exists
		boolean tableExists = true;
		try {
			return mDynamoDBClient.describeTable(table_name).getTable().getTableStatus().equals(TableStatus.ACTIVE.name());
		}
		catch (ResourceNotFoundException e) {
			tableExists = false;
		}
		
		return tableExists;
	}
	
	/**
	 * Creates a new Dynamo Table. 
	 * The caller should check before calling this function if a table with the same name does not
	 * already exist. This method does busy waiting and can take up to 120s to return. To see how 
	 * to create the parameters to this function refer to:
	 * http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LowLevelJavaWorkingWithTables.html#LowLevelJavaCreate
	 */
	public void createTable(ArrayList<AttributeDefinition> attributes, ArrayList<KeySchemaElement> keySchema, Long readCapacity, Long writeCapacity, String tableName) throws AmazonServiceException{
		// provisions 4 reads and 1 write per sec 
		ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
			.withReadCapacityUnits(readCapacity)
			.withWriteCapacityUnits(writeCapacity);
		
		CreateTableRequest request = new CreateTableRequest()
			.withTableName(tableName)
			.withAttributeDefinitions(attributes)
			.withKeySchema(keySchema)
			.withProvisionedThroughput(provisionedThroughput);
		mDynamoDBClient.createTable(request);
		
		// wait for table creation
		int triesLeft = 10;
		int secondsBetweenTrials = 12;
		while (triesLeft > 0) {
			try {
				TableDescription tableDescription = mDynamoDBClient.describeTable(tableName).getTable();
				String status = tableDescription.getTableStatus();
				if (status.equals(TableStatus.ACTIVE.toString())) {
					break;
				}
			}
			catch (ResourceNotFoundException e) {}
			triesLeft -= 1;
			try {
				Thread.sleep(secondsBetweenTrials * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if (triesLeft == 0) {
			throw new AmazonServiceException("Could not create DynamoDB table (it was created but never appeared as active).");
		}
	}
	
	public boolean putItem(String tableName, Map<String, AttributeValue> attributeMap) {
		try {			
			PutItemRequest putItemRequest = new PutItemRequest()
				.withTableName(tableName)
				.withItem(attributeMap);
			mDynamoDBClient.putItem(putItemRequest);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gets an item by its primary key.
	 * Returns a map of attributes or null if it could not load object
	 */
	public Map<String, AttributeValue> getItemByPrimaryKey(String tableName, String primaryKeyName, String primaryKeyValue) {
		try {			
			HashMap<String, AttributeValue> conditions = new HashMap<String, AttributeValue>();
			conditions.put(primaryKeyName, new AttributeValue().withS(primaryKeyValue));

			GetItemRequest getItemRequest = new GetItemRequest()
			    .withTableName(tableName)
			    .withKey(conditions);
			
			GetItemResult result = mDynamoDBClient.getItem(getItemRequest);
			return result.getItem();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Map<String, AttributeValue>> queryByPrimaryKey(String tableName, String primaryKeyName, String primaryKeyValue) {
		Condition primaryKeyCondition = new Condition()
		    .withComparisonOperator(ComparisonOperator.EQ)
		    .withAttributeValueList(new AttributeValue().withS(primaryKeyValue));
	
		Map<String, Condition> keyConditions = new HashMap<String, Condition>();
		keyConditions.put(primaryKeyName, primaryKeyCondition);
	
		List<Map<String, AttributeValue>> items = new ArrayList<Map<String, AttributeValue>>(100); 
		
		Map<String, AttributeValue> lastEvaluatedKey = null;
		while(true) {
			QueryRequest queryRequest = new QueryRequest()
				.withTableName(tableName)
				.withKeyConditions(keyConditions);
			
			if (lastEvaluatedKey != null) {
				queryRequest = queryRequest.addExclusiveStartKeyEntry(primaryKeyName, lastEvaluatedKey.get(primaryKeyName));
			} 
			
			try {
				QueryResult result = mDynamoDBClient.query(queryRequest);
				items.addAll(result.getItems());
				
				if (result.getLastEvaluatedKey() == null) {
					break;
				} else {
					lastEvaluatedKey = result.getLastEvaluatedKey();
				}
			}
			catch (ProvisionedThroughputExceededException e) {
				System.out.println("Provisioned throughput exceeded.. sleeping for a sec");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {	}
			}
			
			return items;
		}
		
		return items;
	}
	
}