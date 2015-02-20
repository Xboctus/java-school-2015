<%@ page contentType="text/html;charset=UTF-8" language="java" import="java.util.Date" %>
<html>
<head>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css"/>
    <title>Scheduler: ${sessionScope.get("username")}</title>
</head>
<body style="margin: 0">
    <div class="header">
        <div>SScheduler: <% out.print(new Date(System.currentTimeMillis())); %></div>
    </div>
    <%
        if (session.getAttribute("username")==null) {
            response.sendRedirect("login.jsp");
        }
    %>
    <div class="addUser">
        <form action="${pageContext.request.contextPath}/" method="post">
            <p>Name   <input name="useradd" type="text" class="field1"/></p>
            <p>Password   <input name="pass" type="text" class="field1"/></p>
            <p>Active   <input name="act" type="text" class="field1"/></p>
            <p>Time Zone   <input name="time" type="text" class="field1"/></p>
            <p><input type="submit" value="Add User"></p>
        </form>
    </div>
    <div class="footer">
        (c) 2015 Java school at Return on Intelligence
    </div>
</body>
</html>