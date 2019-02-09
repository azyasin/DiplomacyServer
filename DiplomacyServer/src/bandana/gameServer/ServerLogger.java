package bandana.gameServer;

import bandana.gameServer.BandanaServer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


class CloseFiles extends Thread {
	private FileHandler messagefh;
	private FileHandler errorfh;

	CloseFiles(FileHandler msg, FileHandler err) {
		this.messagefh = msg;
		this.errorfh = err;
	}

	public void run() {
		messagefh.close();
		errorfh.close();
	}

}

public class ServerLogger {
	//public static void main(String[] args) throws IOException {
	private final static Logger logger = Logger.getLogger(BandanaServer.class.getName());
	private static FileHandler errorfh;
	private static FileHandler messagefh;

	ServerLogger() {
	}

	// Called at start of server to create log files
	static void CreateFiles() {
		try {
			// Date and timem, formatted, for unique file names and ease of labeling
			DateTimeFormatter datetime = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
			LocalDateTime now = LocalDateTime.now();

			// File names
			String msg_file_name = "ServerMessage_" + datetime.format(now) + ".log";
			String error_file_name = "ServerError_" + datetime.format(now) + ".log";

			// Creates files
			messagefh = new FileHandler(msg_file_name, true);
			errorfh = new FileHandler(error_file_name, true);

			// Starts thread to close files when program teerminates. This Prevents buildup of .log.lck files/locks
			Runtime.getRuntime().addShutdownHook(new CloseFiles(messagefh, errorfh));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// For logging errors
	static void LogError(String message) {
		try {
			logger.addHandler(errorfh);
			SimpleFormatter formatter = new SimpleFormatter();
			errorfh.setFormatter(formatter);
			// Errors are produced as warnings
			logger.warning(message);
			logger.removeHandler(errorfh);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	// For logging server messages
	static void LogMessage(String message) {
		try {
			logger.setLevel(Level.ALL);
			logger.addHandler(messagefh);
			SimpleFormatter formatter = new SimpleFormatter();
			messagefh.setFormatter(formatter);
			logger.log(Level.FINE, message);
			logger.removeHandler(messagefh);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void closeLogger() {
		new CloseFiles(messagefh, errorfh);
	}
}
