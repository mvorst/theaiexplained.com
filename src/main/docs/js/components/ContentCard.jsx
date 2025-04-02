import React from 'react';
import { Link } from 'react-router-dom';

const ContentCard = ({ content }) => {
  return (
    <div className="content-card">
      {content.cardHeaderImageUrl && (
        <div className="content-card-header">
          <img
            src={content.cardHeaderImageUrl}
            alt={content.cardTitle}
            className="content-card-image"
          />
        </div>
      )}

      <div className="content-card-body">
        <h2 className="content-card-title">{content.cardTitle}</h2>
        <p className="content-card-subtitle">{content.cardSubtitle}</p>

        <Link to={`/content/${content.contentUuid}`} className="content-card-cta">
          {content.cardCTATitle || "Start Learning"} →
        </Link>
      </div>
    </div>
  );
};

export default ContentCard;