<%@ page contentType="text/html;charset=UTF-8" language="java" import="java.util.Date" %>
<html>
<head>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css"/>
    <title>SScheduler Login</title>
</head>
<body style="margin: 0">
    <div class="header">
        <div>SScheduler: <% out.print(new Date(System.currentTimeMillis())); %></div>
    </div>
    <div class="login">
        <form action="${pageContext.request.contextPath}/" method="post">
            <p>Name   <input name="user" type="text" class="field1"/></p>
            <p>Password   <input name="pass" type="text" class="field1"/></p>
            <p><input type="submit" value="Login"></p>
        </form>
    </div>
    <div class="footer">
        (c) 2015 Java school at Return on Intelligence
    </div>
</body>
</html>
