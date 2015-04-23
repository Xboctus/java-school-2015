import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

/** Provides methods shared between Server and Client. */
public final class Shared {
	/**
	 * Message part delimiter.
	 * <p> U+001F INFORMATION SEPARATOR ONE, a.k.a. unit separator (US).
	 */
	public static final char msgPartDelimiter = '\u001F';

	/**
	 * Message delimiter.
	 * <p> U+001E INFORMATION SEPARATOR TWO, a.k.a. record separator (RS).
	 */
	public static final char msgDelimiter = '\u001E';

	/** Contains result of {@link Shared#getParts getParts}. */
	public static class GetPartsResult {
		/** Any exception thrown by internal IO operation. {@code null} if there isn't one. */
		public final IOException ioe;

		/** Whether end of stream was encountered. {@code false} if {@code ioe != null}. */
		public final boolean endOfStream;

		/** Read parts. Ignored part is not included in this array. */
		public final String[] parts;

		/** Ignored part. Empty if there isn't one. */
		public final String ignoredPart;

		private GetPartsResult(IOException ioe, boolean endOfStream, String[] parts, String ignoredPart) {
			this.ioe = ioe;
			this.endOfStream = endOfStream;
			this.parts = parts;
			this.ignoredPart = ignoredPart;
		}
	}

	private static final String msgPartDelimiterStr = String.valueOf(msgPartDelimiter);

	/**
	 * Reads a message from a reader and splits it into parts.
	 * <p> Skips the reader past the first message delimiter found, than splits the read string into message parts.
	 * @param	reader A reader to read from. (Preferably buffered for better performance.)
	 * @return	A {@link GetPartsResult} containing parts and information about errors.
	 */
	public static GetPartsResult getParts(Reader reader) {
		StringBuilder sb = new StringBuilder();

		IOException ioe = null;
		boolean endOfStream = false;
		try {
			int n = reader.read();
			while (n != -1 && (char)n != msgDelimiter) {
				sb.append((char)n);
				n = reader.read();
			}
			if (n == -1) {
				endOfStream = true;
			}
		} catch (IOException e) {
			ioe = e;
		}

		String[] parts = sb.toString().split(msgPartDelimiterStr, -1);
		String ignoredPart = parts[parts.length - 1];
		parts = Arrays.copyOfRange(parts, 0, parts.length - 1);

		return new GetPartsResult(ioe, endOfStream, parts, ignoredPart);
	}

	/**
	 * Writes message parts to a writer.
	 * @param	parts Message parts to write.
	 * @param	writer A writer to write to.
	 * @throws	IOException If any writing operation fails.
	 * @deprecated	Use putParts(Writer, String...) instead.
	 */
	@Deprecated
	public static void putParts(String[] parts, Writer writer) throws IOException {
		putParts(writer, parts);
	}

	/**
	 * Writes message parts to a writer.
	 * @param	writer A writer to write to.
	 * @param	parts Message parts to write.
	 * @throws	IOException If any writing operation fails.
	 */
	public static void putParts(Writer writer, String... parts) throws IOException {
		for (String part: parts) {
			writer.write(part);
			writer.write(msgPartDelimiter);
		}
		writer.write(msgDelimiter);
		writer.flush();
	}
}
