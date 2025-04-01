import React from 'react';
import StatusBadge from '../../controls/StatusBadge';

const TabGeneralInfo = ({ screenData, handleInputChange }) => {
  return (
    <div className="general-tab">
      <div className="form-group">
        <label className="form-label">Screen Name</label>
        <input
          type="text"
          name="name"
          value={screenData.name}
          onChange={handleInputChange}
          className="form-input"
        />
      </div>

      <div className="form-group">
        <label className="form-label">Screen File Name</label>
        <input
          type="text"
          name="filePath"
          value={screenData.filePath || ''}
          onChange={handleInputChange}
          className="form-input"
          placeholder="e.g. UserProfile"
        />
        <small className="form-hint">Path: src/screens/ScreenName.jsx</small>
      </div>

      <div className="form-group">
        <label className="form-label">Details</label>
        <textarea
          name="details"
          className="form-textarea"
          value={screenData.details}
          onChange={handleInputChange}
          rows="4"
        />
      </div>

      <div className="form-group">
        <label className="form-label">Development Status</label>
        <div className="status-grid">
          <div className="status-card">
            <div className="status-card-title">Wireframe</div>
            <StatusBadge completed={screenData.wireframe} />
          </div>
          <div className="status-card">
            <div className="status-card-title">Design</div>
            <StatusBadge completed={screenData.design} />
          </div>
          <div className="status-card">
            <div className="status-card-title">Front End</div>
            <StatusBadge completed={screenData.frontEnd} />
          </div>
          <div className="status-card">
            <div className="status-card-title">Server API</div>
            <StatusBadge completed={screenData.serverApi} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default TabGeneralInfo;