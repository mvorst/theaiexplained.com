import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

const AccountDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isNewAccount = id === 'new';

  const [account, setAccount] = useState({
    name: '',
    email: '',
    role: 'USER',
    status: 'ACTIVE'
  });

  const [loading, setLoading] = useState(!isNewAccount);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [roleOptions, setRoleOptions] = useState([]);
  const [statusOptions, setStatusOptions] = useState([]);

  useEffect(() => {
    // Fetch role and status options
    const fetchEnums = async () => {
      try {
        const [rolesResponse, statusResponse] = await Promise.all([
          fetch('/rest/api/1/enums/account-roles'),
          fetch('/rest/api/1/enums/account-statuses')
        ]);

        if (!rolesResponse.ok || !statusResponse.ok) {
          throw new Error('Failed to load enum values');
        }

        const roles = await rolesResponse.json();
        const statuses = await statusResponse.json();

        setRoleOptions(roles);
        setStatusOptions(statuses);
      } catch (err) {
        setError('Failed to load form options: ' + err.message);
      }
    };

    fetchEnums();

    // If editing existing account, fetch account details
    if (!isNewAccount) {
      const fetchAccountDetails = async () => {
        try {
          const response = await fetch(`/rest/api/1/accounts/${id}`);
          if (!response.ok) {
            throw new Error(`API error: ${response.status}`);
          }
          const data = await response.json();
          setAccount(data);
          setLoading(false);
        } catch (err) {
          setError(err.message);
          setLoading(false);
        }
      };

      fetchAccountDetails();
    }
  }, [id, isNewAccount]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setAccount(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);

    try {
      const url = isNewAccount
        ? '/rest/api/1/accounts'
        : `/rest/api/1/accounts/${id}`;

      const method = isNewAccount ? 'POST' : 'PUT';

      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(account)
      });

      if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
      }

      const savedAccount = await response.json();

      // Redirect to list view on success
      navigate('/accounts');
    } catch (err) {
      setError('Failed to save account: ' + err.message);
      setSaving(false);
    }
  };

  if (loading) return <div className="loading">Loading account...</div>;

  return (
    <div className="container">
      <div className="section-header">
        <h2>{isNewAccount ? 'Create New Account' : 'Edit Account'}</h2>
      </div>

      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleSubmit} className="account-form">
        <div className="form-group">
          <label htmlFor="name">Name</label>
          <input
            type="text"
            id="name"
            name="name"
            value={account.name}
            onChange={handleChange}
            required
            className="form-control"
          />
        </div>

        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            name="email"
            value={account.email}
            onChange={handleChange}
            required
            className="form-control"
          />
        </div>

        <div className="form-row">
          <div className="form-group half">
            <label htmlFor="role">Role</label>
            <select
              id="role"
              name="role"
              value={account.role}
              onChange={handleChange}
              className="form-control"
            >
              {roleOptions.map(role => (
                <option key={role.value} value={role.value}>
                  {role.label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group half">
            <label htmlFor="status">Status</label>
            <select
              id="status"
              name="status"
              value={account.status}
              onChange={handleChange}
              className="form-control"
            >
              {statusOptions.map(status => (
                <option key={status.value} value={status.value}>
                  {status.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {isNewAccount && (
          <div className="form-row">
            <div className="form-group half">
              <label htmlFor="password">Password</label>
              <input
                type="password"
                id="password"
                name="password"
                onChange={handleChange}
                required={isNewAccount}
                className="form-control"
              />
            </div>

            <div className="form-group half">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                onChange={handleChange}
                required={isNewAccount}
                className="form-control"
              />
            </div>
          </div>
        )}

        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/accounts')}
            className="btn btn-outline"
            disabled={saving}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={saving}
          >
            {saving ? 'Saving...' : 'Save Account'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default AccountDetail;