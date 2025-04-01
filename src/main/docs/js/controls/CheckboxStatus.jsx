import React from 'react';
import PropTypes from 'prop-types';

/**
 * CheckboxStatus - Displays a check or X icon based on status
 * @param {boolean} isChecked - Whether the checkbox is checked
 */
const CheckboxStatus = ({ isChecked }) => (
  <div className="text-center">
    {isChecked ? (
      <svg className="check-icon" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
      </svg>
    ) : (
      <svg className="unchecked-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
      </svg>
    )}
  </div>
);

CheckboxStatus.propTypes = {
  isChecked: PropTypes.bool.isRequired
};

export default CheckboxStatus;