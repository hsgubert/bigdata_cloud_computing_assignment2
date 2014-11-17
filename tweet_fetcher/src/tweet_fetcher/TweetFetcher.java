package tweet_fetcher;

import java.io.IOException;

import twitter4j.TwitterStream;

public class TweetFetcher {

	static final String FETCHED_TWEETS_QUEUE_NAME = "assignment2_fetched_tweets";
	
	public static void main(String[] args) throws IOException {
		SqsHelper sqsHelper = SqsHelper.getInstance();
		
		String queueUrl = sqsHelper.getQueueUrl(FETCHED_TWEETS_QUEUE_NAME);
		if (queueUrl == null) {
			queueUrl = sqsHelper.createQueue(FETCHED_TWEETS_QUEUE_NAME);
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

}
