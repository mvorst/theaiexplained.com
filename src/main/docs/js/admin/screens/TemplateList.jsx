import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

const TemplateList = () => {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [cursor, setCursor] = useState('');
  const [hasMore, setHasMore] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadTemplates();
  }, []);

  const loadTemplates = async (loadCursor = '') => {
    try {
      setLoading(true);
      const response = await axios.get(`/rest/api/1/newsletter/template/`, {
        params: {
          cursor: loadCursor,
          count: 20
        }
      });

      if (loadCursor) {
        setTemplates(prev => [...prev, ...response.data.list]);
      } else {
        setTemplates(response.data.list);
      }

      setCursor(response.data.cursor || '');
      setHasMore(!!response.data.cursor);
      setError(null);
    } catch (err) {
      setError('Failed to load templates');
      console.error('Error loading templates:', err);
    } finally {
      setLoading(false);
    }
  };

  const deleteTemplate = async (templateUuid) => {
    if (!confirm('Are you sure you want to delete this template?')) {
      return;
    }

    try {
      await axios.delete(`/rest/api/1/newsletter/template/${templateUuid}`);
      setTemplates(templates.filter(t => t.templateUuid !== templateUuid));
    } catch (err) {
      setError('Failed to delete template');
      console.error('Error deleting template:', err);
    }
  };

  const duplicateTemplate = async (templateUuid) => {
    try {
      const response = await axios.post(`/rest/api/1/newsletter/template/${templateUuid}/duplicate`);
      setTemplates([response.data, ...templates]);
    } catch (err) {
      setError('Failed to duplicate template');
      console.error('Error duplicating template:', err);
    }
  };

  const handleRowClick = (templateUuid) => {
    navigate(`/newsletter/template/${templateUuid}/detail`);
  };

  if (loading && templates.length === 0) return (
    <div className="loading-container">
      <div className="loading-spinner"></div>
      <p>Loading templates...</p>
    </div>
  );

  if (error) return (
    <div className="error-container">
      <div className="error-icon">⚠️</div>
      <h3>Something went wrong</h3>
      <p>{error}</p>
      <button className="btn btn-primary" onClick={() => window.location.reload()}>Try Again</button>
    </div>
  );

  return (
    <div className="container content-container">
      <div className="section-header">
        <h2>Newsletter Templates</h2>
        <div className="header-actions">
          <Link to="/newsletter/template/new/detail" className="btn btn-primary">
            <span className="btn-icon">+</span> Create New Template
          </Link>
        </div>
      </div>

      <div className="content-list">
        {templates.length > 0 ? (
          <>
            <div className="table-responsive">
              <table className="data-table">
                <thead>
                <tr>
                  <th>Name</th>
                  <th>Description</th>
                  <th>Created</th>
                  <th>Last Modified</th>
                </tr>
                </thead>
                <tbody>
                {templates.map(template => (
                  <tr
                    key={template.templateUuid}
                    onClick={() => handleRowClick(template.templateUuid)}
                    className="content-row"
                  >
                    <td className="content-title">
                      {template.name || 'Untitled Template'}
                    </td>
                    <td className="content-category">
                      <span className="category-badge">{template.description || 'No Description'}</span>
                    </td>
                    <td>
                      {template.createdDate ? new Date(template.createdDate).toLocaleDateString() : 'N/A'}
                    </td>
                    <td>
                      {template.modifiedDate ? new Date(template.modifiedDate).toLocaleDateString() : 'N/A'}
                    </td>
                  </tr>
                ))}
                </tbody>
              </table>
            </div>

            {hasMore && (
              <div className="load-more-container">
                <button
                  className="btn btn-outline load-more-btn"
                  onClick={() => loadTemplates(cursor)}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="loading-dot"></span>
                      <span className="loading-dot"></span>
                      <span className="loading-dot"></span>
                    </>
                  ) : 'Load More Templates'}
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="empty-state">
            <div className="empty-icon">📄</div>
            <h3>No templates yet</h3>
            <p>Create your first newsletter template to speed up newsletter creation.</p>
            <Link to="/newsletter/template/new/detail" className="btn btn-primary">
              <span className="btn-icon">+</span> Create Template
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default TemplateList;