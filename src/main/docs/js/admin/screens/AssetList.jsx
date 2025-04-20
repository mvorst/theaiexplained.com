import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

const AssetList = () => {
  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAssets = async () => {
      try {
        const response = await fetch('/rest/api/1/assets');
        if (!response.ok) {
          throw new Error(`API error: ${response.status}`);
        }
        const data = await response.json();
        setAssets(data);
        setLoading(false);
      } catch (err) {
        setError(err.message);
        setLoading(false);
      }
    };

    fetchAssets();
  }, []);

  if (loading) return <div className="loading">Loading assets...</div>;
  if (error) return <div className="error-message">Error: {error}</div>;

  return (
    <div className="container">
      <div className="section-header">
        <h2>Asset Management</h2>
        <Link to="/assets/new" className="btn btn-primary">Upload New Asset</Link>
      </div>

      <div className="assets-list">
        {assets.length > 0 ? (
          <table className="data-table">
            <thead>
            <tr>
              <th>Preview</th>
              <th>Name</th>
              <th>Type</th>
              <th>Size</th>
              <th>Uploaded</th>
              <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            {assets.map(asset => (
              <tr key={asset.id}>
                <td className="asset-preview">
                  {asset.type.startsWith('image/') ? (
                    <img src={asset.url} alt={asset.name} height="40" />
                  ) : (
                    <div className="file-icon">{asset.type.split('/')[1]}</div>
                  )}
                </td>
                <td><Link to={`/assets/${asset.id}`}>{asset.name}</Link></td>
                <td>{asset.type}</td>
                <td>{formatFileSize(asset.size)}</td>
                <td>{new Date(asset.createdAt).toLocaleDateString()}</td>
                <td className="actions">
                  <Link to={`/assets/${asset.id}`} className="btn btn-sm">Edit</Link>
                  <a
                    href={asset.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="btn btn-sm"
                  >
                    View
                  </a>
                  <button className="btn btn-sm btn-danger">Delete</button>
                </td>
              </tr>
            ))}
            </tbody>
          </table>
        ) : (
          <div className="empty-state">
            <p>No assets found. Upload your first asset to get started.</p>
            <Link to="/assets/new" className="btn btn-primary">Upload Asset</Link>
          </div>
        )}
      </div>
    </div>
  );
};

// Helper function to format file size
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

export default AssetList;