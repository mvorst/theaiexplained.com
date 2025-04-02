import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

const ContentDetail = () => {
  const { contentUuid } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(contentUuid ? true : false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const [content, setContent] = useState({
    contentUuid: null,
    cardHeaderImageUrl: '',
    cardHeaderImageFileUuid: null,
    cardTitle: '',
    cardSubtitle: '',
    cardCTATitle: 'Start Learning',
    headerImageUrl: '',
    headerImageFileUuid: null,
    title: '',
    subtitle: '',
    referenceUrl: '',
    referenceUrlTitle: 'Learn more',
    markupContent: '',
    audioContentUrl: '',
    audioContentFileUuid: null,
    metaTitle: '',
    metaDescription: '',
    metaType: 'article',
    metaUrl: '',
    metaImage: '',
    metaTwitterImageAltText: '',
    metaTwiterCard: 'summary_large_image',
    metaFBAppId: '',
    metaTwitterSite: '@theaiexplained'
  });

  useEffect(() => {
    if (contentUuid) {
      fetchContent();
    }
  }, [contentUuid]);

  const fetchContent = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`/rest/api/v1/content/${contentUuid}`);
      setContent(response.data);
      setLoading(false);
    } catch (err) {
      setError('Failed to load content. Please try again later.');
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setContent(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      let response;

      if (contentUuid) {
        // Update existing content
        response = await axios.put(`/rest/api/v1/content/${contentUuid}`, content);
      } else {
        // Create new content
        response = await axios.post('/rest/api/v1/content/', content);
      }

      // Redirect to content detail page
      navigate(`/content/${response.data.contentUuid}`);
    } catch (err) {
      setError('Failed to save content. Please check your input and try again.');
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="content-form-loading">Loading content data...</div>;
  }

  return (
    <div className="content-form-container">
      <h1 className="content-form-title">
        {contentUuid ? 'Edit Content' : 'Create New Content'}
      </h1>

      {error && (
        <div className="content-form-error">{error}</div>
      )}

      <form onSubmit={handleSubmit} className="content-form">
        <div className="form-section">
          <h2>Card Display</h2>

          <div className="form-group">
            <label htmlFor="cardTitle">Card Title *</label>
            <input
              type="text"
              id="cardTitle"
              name="cardTitle"
              value={content.cardTitle}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="cardSubtitle">Card Subtitle</label>
            <input
              type="text"
              id="cardSubtitle"
              name="cardSubtitle"
              value={content.cardSubtitle}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="cardCTATitle">Card CTA Text</label>
            <input
              type="text"
              id="cardCTATitle"
              name="cardCTATitle"
              value={content.cardCTATitle}
              onChange={handleChange}
              placeholder="Start Learning"
            />
          </div>

          <div className="form-group">
            <label htmlFor="cardHeaderImageUrl">Card Image URL</label>
            <input
              type="text"
              id="cardHeaderImageUrl"
              name="cardHeaderImageUrl"
              value={content.cardHeaderImageUrl}
              onChange={handleChange}
            />
          </div>
        </div>

        <div className="form-section">
          <h2>Content Details</h2>

          <div className="form-group">
            <label htmlFor="title">Content Title *</label>
            <input
              type="text"
              id="title"
              name="title"
              value={content.title}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="subtitle">Content Subtitle</label>
            <input
              type="text"
              id="subtitle"
              name="subtitle"
              value={content.subtitle}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="headerImageUrl">Header Image URL</label>
            <input
              type="text"
              id="headerImageUrl"
              name="headerImageUrl"
              value={content.headerImageUrl}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="markupContent">Content (HTML) *</label>
            <textarea
              id="markupContent"
              name="markupContent"
              value={content.markupContent}
              onChange={handleChange}
              rows={10}
              required
            ></textarea>
          </div>

          <div className="form-group">
            <label htmlFor="audioContentUrl">Audio Content URL</label>
            <input
              type="text"
              id="audioContentUrl"
              name="audioContentUrl"
              value={content.audioContentUrl}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="referenceUrl">Reference URL</label>
            <input
              type="text"
              id="referenceUrl"
              name="referenceUrl"
              value={content.referenceUrl}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="referenceUrlTitle">Reference URL Title</label>
            <input
              type="text"
              id="referenceUrlTitle"
              name="referenceUrlTitle"
              value={content.referenceUrlTitle}
              onChange={handleChange}
              placeholder="Learn more"
            />
          </div>
        </div>

        <div className="form-section">
          <h2>Meta & SEO</h2>

          <div className="form-group">
            <label htmlFor="metaTitle">Meta Title</label>
            <input
              type="text"
              id="metaTitle"
              name="metaTitle"
              value={content.metaTitle}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="metaDescription">Meta Description</label>
            <textarea
              id="metaDescription"
              name="metaDescription"
              value={content.metaDescription}
              onChange={handleChange}
              rows={3}
            ></textarea>
          </div>

          <div className="form-group">
            <label htmlFor="metaImage">Meta Image URL</label>
            <input
              type="text"
              id="metaImage"
              name="metaImage"
              value={content.metaImage}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="metaTwitterImageAltText">Twitter Image Alt Text</label>
            <input
              type="text"
              id="metaTwitterImageAltText"
              name="metaTwitterImageAltText"
              value={content.metaTwitterImageAltText}
              onChange={handleChange}
            />
          </div>
        </div>

        <div className="form-actions">
          <button
            type="button"
            className="btn-cancel"
            onClick={() => navigate(-1)}
          >
            Cancel
          </button>

          <button
            type="submit"
            className="btn-submit"
            disabled={submitting}
          >
            {submitting ? 'Saving...' : (contentUuid ? 'Update Content' : 'Create Content')}
          </button>
        </div>
      </form>
    </div>
  );
};

export default ContentDetail;