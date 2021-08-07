package Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * The Class Validator.
 */
public class Validator {

	/** The Constant CLIENT_LOG_URL. */
	private static final String CLIENT_LOG_URL = "src/main/resources/client/";

	/**
	 * The Enum Manager.
	 */
	enum Manager {

		/** The mtl. */
		MTL,
		/** The lvl. */
		LVL,
		/** The ddo. */
		DDO
	}

	/** The Constant EDITABLE_TEACHER_INFO. */
	protected static final List<String> EDITABLE_TEACHER_INFO = Arrays.asList("ADDRESS", "PHONE", "LOCATION");

	/** The Constant EDITABLE_STUDENT_INFO. */
	protected static final List<String> EDITABLE_STUDENT_INFO = Arrays.asList("COURSEREGISTERED", "STATUS",
			"STATUSDATE");

	/** The Constant TRIM_INPUT. */
	static final String TRIM_INPUT = "\\s*,\\s*";

	/** The output. */
	String output;

	/** The buffer reader. */
	BufferedReader bufferReader;

	/** The log. */
	static LogWriter log = new LogWriter();

	/**
	 * User name.
	 *
	 * @param userName the user name
	 * @return the boolean
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Boolean userName(String userName) throws IOException {

		Pattern pattern = Pattern.compile("^[A-Za-z]{3}[0-9]{4}");
		Matcher matcher = pattern.matcher(userName);
		if (matcher.matches()) {
			String managerPrefix = userName.substring(0, 3).toUpperCase().trim();
			for (Manager managerClient : Manager.values()) {
				if (managerClient.name().equals(managerPrefix)) {
					File file = new File(CLIENT_LOG_URL + userName.toUpperCase() + ".txt");
					if (!file.exists()) {
						if (file.createNewFile()) {
							System.out.println(userName + " has logged in");
							return true;
						} else {
							System.out.println("Failed to logged in, User: " + userName);
						}
					} else {
						System.out.println(userName + " has logged in");
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Teacher record.
	 *
	 * @param userName      the user name
	 * @param teacherRecord the teacher record
	 * @return the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<String> teacherRecord(String userName, String teacherRecord) throws IOException {

		Map<String, String> outputMap = new LinkedHashMap<>();
		List<String> methodReturn = null;
		bufferReader = new BufferedReader(new InputStreamReader(System.in));

		log.clientInfo(userName, LogWriter.PHASE_TR, "Entered Input: " + teacherRecord);
		String[] teacherRecordArray = teacherRecord.trim().split(TRIM_INPUT);
		if (teacherRecordArray.length >= 6) {

			// firstName
			output = Regex.stringRegex(teacherRecordArray[0]);
			outputMap.put("First Name", output);

			// lastName
			output = Regex.stringRegex(teacherRecordArray[1]);
			outputMap.put("Last Name", output);

			// address
			String[] outputs = Arrays.copyOfRange(teacherRecordArray, 2, teacherRecordArray.length - 3);
			outputMap.put("Address", String.join(",", outputs).trim());

			// phone
			output = Regex.numberRegex(teacherRecordArray[teacherRecordArray.length - 3]);
			outputMap.put("Phone", output);

			// specialization
			output = Regex.specializationRegex(teacherRecordArray[teacherRecordArray.length - 2]);
			outputMap.put("Specialization", output.replace("|", ","));

			// location
			output = Regex.locationRegex(teacherRecordArray[teacherRecordArray.length - 1]);
			outputMap.put("Location", output);

			if (outputMap.values().contains("invalidInput") || outputMap.values().contains("invalidLocation")) {
				System.out.println("\n");
				outputMap.forEach((field, outputValue) -> {
					if (outputValue != null
							&& (outputValue.contains("invalidInput") || outputValue.contains("invalidLocation"))) {
						System.out.println(field + " is invalid");
						log.clientInfo(userName, LogWriter.PHASE_TR, field + " is invalid");
					}
				});
				System.out.println("Please match the format mentioned above.");
			} else {
				System.out.println("\nFirstName , LastName  Address , phone , specialization , location ");
				System.out.println(outputMap.values().toString());

				System.out.println("\nPress S to save the current record or Press C to enter new record : ");
				String saveAction = bufferReader.readLine().trim();
				if (saveAction != null && "s".equalsIgnoreCase(saveAction)) {
					methodReturn = new ArrayList<>(outputMap.values());
				}
			}
		} else {
			System.out.println("\nInvalid Teacher Record input. Please match the format mentioned above.");
			log.clientInfo(userName, LogWriter.PHASE_TR,
					"Invalid Teacher Record input. Please match the format mentioned above.");
		}
		return methodReturn;
	}

	/**
	 * Student record.
	 *
	 * @param userName      the user name
	 * @param studentRecord the student record
	 * @return the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<String> studentRecord(String userName, String studentRecord) throws IOException {

		Map<String, String> outputMap = new LinkedHashMap<>();
		List<String> methodReturn = null;
		bufferReader = new BufferedReader(new InputStreamReader(System.in));

		log.clientInfo(userName, LogWriter.PHASE_SR, "Entered Input: " + studentRecord);
		String[] studentRecordArray = studentRecord.trim().split(TRIM_INPUT);
		if (studentRecordArray.length >= 5) {

			// firstName
			output = Regex.stringRegex(studentRecordArray[0]);
			outputMap.put("First Name", output);

			// lastName
			output = Regex.stringRegex(studentRecordArray[1]);
			outputMap.put("Last Name", output);

			// course registered
			output = Regex.specializationRegex(studentRecordArray[2]);
			outputMap.put("Course Registered", output.replace("|", ","));

			// status
			output = Regex.flagRegex(studentRecordArray[3]);
			outputMap.put("Status", output.equalsIgnoreCase("invalidStatus") ? "invalidStatus"
					: output.equalsIgnoreCase("1") ? "Active" : "NotActive");

			// statusDate
			output = Regex.dateRegex(studentRecordArray[4]);
			outputMap.put("Status Date", output);

			if (outputMap.values().contains("invalidInput") || outputMap.values().contains("invalidStatus")
					|| outputMap.values().contains("invalidDate")) {
				System.out.println("\n");
				outputMap.forEach((field, outputValue) -> {
					if (outputValue != null && (outputValue.contains("invalidInput")
							|| outputValue.contains("invalidStatus") || outputValue.contains("invalidDate"))) {
						System.out.println(field + " is invalid");
						log.clientInfo(userName, LogWriter.PHASE_SR, field + " is invalid");
					}
				});
				System.out.println("Please match the format mentioned above.");
			} else {
				System.out.println("\nfirstName, lastName, courseRegistered, status, statusDate)");
				System.out.println(outputMap.values().toString());
				System.out.println("\nPress S to save the current record or Press C to enter new record : ");
				String saveAction = bufferReader.readLine().trim();
				if (saveAction != null && "s".equalsIgnoreCase(saveAction)) {
					methodReturn = new ArrayList<>(outputMap.values());
				}
			}
		} else {
			System.out.println("\nInvalid Student Record input. Please match the format mentioned above.");
			log.clientInfo(userName, LogWriter.PHASE_SR,
					"Invalid Student Record input. Please match the format mentioned above.");
		}
		return methodReturn;
	}

	/**
	 * Edits the record.
	 *
	 * @param userName   the user name
	 * @param editRecord the edit record
	 * @return the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<String> editRecord(String userName, String editRecord) throws IOException {
		List<String> outputList = new ArrayList<>();
		List<String> methodReturn = null;
		bufferReader = new BufferedReader(new InputStreamReader(System.in));

		log.clientInfo(userName, LogWriter.PHASE_ER, "Entered Input: " + editRecord);
		String[] editRecordArray = editRecord.trim().split(TRIM_INPUT);

		if (editRecordArray.length >= 3) {

			if (editRecordArray[0].toUpperCase().startsWith("TR")
					|| editRecordArray[0].toUpperCase().startsWith("SR")) {

				if (editRecordArray[0].toUpperCase().startsWith("TR")
						&& EDITABLE_TEACHER_INFO.contains(editRecordArray[1].toUpperCase())) {
					outputList.add(editRecordArray[0].toUpperCase());
					outputList.add(editRecordArray[1].toUpperCase());
					validateTeacherEditValue(editRecordArray[1].toUpperCase(), editRecordArray[2], outputList);
				} else if (editRecordArray[0].toUpperCase().startsWith("SR")
						&& EDITABLE_STUDENT_INFO.contains(editRecordArray[1].toUpperCase())) {
					outputList.add(editRecordArray[0].toUpperCase());
					outputList.add(editRecordArray[1].toUpperCase());
					validateStudentEditValue(editRecordArray[1].toUpperCase(), editRecordArray[2], outputList);
				} else {
					outputList.add("invalidField");
				}

			} else {
				outputList.add("invalidRecord");

			}

			if (outputList.contains("invalidRecord")) {
				System.out.println("\nInvalid Record ID. Please match the format mentioned above.");
				log.clientInfo(userName, LogWriter.PHASE_ER, "Invalid Record ID");
			} else if (outputList.contains("invalidField")) {
				System.out.println("\nField Name is incorrect. Please match the format mentioned above.");
				log.clientInfo(userName, LogWriter.PHASE_ER, "Field Name is incorrect");
			} else if (outputList.contains("invalidInput") || outputList.contains("invalidStatus")
					|| outputList.contains("invalidDate") || outputList.contains("invalidLocation")) {
				System.out.println("\nValue is in incorrect. Please match the format mentioned above.");
				log.clientInfo(userName, LogWriter.PHASE_ER, "Value is in incorrect");
			} else {
				System.out.println("\n(RecordId, fieldName, newValue)");
				System.out.println(outputList.toString());
				System.out.println("\n Press E to proceed with editing or Press C to enter new record to edit : ");
				String saveAction = bufferReader.readLine().trim();
				if (saveAction != null && "E".equalsIgnoreCase(saveAction)) {
					methodReturn = outputList;
				}
			}

		} else {
			System.out.println("\nInvalid Edit Record input. Please match the format mentioned above.");
			log.clientInfo(userName, LogWriter.PHASE_ER,
					"Invalid Edit Record input. Please match the format mentioned above.");
		}
		return methodReturn;
	}

	/**
	 * Validate teacher edit value.
	 *
	 * @param fieldName  the field name
	 * @param value      the value
	 * @param outputList the output list
	 */
	private void validateTeacherEditValue(String fieldName, String value, List<String> outputList) {

		if (EDITABLE_TEACHER_INFO.get(1).equalsIgnoreCase(fieldName)) {
			// phone
			output = Regex.numberRegex(value);
			outputList.add(output);
		} else if (EDITABLE_TEACHER_INFO.get(2).equalsIgnoreCase(fieldName)) {
			// location
			output = Regex.locationRegex(value);
			outputList.add(output);
		} else {
			outputList.add(value);
		}
	}

	/**
	 * Validate student edit value.
	 *
	 * @param fieldName  the field name
	 * @param value      the value
	 * @param outputList the output list
	 */
	private void validateStudentEditValue(String fieldName, String value, List<String> outputList) {

		if (EDITABLE_STUDENT_INFO.get(0).equalsIgnoreCase(fieldName)) {
			// courseRegistered
			output = Regex.specializationRegex(value);
			outputList.add(output.replace("|", ","));
		}
		if (EDITABLE_STUDENT_INFO.get(1).equalsIgnoreCase(fieldName)) {
			// status
			output = Regex.flagRegex(value);
			outputList.add(output.equalsIgnoreCase("1") ? "Active" : "NotActive");
		}
		if (EDITABLE_STUDENT_INFO.get(2).equalsIgnoreCase(fieldName)) {
			// statusDate
			output = Regex.dateRegex(value);
			outputList.add(output);
		}
	}

	/**
	 * Transfer record.
	 *
	 * @param userName       the user name
	 * @param transferRecord the transfer record
	 * @return the list
	 */
	public List<String> transferRecord(String userName, String transferRecord) {
		Map<String, String> outputMap = new LinkedHashMap<>();
		List<String> methodReturn = null;
		bufferReader = new BufferedReader(new InputStreamReader(System.in));

		log.clientInfo(userName, LogWriter.PHASE_TRFR, "Entered Input: " + transferRecord);
		String[] transferRecordArray = transferRecord.trim().split(TRIM_INPUT);

		if (transferRecordArray.length >= 2) {

			// recordId
			if (transferRecordArray[0].toUpperCase().startsWith("TR")
					|| transferRecordArray[0].toUpperCase().startsWith("SR")) {
				outputMap.put("recordId", transferRecordArray[0].toUpperCase());
			} else {
				outputMap.put("recordId", "invalidRecordId");
			}

			// location
			output = Regex.locationRegex(transferRecordArray[1].toUpperCase());
			outputMap.put("remoteServerName", output);

			if (outputMap.values().contains("invalidRecordId") || outputMap.values().contains("invalidLocation")) {
				System.out.println("\n");
				outputMap.forEach((field, outputValue) -> {
					if (outputValue != null
							&& (outputValue.contains("invalidRecordId") || outputValue.contains("invalidLocation"))) {
						System.out.println(field + " is invalid");
						log.clientInfo(userName, LogWriter.PHASE_TRFR, field + " is invalid");
					}
				});
				System.out.println("Please match the format mentioned above.");
			} else {
				methodReturn = new ArrayList<>(outputMap.values());
			}

		} else {
			System.out.println("\nInvalid transfer Record input. Please match the format mentioned above.");
			log.clientInfo(userName, LogWriter.PHASE_TRFR,
					"Invalid transfer Record input. Please match the format mentioned above.");
		}
		return methodReturn;

	}

}
