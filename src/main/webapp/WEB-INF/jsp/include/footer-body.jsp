<%@page import="com.mattvorst.shared.util.Environment"%>
<%@page import="com.mattvorst.shared.constant.EnvironmentConstants"%>
<%@ page import="com.mattvorst.shared.constant.EnvironmentType" %>
<%@ page import="com.mattvorst.shared.util.Environment" %>
</div>

		<!--
			Copyright © 2025 Matthew A Vorst.
			All Rights Reserved.
			Permission to use, copy, modify, and distribute this software is strictly prohibited without the explicit written consent of Matthew A Vorst.
		-->

		<!-- Load React. -->
		<script type="text/javascript" src="<%= Environment.get(EnvironmentConstants.EXTERNAL_JS_URL) %>/js/external/react.production.18.3.1.min.js" crossorigin></script>
		<script type="text/javascript" src="<%= Environment.get(EnvironmentConstants.EXTERNAL_JS_URL) %>/js/external/react-dom.production.18.3.1.min.js" crossorigin></script>
		<script type="text/javascript" src="<%= Environment.get(EnvironmentConstants.EXTERNAL_JS_URL) %>/js/external/axios.1.7.9.min.js" crossorigin></script>

		<%if (EnvironmentType.DEV.equals(Environment.getEnvironmentType())) {%>
			<script type="module">
				import RefreshRuntime from '<%= Environment.get(EnvironmentConstants.BASE_URL) %>:3011/@react-refresh'

				RefreshRuntime.injectIntoGlobalHook(window)
				window.$RefreshReg$ = () => {
				}
				window.$RefreshSig$ = () => (type) => type
				window.__vite_plugin_react_preamble_installed__ = true
			</script>
			<script type="module" src="<%= Environment.get(EnvironmentConstants.BASE_URL) %>:3011/js<%= request.getRequestURI().replace(".action", "").replace(".jsp", "").replace("/WEB-INF/jsp/", "/") %>.jsx" crossorigin></script>
		<% } else {%>
			<script type="text/javascript" src="<%= Environment.get(EnvironmentConstants.CDN_URL) %>/<%= Environment.get(EnvironmentConstants.BUILD_NUMBER) %>/dist/js<%= request.getRequestURI().replace(".action", "").replace(".jsp", "").replace("/WEB-INF/jsp/", "/") %>.js" crossorigin></script>
		<% }%>
</body>
</html>
