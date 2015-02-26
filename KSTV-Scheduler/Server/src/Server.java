import java.util.*;
import java.io.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class Server extends HttpServlet {
	private ServletContext servletContext;

	@Override
	public void init() throws UnavailableException {
		servletContext = getServletContext();
		try {
			DbConnector.init(servletContext);
		} catch (Exception e) {
			UnavailableException ue = new UnavailableException("DbConnector.init() failed");
			ue.initCause(e);
			throw ue;
		}
		try {
			SocketInterface.init();
		} catch (Exception e) {
			servletContext.log("SocketInterface.init() failed", e);
		}
		ServerHandler.startTimer();
	}

	@Override
	public void destroy() {
		ServerHandler.stopTimer();
		try {
			SocketInterface.destroy();
		} catch (Exception e) {
			servletContext.log("SocketInterface.destroy() failed", e);
		}
		try {
			DbConnector.destroy();
		} catch (SQLException e) {
			servletContext.log("DbConnector.destroy() failed", e);
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
//	private static final int SC_NO_SUCH_USER = 451;

	private static final char MessagePartsSeparator = '\u001F';
	private static final char MessageTerminator = '\u001E';

	public static void printMessage(String[] parts, PrintWriter writer) {
		for (int i = 0; i < parts.length - 1; ++i) {
			writer.print(parts[i]);
			writer.print(MessagePartsSeparator);
		}
		writer.print(parts[parts.length - 1]);
		writer.print(MessageTerminator);
		writer.flush();
	}

	private static class Response {
		public int statusCode;
		public String[] parts;
		public String debug;

		public Response(int statusCode, String[] parts) {
			this.statusCode = statusCode;
			this.parts = parts;
		}

		public Response(int statusCode) {
			this.statusCode = statusCode;
			this.parts = null;
		}

		public Response(String[] parts) {
			this.statusCode = HttpServletResponse.SC_OK;
			this.parts = parts;
		}

		public static final Response BAD_REQUEST_RESPONSE = new Response(HttpServletResponse.SC_BAD_REQUEST);
		public static final Response INTERNAL_ERROR_RESPONSE = new Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		public static final Response NOT_FOUND_RESPONSE = new Response(HttpServletResponse.SC_NOT_FOUND);

		public void send(HttpServletResponse res) {
			res.setStatus(statusCode);
			res.setContentType("text/plain");
			if (debug != null) {
				res.addHeader("Debug", debug);
			}
			if (parts != null) {
				try (PrintWriter pw = res.getWriter()) {
					printMessage(parts, pw);
				} catch (IOException e) {
					res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		Response response;

		String path = req.getPathInfo();

		if (path == null) {
			response = Response.INTERNAL_ERROR_RESPONSE;
			response.debug = "path is null";
		} else {
			if (path.equals("/event_port")) {
				response = new Response(new String[] {Integer.toString(SocketInterface.getEventPort())});
				response.debug = "path is " + path;
			} else {
				response = Response.NOT_FOUND_RESPONSE;
				response.debug = "path is " + path;
			}
		}

		response.send(res);
	}
/*
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
*/
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
/*			case "test":
				response = serveTest(pars, req.getSession().getId());
				break;
*/			default:
				response = Response.BAD_REQUEST_RESPONSE;
			}
		}

		response.send(res);
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

		String listenPort = Integer.toString(result.serverPort);
		String[] body = new String[] {listenPort};

		if (result.error == ServerHandler.HandlingError.USER_EXISTS) {
			return new Response(SC_USER_EXISTS, body);
		}
		return new Response(HttpServletResponse.SC_OK, body);
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

		response.send(res);
	}
}
