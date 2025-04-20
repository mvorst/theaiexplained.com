import React from 'react';

/**
 * Optional component that conditionally renders its children
 * @param {Object} props - Component props
 * @param {boolean} props.show - Whether to show the children
 * @param {React.ReactNode} props.children - The children to show when condition is true
 * @param {React.ReactNode} [props.fallback] - Optional content to show when condition is false
 * @returns {React.ReactNode|null} The children if show is true, the fallback if provided, or null
 */
const Optional = ({ show, children, fallback = null }) => {
    return show ? children : fallback;
};

export default Optional;