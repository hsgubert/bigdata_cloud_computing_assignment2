package helpers;

import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

public class EmailSenderHelper {

	private static final String VERIFIED_FROM = "hs2807@columbia.edu"; 
	
	private static EmailSenderHelper sEmailSenderHelper;
	public static EmailSenderHelper getInstance() throws IOException {
		if (sEmailSenderHelper == null) {
			sEmailSenderHelper = new EmailSenderHelper(ApplicationHelper.getCredentials());
		}
		return sEmailSenderHelper;
	}

	private AmazonSimpleEmailServiceClient mAmazonSimpleEmailServiceClient = null;	
	
	public EmailSenderHelper(AWSCredentials awsCredentials) {
		this.mAmazonSimpleEmailServiceClient = new AmazonSimpleEmailServiceClient(awsCredentials);

		// set region to US East
		mAmazonSimpleEmailServiceClient.setRegion(ApplicationHelper.getAmazonRegion());
	}
	
	public boolean sendEmail(String to, String subject, String body) {
		// Construct an object to contain the recipient address.
        Destination destination = new Destination().withToAddresses(new String[]{to});
        
        // Create a message with the specified subject and body.
        Content subjectContent = new Content().withData(subject);
        Body bodyContent = new Body().withHtml(new Content().withData(body));
        Message message = new Message().withSubject(subjectContent).withBody(bodyContent);
        
        // Assemble the email.
        SendEmailRequest sendEmailRequest = new SendEmailRequest().
        		withSource(VERIFIED_FROM).
        		withDestination(destination).
        		withMessage(message);
        
        try {        
            // Send the email.
            mAmazonSimpleEmailServiceClient.sendEmail(sendEmailRequest);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
	
}
