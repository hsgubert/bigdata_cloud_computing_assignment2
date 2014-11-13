<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h3>Hello ${user.name},</h3>

<p>To activate your Tweet Map account please click on the link below:</p>

<c:set var="req" value="${pageContext.request}" />
<a href="${fn:replace(req.requestURL, req.requestURI, '')}${req.contextPath}/activate_account?email=${user.email}&key=${user.encryptedPassword}">
	Activate account
</a>
