import java.util.*;
import java.io.*;

public class Shared {
	public static final String MessagePartDelimiter = "\u001F";
	public static final String MessageTerminator = "\u001E";

	public static void sendMessage(String[] parts, PrintWriter writer) {
		if (parts != null) {
			for (int i = 0; i < parts.length; ++i) {
				writer.print(parts[i]);
				writer.print(MessagePartDelimiter);
			}
		}
		writer.print(MessageTerminator);
		writer.flush();
	}

	public static String[] getPars(BufferedReader reader) {
		String message;
		try (Scanner msgSc = new Scanner(reader)) {
			try {
				msgSc.skip(MessageTerminator);
				return new String[] {};
			} catch (NoSuchElementException e) {
				try {
					message = msgSc.useDelimiter(MessageTerminator).next();
				} catch (NoSuchElementException e2) {
					return null;
				}
			}
		}

		String[] parts = message.split(MessagePartDelimiter, -1);
		if (!parts[parts.length - 1].isEmpty()) {
			return null;
		}
		return Arrays.copyOfRange(parts, 0, parts.length - 1);
	}

}
