import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const ContentList = () => {
  const [contentItems, setContentItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [cursor, setCursor] = useState(null);
  const [hasMore, setHasMore] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchContent = async () => {
      try {
        let url = '/rest/api/1/content/';
        if (cursor) {
          url += `?cursor=${cursor}`;
        }

        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`API error: ${response.status}`);
        }

        const data = await response.json();
        setContentItems(data.list || []);
        setHasMore(data.cursor !== null);
        setCursor(data.cursor);
        setLoading(false);
      } catch (err) {
        setError(err.message);
        setLoading(false);
      }
    };

    fetchContent();
  }, []);

  const loadMore = async () => {
    if (!cursor || !hasMore) return;

    setLoading(true);
    try {
      const response = await fetch(`/rest/api/1/content/?cursor=${cursor}`);
      if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
      }

      const data = await response.json();
      setContentItems([...contentItems, ...(data.list || [])]);
      setHasMore(data.cursor !== null);
      setCursor(data.cursor);
      setLoading(false);
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
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