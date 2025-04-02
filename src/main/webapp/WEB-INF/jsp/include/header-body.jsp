<%@page import="com.mattvorst.shared.util.Environment"%>
<%@page import="com.mattvorst.shared.constant.EnvironmentConstants"%>
<%@ page import="com.mattvorst.shared.util.Environment" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<%
	response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setHeader("Access-Control-Allow-Origin","*");
	response.setDateHeader("Expires", 0); //prevents caching at the proxy server
	
	String htmlTitle = "Home";
	String stylesheet = "public.css";
	
	if(request.getAttribute("htmlTitle") != null)
	{
		htmlTitle = (String)request.getAttribute("htmlTitle");
	}

	if (request.getAttribute("stylesheet") != null) {
		stylesheet = (String) request.getAttribute("stylesheet");
	}
%>
<!DOCTYPE html>
<html>
<head>
	<title>The AI Explained | <%= htmlTitle %></title>

	<meta name="application-name" content="The AI Explained" />
	<meta name="apple-mobile-web-app-capable" content="yes" />
	<meta name="apple-mobile-web-app-status-bar-style" content="default" />
	<meta name="apple-mobile-web-app-title" content="The AI Explained" />
	<meta name="description" content="The AI Explained - We simplify AI concepts and show you practical applications that enhance your life." />
	<meta name="format-detection" content="telephone=no" />
	<meta name="mobile-web-app-capable" content="yes" />
	<meta name="theme-color" content="#FFFFFF" />

	<link rel="icon shortcut apple-touch-icon" sizes="180x180" href="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/apple-touch-icon.png">
	<link rel="icon" type="image/png" sizes="32x32" href="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/favicon-32x32.png">
	<link rel="icon" type="image/png" sizes="16x16" href="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/favicon-16x16.png">
	<link rel="mask-icon" href="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/safari-pinned-tab.svg" color="#5bbad5">
	<meta name="msapplication-TileColor" content="#da532c">
	<meta name="theme-color" content="#ffffff">

	<meta charset="UTF-8">

	<link href="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/style/<%=stylesheet%>" rel="stylesheet" type="text/css">

	<meta charset="UTF-8">

	<meta name="viewport" content="user-scalable=no, width=controller-width, minimum-scale=1, maximum-scale=1">

	<script type="text/javascript">
		const environment = { baseUrl:"<%= Environment.get(EnvironmentConstants.BASE_URL) %>", buildNumber:"<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>", cdnUrl:"<%= Environment.get(EnvironmentConstants.CDN_URL) %>" };
	</script>

</head>

<body>
	<div id=full-page>
	