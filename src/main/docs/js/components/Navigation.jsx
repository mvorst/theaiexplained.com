import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const Navigation = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const currentPath = location.pathname;

  const isActive = (path) => {
    if (path === '/') {
      return currentPath === '/';
    }
    return currentPath.startsWith(path);
  };

  const handleNavigation = (path) => {
    navigate(path);
  };

  return (
    <div className="navigation-container">
      <div className="navigation-tabs">
        <button
          className={`nav-tab ${isActive('/') ? 'active' : ''}`}
          onClick={() => handleNavigation('/')}
        >
          Home
        </button>
        <button
          className={`nav-tab ${isActive('/screen/') ? 'active' : ''}`}
          onClick={() => handleNavigation('/screen/')}
        >
          Screens
        </button>
        <button
          className={`nav-tab ${isActive('/navigation') ? 'active' : ''}`}
          onClick={() => handleNavigation('/navigation')}
        >
          Navigation
        </button>
        <button
          className={`nav-tab ${isActive('/reference-files') ? 'active' : ''}`}
          onClick={() => handleNavigation('/reference-files')}
        >
          Reference Files
        </button>
      </div>
    </div>
  );
};

export default Navigation;