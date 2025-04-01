import React from 'react';
import PropTypes from 'prop-types';

/**
 * AssetPreview - Reusable component for displaying asset previews with thumbnails
 * @param {Array} files - Array of file objects with fileUuid, fileUrl, and fileName
 * @param {number} activeIndex - Index of the currently active file
 * @param {function} onSelectFile - Callback when a thumbnail is selected
 * @param {function} onRemoveFile - Callback when the remove button is clicked
 * @param {function} onAddFile - Callback when the add button is clicked
 * @param {string} uploadId - ID for the upload input
 * @param {string} acceptedTypes - Comma-separated list of accepted file types
 * @param {string} altText - Alt text for the image
 */
const AssetPreview = ({
                        files,
                        activeIndex,
                        onSelectFile,
                        onRemoveFile,
                        onAddFile,
                        uploadId,
                        acceptedTypes,
                        altText
                      }) => {
  const activeFile = files[activeIndex];

  return (
    <div className="asset-preview">
      <div className="asset-preview-image">
        <img
          src={activeFile?.fileUrl}
          alt={altText || "Asset Preview"}
        />
      </div>
      <div className="asset-preview-info">
        <span className="asset-filename">
          {activeFile?.fileName || altText}
        </span>
        <div className="asset-actions">
          <button className="asset-action-btn">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
            </svg>
          </button>
          <button
            className="asset-action-btn"
            onClick={() => onRemoveFile(activeIndex)}
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>

      {/* Thumbnails gallery */}
      <div className="asset-thumbnails">
        {files.map((file, index) => (
          <div
            key={file.fileUuid}
            className={`asset-thumbnail ${index === activeIndex ? 'active' : ''}`}
            onClick={() => onSelectFile(index)}
          >
            <img src={file.fileUrl} alt={`${altText} ${index + 1}`} />
          </div>
        ))}
        <div className="asset-thumbnail asset-thumbnail-add">
          <label htmlFor={uploadId} className="upload-thumbnail-label">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
          </label>
          <input
            id={uploadId}
            type="file"
            accept={acceptedTypes}
            className="hidden-upload"
            onChange={onAddFile}
          />
        </div>
      </div>
    </div>
  );
};

AssetPreview.propTypes = {
  files: PropTypes.arrayOf(
    PropTypes.shape({
      fileUuid: PropTypes.string.isRequired,
      fileUrl: PropTypes.string.isRequired,
      fileName: PropTypes.string.isRequired
    })
  ).isRequired,
  activeIndex: PropTypes.number.isRequired,
  onSelectFile: PropTypes.func.isRequired,
  onRemoveFile: PropTypes.func.isRequired,
  onAddFile: PropTypes.func.isRequired,
  uploadId: PropTypes.string.isRequired,
  acceptedTypes: PropTypes.string.isRequired,
  altText: PropTypes.string
};

export default AssetPreview;