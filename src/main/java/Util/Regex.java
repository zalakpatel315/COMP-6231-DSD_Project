package Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Util.Validator.Manager;

// TODO: Auto-generated Javadoc
/**
 * The Class Regex.
 */
public class Regex {

	/** The output. */
	static String output;
	
	/** The Constant STRING_FORMAT. */
	static final String STRING_FORMAT = "^[a-zA-Z\\s]*$";
	
	/** The Constant SPECIALIZATION_FORMAT. */
	static final String SPECIALIZATION_FORMAT = "^[a-zA-Z0-9\\s\\|\\.]*$";
	
	/** The Constant PHONE_NUMBER_FORMAT. */
	static final String PHONE_NUMBER_FORMAT = "^\\d{10}$";
	
	/** The Constant ALPHNUMERIC_FORMAT. */
	static final String ALPHNUMERIC_FORMAT = "^(?:[a-zA-Z0-9]+(?:,[a-zA-Z0-9]+)*)?$";
	
	/** The Constant DATE_FORMAT. */
	static final String DATE_FORMAT = "^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$";
	
	/** The Constant BOOLEAN_FORMAT. */
	static final String BOOLEAN_FORMAT = "^[0|1]{1}";
	
	/** The Constant INVALID_INPUT. */
	static final String INVALID_INPUT = "invalidInput";

	/**
	 * String regex.
	 *
	 * @param inputRecord the input record
	 * @return the string
	 */
	public static String stringRegex(String inputRecord) {
		output = null;
		Pattern pattern = Pattern.compile(STRING_FORMAT);
		Matcher matcher = pattern.matcher(inputRecord);
		if (matcher.matches()) {
			return inputRecord;
		} else {
			return INVALID_INPUT;
		}
	}

	/**
	 * Number regex.
	 *
	 * @param inputRecord the input record
	 * @return the string
	 */
	public static String numberRegex(String inputRecord) {
		output = null;
		Pattern pattern = Pattern.compile(PHONE_NUMBER_FORMAT);
		Matcher matcher = pattern.matcher(inputRecord);
		if (matcher.matches()) {
			return inputRecord;
		} else {
			return INVALID_INPUT;
		}
	}

	/**
	 * Alphanumeric regex.
	 *
	 * @param inputRecord the input record
	 * @return the string
	 */
	public static String alphanumericRegex(String inputRecord) {
		output = null;
		Pattern pattern = Pattern.compile(ALPHNUMERIC_FORMAT);
		Matcher matcher = pattern.matcher(inputRecord);
		if (matcher.matches()) {
			return inputRecord;
		} else {
			return INVALID_INPUT;
		}
	}

	/**
	 * Location regex.
	 *
	 * @param inputRecord the input record
	 * @return the string
	 */
	public static String locationRegex(String inputRecord) {
		output = null;
		String validLocation = null;
		for (Manager managerClient : Manager.values()) {
			if (managerClient.name().equals(inputRecord.toUpperCase().trim())) {
				validLocation = inputRecord.toUpperCase();
				break;
			} else {
				validLocation = "invalidLocation";
			}
		}
		return validLocation;
	}

	/**
	 * Date regex.
	 *
	 * @param inputRecord the input record
	 * @return the string
	 */
	public static String dateRegex(String inputRecord) {
		output = null;
		Pattern pattern = Pattern.compile(DATE_FORMAT);
		Matcher matcher = pattern.matcher(inputRecord);
		if (matcher.matches()) {
			return inputRecord;
		} else {
			return "invalidDate";
		}
	}

	/**
	 * Flag regex.
	 *
	 * @param inputRecord the input record
	 * @return the string
	 */
	public static String flagRegex(String inputRecord) {
		output = null;
		Pattern pattern = Pattern.compile(BOOLEAN_FORMAT);
		Matcher matcher = pattern.matcher(inputRecord);
		if (matcher.matches()) {
			return inputRecord;
		} else {
			return "invalidStatus";
		}
	}

	/**
	 * Specialization regex.
	 *
	 * @param inputRecord the input record
	 * @return the string
	 */
	public static String specializationRegex(String inputRecord) {
		output = null;
		Pattern pattern = Pattern.compile(SPECIALIZATION_FORMAT);
		Matcher matcher = pattern.matcher(inputRecord);
		if (matcher.matches()) {
			return inputRecord;
		} else {
			return INVALID_INPUT;
		}
	}
}
