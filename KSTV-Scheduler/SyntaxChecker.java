import java.util.regex.*;

/**
 * Utilities for checking strings as valid representations of data types.
 * <p> Data types and their valid representaions are described in <tt>server-interface-data-types.txt</tt>.
 */
public final class SyntaxChecker {
	private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z_]\\w*");

	/**
	 * Checks whether a string is a valid user name.
	 * <p> Checking is performed according to the UserName data type.
	 * @param	name The string to check.
	 * @return	<tt>true</tt> if <tt>name</tt> is valid, <tt>false</tt> otherwise.
	 */
	public static boolean checkName(String name) {
		return NAME_PATTERN.matcher(name).matches();
	}

	private static final Pattern ABS_TIME_ZONE_PATTERN = Pattern.compile("GMT[-+]\\d\\d?(?::?\\d\\d)?");

	/**
	 * Checks whether a string is a valid representaion of a time zone in the absolute format.
	 * <p> Checking is performed according to the AbsTimeZone data type.
	 * @param	timeZoneStr The string to check.
	 * @return	<tt>true</tt> if <tt>timeZoneStr</tt> is valid, <tt>false</tt> otherwise.
	 */
	public static boolean checkAbsTimeZone(String timeZoneStr) {
		return ABS_TIME_ZONE_PATTERN.matcher(timeZoneStr).matches();
	}

	private static final Pattern DATE_TIME_PATTERN =
		Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");

	/**
	 * Checks whether a string is a valid representaion of a time zone in the absolute format.
	 * <p> Checking is performed according to the AbsTimeZone data type.
	 * @param	timeZoneStr The string to check.
	 * @return	<tt>true</tt> if <tt>timeZoneStr</tt> is valid, <tt>false</tt> otherwise.
	 */
	public static boolean checkLocalDateTime(String localDateTimeStr) {
		return DATE_TIME_PATTERN.matcher(localDateTimeStr).matches();
	}

	/**
	 * Checks whether a string is a valid representaion of a user's status.
	 * <p> Checking is performed according to the Boolean data type.
	 * @param	activeStr The string to check.
	 * @return	<tt>true</tt> if <tt>activeStr</tt> is valid, <tt>false</tt> otherwise.
	 */
	public static boolean checkActive(String activeStr) {
		return activeStr.equals("true") || activeStr.equals("false");
	}
}
