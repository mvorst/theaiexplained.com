import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import FileUpload from "../../controls/FileUpload.jsx";
import Optional from "../../controls/Optional.jsx";
import QuillEditor from "../../controls/QuillEditor.jsx";

const theme = {
  ltr: 'ltr',
  rtl: 'rtl',
  paragraph: 'editor-paragraph',
  quote: 'editor-quote',
  heading: {
    h1: 'editor-heading-h1',
    h2: 'editor-heading-h2',
    h3: 'editor-heading-h3',
    h4: 'editor-heading-h4',
    h5: 'editor-heading-h5',
  },
  list: {
    nested: {
      listitem: 'editor-nested-listitem',
    },
    ol: 'editor-list-ol',
    ul: 'editor-list-ul',
    listitem: 'editor-listitem',
  },
  image: 'editor-image',
  link: 'editor-link',
  text: {
    bold: 'editor-text-bold',
    italic: 'editor-text-italic',
    underline: 'editor-text-underline',
    code: 'editor-text-code',
  },
}

const ContentDetail = () => {
  const { id } = useParams();
  const contentUuid = id;
  const navigate = useNavigate();
  const [loading, setLoading] = useState(contentUuid ? true : false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [editorState, setEditorState] = useState(null);

  const [content, setContent] = useState({
    contentUuid: null,
    contentCategoryType: 'BLOG_POST',
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
    metaTwitterSite: '@theaiexplained',
    featured: false,
    publishedDate: formatDateForInput(new Date())
  });

  // Function to format date for HTML input
  function formatDateForInput(date) {
    if (!date) return '';
    if (typeof date === 'string') {
      date = new Date(date);
    }
    return date instanceof Date && !isNaN(date)
      ? date.toISOString().split('T')[0]
      : '';
  }

  useEffect(() => {
    if (contentUuid && contentUuid !== 'new') {
      fetchContent();
    } else {
      setLoading(false);
    }
  }, [contentUuid]);

  const fetchContent = async () => {
    try {
      setLoading(true);
      // Updated URL to match the endpoint in AdminController
      const response = await axios.get(`/rest/admin/1/content/${contentUuid}`);
      const contentData = response.data;

      // Format the publishedDate if it exists
      if (contentData.publishedDate) {
        contentData.publishedDate = formatDateForInput(contentData.publishedDate);
      }

      setContent(contentData);
      setEditorContent(contentData.markupContent);

      setLoading(false);
    } catch (err) {
      setError('Failed to load content. Please try again later.');
      setLoading(false);
      console.error('Error fetching content:', err);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    // Handle checkbox inputs differently from other inputs
    const newValue = type === 'checkbox' ? checked : value;

    setContent(prev => ({
      ...prev,
      [name]: newValue
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      // Prepare submission data
      const submissionData = { ...content };

      submissionData.markupContent = editorContent;

      // Convert publishedDate from string to Date object if it's set
      if (submissionData.publishedDate) {
        submissionData.publishedDate = new Date(submissionData.publishedDate);
      }

      let response;

      if (contentUuid === 'new') {
        // Create new content
        response = await axios.post('/rest/admin/1/content/', submissionData);
      } else {
        // Update existing content
        response = await axios.put(`/rest/admin/1/content/${contentUuid}`, submissionData);
      }

      // Redirect to content list page after successful save
      navigate('/content/');
    } catch (err) {
      setError('Failed to save content. Please check your input and try again.');
      setSubmitting(false);
      console.error('Error saving content:', err);
    }
  };

  const handleFileUpload = async (assetType, e) => {
    const file = e.target.files[0];
    if (!file) return;

    try {
      const response = await axios.get(`/rest/admin/1/s3/upload/url`);
      const { url, s3Bucket, s3Key } = response.data;

      const axiosInstance = axios.create({
        headers: {
          'Content-Type': file.type
        }
      });
      await axiosInstance.put(url, file);

      const uploadResponse = await axios.post(`/rest/admin/1/s3/upload/complete/`, {
        s3Bucket: s3Bucket,
        s3Key: s3Key,
        name: file.name,
        contentType: file.type,
        size: file.size,
        assetType: assetType
      });

      const s3UploadComplete = uploadResponse.data;

      // Update the appropriate image URL based on asset type
      if (assetType === 'MARKETING_BANNER_IMAGE') {
        setContent(prev => ({
          ...prev,
          headerImageUrl: s3UploadComplete.downloadUrl,
          headerImageFileUuid: s3UploadComplete.fileUuid
        }));
      } else if (assetType === 'BLOG_CARD_IMAGE') {
        setContent(prev => ({
          ...prev,
          cardHeaderImageUrl: s3UploadComplete.downloadUrl,
          cardHeaderImageFileUuid: s3UploadComplete.fileUuid
        }));
      } else if (assetType === 'AUDIO_CONTENT') {
        setContent(prev => ({
          ...prev,
          audioContentUrl: s3UploadComplete.downloadUrl,
          audioContentFileUuid: s3UploadComplete.fileUuid
        }));
      }

    } catch (error) {
      console.error('Error uploading file:', error);
    }
  };

  const [editorContent, setEditorContent] = useState("Some COntent");

  const handleEditorChange = (htmlContent) => {
    setEditorContent(htmlContent);
  };

  if (loading) {
    return <div className="content-form-loading">Loading content data...</div>;
  }

  return (
    <div className="content-form-container">
      <h1 className="content-form-title">
        {contentUuid ? 'Edit Content' : 'Create New Content'}
      </h1>

      <Optional show={error !== null}>
        <div className="content-form-error">{error}</div>
      </Optional>

      <form className="content-form">

        <div className="form-section">
          <h2>Content</h2>

          <div className="form-group checkbox-group">
            <input
              type="checkbox"
              id="featured"
              name="featured"
              checked={content.featured}
              onChange={handleChange}
            />
            <label htmlFor="featured">Featured Content</label>
          </div>

          <div className="form-group">
            <label htmlFor="publishedDate">Publication Date</label>
            <input
              type="date"
              id="publishedDate"
              name="publishedDate"
              value={content.publishedDate}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="contentCategoryType">Content Category Type *</label>
            <select
              id="contentCategoryType"
              name="contentCategoryType"
              value={content.contentCategoryType}
              onChange={handleChange}
              required
            >
              <optgroup label="Screens">
                <option value="START_HERE">Start Here</option>
                <option value="NEWS_ARTICLE">AI News</option>
                <option value="RESOURCES">Resources</option>
                <option value="BLOG_POST">Blog</option>
              </optgroup>
              <optgroup label="Other">
                <option value="INSTRUCTIONS">Instructions</option>
                <option value="MODEL">Model</option>
                <option value="COMPANY">Company</option>
                <option value="PERSON">Person</option>
              </optgroup>
            </select>
          </div>

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

          <Optional show={content?.headerImageUrl?.length}>
            <img src={content?.headerImageUrl} alt="" />
          </Optional>
          <Optional show={!content?.headerImageUrl?.length}>
            <FileUpload
              id="banner-image-file-upload"
              acceptedTypes=".png,.jpg,.jpeg"
              onFileSelected={(e) => handleFileUpload('MARKETING_BANNER_IMAGE', e)}
              hint="PNG, JPG up to 10MB"
            />
          </Optional>

          <div className="form-group">
            <label htmlFor="markupContent">Content (HTML) *</label>

            <div className="editor-container">
              <QuillEditor
                initialContent={editorContent}
                onChange={handleEditorChange}
              />
            </div>

            <textarea
              id="markupContent"
              name="markupContent"
              value={content.markupContent}
              onChange={handleChange}
              rows={10}
              required
              style={{ display: 'none' }}
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

          <FileUpload
            id="audio-content-file-upload"
            acceptedTypes=".mp3,.wav,.ogg"
            onFileSelected={(e) => handleFileUpload('AUDIO_CONTENT', e)}
            hint="MP3, WAV, OGG up to 50MB"
          />

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

        {/* Rest of the form remains the same */}
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

          <FileUpload
            id="card-image-file-upload"
            acceptedTypes=".png,.jpg,.jpeg"
            onFileSelected={(e) => handleFileUpload('BLOG_CARD_IMAGE', e)}
            hint="PNG, JPG up to 10MB"
          />
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
            onClick={handleSubmit}
          >
            {submitting ? 'Saving...' : (contentUuid ? 'Update Content' : 'Create Content')}
          </button>
        </div>
      </form>
    </div>
  );
};

export default ContentDetail;