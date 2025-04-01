import React from 'react';
import PropTypes from 'prop-types';

/**
 * MethodBadge - Displays an HTTP method with appropriate styling
 * @param {string} method - The HTTP method (GET, POST, PUT, DELETE, etc.)
 * @param {string} className - Additional CSS classes
 */
const MethodBadge = ({ method, className = '' }) => {
  // Normalize method to lowercase for CSS class
  const normalizedMethod = method.toLowerCase();

  return (
    <span className={`method-badge method-${normalizedMethod} ${className}`}>
      {method}
    </span>
  );
};

MethodBadge.propTypes = {
  method: PropTypes.oneOf(['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS', 'HEAD']).isRequired,
  className: PropTypes.string
};

export default MethodBadge;