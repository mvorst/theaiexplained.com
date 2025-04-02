import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

const ContentDetail = () => {
  const { contentUuid } = useParams();
  const [content, setContent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchContent = async () => {
      try {
        setLoading(true);
        const response = await axios.get(`/rest/api/v1/content/${contentUuid}`);
        setContent(response.data);

        // Update document metadata for SEO
        if (response.data.metaTitle) {
          document.title = response.data.metaTitle;
        }

        const metaDescription = document.querySelector('meta[name="description"]');
        if (metaDescription && response.data.metaDescription) {
          metaDescription.setAttribute('content', response.data.metaDescription);
        }

        setLoading(false);
      } catch (err) {
        setError('Failed to load content. Please try again later.');
        setLoading(false);
      }
    };

    fetchContent();
  }, [contentUuid]);

  if (loading) {
    return <div className="content-detail-loading">Loading content...</div>;
  }

  if (error || !content) {
    return <div className="content-detail-error">{error || 'Content not found'}</div>;
  }

  return (
    <div className="content-detail">
      {content.headerImageUrl && (
        <div className="content-detail-header">
          <img
            src={content.headerImageUrl}
            alt={content.title}
            className="content-detail-header-image"
          />
        </div>
      )}

      <div className="content-detail-container">
        <h1 className="content-detail-title">{content.title}</h1>
        {content.subtitle && (
          <h2 className="content-detail-subtitle">{content.subtitle}</h2>
        )}

        <div
          className="content-detail-body"
          dangerouslySetInnerHTML={{ __html: content.markupContent }}
        />

        {content.audioContentUrl && (
          <div className="content-detail-audio">
            <h3>Audio Version</h3>
            <audio controls>
              <source src={content.audioContentUrl} type="audio/mpeg" />
              Your browser does not support the audio element.
            </audio>
          </div>
        )}

        {content.referenceUrl && (
          <div className="content-detail-reference">
            <a href={content.referenceUrl} target="_blank" rel="noopener noreferrer">
              {content.referenceUrlTitle || "Learn more"}
            </a>
          </div>
        )}
      </div>
    </div>
  );
};

export default ContentDetail;