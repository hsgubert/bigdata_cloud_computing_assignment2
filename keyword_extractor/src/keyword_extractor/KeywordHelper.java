package keyword_extractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class KeywordHelper {

	private static final String[] KEYWORDS = new String[] {
		"ebola","health","justinbieber","thanksgiving","unemployment",
		"theflash","dumbanddumber","alibaba","iphone","obama","climate",
		"parade", "government"
	};
	private static final String NONE = "_none_";
	
	// Singleton mechanism
	private static KeywordHelper sKeywordHelper;
	public static KeywordHelper getInstance() {
		if (sKeywordHelper == null) {
			sKeywordHelper = new KeywordHelper();
		}
		return sKeywordHelper;
	}
	
	private HashSet<String> mKeywords;
	
	public KeywordHelper() {
		mKeywords = new HashSet<String>();
		for (String keyword : KEYWORDS) {
			mKeywords.add(keyword);
		}
	}
	
	/**
	 * Identifies the keywords present in a piece of text and returns them
	 * in a list. Always adds "_none_" to the list, so the list is never empty.
	 */
	public List<String> getKeywords(String text) {
		List<String> keywords = new ArrayList<String>(2);
		keywords.add(NONE);
		
		// replace all non-characters simbols with space
		text = text.toLowerCase();
		for (String token : text.split("[^a-z0-9]")) {
			if (mKeywords.contains(token)) {
				keywords.add(token);
			}
		}
		
		return keywords;
	}
}
