import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import QuillEditor from '../../controls/QuillEditor.jsx';

const NewsletterDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isNew = id === 'new';

  const [newsletter, setNewsletter] = useState({
    title: '',
    subject: '',
    previewText: '',
    htmlContent: '',
    textContent: '',
    status: 'INACTIVE',
    scheduledDate: '',
    templateId: '',
    campaignTags: ''
  });

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [activeTab, setActiveTab] = useState('content');
  const [templates, setTemplates] = useState([]);
  const [templatesLoading, setTemplatesLoading] = useState(false);

  useEffect(() => {
    if (!isNew) {
      loadNewsletter();
    }
    loadTemplates();
  }, [id]);

  const loadNewsletter = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`/rest/api/1/newsletter/${id}`);
      setNewsletter(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to load newsletter');
      console.error('Error loading newsletter:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadTemplates = async () => {
    try {
      setTemplatesLoading(true);
      const response = await axios.get('/rest/api/1/newsletter/template/', {
        params: {
          count: 100 // Load up to 100 templates for the dropdown
        }
      });
      setTemplates(response.data.list || []);
    } catch (err) {
      console.error('Error loading templates:', err);
      // Don't show error for templates, just log it
    } finally {
      setTemplatesLoading(false);
    }
  };

  const saveNewsletter = async () => {
    try {
      setSaving(true);
      setError(null);

      const payload = { ...newsletter };
      
      // Convert scheduled date to proper format if provided
      if (payload.scheduledDate) {
        payload.scheduledDate = new Date(payload.scheduledDate).toISOString();
      }

      let response;
      if (isNew) {
        response = await axios.post('/rest/api/1/newsletter/', payload);
        navigate(`/newsletter/${response.data.newsletterUuid}/detail`, { replace: true });
      } else {
        response = await axios.put(`/rest/api/1/newsletter/${id}`, payload);
      }

      setNewsletter(response.data);
      setSuccess('Newsletter saved successfully');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save newsletter');
      console.error('Error saving newsletter:', err);
    } finally {
      setSaving(false);
    }
  };

  const scheduleNewsletter = async () => {
    try {
      setSaving(true);
      setError(null);

      const response = await axios.post(`/rest/api/1/newsletter/${id}/schedule`);
      setNewsletter(response.data);
      setSuccess('Newsletter scheduled successfully');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to schedule newsletter');
      console.error('Error scheduling newsletter:', err);
    } finally {
      setSaving(false);
    }
  };

  const sendNewsletter = async () => {
    if (!confirm('Are you sure you want to send this newsletter? This action cannot be undone.')) {
      return;
    }

    try {
      setSaving(true);
      setError(null);

      const response = await axios.post(`/rest/api/1/newsletter/${id}/send`);
      setNewsletter(response.data);
      setSuccess('Newsletter sent successfully');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send newsletter');
      console.error('Error sending newsletter:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleInputChange = (field, value) => {
    setNewsletter(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleTemplateChange = async (templateId) => {
    // Update the templateId field
    handleInputChange('templateId', templateId);

    // If a template is selected, offer to populate content
    if (templateId && canEdit) {
      const selectedTemplate = templates.find(t => t.templateUuid === templateId);
      if (selectedTemplate) {
        const shouldPopulate = confirm(
          `Would you like to populate the newsletter content with the template "${selectedTemplate.name}"? This will replace your current content.`
        );
        
        if (shouldPopulate) {
          setNewsletter(prev => ({
            ...prev,
            templateId: templateId,
            htmlContent: selectedTemplate.htmlContent || prev.htmlContent,
            textContent: selectedTemplate.textContent || prev.textContent
          }));
        }
      }
    }
  };

  const getStatusBadge = (status) => {
    const statusMap = {
      INACTIVE: { class: 'status-draft', text: 'Draft' },
      ACTIVE: { class: 'status-scheduled', text: 'Scheduled' },
      ARCHIVED: { class: 'status-sent', text: 'Sent' }
    };

    const statusInfo = statusMap[status] || { class: 'status-draft', text: status };
    return <span className={`status-badge ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const canSchedule = newsletter.status === 'INACTIVE' && newsletter.title && newsletter.subject && newsletter.htmlContent;
  const canSend = newsletter.status === 'ACTIVE';
  const canEdit = newsletter.status !== 'ARCHIVED';

  if (loading) {
    return (
      <div className="content-area">
        <div className="loading">Loading newsletter...</div>
      </div>
    );
  }

  return (
    <div className="content-area">
      <div className="content-header">
        <div className="content-title">
          <h1>{isNew ? 'Create Newsletter' : newsletter.title || 'Edit Newsletter'}</h1>
          <div className="title-meta">
            {!isNew && (
              <>
                {getStatusBadge(newsletter.status)}
                <span className="meta-separator">•</span>
                <span>ID: {newsletter.newsletterUuid}</span>
              </>
            )}
          </div>
        </div>
        <div className="content-actions">
          <Link to="/newsletter" className="btn btn-outline">
            Back to List
          </Link>
          {canEdit && (
            <button 
              onClick={saveNewsletter}
              disabled={saving}
              className="btn btn-primary"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          )}
          {!isNew && canSchedule && (
            <button 
              onClick={scheduleNewsletter}
              disabled={saving}
              className="btn btn-secondary"
            >
              Schedule
            </button>
          )}
          {!isNew && canSend && (
            <button 
              onClick={sendNewsletter}
              disabled={saving}
              className="btn btn-success"
            >
              Send Now
            </button>
          )}
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
        <div className="tabs">
          <button 
            className={`tab ${activeTab === 'content' ? 'active' : ''}`}
            onClick={() => setActiveTab('content')}
          >
            Content
          </button>
          <button 
            className={`tab ${activeTab === 'settings' ? 'active' : ''}`}
            onClick={() => setActiveTab('settings')}
          >
            Settings
          </button>
          {!isNew && (
            <button 
              className={`tab ${activeTab === 'analytics' ? 'active' : ''}`}
              onClick={() => setActiveTab('analytics')}
            >
              Analytics
            </button>
          )}
        </div>

        <div className="tab-content">
          {activeTab === 'content' && (
            <div className="form-section">
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="title">Newsletter Title *</label>
                  <input
                    id="title"
                    type="text"
                    value={newsletter.title}
                    onChange={(e) => handleInputChange('title', e.target.value)}
                    disabled={!canEdit}
                    placeholder="Enter newsletter title"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="subject">Email Subject *</label>
                  <input
                    id="subject"
                    type="text"
                    value={newsletter.subject}
                    onChange={(e) => handleInputChange('subject', e.target.value)}
                    disabled={!canEdit}
                    placeholder="Enter email subject line"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="previewText">Preview Text</label>
                  <input
                    id="previewText"
                    type="text"
                    value={newsletter.previewText}
                    onChange={(e) => handleInputChange('previewText', e.target.value)}
                    disabled={!canEdit}
                    placeholder="Preview text shown in email clients"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>HTML Content *</label>
                  <QuillEditor
                    value={newsletter.htmlContent}
                    onChange={(value) => handleInputChange('htmlContent', value)}
                    readOnly={!canEdit}
                    placeholder="Enter your newsletter content here..."
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="textContent">Plain Text Content</label>
                  <textarea
                    id="textContent"
                    value={newsletter.textContent}
                    onChange={(e) => handleInputChange('textContent', e.target.value)}
                    disabled={!canEdit}
                    rows={6}
                    placeholder="Plain text version of your newsletter"
                  />
                </div>
              </div>
            </div>
          )}

          {activeTab === 'settings' && (
            <div className="form-section">
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="scheduledDate">Scheduled Date</label>
                  <input
                    id="scheduledDate"
                    type="datetime-local"
                    value={newsletter.scheduledDate ? new Date(newsletter.scheduledDate).toISOString().slice(0, 16) : ''}
                    onChange={(e) => handleInputChange('scheduledDate', e.target.value)}
                    disabled={!canEdit}
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="templateId">Newsletter Template</label>
                  <div className="template-selection">
                    <select
                      id="templateId"
                      value={newsletter.templateId || ''}
                      onChange={(e) => handleTemplateChange(e.target.value)}
                      disabled={!canEdit || templatesLoading}
                      className="form-select"
                    >
                      <option value="">No Template (Custom Content)</option>
                      {templates.map(template => (
                        <option key={template.templateUuid} value={template.templateUuid}>
                          {template.name} ({template.category})
                        </option>
                      ))}
                    </select>
                    {newsletter.templateId && (
                      <Link 
                        to={`/newsletter/template/${newsletter.templateId}/detail`}
                        className="btn btn-outline btn-sm"
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        View Template
                      </Link>
                    )}
                  </div>
                  {templatesLoading && (
                    <small className="form-help">Loading templates...</small>
                  )}
                  {!templatesLoading && templates.length === 0 && (
                    <small className="form-help">
                      No templates available. <Link to="/newsletter/settings/templates">Create your first template</Link>.
                    </small>
                  )}
                  {newsletter.templateId && (
                    <small className="form-help">
                      Template selected. Content can be customized after applying the template.
                    </small>
                  )}
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="campaignTags">Campaign Tags</label>
                  <input
                    id="campaignTags"
                    type="text"
                    value={newsletter.campaignTags}
                    onChange={(e) => handleInputChange('campaignTags', e.target.value)}
                    disabled={!canEdit}
                    placeholder="Comma-separated tags for organization"
                  />
                </div>
              </div>
            </div>
          )}

          {activeTab === 'analytics' && !isNew && (
            <div className="analytics-section">
              <div className="analytics-grid">
                <div className="analytics-card">
                  <h3>Recipients</h3>
                  <div className="analytics-value">{newsletter.totalRecipients || 0}</div>
                </div>
                <div className="analytics-card">
                  <h3>Delivered</h3>
                  <div className="analytics-value">{newsletter.deliveredCount || 0}</div>
                </div>
                <div className="analytics-card">
                  <h3>Opened</h3>
                  <div className="analytics-value">{newsletter.openedCount || 0}</div>
                  <div className="analytics-rate">
                    {newsletter.openedCount && newsletter.deliveredCount 
                      ? `${Math.round((newsletter.openedCount / newsletter.deliveredCount) * 100)}%`
                      : '0%'}
                  </div>
                </div>
                <div className="analytics-card">
                  <h3>Clicked</h3>
                  <div className="analytics-value">{newsletter.clickedCount || 0}</div>
                  <div className="analytics-rate">
                    {newsletter.clickedCount && newsletter.deliveredCount 
                      ? `${Math.round((newsletter.clickedCount / newsletter.deliveredCount) * 100)}%`
                      : '0%'}
                  </div>
                </div>
                <div className="analytics-card">
                  <h3>Bounced</h3>
                  <div className="analytics-value">{newsletter.bouncedCount || 0}</div>
                  <div className="analytics-rate">
                    {newsletter.bouncedCount && newsletter.totalRecipients 
                      ? `${Math.round((newsletter.bouncedCount / newsletter.totalRecipients) * 100)}%`
                      : '0%'}
                  </div>
                </div>
              </div>

              {newsletter.sentDate && (
                <div className="analytics-info">
                  <p><strong>Sent:</strong> {new Date(newsletter.sentDate).toLocaleString()}</p>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default NewsletterDetail;