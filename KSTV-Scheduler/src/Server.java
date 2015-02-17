import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Server extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setContentType("text/html");

		PrintWriter pw = res.getWriter();
		pw.println("<html>");
		pw.println(" <body>");
		pw.println("  Hello, world!");
		pw.println(" </body>");
		pw.println("</html>");

		pw.close();
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		BufferedReader reader = req.getReader();

		String r = reader.readLine(); // request should contain exactly one line

		HashMap<String, String> pars = new HashMap<String, String>();
		String[] kvs = r.split("&");
		for (String kv: kvs) {
			int i = kv.indexOf('=');
			pars.put(kv.substring(0, i), kv.substring(i + 1));
		}

		res.setContentType("text/plain");

		PrintWriter pw = res.getWriter();
		pw.close();
	}
}
