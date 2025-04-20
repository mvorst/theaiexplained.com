import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

const AccountList = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const response = await fetch('/rest/api/1/accounts');
        if (!response.ok) {
          throw new Error(`API error: ${response.status}`);
        }
        const data = await response.json();
        setAccounts(data);
        setLoading(false);
      } catch (err) {
        setError(err.message);
        setLoading(false);
      }
    };

    fetchAccounts();
  }, []);

  if (loading) return <div className="loading">Loading accounts...</div>;
  if (error) return <div className="error-message">Error: {error}</div>;

  return (
    <div className="container">
      <div className="section-header">
        <h2>Account Management</h2>
        <Link to="/accounts/new" className="btn btn-primary">Create New Account</Link>
      </div>

      <div className="accounts-list">
        {accounts.length > 0 ? (
          <table className="data-table">
            <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Created</th>
              <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            {accounts.map(account => (
              <tr key={account.id}>
                <td><Link to={`/accounts/${account.id}`}>{account.name}</Link></td>
                <td>{account.email}</td>
                <td>{account.role}</td>
                <td>{account.status}</td>
                <td>{new Date(account.createdAt).toLocaleDateString()}</td>
                <td className="actions">
                  <Link to={`/accounts/${account.id}`} className="btn btn-sm">Edit</Link>
                  <button className="btn btn-sm btn-danger">Delete</button>
                </td>
              </tr>
            ))}
            </tbody>
          </table>
        ) : (
          <div className="empty-state">
            <p>No accounts found. Create your first account to get started.</p>
            <Link to="/accounts/new" className="btn btn-primary">Create Account</Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default AccountList;