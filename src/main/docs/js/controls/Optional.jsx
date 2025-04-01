import React from 'react';
import PropTypes from 'prop-types';

const Optional = ({ show, children }) => {
    return show ? <>{children}</> : null;
};

Optional.propTypes = {
    show: PropTypes.bool.isRequired,
    children: PropTypes.node.isRequired,
};

export default Optional;