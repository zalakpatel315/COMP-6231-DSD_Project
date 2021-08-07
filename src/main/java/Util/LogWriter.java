package Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.time.LocalDateTime;

// TODO: Auto-generated Javadoc
/**
 * The Class LogWriter.
 */
public class LogWriter implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant CLIENT_LOG_URL. */
	private static final String CLIENT_LOG_URL = "src/main/resources/client/";

	/** The Constant SERVER_LOG_URL. */
	private static final String SERVER_LOG_URL = "src/main/resources/server/";

	/** The Constant PHASE_TR. */
	public static final String PHASE_TR = "createTeacherRecord";

	/** The Constant PHASE_SR. */
	public static final String PHASE_SR = "createStudentRecord";

	/** The Constant PHASE_ER. */
	public static final String PHASE_ER = "editRecord";

	/** The Constant PHASE_GRC. */
	public static final String PHASE_GRC = "getRecordCount";

	/** The Constant PHASE_GRC. */
	public static final String PHASE_TRFR = "transferRecord";

	/** The Constant ACCESS_SYSTEM. */
	public static final String ACCESS_SYSTEM = "choosing system options";

	/** The Constant MONTREAL. */
	public static final String MONTREAL = "Montreal";

	/** The Constant LAVAL. */
	public static final String LAVAL = "Laval";

	/** The Constant DOLLARD. */
	public static final String DOLLARD = "Dollard-des-Ormeaux";

	/** The Constant SYSTEM. */
	public static final String SYSTEM = "System";

	/** The Constant PHASE_STARTUP. */
	public static final String PHASE_STARTUP = "StartUp";

	/** The Constant PHASE_REQUEST. */
	public static final String PHASE_REQUEST = "Request";

	/** The Constant PHASE_RESPONSE. */
	public static final String PHASE_RESPONSE = "Response";

	/**
	 * Client info.
	 *
	 * @param clientName the client name
	 * @param phase      the phase
	 * @param message    the message
	 */
	public void clientInfo(String clientName, String phase, String message) {

		String fileName = CLIENT_LOG_URL + clientName.toUpperCase() + ".txt";

		try (Writer output = new BufferedWriter(new FileWriter(fileName, true));) {
			output.append("\n");
			output.append(writeClientRecord(clientName, phase, message));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Server info.
	 *
	 * @param serverName the server name
	 * @param clientName the client name
	 * @param phase      the phase
	 * @param message    the message
	 */
	public void serverInfo(String serverName, String clientName, String phase, String message) {

		String fileName = SERVER_LOG_URL + serverName.toUpperCase() + ".txt";

		try (Writer output = new BufferedWriter(new FileWriter(fileName, true));) {
			output.append("\n");
			output.append(writeServerRecord(serverName, clientName, phase, message));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write client record.
	 *
	 * @param clientName the client name
	 * @param phase      the phase
	 * @param message    the message
	 * @return the string
	 */
	private String writeClientRecord(String clientName, String phase, String message) {
		return LocalDateTime.now() + " <> UserName: " + clientName.toUpperCase() + " <> Phase: " + phase + " <> Log: "
				+ message;
	}

	/**
	 * Write server record.
	 *
	 * @param serverName the server name
	 * @param clientName the client name
	 * @param phase      the phase
	 * @param message    the message
	 * @return the string
	 */
	private String writeServerRecord(String serverName, String clientName, String phase, String message) {
		return LocalDateTime.now() + " <> ServerName: " + serverName.toUpperCase() + " <> Operator: " + clientName
				+ " <> Phase: " + phase + " <> Log: " + message;
	}

}
