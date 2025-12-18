import React, { useState, useEffect } from 'react';
import { createRoot } from "react-dom/client";
import { HashRouter, Route, Routes, useNavigate } from "react-router";
import axios from "axios";

// Application UUID for trip-split app
const APPLICATION_UUID = 'a6292bac-855b-4036-8d66-8a85416b85d4';

// Trip List Component
const TripList = () => {
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showNewTripForm, setShowNewTripForm] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchRecentTrips();
  }, []);

  const fetchRecentTrips = async () => {
    try {
      setLoading(true);
      const response = await axios.get(
        `/rest/api/v1/data/${APPLICATION_UUID}/trip/recent/list/`,
        {
          params: {
            sortOrder: 'DESC',
            limit: 50
          }
        }
      );
      setTrips(response.data.list || []);
      setError(null);
    } catch (err) {
      setError('Failed to load trips');
      console.error('Error fetching trips:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleTripClick = (trip) => {
    navigate(`/trip/${trip.sortKey}`);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  return (
    <div className="trip-list-container">
      <div className="header">
        <h1>Trip Split</h1>
        <button 
          className="btn btn-primary"
          onClick={() => setShowNewTripForm(true)}
        >
          + New Trip
        </button>
      </div>

      {showNewTripForm && (
        <NewTripForm 
          onClose={() => setShowNewTripForm(false)}
          onTripCreated={() => {
            setShowNewTripForm(false);
            fetchRecentTrips();
          }}
        />
      )}

      {loading && <div className="loading">Loading trips...</div>}
      
      {error && (
        <div className="error">
          {error}
          <button onClick={fetchRecentTrips}>Retry</button>
        </div>
      )}

      {!loading && !error && trips.length === 0 && (
        <div className="empty-state">
          <p>No trips yet</p>
          <button 
            className="btn btn-primary"
            onClick={() => setShowNewTripForm(true)}
          >
            Create your first trip
          </button>
        </div>
      )}

      {!loading && !error && trips.length > 0 && (
        <div className="trip-grid">
          {trips.map((trip, index) => (
            <div 
              key={trip.sortKey || index}
              className="trip-card"
              onClick={() => handleTripClick(trip)}
            >
              <h3>{trip.data?.name || 'Unnamed Trip'}</h3>
              <p className="trip-dates">
                {trip.data?.startDate && formatDate(trip.data.startDate)}
              </p>
              {trip.data?.location && (
                <p className="trip-location">
                  📍 {trip.data.location}
                </p>
              )}
              {trip.data?.description && (
                <p className="trip-description">{trip.data.description}</p>
              )}
              <div className="trip-meta">
                <span className="created-date">
                  Created {formatDate(trip.createdDate)}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

// New Trip Form Component
const NewTripForm = ({ onClose, onTripCreated }) => {
  const [formData, setFormData] = useState({
    name: '',
    startDate: '',
    endDate: '',
    location: '',
    description: '',
    participants: ['']
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.name.trim()) {
      setError('Trip name is required');
      return;
    }

    try {
      setSaving(true);
      setError(null);

      // Use timestamp as sortKey for chronological ordering
      const sortKey = new Date().toISOString();
      
      // Filter out empty participants
      const participants = formData.participants.filter(p => p.trim());

      const tripData = {
        data: {
          name: formData.name.trim(),
          startDate: formData.startDate || null,
          endDate: formData.endDate || null,
          location: formData.location.trim() || null,
          description: formData.description.trim(),
          participants: participants,
          status: 'active',
          createdAt: sortKey
        }
      };

      await axios.post(
        `/rest/api/v1/data/${APPLICATION_UUID}/trip/recent/${sortKey}/`,
        tripData
      );

      onTripCreated();
    } catch (err) {
      setError('Failed to create trip');
      console.error('Error creating trip:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleParticipantChange = (index, value) => {
    const newParticipants = [...formData.participants];
    newParticipants[index] = value;
    setFormData({ ...formData, participants: newParticipants });
  };

  const addParticipant = () => {
    setFormData({
      ...formData,
      participants: [...formData.participants, '']
    });
  };

  const removeParticipant = (index) => {
    const newParticipants = formData.participants.filter((_, i) => i !== index);
    setFormData({ ...formData, participants: newParticipants });
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h2>New Trip</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        
        <form onSubmit={handleSubmit}>
          {error && <div className="form-error">{error}</div>}
          
          <div className="form-group">
            <label htmlFor="name">Trip Name *</label>
            <input
              type="text"
              id="name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Summer Vacation 2024"
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="startDate">Start Date</label>
              <input
                type="date"
                id="startDate"
                value={formData.startDate}
                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label htmlFor="endDate">End Date</label>
              <input
                type="date"
                id="endDate"
                value={formData.endDate}
                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="location">Location</label>
            <input
              type="text"
              id="location"
              value={formData.location}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
              placeholder="Paris, France"
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Trip details, destination, etc."
              rows={3}
            />
          </div>

          <div className="form-group">
            <label>Participants</label>
            {formData.participants.map((participant, index) => (
              <div key={index} className="participant-input">
                <input
                  type="text"
                  value={participant}
                  onChange={(e) => handleParticipantChange(index, e.target.value)}
                  placeholder="Enter participant name"
                />
                {formData.participants.length > 1 && (
                  <button
                    type="button"
                    className="btn-remove"
                    onClick={() => removeParticipant(index)}
                  >
                    Remove
                  </button>
                )}
              </div>
            ))}
            <button
              type="button"
              className="btn btn-secondary"
              onClick={addParticipant}
            >
              + Add Participant
            </button>
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-cancel" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Creating...' : 'Create Trip'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Trip Detail Component (placeholder for now)
const TripDetail = () => {
  const tripId = window.location.hash.split('/')[2];
  
  return (
    <div className="trip-detail">
      <h2>Trip Details</h2>
      <p>Trip ID: {tripId}</p>
      <p>Full trip management interface coming soon...</p>
      <a href="#/">← Back to trips</a>
    </div>
  );
};

// Main App Component
const TripSplitApp = () => {
  // Add an Axios Interceptor to add the Authorization header to all requests
  axios.interceptors.request.use((config) => {
      config.headers.Authorization = 'Bearer ' + localStorage.getItem('token');
      return config;
    },
    error => Promise.reject(error));

  // Add an Axios Interceptor to update the token in local storage when it changes
  axios.interceptors.response.use((response) => {
      if(response.headers.authorization){
        localStorage.setItem('token', response.headers.authorization);
      }
      return response;
    },
    error => Promise.reject(error));

  return (
    <HashRouter>
      <Routes>
        <Route path="/" element={<TripList />} />
        <Route path="/trip/:id" element={<TripDetail />} />
      </Routes>
    </HashRouter>
  );
};

export default TripSplitApp;

// Mount the app
createRoot(document.getElementById('app_container')).render(<TripSplitApp />);