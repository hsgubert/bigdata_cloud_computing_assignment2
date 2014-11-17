package compressor;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogHelper {

	// Singleton mechanism
	private static LogHelper sLogHelper;
	public static LogHelper getInstance() throws IOException {
		if (sLogHelper == null) {
			sLogHelper = new LogHelper();
		}
		return sLogHelper;
	}
	
	private Logger mLogger = null;
	private FileHandler mLogFileHandler = null;
	
	public LogHelper() throws IOException {
		mLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		mLogger.setLevel(Level.INFO);
		
		File logFolder = new File("log");
		logFolder.mkdir();
		
		mLogFileHandler = new FileHandler("log/output.log");
		mLogFileHandler.setFormatter(new SimpleFormatter());
		mLogger.addHandler(mLogFileHandler);
	}
	
	public void info(String message) {
		mLogger.info(message);
	}
	
	public void warning(String message) {
		mLogger.warning(message);
	}
	
	public void error(String message) {
		mLogger.severe(message);
	}
	
	public void printException(Exception e) {
		error("Exception occured listening to tweets!");
        error("Message: " + e.toString());
    	for (StackTraceElement line : e.getStackTrace()) {
    		error("    " + line.toString());
    	}
	}
}
