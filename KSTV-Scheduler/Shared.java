import java.util.*;
import java.io.*;

/**
 * Provides methods shared between Server and Client.
 */
public final class Shared {
	/**
	 * Message part delimiter.
	 * <p> U+001F INFORMATION SEPARATOR ONE, a.k.a. unit separator (US).
	 */
	private static final String msgPartDelimiter = "\u001F";

	/**
	 * Message delimiter.
	 * <p> U+001E INFORMATION SEPARATOR TWO, a.k.a. record separator (RS).
	 */
	private static final String msgDelimiter = "\u001E";

	/**
	 * Constructs and prepares a scanner to be used in {@link #getParts(Scanner)}.
	 * @param	reader A reader to read from.
	 * @return	Constructed and prepared scanner.
	 */
	@Deprecated
	public static Scanner createPartsScanner(Reader reader) {
		Scanner scanner = new Scanner(reader);
		scanner.useDelimiter(msgDelimiter);
		return scanner;
	}

	/**
	 * Reads a message from a scanner and splits it into parts.
	 * <p> Skips the scanner past the first message delimiter found,
	 * than splits the read string into message parts.
	 * <p> NB: The scanner should have been obtained via {@link #createPartsScanner(Reader)}.
	 * <p> NB: The result should be checked against {@link #checkMsgDelimiterNotFound(String[])} and
	 * {@link #checkTextPastTheLastPart(String[])} and handled appropriately.
	 * <p> NB: If either error is present the result is unspecified.
	 * @param	scanner A scanner to scan from.
	 * @return	Array of message parts, or {@link #endOfStream} or
	 * {@link #textPastTheLastPart} if appropriate.
	 */
	@Deprecated
	public static String[] getParts(Scanner scanner) {
		Objects.requireNonNull(scanner);

		String message = "";

		boolean msgDelimiterNotFound = false;
		boolean textPastTheLastPart = false;

		// scanner.next() would skip the delimiter if it was the first character
		// so we must check manually
		try {
			scanner.skip(msgDelimiter);
			return new String[] {};
		} catch (NoSuchElementException e) {
			try {
				message = scanner.next();
			} catch (NoSuchElementException e2) {
				msgDelimiterNotFound = true;
			}
		}

		String[] parts = message.split(msgPartDelimiter, -1);
		if (!parts[parts.length - 1].isEmpty()) {
			textPastTheLastPart = true;
		}

		if (msgDelimiterNotFound || textPastTheLastPart) {
			String[] result = Arrays.copyOf(parts, parts.length + 1);
			result[parts.length] =
				(msgDelimiterNotFound ? msgDelimiter : "") +
				(textPastTheLastPart ? msgPartDelimiter : "");
			return result;
		}
		return Arrays.copyOfRange(parts, 0, parts.length - 1);
	}

	/**
	 * Contains result of {@link #getParts2}.
	 */
	public static class GetPartsResult {
		/**
		 * Array of parts. Ignored part is not part of this array.
		 */
		public final String[] parts;
		/**
		 * Whether end of stream was encountered before first message delimiter.
		 */
		public final boolean endOfStream;
		/**
		 * Ignored part. Empty if there isn't one.
		 */
		public final String ignoredPart;
		/**
		 * Any exception thrown by internal IO operations. null if there isn't one.
		 */
		public final IOException ioe;

		public GetPartsResult(String[] parts, boolean endOfStream, String ignoredPart, IOException ioe) {
			this.parts = parts;
			this.endOfStream = endOfStream;
			this.ignoredPart = ignoredPart;
			this.ioe = ioe;
		}
	}

	// ServletRequest.getInputStream() -> ServletInputStream
	// ServletRequest.getReader() -> BufferedReader
	// Socket.getInputStream() -> InputStream
	// HttpURLConnection.getErrorStream() -> InputStream
	// HttpURLConnection.getInputStream() -> InputStream
	/**
	 * Reads a message from a reader and splits it into parts.
	 * <p> Skips the reader past the first message delimiter found,
	 * than splits the read string into message parts.
	 * @param	reader A reader to read from.
	 * @return	A {@link #GetPartsResult} containing parts, error info
	 * and possibly thrown exception.
	 */
	public static GetPartsResult getParts2(Reader reader) {
		StringBuilder sb = new StringBuilder();

		IOException ioe = null;
		boolean endOfStream = false;
		try {
			int n = reader.read();
			while (n != -1 && (char)n != '\u001E') {
				sb.append((char)n);
				n = reader.read();
			}
			if (n == -1) {
				endOfStream = true;
			}
		} catch (IOException e) {
			ioe = e;
		}

		String ignoredPart = null;
		String[] parts = sb.toString().split(msgPartDelimiter, -1);
		if (!parts[parts.length - 1].isEmpty()) {
			ignoredPart = parts[parts.length - 1];
		}
		parts = Arrays.copyOfRange(parts, 0, parts.length - 1);

		return new GetPartsResult(parts, endOfStream, ignoredPart, ioe);
	}

	/**
	 * Special value indicating that the message delimiter was not found.
	 * <p> Note: This reference should be unique.
	 * @see		{@link #getParts(Scanner)}
	 */
	/**
	 * Checks result of {@link #getParts(Scanner)} for "Message delimiter not found" error.
	 * @param	parts Result to check.
	 * @return	Whether the error was detected.
	 */
	@Deprecated
	public static boolean checkMsgDelimiterNotFound(String[] parts) {
		return parts.length > 0 && parts[parts.length - 1].startsWith(msgDelimiter);
	}

	/**
	 * Special value indicating that there was some text after the last message part.
	 * <p> Note: This reference should be unique.
	 * @see		{@link #getParts(Scanner)}
	 */
	/**
	 * Checks result of {@link #getParts(Scanner)} for "Text after last part" error.
	 * @param	parts Result to check.
	 * @return	Whether the error was detected.
	 */
	@Deprecated
	public static boolean checkTextPastTheLastPart(String[] parts) {
		return parts.length > 0 && parts[parts.length - 1].endsWith(msgPartDelimiter);
	}

	/**
	 * Shortcut of {@link #getParts(Scanner)} for Reader.
	 * <p> NB: This version closes the reader after using it.
	 * @param	reader A reader to read from.
	 * @return	Array of message parts, or {@link #endOfStream} or
	 * {@link #textPastTheLastPart} if appropriate.
	 */
	@Deprecated
	public static String[] getParts(Reader reader) {
		try (Scanner msgSc = createPartsScanner(reader)) {
			return getParts(msgSc);
		}
	}

	/**
	 * Writes message parts to a writer.
	 * @param	parts Message parts to write.
	 * @param	writer A writer to write to.
	 * @throws	IOException If any writing operation fails.
	 */
	public static void putParts(String[] parts, Writer writer) throws IOException {
		Objects.requireNonNull(parts);
		Objects.requireNonNull(writer);

		for (String part: parts) {
			writer.write(part);
			writer.write(msgPartDelimiter);
		}

		writer.write(msgDelimiter);
		writer.flush();
	}
}
