import java.io.*;
import java.util.HashMap;
import java.util.TimeZone;

import javax.servlet.*;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class Server extends HttpServlet {
	@Override
	public void init() throws UnavailableException {
		try {
			ServerHandler.establishDbConnection(getServletContext());
			try {
				SocketInterface.openServerSocket();
			} catch (Exception e) {
				ServerHandler.closeDbConnection();
				throw e;
			}
		} catch (Exception cause) {
			UnavailableException ue = new UnavailableException("Initialization error");
			ue.initCause(cause);
			throw ue;
		}
		ServerHandler.startTimer();
	}

	@Override
	public void destroy() {
		ServerHandler.stopTimer();
		try {
			SocketInterface.closeServerSocket();
		} catch (Exception e) {
			getServletContext().log("Exception during destroy()", e);
		}
		try {
			ServerHandler.closeDbConnection();
		} catch (Exception e) {
			getServletContext().log("Exception during destroy()", e);
		}
	}

	private static HashMap<String, String> getPars(HttpServletRequest req) {
		String parsStr;
		try (BufferedReader reader = req.getReader()) {
			parsStr = reader.readLine(); // request should contain exactly one line
		} catch (IOException e) {
			return null;
		}

		if (parsStr == null) {
			return null;
		}

		HashMap<String, String> pars = new HashMap<>();
		for (String parStr: parsStr.split("&")) {
			int i = parStr.indexOf('=');
			pars.put(parStr.substring(0, i), parStr.substring(i + 1));
		}

		return pars;
	}

	private static final int SC_USER_EXISTS = 450;
	private static final int SC_NO_SUCH_USER = 451;

	private static class Response {
		public String body;
		public int statusCode;

		public Response(String body, int statusCode) {
			this.body = body;
			this.statusCode = statusCode;
		}

		public static final Response BAD_REQUEST_RESPONSE = new Response(null, HttpServletResponse.SC_BAD_REQUEST);
		public static final Response INTERNAL_ERROR_RESPONSE = new Response(null, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	private static void sendResponse(HttpServletResponse res, Response response) {
		res.setStatus(response.statusCode);
		if (response.body != null) {
			res.setContentType("text/plain");
			try (PrintWriter pw = res.getWriter()) {
				pw.print(response.body);
				pw.flush();
			} catch (IOException e) {
				res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}

	private static Response serveTest(HashMap<String, String> pars, String sessionId) {
		String login = pars.get("login");
		if (login == null || !SyntaxChecker.checkLogin(login)) {
			return Response.BAD_REQUEST_RESPONSE;
		}

		ServerHandler.TestResult testResult = ServerHandler.test(login, sessionId);
		if (testResult.error == ServerHandler.HandlingError.INTERNAL_ERROR) {
			return Response.INTERNAL_ERROR_RESPONSE;
		}

		String resultKv = (testResult.exists ? "yes" : "no");
		String listerPortKv = Integer.toString(testResult.serverPort);
		String body = resultKv + " " + listerPortKv;

		if (testResult.error == ServerHandler.HandlingError.NO_SUCH_USER) {
			return new Response(body, SC_NO_SUCH_USER);
		}
		return new Response(body, HttpServletResponse.SC_OK);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) {
		Response response;

		HashMap<String, String> pars = getPars(req);

		if (pars == null) {
			response = Response.BAD_REQUEST_RESPONSE;
		} else {
			String action = pars.get("action");
			if (action == null) {
				response = Response.BAD_REQUEST_RESPONSE;
			} else switch (action) {
			case "test":
				response = serveTest(pars, req.getSession().getId());
				break;
			default:
				response = Response.BAD_REQUEST_RESPONSE;
			}
		}

		sendResponse(res, response);
	}

	private static Response serveCreateUser(HashMap<String, String> pars, String sessionId) {
		String login = pars.get("login");
		String password = pars.get("password");
		String timeZoneStr = pars.get("timezone");
		String activeStr = pars.get("active");
		if (login == null || !SyntaxChecker.checkLogin(login)) {
			return Response.BAD_REQUEST_RESPONSE;
		}
		if (password == null) {
			return Response.BAD_REQUEST_RESPONSE;
		}
		if (timeZoneStr == null || !SyntaxChecker.checkTimeZone(timeZoneStr)) {
			return Response.BAD_REQUEST_RESPONSE;
		}
		if (activeStr != null && !SyntaxChecker.checkActive(activeStr)) {
			return Response.BAD_REQUEST_RESPONSE;
		}

		TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
		boolean active = (activeStr == null || Boolean.parseBoolean(activeStr));

		ServerHandler.CreateUserResult result = ServerHandler.createUser(login, password, timeZone, active);
		if (result.error == ServerHandler.HandlingError.INTERNAL_ERROR) {
			return Response.INTERNAL_ERROR_RESPONSE;
		}

		String listerPort = Integer.toString(result.serverPort);
		String body = listerPort;

		if (result.error == ServerHandler.HandlingError.USER_EXISTS) {
			return new Response(body, SC_USER_EXISTS);
		}
		return new Response(body, HttpServletResponse.SC_OK);
	}

	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) {
		Response response;

		HashMap<String, String> pars = getPars(req);

		if (pars == null) {
			response = Response.BAD_REQUEST_RESPONSE;
		} else {
			String action = pars.get("action");
			if (action == null) {
				response = Response.BAD_REQUEST_RESPONSE;
			} else switch (action) {
			case "create_user":
				response = serveCreateUser(pars, req.getSession().getId());
				break;
			default:
				response = Response.BAD_REQUEST_RESPONSE;
			}
		}

		sendResponse(res, response);
	}
}
