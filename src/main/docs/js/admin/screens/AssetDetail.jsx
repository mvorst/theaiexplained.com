import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

const AssetDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isNewAsset = id === 'new';
  const fileInputRef = useRef(null);

  const [asset, setAsset] = useState({
    name: '',
    description: '',
    type: '',
    size: 0,
    url: ''
  });

  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState('');
  const [loading, setLoading] = useState(!isNewAsset);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);
  const [assetTypes, setAssetTypes] = useState([]);

  useEffect(() => {
    // Fetch asset types
    const fetchAssetTypes = async () => {
      try {
        const response = await fetch('/rest/api/1/enums/asset-types');
        if (!response.ok) {
          throw new Error('Failed to load asset types');
        }
        const types = await response.json();
        setAssetTypes(types);
      } catch (err) {
        setError('Failed to load asset types: ' + err.message);
      }
    };

    fetchAssetTypes();

    // If editing existing asset, fetch asset details
    if (!isNewAsset) {
      const fetchAssetDetails = async () => {
        try {
          const response = await fetch(`/rest/api/1/assets/${id}`);
          if (!response.ok) {
            throw new Error(`API error: ${response.status}`);
          }
          const data = await response.json();
          setAsset(data);
          setPreview(data.url);
          setLoading(false);
        } catch (err) {
          setError(err.message);
          setLoading(false);
        }
      };

      fetchAssetDetails();
    }
  }, [id, isNewAsset]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setAsset(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      const selectedFile = e.target.files[0];
      setFile(selectedFile);

      // Set asset name if not already set
      if (!asset.name) {
        setAsset(prev => ({
          ...prev,
          name: selectedFile.name
        }));
      }

      // Create preview for images
      if (selectedFile.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = (event) => {
          setPreview(event.target.result);
        };
        reader.readAsDataURL(selectedFile);
      } else {
        // Non-image file, clear preview
        setPreview('');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setUploading(true);
    setError(null);

    try {
      // For new assets, upload the file first
      if (isNewAsset) {
        if (!file) {
          throw new Error('Please select a file to upload');
        }

        // Create FormData for file upload
        const formData = new FormData();
        formData.append('file', file);
        formData.append('name', asset.name);
        formData.append('description', asset.description);

        const uploadResponse = await fetch('/rest/api/1/assets/upload', {
          method: 'POST',
          body: formData
        });

        if (!uploadResponse.ok) {
          throw new Error(`Upload failed: ${uploadResponse.status}`);
        }

        const uploadResult = await uploadResponse.json();

        // Redirect to asset detail page
        navigate('/assets');
      } else {
        // Update existing asset metadata
        const updateResponse = await fetch(`/rest/api/1/assets/${id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            name: asset.name,
            description: asset.description
          })
        });

        if (!updateResponse.ok) {
          throw new Error(`Update failed: ${updateResponse.status}`);
        }

        // Redirect to assets list
        navigate('/assets');
      }
    } catch (err) {
      setError(err.message);
      setUploading(false);
    }
  };

  const triggerFileInput = () => {
    fileInputRef.current.click();
  };

  if (loading) return <div className="loading">Loading asset...</div>;

  return (
    <div className="container">
      <div className="section-header">
        <h2>{isNewAsset ? 'Upload New Asset' : 'Edit Asset'}</h2>
      </div>

      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleSubmit} className="asset-form">
        {isNewAsset && (
          <div className="asset-upload-area">
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              style={{ display: 'none' }}
            />
            <div
              className={`upload-zone ${preview ? 'has-preview' : ''}`}
              onClick={triggerFileInput}
            >
              {preview ? (
                <div className="asset-preview">
                  <img src={preview} alt="Asset preview" />
                </div>
              ) : (
                <div className="upload-prompt">
                  <div className="upload-icon">+</div>
                  <p>Click to select a file or drag and drop</p>
                </div>
              )}
            </div>
            {file && (
              <div className="file-info">
                <p>Selected file: {file.name}</p>
                <p>Type: {file.type}</p>
                <p>Size: {formatFileSize(file.size)}</p>
              </div>
            )}
          </div>
        )}

        {!isNewAsset && asset.type.startsWith('image/') && (
          <div className="asset-preview existing">
            <img src={asset.url} alt={asset.name} />
          </div>
        )}

        <div className="form-group">
          <label htmlFor="name">Asset Name</label>
          <input
            type="text"
            id="name"
            name="name"
            value={asset.name}
            onChange={handleChange}
            required
            className="form-control"
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={asset.description || ''}
            onChange={handleChange}
            className="form-control"
            rows="3"
          ></textarea>
        </div>

        {!isNewAsset && (
          <div className="form-row">
            <div className="form-group half">
              <label>Asset Type</label>
              <p className="form-static-value">{asset.type}</p>
            </div>

            <div className="form-group half">
              <label>Size</label>
              <p className="form-static-value">{formatFileSize(asset.size)}</p>
            </div>
          </div>
        )}

        {!isNewAsset && (
          <div className="form-group">
            <label>URL</label>
            <div className="url-display">
              <p className="form-static-value">{asset.url}</p>
              <button
                type="button"
                className="btn btn-sm"
                onClick={() => navigator.clipboard.writeText(asset.url)}
              >
                Copy
              </button>
            </div>
          </div>
        )}

        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/assets')}
            className="btn btn-outline"
            disabled={uploading}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={uploading}
          >
            {uploading ? 'Saving...' : isNewAsset ? 'Upload Asset' : 'Save Changes'}
          </button>
        </div>
      </form>
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

export default AssetDetail;