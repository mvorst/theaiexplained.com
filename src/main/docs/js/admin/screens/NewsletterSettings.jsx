import React from 'react';
import { Link } from 'react-router-dom';

const NewsletterSettings = () => {
  const settingsOptions = [
    {
      id: 'templates',
      title: 'Templates',
      description: 'Manage newsletter templates for consistent formatting',
      icon: '📄',
      path: '/newsletter/settings/templates'
    }
  ];

  return (
    <div className="container content-container">
      <div className="section-header">
        <h2>Newsletter Settings</h2>
      </div>

      <div className="settings-grid">
        {settingsOptions.map(option => (
          <Link 
            key={option.id} 
            to={option.path} 
            className="settings-card"
          >
            <div className="settings-icon">{option.icon}</div>
            <div className="settings-content">
              <h3>{option.title}</h3>
              <p>{option.description}</p>
            </div>
            <div className="settings-arrow">→</div>
          </Link>
        ))}
      </div>
    </div>
  );
};

export default NewsletterSettings;