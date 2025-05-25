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
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [validationErrors, setValidationErrors] = useState({});

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

  const validateTemplate = () => {
    const errors = {};
    
    if (!template.name || template.name.trim() === '') {
      errors.name = 'Template name is required';
    }
    
    if (!template.htmlContent || template.htmlContent.trim() === '') {
      errors.htmlContent = 'HTML content is required';
    }
    
    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const saveTemplate = async () => {
    if (!validateTemplate()) {
      setError('Please fix validation errors before saving');
      return;
    }

    try {
      setSaving(true);
      setError(null);
      setValidationErrors({});

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

  const deleteTemplate = async () => {
    if (!confirm('Are you sure you want to delete this template? This action cannot be undone.')) {
      return;
    }

    try {
      setDeleting(true);
      setError(null);

      await axios.delete(`/rest/api/1/newsletter/template/${id}`);
      setSuccess('Template deleted successfully');
      
      // Navigate back to template list after a short delay
      setTimeout(() => {
        navigate('/newsletter/settings/templates');
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete template');
      console.error('Error deleting template:', err);
    } finally {
      setDeleting(false);
    }
  };

  const duplicateTemplate = async () => {
    try {
      setSaving(true);
      setError(null);

      const response = await axios.post(`/rest/api/1/newsletter/template/${id}/duplicate`);
      setSuccess('Template duplicated successfully');
      
      // Navigate to the new template
      setTimeout(() => {
        navigate(`/newsletter/template/${response.data.templateUuid}/detail`);
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to duplicate template');
      console.error('Error duplicating template:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleInputChange = (field, value) => {
    setTemplate(prev => ({
      ...prev,
      [field]: value
    }));
    
    // Clear validation error for this field when user starts typing
    if (validationErrors[field]) {
      setValidationErrors(prev => ({
        ...prev,
        [field]: null
      }));
    }
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
          {!isNew && (
            <>
              <button 
                onClick={duplicateTemplate}
                disabled={saving || deleting}
                className="btn btn-secondary"
              >
                {saving ? 'Duplicating...' : 'Duplicate'}
              </button>
              <button 
                onClick={deleteTemplate}
                disabled={saving || deleting}
                className="btn btn-danger"
              >
                {deleting ? 'Deleting...' : 'Delete'}
              </button>
            </>
          )}
          <button 
            onClick={saveTemplate}
            disabled={saving || deleting}
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
                className={validationErrors.name ? 'error' : ''}
              />
              {validationErrors.name && (
                <div className="field-error">{validationErrors.name}</div>
              )}
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
                placeholder="Comma-separated variables (e.g. firstName, lastName)"
              />
              <small className="form-help">
                Define variables that can be replaced when using this template. Use double curly braces like {'{'}{'{'} variableName {'}'}{'}'}. 
              </small>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>HTML Content *</label>
              <QuillEditor
                value={template.htmlContent}
                onChange={(value) => handleInputChange('htmlContent', value)}
                placeholder="Enter your template content here... Use double curly braces for dynamic content."
              />
              {validationErrors.htmlContent && (
                <div className="field-error">{validationErrors.htmlContent}</div>
              )}
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