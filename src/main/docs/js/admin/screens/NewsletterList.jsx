import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

const NewsletterList = () => {
  const [newsletters, setNewsletters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [cursor, setCursor] = useState('');
  const [hasMore, setHasMore] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadNewsletters();
  }, []);

  const loadNewsletters = async (loadCursor = '') => {
    try {
      setLoading(true);
      const response = await axios.get(`/rest/api/1/newsletter/`, {
        params: {
          cursor: loadCursor,
          count: 20
        }
      });

      if (loadCursor) {
        setNewsletters(prev => [...prev, ...response.data.list]);
      } else {
        setNewsletters(response.data.list);
      }

      setCursor(response.data.cursor || '');
      setHasMore(!!response.data.cursor);
      setError(null);
    } catch (err) {
      setError('Failed to load newsletters');
      console.error('Error loading newsletters:', err);
    } finally {
      setLoading(false);
    }
  };

  const deleteNewsletter = async (newsletterUuid) => {
    if (!confirm('Are you sure you want to delete this newsletter?')) {
      return;
    }

    try {
      await axios.delete(`/rest/api/1/newsletter/${newsletterUuid}`);
      setNewsletters(newsletters.filter(n => n.newsletterUuid !== newsletterUuid));
    } catch (err) {
      setError('Failed to delete newsletter');
      console.error('Error deleting newsletter:', err);
    }
  };

  const duplicateNewsletter = async (newsletterUuid) => {
    try {
      const response = await axios.post(`/rest/api/1/newsletter/${newsletterUuid}/duplicate`);
      setNewsletters([response.data, ...newsletters]);
    } catch (err) {
      setError('Failed to duplicate newsletter');
      console.error('Error duplicating newsletter:', err);
    }
  };

  const handleRowClick = (newsletterUuid) => {
    navigate(`/newsletter/${newsletterUuid}/detail`);
  };

  const getStatusBadge = (status) => {
    const statusMap = {
      INACTIVE: { class: 'draft', text: 'Draft' },
      ACTIVE: { class: 'scheduled', text: 'Scheduled' },
      ARCHIVED: { class: 'published', text: 'Sent' }
    };

    const statusInfo = statusMap[status] || { class: 'draft', text: status };
    return <span className={`status-indicator ${statusInfo.class}`}>{statusInfo.text}</span>;
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading && newsletters.length === 0) return (
    <div className="loading-container">
      <div className="loading-spinner"></div>
      <p>Loading newsletters...</p>
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
        <h2>Newsletter Management</h2>
        <div className="header-actions">
          <Link to="/newsletter/new/detail" className="btn btn-primary">
            <span className="btn-icon">+</span> Create New Newsletter
          </Link>
          <Link to="/newsletter/settings" className="btn btn-secondary">
            <span className="btn-icon">⚙️</span>
          </Link>
        </div>
      </div>

      <div className="content-list">
        {newsletters.length > 0 ? (
          <>
            <div className="table-responsive">
              <table className="data-table">
                <thead>
                <tr>
                  <th>Title</th>
                  <th>Subject</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th>Scheduled</th>
                </tr>
                </thead>
                <tbody>
                {newsletters.map(newsletter => (
                  <tr
                    key={newsletter.newsletterUuid}
                    onClick={() => handleRowClick(newsletter.newsletterUuid)}
                    className="content-row"
                  >
                    <td className="content-title">
                      {newsletter.title || 'Untitled Newsletter'}
                    </td>
                    <td className="content-category">
                      <span className="category-badge">{newsletter.subject || 'No Subject'}</span>
                    </td>
                    <td className="content-status">
                      {getStatusBadge(newsletter.status)}
                    </td>
                    <td>
                      {newsletter.createdDate ? new Date(newsletter.createdDate).toLocaleDateString() : 'N/A'}
                    </td>
                    <td>
                      {newsletter.scheduledDate ? new Date(newsletter.scheduledDate).toLocaleDateString() : 'Not Scheduled'}
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
                  onClick={() => loadNewsletters(cursor)}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="loading-dot"></span>
                      <span className="loading-dot"></span>
                      <span className="loading-dot"></span>
                    </>
                  ) : 'Load More Newsletters'}
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="empty-state">
            <div className="empty-icon">📧</div>
            <h3>No newsletters yet</h3>
            <p>Create your first newsletter to start engaging with your audience.</p>
            <Link to="/newsletter/new/detail" className="btn btn-primary">
              <span className="btn-icon">+</span> Create Newsletter
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default NewsletterList;