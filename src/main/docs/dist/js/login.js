(function (react, require$$0, axios) {
  'use strict';

  var client = {};

  var hasRequiredClient;

  function requireClient () {
  	if (hasRequiredClient) return client;
  	hasRequiredClient = 1;

  	var m = require$$0;
  	{
  	  client.createRoot = m.createRoot;
  	  client.hydrateRoot = m.hydrateRoot;
  	}
  	return client;
  }

  var clientExports = requireClient();

  function App() {
    const [username, setUsername] = react.useState('');
    const [password, setPassword] = react.useState('');
    const [error, setError] = react.useState('');
    const handleSubmit = async event => {
      event.preventDefault();
      setError('');
      try {
        const response = await axios.post('/rest/auth/user/login', {
          username,
          password
        });
        console.log('Login successful:', response.data);

        // Store the token in local storage
        localStorage.setItem('token', response.data.token);
        window.location.href = '/admin/';
      } catch (error) {
        var _error$response;
        setError(((_error$response = error.response) === null || _error$response === void 0 || (_error$response = _error$response.data) === null || _error$response === void 0 ? void 0 : _error$response.message) || 'Invalid credentials');
      }
    };
    return /*#__PURE__*/React.createElement("div", {
      className: "logInContainer"
    }, /*#__PURE__*/React.createElement("form", {
      onSubmit: handleSubmit,
      className: "logInForm"
    }, /*#__PURE__*/React.createElement("h1", {
      className: "txtC"
    }, "Login"), /*#__PURE__*/React.createElement("div", {
      className: "mgT2"
    }, /*#__PURE__*/React.createElement("label", null, "Email Address", /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("input", {
      type: "text",
      value: username,
      onChange: e => setUsername(e.target.value),
      required: true
    }))), /*#__PURE__*/React.createElement("div", null)), /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("label", null, "Password", /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement("input", {
      type: "password",
      value: password,
      onChange: e => setPassword(e.target.value),
      required: true
    }))), /*#__PURE__*/React.createElement("div", null)), error && /*#__PURE__*/React.createElement("div", {
      style: {
        color: 'red'
      }
    }, error), /*#__PURE__*/React.createElement("div", {
      className: "loginActionContainer"
    }, /*#__PURE__*/React.createElement("a", {
      href: `/forgot-password/?username=${username}`,
      className: "white"
    }, "Forgot Password?"), /*#__PURE__*/React.createElement("button", {
      type: "submit"
    }, "Login"))));
  }
  clientExports.createRoot(document.getElementById('app_container')).render(/*#__PURE__*/React.createElement(App, null));

})(React, ReactDOM, axios);
//# sourceMappingURL=login.js.map
