import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from "axios";

const ContentList = () => {
  const [contentItems, setContentItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [cursor, setCursor] = useState(null);
  const [hasMore, setHasMore] = useState(false);
  const [contentCategoryType, setContentCategoryType] = useState("BLOG_POST");
  const navigate = useNavigate();

  useEffect(() => {
    fetchContent();
  }, [contentCategoryType]);

  const fetchContent = async () => {
    try {
      setLoading(true);
      let url = `/rest/admin/1/content/?contentCategoryType=${contentCategoryType}`;
      if (cursor && contentCategoryType === prevCategoryRef.current) {
        url += `&cursor=${cursor}`;
      } else {
        // Reset pagination when category changes
        setCursor(null);
      }

      const response = await axios.get(url);
      if (response.status !== 200) {
        throw new Error(`API error: ${response.status}`);
      }

      const data = response.data;
      setContentItems(data.list || []);
      setHasMore(data.cursor !== null);
      setCursor(data.cursor);
      setLoading(false);
      prevCategoryRef.current = contentCategoryType;
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  // Reference to keep track of category changes
  const prevCategoryRef = React.useRef(contentCategoryType);

  const loadMore = async () => {
    if (!cursor || !hasMore) return;

    setLoading(true);
    try {
      const response = await axios.get(`/rest/admin/1/content/?contentCategoryType=${contentCategoryType}&cursor=${cursor}`);
      if (response.status !== 200) {
        throw new Error(`API error: ${response.status}`);
      }

      const data = response.data;
      setContentItems([...contentItems, ...(data.list || [])]);
      setHasMore(data.cursor !== null);
      setCursor(data.cursor);
      setLoading(false);
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  const handleCategoryChange = (e) => {
    setContentCategoryType(e.target.value);
  };

  const handleRowClick = (contentUuid) => {
    navigate(`/content/${contentUuid}/detail`);
  };

  if (loading && contentItems.length === 0) return (
    <div className="loading-container">
      <div className="loading-spinner"></div>
      <p>Loading content...</p>
    </div>
  );

  if (error) return (
    <div className="error-container">
      <div className="error-icon">⚠️</div>
      <h3>Something went wrong</h3>
      <p>{error}</p>
      <button className="btn btn-primary" onClick={() => window.location.reload()}>Try Again</button>
    </div>
  );

  return (
    <div className="container content-container">
      <div className="section-header">
        <h2>Content Management</h2>
        <Link to="/content/new/detail" className="btn btn-primary">
          <span className="btn-icon">+</span> Create New Content
        </Link>
      </div>

      <div className="filter-section">
        <div className="filter-group">
          <label htmlFor="contentCategoryType">Filter by category:</label>
          <select
            id="contentCategoryType"
            className="form-select"
            value={contentCategoryType}
            onChange={handleCategoryChange}
          >
            <option value="BLOG_POST">Blog Post</option>
            <option value="INSTRUCTIONS">Instructions</option>
            <option value="START_HERE">Start Here</option>
            <option value="RESOURCES">Resources</option>
            <option value="MODEL">Model</option>
            <option value="COMPANY">Company</option>
            <option value="PERSON">Person</option>
            <option value="NEWS_ARTICLE">News Article</option>
            <option value="HOME_CONTENT">Home Content</option>
          </select>
        </div>
      </div>

      <div className="content-list">
        {contentItems.length > 0 ? (
          <>
            <div className="table-responsive">
              <table className="data-table">
                <thead>
                <tr>
                  <th>Title</th>
                  <th>Category</th>
                  <th>Created</th>
                  <th>Published</th>
                </tr>
                </thead>
                <tbody>
                {contentItems.map(item => (
                  <tr
                    key={item.contentUuid}
                    onClick={() => handleRowClick(item.contentUuid)}
                    className="content-row"
                  >
                    <td className="content-title">
                      {item.title || item.cardTitle || 'Untitled Content'}
                    </td>
                    <td className="content-category">
                      <span className="category-badge">{item.contentCategoryType || 'Uncategorized'}</span>
                    </td>
                    <td>
                      {item.createdAt ? new Date(item.createdAt).toLocaleDateString() : 'N/A'}
                    </td>
                    <td className="content-status">
                      <span className={`status-indicator ${item.publishedDate ? 'published' : 'draft'}`}>
                        {item.publishedDate ? new Date(item.publishedDate).toLocaleDateString() : 'Draft'}
                      </span>
                    </td>
                  </tr>
                ))}
                </tbody>
              </table>
            </div>

            {hasMore && (
              <div className="load-more-container">
                <button
                  className="btn btn-outline load-more-btn"
                  onClick={loadMore}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="loading-dot"></span>
                      <span className="loading-dot"></span>
                      <span className="loading-dot"></span>
                    </>
                  ) : 'Load More Content'}
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="empty-state">
            <div className="empty-icon">📝</div>
            <h3>No content yet</h3>
            <p>Create your first content item to get started with your AI knowledge base.</p>
            <Link to="/content/new/detail" className="btn btn-primary">
              <span className="btn-icon">+</span> Create Content
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default ContentList;