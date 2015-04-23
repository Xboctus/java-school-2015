import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
public class Server extends HttpServlet {
	private static ServletContext servletContext;

	public static String getRealPath(String path) {
		return servletContext.getRealPath(path);
	}

	private static void logExc(String msg, Throwable exc) {
		servletContext.log(msg, exc);
	}

	@Override
	public void init() throws UnavailableException {
		servletContext = getServletContext();

		EventTimer.startTimer();

		try {
			DbConnector.init();
		} catch (Exception e) {
			UnavailableException ue = new UnavailableException("DbConnector.init() failed");
			ue.initCause(e);
			throw ue;
		}

		try {
			ConnectionDispatcher.init();
		} catch (Exception e) {
			logExc("ConnectionDispatcher.init() failed", e);
		}
	}

	@Override
	public void destroy() {
		try {
			ConnectionDispatcher.fin();
		} catch (InterruptedException e) {
			logExc("ConnectionDispatcher.fin() was interrupted", e);
		}
		for (Exception e: ConnectionDispatcher.finExceptions) {
			logExc("Exception during ConnectionDispatcher.fin()", e);
		}

		try {
			DbConnector.fin();
		} catch (Exception e) {
			logExc("DbConnector.fin()", e);
		}

		EventTimer.stopTimer();

		servletContext = null;
	}

	private static class Response {
		public static final ThreadLocal<String> debugInfo = new ThreadLocal<>();

		private final int statusCode;
		private final String[] parts;

		private Response(int statusCode, String... parts) {
			this.statusCode = statusCode;
			this.parts = parts;
		}

		private Response(int statusCode) {
			this(statusCode, new String[] {});
		}

		public Response(String... parts) {
			this(HttpServletResponse.SC_OK, parts);
		}

		public static Response badRequest(String reason) {
			return new Response(HttpServletResponse.SC_BAD_REQUEST, new String[] {reason});
		}

		public static Response unauthorized(String reason) {
			return new Response(HttpServletResponse.SC_UNAUTHORIZED, new String[] {reason});
		}

		public static final Response OK_RESPONSE = new Response(HttpServletResponse.SC_OK);
		public static final Response NOT_FOUND_RESPONSE = new Response(HttpServletResponse.SC_NOT_FOUND);
		public static final Response METHOD_NOT_ALLOWED_RESPONSE = new Response(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		public static final Response INTERNAL_ERROR_RESPONSE = new Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		public void send(HttpServletResponse res) throws IOException {
			res.setStatus(statusCode);
			res.setContentType("text/plain");

			String debug = debugInfo.get();
			if (debug != null) {
				res.addHeader("X-Debug", debug);
			}
			try (PrintWriter pw = res.getWriter()) {
				Shared.putParts(pw, parts);
			}
		}
	}

	private static class ReqResInfo {
		String path;
		String[] reqPars = null;
		Exception exc = null;
		Response response = null;
	}

	private static final String INFO_ATTR = "scheduler.info";
	private static final String USERNAME_ATTR = "scheduler.username";

	private static final String[] validUrls = new String[] {
		"/event_port",
		"/login",
		"/logout",
		"/users",
		"/users/:me",
		"/users/:me/events",
		"/users/:me/events/[0-9]+",
	};

	private static final Pattern[] validUrlPatterns;

	static {
		validUrlPatterns = new Pattern[validUrls.length];
		for (int i = 0; i < validUrls.length; ++i) {
			validUrlPatterns[i] = Pattern.compile(validUrls[i]);
		}
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		switch (req.getMethod()) {
		case "GET":
		case "POST":
		case "PUT":
		case "DELETE":
			break;
		default:
			super.service(req, res);
			return;
		}

		Response.debugInfo.set(null);

		Response response;

		gettingResponse: {
			ReqResInfo info = new ReqResInfo();

			info.path = req.getPathInfo();
			if (info.path == null) {
				response = Response.NOT_FOUND_RESPONSE;
				break gettingResponse;
			}

			boolean urlValid = false;
			for (Pattern p: validUrlPatterns) {
				if (p.matcher(info.path).matches()) {
					urlValid = true;
					break;
				}
			}
			if (!urlValid) {
				response = Response.NOT_FOUND_RESPONSE;
				break gettingResponse;
			}

			try (BufferedReader reader = req.getReader()) {
				Shared.GetPartsResult r = Shared.getParts(reader);
				info.reqPars = r.parts;
				if (r.ioe != null) {
					logExc("Exception during reading request parts", r.ioe);
				}
			} catch (IOException e) {
				if (info.reqPars == null) {
					logExc("Opening of request reader failed", e);
					response = Response.INTERNAL_ERROR_RESPONSE;
					break gettingResponse;
				}
				logExc("Closing of request reader failed", e);
			}

			req.setAttribute(INFO_ATTR, info);
			super.service(req, res); // polymorphically dispatches to this.doXXX()

			if (info.exc != null) {
				logExc("Exception during request handling", info.exc);
				response = Response.INTERNAL_ERROR_RESPONSE;
			} else if (info.response == null) {
				response = Response.METHOD_NOT_ALLOWED_RESPONSE;
			} else {
				response = info.response;
			}
		}

		try {
			response.send(res);
		} catch (IOException e) {
			logExc("Sending response failed", e);
			if (!res.isCommitted()) {
				try {
					res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (IOException e2) {
					logExc("Sending error failed", e2);
				}
			}
		}
	}

	private static Response serveGetEventPort() {
		return new Response(Integer.toString(ConnectionDispatcher.eventPort));
	}

	private static Response serveGetOwnInfo(HttpServletRequest req) throws Exception {
		String name = (String)req.getSession().getAttribute(USERNAME_ATTR);
		if (name == null) {
			return Response.unauthorized("No session");
		}
		ServerHandler.OwnInfo ownInfo = ServerHandler.getOwnInfo(name);
		if (ownInfo.error == ServerHandler.HandlingError.NO_SUCH_USER) {
			req.getSession().invalidate();
			throw new Exception("No such user");
		}
		assert ownInfo.error == ServerHandler.HandlingError.NO_ERROR;
		return new Response(
			ownInfo.timeZone.getID(),
			Boolean.toString(ownInfo.active),
			Integer.toString(ownInfo.eventCount)
		);
	}

	private static Response serveGetOwnEvents(HttpServletRequest req) throws Exception {
		String name = (String)req.getSession().getAttribute(USERNAME_ATTR);
		if (name == null) {
			return Response.unauthorized("No session");
		}
		ServerHandler.OwnEvents ownEvents = ServerHandler.getOwnEvents(name);
		if (ownEvents.error == ServerHandler.HandlingError.NO_SUCH_USER) {
			req.getSession().invalidate();
			throw new Exception("No such user");
		}
		assert ownEvents.error == ServerHandler.HandlingError.NO_ERROR;
		String[] parts = new String[1 + 2*ownEvents.events.length];
		parts[0] = Integer.toString(ownEvents.events.length);
		for (int i = 0; i < ownEvents.events.length; ++i) {
			parts[1 + 2*i] = ownEvents.events[i].dateTime.toString();
			parts[1 + 2*i + 1] = ownEvents.events[i].text;
		}
		return new Response(parts);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		ReqResInfo info = (ReqResInfo)req.getAttribute(INFO_ATTR);

		try {
			switch (info.path) {
			case "/event_port":
				info.response = serveGetEventPort();
				break;
			case "/users/:me":
				info.response = serveGetOwnInfo(req);
				break;
			case "/users/:me/events":
				info.response = serveGetOwnEvents(req);
				break;
			}
		} catch (Exception e) {
			info.exc = e;
		}
	}

	private static Response servePutLogin(HttpServletRequest req, String[] pars) throws Exception {
		if (pars.length < 2) {
			return Response.badRequest("Not enough parameters");
		}

		String name = pars[0];
		if (!SyntaxChecker.checkName(name)) {
			return Response.badRequest("Name syntax invalid");
		}
		String password = pars[1];

		ServerHandler.HandlingError error = ServerHandler.login(name, password);
		if (error == ServerHandler.HandlingError.UNAUTHORIZED) {
			return Response.unauthorized("Invalid name-password pair");
		}
		assert error == ServerHandler.HandlingError.NO_ERROR;
		req.getSession().setAttribute(USERNAME_ATTR, name);
		return Response.OK_RESPONSE;
	}

	private static Response servePutLogout(HttpServletRequest req) throws Exception {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return Response.OK_RESPONSE;
	}

	public static Response servePutOwnInfo(HttpServletRequest req, String[] pars) throws Exception {
		String name = (String)req.getSession().getAttribute(USERNAME_ATTR);
		if (name == null) {
			return Response.unauthorized("No session");
		}

		String oldPassword = null;
		String newPassword = null;
		String newTimeZoneStr = null;
		String newActiveStr = null;

		for (String kv: pars) {
			int ep = kv.indexOf('=');
			if (ep == -1) {
				return Response.badRequest("Parameters should have 'key=value' form");
			}
			String key = kv.substring(0, ep);
			String value = kv.substring(ep + 1);
			switch (key) {
			case "old_password":
				oldPassword = value;
				break;
			case "new_password":
				newPassword = value;
				break;
			case "new_timezone":
				newTimeZoneStr = value;
				break;
			case "new_active":
				newActiveStr = value;
				break;
			default:
				return Response.badRequest("Unknown key '" + key + "'");
			}
		}

		if (newPassword != null && oldPassword == null) {
			return Response.badRequest("If new_password is present old_password should also be");
		}
		if (newTimeZoneStr != null && !SyntaxChecker.checkAbsTimeZone(newTimeZoneStr)) {
			return Response.badRequest("new_timezone has invalid format");
		}
		if (newActiveStr != null && !SyntaxChecker.checkActive(newActiveStr)) {
			return Response.badRequest("new_active has invalid format");
		}

		// FIXME: time zone out of range (GMT-12..GMT+14) or invalid format (GMT+00:99)
		TimeZone newTimeZone = newTimeZoneStr != null ? TimeZone.getTimeZone(newTimeZoneStr) : null;
		Boolean newActive = newActiveStr != null ? Boolean.parseBoolean(newActiveStr) : null;

		ServerHandler.HandlingError result =
			ServerHandler.changeOwnInfo(name, oldPassword, newPassword, newTimeZone, newActive);

		if (result == ServerHandler.HandlingError.UNAUTHORIZED) {
			return Response.unauthorized("Wrong password");
		}
		if (result == ServerHandler.HandlingError.NO_SUCH_USER) {
			req.getSession().invalidate();
			throw new Exception("No such user");
		}
		assert result == ServerHandler.HandlingError.NO_ERROR;
		return Response.OK_RESPONSE;
	}

	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) {
		ReqResInfo info = (ReqResInfo)req.getAttribute(INFO_ATTR);

		try {
			switch (info.path) {
			case "/login":
				info.response = servePutLogin(req, info.reqPars);
				break;
			case "/logout":
				info.response = servePutLogout(req);
				break;
			case "/users/:me":
				info.response = servePutOwnInfo(req, info.reqPars);
				break;
			}
		} catch (Exception e) {
			info.exc = e;
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) {
		ReqResInfo info = (ReqResInfo)req.getAttribute(INFO_ATTR);

		try {
			switch (info.path) {
			}
		} catch (Exception e) {
			info.exc = e;
		}
	}

	@Override
	public void doDelete(HttpServletRequest req, HttpServletResponse res) {
		ReqResInfo info = (ReqResInfo)req.getAttribute(INFO_ATTR);

		try {
			switch (info.path) {
			}
		} catch (Exception e) {
			info.exc = e;
		}
	}
}
