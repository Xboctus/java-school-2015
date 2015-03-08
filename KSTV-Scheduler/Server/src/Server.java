import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class Server extends HttpServlet {
	public static ServletContext servletContext;

	public static void log_exc(String msg, Throwable exc) {
		servletContext.log(msg, exc);
	}

	@Override
	public void init() throws UnavailableException {
		servletContext = getServletContext();
		try {
			DbConnector.init();
		} catch (Exception e) {
			UnavailableException ue = new UnavailableException("DbConnector.init() failed");
			ue.initCause(e);
			throw ue;
		}
		try {
			SocketInterface.init();
		} catch (Exception e) {
			log_exc("SocketInterface.init() failed", e);
		}
		ServerHandler.startTimer();
	}

	@Override
	public void destroy() {
		ServerHandler.stopTimer();
		try {
			SocketInterface.destroy();
		} catch (Exception e) {
			log_exc("SocketInterface.destroy() failed", e);
		}
		try {
			DbConnector.destroy();
		} catch (Exception e) {
			log_exc("DbConnector.destroy() failed", e);
		}
		servletContext = null;
	}

	private static class Response {
		public static final ThreadLocal<String> debugInfo = new ThreadLocal<>();

		private int statusCode;
		private String[] parts;

		private Response(int statusCode) {
			this.statusCode = statusCode;
			this.parts = null;
		}

		public Response(String[] parts) {
			this.statusCode = HttpServletResponse.SC_OK;
			this.parts = parts;
		}

		public static final Response OK_RESPONSE = new Response(HttpServletResponse.SC_OK);
//		public static final Response BAD_REQUEST_RESPONSE = new Response(HttpServletResponse.SC_BAD_REQUEST);
		public static final Response UNAUTHORIZED_RESPONSE = new Response(HttpServletResponse.SC_UNAUTHORIZED);
		public static final Response NOT_FOUND_RESPONSE = new Response(HttpServletResponse.SC_NOT_FOUND);
		public static final Response INTERNAL_ERROR_RESPONSE = new Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		public static Response badRequest(String reason) {
			Response response = new Response(HttpServletResponse.SC_BAD_REQUEST);
			response.parts = new String[] {reason};
			return response;
		}

		public void send(HttpServletResponse res) throws IOException {
			res.setStatus(statusCode);
			res.setContentType("text/plain");

			String debug = debugInfo.get();
			if (debug != null) {
				res.addHeader("Debug", debug);
			}
			try (PrintWriter pw = res.getWriter()) {
				Shared.putParts(parts, pw);
			}
		}
	}

	private static class Info {
		String path;
		String[] reqPars = null;
		Response response = null;
		Exception exc = null;
	}

	private static final String INFO_ATTR = "scheduler.info";
	private static final String USERNAME_ATTR = "scheduler.username";

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
			Info info = new Info();

			info.path = req.getPathInfo();
			if (info.path == null) {
				response = Response.NOT_FOUND_RESPONSE;
				break gettingResponse;
			}

			try (BufferedReader reader = req.getReader()) {
				Shared.GetPartsResult r = Shared.getParts2(reader);
				info.reqPars = r.parts;
				if (r.ioe != null) {
					throw r.ioe;
				}
			} catch (IOException e) {
				if (info.reqPars == null) {
					log_exc("Opening of request reader failed", e);
					response = Response.INTERNAL_ERROR_RESPONSE;
					break gettingResponse;
				}
				log_exc("Closing of request reader failed", e);
			}

			req.setAttribute(INFO_ATTR, info);
			super.service(req, res); // polymorphically dispatches to this.doXXX()

			if (info.exc != null) {
				log_exc("Exception during request handling", info.exc);
				response = Response.INTERNAL_ERROR_RESPONSE;
			} else if (info.response == null) {
				response = Response.NOT_FOUND_RESPONSE;
			} else {
				response = info.response;
			}
		}

		try {
			response.send(res);
		} catch (IOException e) {
			log_exc("Sending response failed", e);
			if (!res.isCommitted()) {
				try {
					res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (IOException e2) {
					log_exc("Sending error failed", e2);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private static Response serveGetEventPort(HttpServletRequest req, String[] pars) {
		return new Response(new String[] {
			Integer.toString(SocketInterface.getEventPort())
		});
	}

/*	private static Response serveOwnInfo(HttpServletRequest req, String[] pars) {
		return null;
	}
*/
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		Info info = (Info)req.getAttribute(INFO_ATTR);

		try {
			if (info.path.equals("/event_port")) {
				info.response = serveGetEventPort(req, info.reqPars);
			}
		} catch (Exception e) {
			info.exc = e;
		}
	}

	private static Response serveLogin(HttpServletRequest req, String[] pars) throws Exception {
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
			return Response.UNAUTHORIZED_RESPONSE;
		}
		assert error == ServerHandler.HandlingError.NO_ERROR;
		req.getSession().setAttribute(USERNAME_ATTR, name);
		return Response.OK_RESPONSE;
	}

	@SuppressWarnings("unused")
	private static Response serveLogout(HttpServletRequest req, String[] pars) throws Exception {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return Response.OK_RESPONSE;
	}

	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) {
		Info info = (Info)req.getAttribute(INFO_ATTR);

		try {
			if (info.path.equals("/login")) {
				info.response = serveLogin(req, info.reqPars);
			} else if (info.path.equals("/logout")) {
				info.response = serveLogout(req, info.reqPars);
			}
		} catch (Exception e) {
			info.exc = e;
		}
	}
}
