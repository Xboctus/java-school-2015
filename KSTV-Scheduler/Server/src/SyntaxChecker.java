public final class SyntaxChecker {
	public static boolean isLetter(char c) {
		return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || c == '_';
	}

	public static boolean isDigit(char c) {
		return '0' <= c && c <= '9';
	}

	public static boolean checkLogin(String login) {
		if (login.isEmpty()) {
			return false;
		}
		if (!isLetter(login.charAt(0))) {
			return false;
		}
		for (int i = 1; i < login.length(); ++i) {
			if (!(isLetter(login.charAt(i)) || isDigit(login.charAt(i)))) {
				return false;
			}
		}
		return true;
	}
}
