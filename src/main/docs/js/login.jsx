import { useState } from 'react';
import { createRoot } from 'react-dom/client';
import axios from 'axios';

function App() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');

    try {
      const response = await axios.post('/rest/auth/user/login', {
        username,
        password,
      });

      console.log('Login successful:', response.data);

      // Store the token in local storage
      localStorage.setItem('token', response.data.token);

      window.location.href = '/admin/';

    } catch (error) {
      setError(error.response?.data?.message || 'Invalid credentials');
    }
  };

  return (
    <div className="logInContainer">
      <form onSubmit={handleSubmit} className="logInForm">
        <h1 className="txtC">Login</h1>
        <div className="mgT2">
          <label>
            Email Address
            <div>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
          </label>
          <div></div>
        </div>
        <div>
          <label>
            Password
            <div>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </label>
          <div>

          </div>
        </div>
        {error && <div style={{ color: 'red' }}>{error}</div>}
        <div className="loginActionContainer">
          <a href={`/forgot-password/?username=${username}`} className="white">Forgot Password?</a>
          <button type="submit">Login</button>
        </div>
      </form>
    </div>
  );
}

createRoot(document.getElementById('app_container')).render(<App />);