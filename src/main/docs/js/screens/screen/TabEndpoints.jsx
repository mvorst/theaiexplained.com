import React from 'react';
import Optional from "../../controls/Optional.jsx";
import CheckboxStatus from "../../controls/CheckboxStatus.jsx";
import MethodBadge from "../../controls/MethodBadge.jsx";

const TabEndpoints = ({
                        screenData,
                        newEndpoint,
                        handleNewEndpointChange,
                        handleCheckboxChange,
                        handleAddEndpoint,
                        handleRemoveEndpoint
                      }) => {
  return (
    <div className="endpoints-tab">
      <h3 className="section-title">Server API Endpoints</h3>
      <p className="section-description">Define the endpoints that this screen will interact with</p>

      <div className="table-container">
        <table className="table endpoints-table">
          <thead>
          <tr>
            <th>PATH</th>
            <th>METHOD</th>
            <th>CONTROLLER</th>
            <th>SERVICE</th>
            <th>DAO</th>
            <th>TESTS</th>
            <th>ACTIONS</th>
          </tr>
          </thead>
          <tbody>
          {screenData?.screenEndpointList?.map((endpoint) => (
            <tr key={endpoint.endpointUuid}>
              <td>{endpoint.path}</td>
              <td>
                <MethodBadge method={endpoint.method} />
              </td>
              <td>
                <CheckboxStatus isChecked={endpoint.controller} />
              </td>
              <td>
                <CheckboxStatus isChecked={endpoint.service} />
              </td>
              <td>
                <CheckboxStatus isChecked={endpoint.dao} />
              </td>
              <td>
                <CheckboxStatus isChecked={endpoint.tests} />
              </td>
              <td className="text-center">
                <button
                  className="delete-btn"
                  onClick={() => handleRemoveEndpoint(endpoint.endpointUuid)}
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </td>
            </tr>
          ))}
          <Optional show={screenData?.screenEndpointList?.length === 0}>
            <tr>
              <td colSpan="7" className="empty-state">
                No endpoints defined yet. Add your first endpoint below.
              </td>
            </tr>
          </Optional>
          </tbody>
        </table>
      </div>

      <div className="add-endpoint">
        <h4 className="subsection-title">Add New Endpoint</h4>
        <div className="add-endpoint-form">
          <div className="endpoint-form-group">
            <label className="form-label">Endpoint Path</label>
            <input
              type="text"
              name="path"
              placeholder="/api/resource"
              value={newEndpoint.path}
              onChange={handleNewEndpointChange}
              className="form-input"
            />
          </div>

          <div className="endpoint-form-group">
            <label className="form-label">Method</label>
            <select
              name="method"
              value={newEndpoint.method}
              onChange={handleNewEndpointChange}
              className="form-input"
            >
              <option value="GET">GET</option>
              <option value="POST">POST</option>
              <option value="PUT">PUT</option>
              <option value="DELETE">DELETE</option>
              <option value="PATCH">PATCH</option>
            </select>
          </div>

          <div className="endpoint-form-group">
            <label className="form-label">Implementation</label>
            <div className="checkbox-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="controller"
                  checked={newEndpoint.controller}
                  onChange={handleCheckboxChange}
                />
                <span>Controller</span>
              </label>
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="service"
                  checked={newEndpoint.service}
                  onChange={handleCheckboxChange}
                />
                <span>Service</span>
              </label>
            </div>
          </div>

          <div className="endpoint-form-group">
            <label className="form-label">Data</label>
            <div className="checkbox-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="dao"
                  checked={newEndpoint.dao}
                  onChange={handleCheckboxChange}
                />
                <span>DAO</span>
              </label>
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="tests"
                  checked={newEndpoint.tests}
                  onChange={handleCheckboxChange}
                />
                <span>Tests</span>
              </label>
            </div>
          </div>
        </div>

        {/* New rows for Model Object and Description */}
        <div className="form-group">
          <label className="form-label">Model Object</label>
          <input
            type="text"
            name="modelObject"
            placeholder="com.mattvorst.codebuilder.dao.model.Resource"
            value={newEndpoint.modelObject}
            onChange={handleNewEndpointChange}
            className="form-input"
          />
        </div>

        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea
            name="description"
            className="form-textarea"
            placeholder="Purpose of this endpoint and expected behavior..."
            value={newEndpoint.description}
            onChange={handleNewEndpointChange}
            rows="3"
          />
        </div>

        <div className="endpoint-form-group endpoint-btn-group">
          <button
            className="btn btn-primary"
            onClick={handleAddEndpoint}
          >
            <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
            Add
          </button>
        </div>
      </div>
    </div>
  );
};

export default TabEndpoints;