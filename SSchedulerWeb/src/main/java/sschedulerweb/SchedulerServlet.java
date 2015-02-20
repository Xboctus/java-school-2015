package sschedulerweb;

import sschedulerweb.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Enumeration;

public class SchedulerServlet extends HttpServlet {

    public void init() throws ServletException {
        super.init();
        try {
            Context context = new InitialContext();
            SchedulerBeanRemote bean = (SchedulerBeanRemote) context.lookup("java:comp/env/ejb/SchedulerBean");
            bean.init();
        }
        catch (Exception e) {}
    }

    protected void doGet( HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        String username = (String) request.getSession().getAttribute("username"),
                addr = (username == null) ? "/login.jsp" : "/main.jsp";
        response.sendRedirect(getServletContext().getContextPath() + addr);
    }

    protected void doPost( HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Context context = new InitialContext();
            SchedulerBeanRemote bean = (SchedulerBeanRemote)context.lookup("java:comp/env/ejb/SchedulerBean");
            Enumeration<String> params = request.getParameterNames();
            String firstP = params.nextElement();
            switch (firstP) {
                case "user":
                    String username = request.getParameterValues(firstP)[0],
                            pass = request.getParameterValues("pass")[0];
                    SchUser u = bean.getUser(username);
                    if (u!=null && u.getPassword().equals(pass))
                        request.getSession().setAttribute("username", username);
                    break;
                case "useradd":
                    username = request.getParameterValues(firstP)[0];
                    pass = request.getParameterValues("pass")[0];
                    String active = request.getParameterValues("act")[0],
                            tz = request.getParameterValues("time")[0];
                    bean.addUser(username,pass,Boolean.parseBoolean(active),tz);
                    break;
                case "usermod":
                    username = request.getParameterValues(firstP)[0];
                    pass = request.getParameterValues("pass")[0];
                    active = request.getParameterValues("act")[0];
                    tz = request.getParameterValues("time")[0];
                    bean.modifyUser(username,pass,Boolean.parseBoolean(active),tz);
                    break;
                case "srcuser":
                    username = request.getParameterValues(firstP)[0];
                    String destuser = request.getParameterValues("destuser")[0],
                        text = request.getParameterValues("event")[0];
                    bean.copyEvent(username,destuser,text);
                    break;
                case "owner":
                    username = request.getParameterValues(firstP)[0];
                    String date = request.getParameterValues("date")[0],
                            info = request.getParameterValues("info")[0];
                    bean.addEvent(username,date,info);
                    break;
                case "eventrm":
                    info = request.getParameterValues(firstP)[0];
                    username = request.getParameterValues("owner")[0];
                    bean.removeEvent(username,info);
                    break;
                default:
                    username = (String)request.getSession().getAttribute("username");
                    while (params.hasMoreElements()) {
                        String datelabel = params.nextElement();
                        String newDate = request.getParameterValues(datelabel)[0],
                                newInfo = request.getParameterValues(params.nextElement())[0];
                        int id = Integer.parseInt(datelabel.substring(4));
                        bean.modifyEvent(id,newDate,newInfo);
                    }
            }
            response.sendRedirect(getServletContext().getContextPath());
        }
        catch (Exception e) {
            response.getWriter().print("<html><body>"+e+" "+e.getMessage()+"</body></html>");
        }
    }
}
