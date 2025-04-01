import React from 'react';
import PropTypes from 'prop-types';

/**
 * Button - Reusable button component with standardized styling
 * @param {string} variant - Button style variant (primary, secondary, danger)
 * @param {function} onClick - Click handler
 * @param {React.ReactNode} children - Button content
 * @param {string} className - Additional CSS classes
 * @param {Object} rest - Any additional props to pass to the button element
 */
const Button = ({
                  variant = 'primary',
                  onClick,
                  children,
                  className = '',
                  ...rest
                }) => {
  const baseClass = 'btn';
  const variantClass = `btn-${variant}`;

  return (
    <button
      className={`${baseClass} ${variantClass} ${className}`}
      onClick={onClick}
      {...rest}
    >
      {children}
    </button>
  );
};

Button.propTypes = {
  variant: PropTypes.oneOf(['primary', 'secondary', 'danger']),
  onClick: PropTypes.func,
  children: PropTypes.node.isRequired,
  className: PropTypes.string
};

export default Button;