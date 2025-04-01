import React from 'react';
import PropTypes from 'prop-types';

/**
 * FileUpload - Reusable component for file upload areas
 * @param {string} id - Unique ID for the input element
 * @param {string} acceptedTypes - Comma-separated list of accepted file types
 * @param {function} onFileSelected - Callback when a file is selected
 * @param {string} hint - Text explaining allowed file types
 */
const FileUpload = ({ id, acceptedTypes, onFileSelected, hint }) => {
  return (
    <div className="upload-area">
      <div className="upload-area-content">
        <svg className="upload-area-icon w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
        </svg>
        <div className="upload-area-text">
          <label htmlFor={id} className="upload-label">
            Upload a file
          </label>
          <span> or drag and drop</span>
        </div>
        <p className="upload-area-hint">{hint}</p>
        <input
          id={id}
          type="file"
          accept={acceptedTypes}
          className="hidden-upload"
          onChange={onFileSelected}
        />
      </div>
    </div>
  );
};

FileUpload.propTypes = {
  id: PropTypes.string.isRequired,
  acceptedTypes: PropTypes.string.isRequired,
  onFileSelected: PropTypes.func.isRequired,
  hint: PropTypes.string.isRequired
};

export default FileUpload;