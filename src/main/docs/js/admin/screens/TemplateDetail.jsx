import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import QuillEditor from '../../controls/QuillEditor.jsx';

const TemplateDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isNew = id === 'new';

  const [template, setTemplate] = useState({
    name: '',
    description: '',
    htmlContent: '',
    textContent: '',
    variables: '',
    category: 'GENERAL'
  });

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    if (!isNew) {
      loadTemplate();
    }
  }, [id]);

  const loadTemplate = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`/rest/api/1/newsletter/template/${id}`);
      setTemplate(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to load template');
      console.error('Error loading template:', err);
    } finally {
      setLoading(false);
    }
  };

  const saveTemplate = async () => {
    try {
      setSaving(true);
      setError(null);

      const payload = { ...template };

      let response;
      if (isNew) {
        response = await axios.post('/rest/api/1/newsletter/template/', payload);
        navigate(`/newsletter/template/${response.data.templateUuid}/detail`, { replace: true });
      } else {
        response = await axios.put(`/rest/api/1/newsletter/template/${id}`, payload);
      }

      setTemplate(response.data);
      setSuccess('Template saved successfully');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save template');
      console.error('Error saving template:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleInputChange = (field, value) => {
    setTemplate(prev => ({
      ...prev,
      [field]: value
    }));
  };

  if (loading) {
    return (
      <div className="content-area">
        <div className="loading">Loading template...</div>
      </div>
    );
  }

  return (
    <div className="content-area">
      <div className="content-header">
        <div className="content-title">
          <h1>{isNew ? 'Create Template' : template.name || 'Edit Template'}</h1>
          <div className="title-meta">
            {!isNew && (
              <>
                <span>ID: {template.templateUuid}</span>
                <span className="meta-separator">•</span>
                <span>Category: {template.category}</span>
              </>
            )}
          </div>
        </div>
        <div className="content-actions">
          <Link to="/newsletter/settings/templates" className="btn btn-outline">
            Back to Templates
          </Link>
          <button 
            onClick={saveTemplate}
            disabled={saving}
            className="btn btn-primary"
          >
            {saving ? 'Saving...' : 'Save Template'}
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-error">
          {error}
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          {success}
        </div>
      )}

      <div className="content-body">
        <div className="form-section">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="name">Template Name *</label>
              <input
                id="name"
                type="text"
                value={template.name}
                onChange={(e) => handleInputChange('name', e.target.value)}
                placeholder="Enter template name"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="description">Description</label>
              <input
                id="description"
                type="text"
                value={template.description}
                onChange={(e) => handleInputChange('description', e.target.value)}
                placeholder="Brief description of this template"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="category">Category</label>
              <select
                id="category"
                value={template.category}
                onChange={(e) => handleInputChange('category', e.target.value)}
                className="form-select"
              >
                <option value="GENERAL">General</option>
                <option value="MARKETING">Marketing</option>
                <option value="ANNOUNCEMENT">Announcement</option>
                <option value="NEWSLETTER">Newsletter</option>
                <option value="WELCOME">Welcome</option>
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="variables">Template Variables</label>
              <input
                id="variables"
                type="text"
                value={template.variables}
                onChange={(e) => handleInputChange('variables', e.target.value)}
                placeholder="Comma-separated variables (e.g. {{firstName}}, {{lastName}})"
              />
              <small className="form-help">
                Define variables that can be replaced when using this template. Use double curly braces like {{variableName}}.
              </small>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>HTML Content *</label>
              <QuillEditor
                value={template.htmlContent}
                onChange={(value) => handleInputChange('htmlContent', value)}
                placeholder="Enter your template content here... Use {{variableName}} for dynamic content."
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="textContent">Plain Text Content</label>
              <textarea
                id="textContent"
                value={template.textContent}
                onChange={(e) => handleInputChange('textContent', e.target.value)}
                rows={6}
                placeholder="Plain text version of your template"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TemplateDetail;