import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class Server extends HttpServlet {
	public static ServletContext servletContext;
	private static final ThreadLocal<String> debugInfo = new ThreadLocal<>();

	@Override
	public void init() throws UnavailableException {
		servletContext = getServletContext();
		try {
			DbConnector.init();
		} catch (Exception e) {
			// TODO: server can function without DB
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
		} catch (Exception e) {
			servletContext.log("DbConnector.destroy() failed", e);
		}
	}

	private static class Response {
		public int statusCode;
		public String[] parts;

		public Response(int statusCode) {
			this.statusCode = statusCode;
			this.parts = null;
		}

		public Response(String[] parts) {
			this.statusCode = HttpServletResponse.SC_OK;
			this.parts = parts;
		}

		public static final Response UNAUTHORIZED_RESPONSE = new Response(HttpServletResponse.SC_UNAUTHORIZED);
		public static final Response OK_RESPONSE = new Response(HttpServletResponse.SC_OK);
		public static final Response BAD_REQUEST_RESPONSE = new Response(HttpServletResponse.SC_BAD_REQUEST);
		public static final Response INTERNAL_ERROR_RESPONSE = new Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		public static final Response NOT_FOUND_RESPONSE = new Response(HttpServletResponse.SC_NOT_FOUND);

		public void send(HttpServletResponse res) {
			res.setStatus(statusCode);
			res.setContentType("text/plain");

			String debug = debugInfo.get();
			if (debug != null) {
				res.addHeader("Debug", debug);
			}
			try (PrintWriter pw = res.getWriter()) {
				Shared.sendMessage(parts, pw);
			} catch (IOException e) {
				try {
					res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (IOException e2) {
					servletContext.log("", e2);
				}
			}
		}
	}

	private static class Info {
		String path;
		String[] reqPars;
		Response response = null;
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		switch (req.getMethod()) {
		case "GET":
		case "POST":
		case "PUT":
		case "DELETE":
			debugInfo.set(null);

			Info info = new Info();
			info.path = req.getPathInfo();
			if (info.path == null) {
				Response.NOT_FOUND_RESPONSE.send(res);
				return;
			}
			try {
				info.reqPars = Shared.getPars(req.getReader());
			} catch (IOException e) {
				servletContext.log("Extracrtion of request parameters failed", e);
				Response.INTERNAL_ERROR_RESPONSE.send(res);
				return;
			}
			if (info.reqPars == null) {
				Response resp = Response.BAD_REQUEST_RESPONSE;
				resp.send(res);
				return;
			}
			req.setAttribute("scheduler.info", info);

			super.service(req, res); // polymorphically dispatches to this.doXXX()

			if (info.response == null) {
				Response.NOT_FOUND_RESPONSE.send(res);
			} else {
				info.response.send(res);
			}

			break;
		default:
			super.service(req, res);
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		Info info = (Info)req.getAttribute("scheduler.info");

		if (info.path.equals("/event_port")) {
			info.response = new Response(new String[] {
				Integer.toString(SocketInterface.getEventPort()),
				Long.toString(System.currentTimeMillis())
			});
//			info.response = new Response(info.reqPars);
		}
	}
/*
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
			default:
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
*/
	private static Response serveLogin(HttpServletRequest req, String[] pars) {
		String name = pars[0];
		if (!SyntaxChecker.checkName(name)) {
			return Response.BAD_REQUEST_RESPONSE;
		}
		String password = pars[1];

		ServerHandler.HandlingError error = ServerHandler.login(name, password);
		switch (error) {
		case INTERNAL_ERROR:
			return Response.INTERNAL_ERROR_RESPONSE;
		case UNAUTHORIZED:
			return Response.UNAUTHORIZED_RESPONSE;
		default:
			assert error == ServerHandler.HandlingError.NO_ERROR;
			req.getSession().setAttribute("scheduler.name", name);
			return Response.OK_RESPONSE;
		}
	}

	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) {
		Info info = (Info)req.getAttribute("scheduler.info");

		if (info.path.equals("/login")) {
			info.response = serveLogin(req, info.reqPars);
		}
/*
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
*/
	}
}
