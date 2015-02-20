<%@ page contentType="text/html;charset=UTF-8" language="java"
        import="java.util.*"
        import="sschedulerweb.ejb.*" %>
<%@ page import="javax.naming.Context" %>
<%@ page import="javax.naming.InitialContext" %>
<%@ page import="java.text.SimpleDateFormat" %>
<html>
    <head>
        <script src="${pageContext.request.contextPath}/scripts/jquery-1.11.2.js"></script>
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/css/style.css"/>
        <title>Scheduler: ${sessionScope.get("username")}</title>
    </head>
    <body style="margin: 0">
        <div class="header">
            <div>SScheduler: <% out.print(new Date(System.currentTimeMillis())); %></div>
        </div>
        <% if (session.getAttribute("username")!=null) {
            Context context = new InitialContext();
            SchedulerBeanRemote bean = (SchedulerBeanRemote)context.lookup("java:comp/env/ejb/SchedulerBean");
            SchUser u = bean.getUser((String)session.getAttribute("username"));
        %>
        <div class="main">
            <div class="menu">
                <p>Name   <% out.print(u.getName()); %></p>
                <p>Active   <% out.print(u.isactive()); %></p>
                <p>Time Zone   <% out.print(u.getTimeZone()); %></p>
                <a href="addUser.jsp"><p>  <input type="button" value="Add User"></p></a>
                <a href="modifyUser.jsp"> <p>  <input type="button" value="Modify User"></p></a>
                <a href="addEvent.jsp"> <p>  <input type="button" value="Add event"></p></a>
                <a href="copyEvent.jsp"> <p>  <input type="button" value="Copy event"></p></a>
                <a href="removeEvent.jsp"> <p>  <input type="button" value="Remove event"></p></a>
            </div>
            <div id="eventTable">
                <p>
                    <div style="float: left">ID</div>
                    <div style="float: left; padding-left: 100px">Date</div>
                    <div style="padding-left: 300px">Text</div>
                </p>
                <form action="${pageContext.request.contextPath}/" method="post">
                    <input type="text" name="_null" style="display: none">
                    <%
                        String elem1 = "<input type=\"text\" name=\"date",
                                elem2 = "<input type=\"text\" name=\"info";
                        List<SchEvent> events = bean.getUserEvents(u.getName());
                        if (events!=null)
                        for (int i = 0; i < events.size(); i++) {
                            SchEvent e = events.get(i);
                            out.print("<p>" + e.getId() + "    " +
                                    elem1 + e.getId() + "\" value=\"" + e.getTime() + "\"/>     " +
                                    elem2 + e.getId() + "\" value=\"" + e.getInfo() + "\"/></p>");
                        }
                    %>
                    <input type="submit" value="Save events" />
                </form>
            </div>
            <textarea id="logArea" rows="10" style="width: 100%"><%
                //ArrayList<String> msgs = bean.getMessages();
                //for (String msg : msgs)
                //    out.println(msg);
            %></textarea>
        </div>
        <% }
            else {
            response.sendRedirect("login.jsp");
        }
        %>
        <div class="footer">
            (c) 2015 Java school at Return on Intelligence
        </div>
    </body>
    <script>
        function refresh() {
            $('#logArea').load('events.log');
            setTimeout(refresh,5000);
        }
        refresh();
    </script>
</html>