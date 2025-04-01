import React from 'react';
import PropTypes from 'prop-types';

/**
 * StatusBadge - Reusable component for displaying completion status
 * @param {boolean} completed - Whether the status is complete
 * @param {string} label - Label to display (optional, will use default labels if not provided)
 */
const StatusBadge = ({ completed, label }) => (
  <span className={`status-badge ${completed ? 'status-complete' : 'status-pending'}`}>
    {completed ? (
      <>
        <svg className="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
        </svg>
        {label || 'Complete'}
      </>
    ) : (
      <>{label || 'Pending'}</>
    )}
  </span>
);

StatusBadge.propTypes = {
  completed: PropTypes.bool.isRequired,
  label: PropTypes.string
};

export default StatusBadge;