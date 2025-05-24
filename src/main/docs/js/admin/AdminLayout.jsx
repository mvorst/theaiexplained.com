import React from 'react';
import { Outlet, NavLink } from 'react-router-dom';

const Layout = () => {
  return (
    <div className="homepage">
      {/* Header */}
      <header className="header">
        <div className="container">
          <div className="header-content">
            <div className="logo">
              <NavLink to="/"><img src="/img/LogoH.svg" alt="The AI Explained Logo" /></NavLink>
            </div>
            <nav className="main-nav">
              <ul>
                <li><NavLink to="/content" className={({ isActive }) => isActive ? 'active' : ''}>Content</NavLink></li>
                <li><NavLink to="/newsletter" className={({ isActive }) => isActive ? 'active' : ''}>Newsletter</NavLink></li>
                <li><NavLink to="/account" className={({ isActive }) => isActive ? 'active' : ''}>Accounts</NavLink></li>
                <li><NavLink to="/asset" className={({ isActive }) => isActive ? 'active' : ''}>Assets</NavLink></li>
              </ul>
            </nav>
            <div className="header-buttons">
              <button className="btn btn-outline">Profile</button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="main-content">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="footer">
        <div className="container">
          <div className="footer-content">
            <div className="footer-logo">
            </div>
            <div className="footer-links">
              <div className="footer-column">
              </div>
              <div className="footer-column">
              </div>
              <div className="footer-column">
              </div>
            </div>
            <div className="footer-social">
            </div>
          </div>
          <div className="footer-bottom">
            <p>© 2025 TheAIExplained.com. All rights reserved.</p>
            <div className="footer-legal">
              <NavLink to="/privacy">Privacy Policy</NavLink>
              <NavLink to="/terms">Terms of Use</NavLink>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Layout;