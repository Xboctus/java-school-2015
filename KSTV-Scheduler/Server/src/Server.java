import java.io.*;
//import java.io.PrintWriter;
import java.util.HashMap;
//import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.*;

public class Server extends HttpServlet {
	private static HashMap<String, String> getPars(HttpServletRequest req) throws IOException {
		BufferedReader reader = req.getReader();
		String r = reader.readLine(); // request should contain exactly one line

		HashMap<String, String> pars = new HashMap<>();
		String[] kvs = r.split("&");
		for (String kv: kvs) {
			int i = kv.indexOf('=');
			pars.put(kv.substring(0, i), kv.substring(i + 1));
		}

		return pars;
	}

	private static final int SC_NO_SUCH_USER = 451;

	private static class Response {
		public String body;
		public int statusCode;

		public Response(String body, int statusCode) {
			this.body = body;
			this.statusCode = statusCode;
		}

		public static final Response BAD_REQUEST_RESPONSE = new Response(null, HttpServletResponse.SC_BAD_REQUEST);
	}

	private static boolean isLetter(char c) {
		return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || c == '_';
	}

	private static boolean isDigit(char c) {
		return '0' <= c && c <= '9';
	}

	private static boolean checkLogin(String login) {
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

	private static boolean checkPort(int port) {
		return 0 <= port && port < (1 << 16);
	}

/*	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HashMap<String, String> pars = getPars(req);
		String action = pars.get("action");
		String loginFrom = pars.get("login_from");

		if (action == null || loginFrom == null) {
			res.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		ServerHandler.UserInfoResult userInfo = ServerHandler.userInfo(loginFrom);

		if (userInfo.error == ServerHandler.Error.NO_SUCH_USER) {
			res.sendError(451);
			return;
		}

		res.setContentType("text/plain");
		PrintWriter pw = res.getWriter();
		pw.close();
	}*/

/*	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpSession session = req.getSession();
		boolean sessionIsNew = session.isNew();

		HashMap<String, String> pars = getPars(req);
		String action = pars.get("action");

		if (action == null) {
			res.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} else switch (action) {
		case "start_session": {
			String login = pars.get("login");
			String password = pars.get("password");
			String listenPort = pars.get("listen_port");
			if (login == null || password == null || listenPort == null) {
				res.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			int port;
			try {
				port = Integer.parseInt(listenPort);
			} catch (NumberFormatException e) {
				res.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			ServerHandler.Error error = ServerHandler.startSession(sessionIsNew, login, password, port);
			if (error == ServerHandler.Error.UNAUTHORIZED) {
				res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			break;
		}
		default:
			res.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}*/

	private static Response serveTest(boolean sessionIsNew, HashMap<String, String> pars) throws ServletException, IOException {
		String login = pars.get("login");
		String listenPortStr = pars.get("listen_port");
		if (login == null || listenPortStr == null) {
			return Response.BAD_REQUEST_RESPONSE;
		}

		if (!checkLogin(login)) {
			return Response.BAD_REQUEST_RESPONSE;
		}

		int listenPort;
		try {
			listenPort = Integer.parseInt(listenPortStr);
		} catch (NumberFormatException e) {
			return Response.BAD_REQUEST_RESPONSE;
		}

		if (!checkPort(listenPort)) {
			return Response.BAD_REQUEST_RESPONSE;
		}

		ServerHandler.TestResult testResult = ServerHandler.test(login, listenPort);
		if (testResult.error == ServerHandler.Error.NO_SUCH_USER) {
			return new Response(null, SC_NO_SUCH_USER);
		}

		if (testResult.exists) {
			return new Response("result=yes", HttpServletResponse.SC_OK);
		} else {
			return new Response("result=no", HttpServletResponse.SC_OK);
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpSession session = req.getSession();
		boolean sessionIsNew = session.isNew();

		HashMap<String, String> pars = getPars(req);

		Response respose;

		String action = pars.get("action");
		if (action == null) {
			respose = Response.BAD_REQUEST_RESPONSE;
		} else switch (action) {
		case "test": {
			respose = serveTest(sessionIsNew, pars);
			break;
		}
/*		case "start_session":
			String login = pars.get("login");
			String password = pars.get("password");
			String listen_port = pars.get("listen_port");
			if (login == null || password == null || listen_port == null) {
				pw.print("result=lexical_error");
			} else {
				;
			}
			break;
		case "create_user":
			;
			break;
		case "change_user":
			;
			break;
		case "user_info":
			;
			break;
		case "add_event":
			;
			break;
		case "remove_event":
			;
			break;
		case "add_random_event":
			;
			break;
		case "clone_event":
			;
			break;*/
		default:
			respose = Response.BAD_REQUEST_RESPONSE;
		}

		res.setStatus(respose.statusCode);
		if (respose.body != null) {
			res.setContentType("text/plain");
			PrintWriter pw = res.getWriter();
			pw.print(respose.body);
			pw.close();
		}
	}
}
