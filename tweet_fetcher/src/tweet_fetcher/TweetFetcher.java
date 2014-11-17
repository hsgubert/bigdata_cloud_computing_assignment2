package tweet_fetcher;

import twitter4j.TwitterStream;

public class TweetFetcher {

	static final String FETCHED_TWEETS_QUEUE_NAME = "assignment2_fetched_tweets";
	
	static LogHelper sLogHelper = null;
	static SqsHelper sSqsHelper = null;
	
	public static void main(String[] args) {
		try {
			sLogHelper = LogHelper.getInstance();
			sSqsHelper = SqsHelper.getInstance();
			
			String queueUrl = sSqsHelper.getQueueUrl(FETCHED_TWEETS_QUEUE_NAME);
			if (queueUrl == null) {
				queueUrl = sSqsHelper.createQueue(FETCHED_TWEETS_QUEUE_NAME);
				if (queueUrl == null) {
					System.out.println("Could not create queue.");
					System.exit(1);
				}
			}
			
			TwitterStream twitterStream = Twitter4jHelper.getTwitterClient();
	        TweetListener listener = new TweetListener(queueUrl); 
	        twitterStream.addListener(listener);
	        LogHelper.getInstance().info("Starting to sample tweets");
	        twitterStream.sample();
		}
		catch (Exception e) {
			if (sLogHelper != null)
				sLogHelper.printException(e);
		}
	}

}
