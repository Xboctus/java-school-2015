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
	public static Scanner createPartsScanner(Reader reader) {
		Scanner scanner = new Scanner(reader);
		scanner.useDelimiter(msgDelimiter);
		return scanner;
	}

	/**
	 * Special value indicating that the message delimiter was not found.
	 * <p> Note: This reference should be unique.
	 * @see		{@link #getParts(Scanner)}
	 */
	public static final String[] msgDelimiterNotFound = new String[] {msgDelimiter};

	/**
	 * Special value indicating that there was some text after the last message part.
	 * <p> Note: This reference should be unique.
	 * @see		{@link #getParts(Scanner)}
	 */
	public static final String[] textPastTheLastPart = new String[] {msgPartDelimiter};

	/**
	 * Reads a message from a scanner and splits it into parts.
	 * <p> Skips the scanner past the first message delimiter found,
	 * than splits the read string into message parts.
	 * <p> NB: The scanner should have been obtained via {@link #createPartsScanner(Reader)}
	 * @param	scanner A scanner to scan from.
	 * @return	Array of message parts, or {@link #msgDelimiterNotFound} or
	 * {@link #textPastTheLastPart} if appropriate.
	 */
	public static String[] getParts(Scanner scanner) {
		Objects.requireNonNull(scanner);

		String message;

		// scanner.next() would skip the delimiter if it was the first character
		// so we must check manually
		try {
			scanner.skip(msgDelimiter);
			return new String[] {};
		} catch (NoSuchElementException e) {
			try {
				message = scanner.next();
			} catch (NoSuchElementException e2) {
				return msgDelimiterNotFound;
			}
		}

		String[] parts = message.split(msgPartDelimiter, -1);
		if (!parts[parts.length - 1].isEmpty()) {
			return textPastTheLastPart;
		}
		return Arrays.copyOfRange(parts, 0, parts.length - 1);
	}

	/**
	 * Shortcut of {@link #getParts(Scanner)} for Reader.
	 * <p> NB: This version closes the reader after using it.
	 * @param	reader A reader to read from.
	 * @return	Array of message parts, or {@link #msgDelimiterNotFound} or
	 * {@link #textPastTheLastPart} if appropriate.
	 */
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
		Objects.requireNonNull(writer);

		for (String part: parts) {
			writer.write(part);
			writer.write(msgPartDelimiter);
		}

		writer.write(msgDelimiter);
		writer.flush();
	}
}
