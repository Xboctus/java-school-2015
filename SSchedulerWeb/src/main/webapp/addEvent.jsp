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
        <p>User   <input name="owner" type="text" class="field1"/></p>
        <p>Date   <input name="date" type="text" class="field1"/></p>
        <p>Text   <input name="info" type="text" class="field1"/></p>
        <p><input type="submit" value="Add event"></p>
    </form>
</div>
<div class="footer">
    (c) 2015 Java school at Return on Intelligence
</div>
</body>
</html>