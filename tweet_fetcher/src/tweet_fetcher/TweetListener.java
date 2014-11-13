package tweet_fetcher;

import twitter4j.JSONObject;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TweetListener implements StatusListener {

	// Queue used to put fetched tweets
	private String mQueueURL;
	private SqsHelper mSqsHelper;
	
	public TweetListener(String queueUrl) {
		mQueueURL = queueUrl;
		mSqsHelper = SqsHelper.getInstance();
	}
	
	@Override
    public void onStatus(Status status) {
        if (status.getGeoLocation() != null && status.getPlace() != null && status.getPlace().getCountryCode().equals("US")) {
        	try {
        		JSONObject json = new JSONObject();
        		json.put("id", status.getId());
        		json.put("created_at", status.getCreatedAt().getTime());
        		json.put("text", status.getText());
        		json.put("country_code", status.getPlace().getCountryCode());
        		json.put("latitude", status.getGeoLocation().getLatitude());
        		json.put("longitude", status.getGeoLocation().getLongitude());
        		mSqsHelper.sendMessage(mQueueURL, json.toString());
			} 
        	catch (Exception e) { }
//        	System.out.println("(" + String.valueOf(status.getGeoLocation().getLatitude()) + "," + String.valueOf(status.getGeoLocation().getLongitude()) + "): " + status.getText());
//        	if (status.getPlace() != null) {
//        		Place place = status.getPlace();
//        		System.out.println("    Country:" + place.getCountry());
//        		System.out.println("    Country code:" + place.getCountryCode());
//        		System.out.println("    Name:" + place.getName());
//        		System.out.println("    Fullname:" + place.getFullName());
//        		System.out.println("    Street address:" + place.getStreetAddress());
//        		System.out.println("    Place type:" + place.getPlaceType());
//        		System.out.println("    Geometry coordinates:" + place.getGeometryCoordinates());
//        	}
        }
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
//        System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
//        System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
        System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
    }

    @Override
    public void onStallWarning(StallWarning warning) {
//        System.out.println("Got stall warning:" + warning);
    }

    @Override
    public void onException(Exception ex) {
        ex.printStackTrace();
    }

}
