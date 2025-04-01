import React, {useEffect, useState} from 'react';
import { useNavigate } from 'react-router-dom';

const ScreenList = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [sortColumn, setSortColumn] = useState('name');
  const [sortDirection, setSortDirection] = useState('asc');

  // Mock data - would typically come from an API
  const [screenList, setScreenList] = useState([]);

  const handleSort = (column) => {
    if (sortColumn === column) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortColumn(column);
      setSortDirection('asc');
    }
  };

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleAddNewScreen = () => {
    handleViewScreen('new');
  };

  const handleViewScreen = (screenUuid) => {
    navigate(`/screen/${screenUuid}/detail`);
  };

  // Filter screenList based on search term
  const filteredScreenList = screenList.filter(screen =>
    screen.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Sort screenList based on current sort column and direction
  const sortedScreenList = [...filteredScreenList].sort((a, b) => {
    if (sortColumn === 'name') {
      return sortDirection === 'asc'
        ? a.name.localeCompare(b.name)
        : b.name.localeCompare(a.name);
    } else if (sortColumn === 'lastUpdated') {
      return sortDirection === 'asc'
        ? new Date(a.lastUpdated) - new Date(b.lastUpdated)
        : new Date(b.lastUpdated) - new Date(a.lastUpdated);
    }
    return 0;
  });
  
  useEffect(() => {
    loadScreenList().then();
  },[]);
  
  const loadScreenList = async () => {
    try {
      const response = await axios.get(`/rest/api/1/screen/`);
      setScreenList(response?.data?.list);
    } catch (error) {
    }
  }

  // Pagination state (could be expanded for actual pagination)
  const page = 1;
  const perPage = 10;
  const total = filteredScreenList.length;

  const StatusIndicator = ({ completed }) => (
    <span className={`status-badge ${completed ? 'status-complete' : 'status-pending'}`}>
      {completed ? (
        <>
          <svg className="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
          </svg>
          Complete
        </>
      ) : 'Pending'}
    </span>
  );

  return (
    <div className="screenList-page">
      <div className="card">
        <div className="card-header">
          <h1 className="page-title">ScreenList</h1>
          <div className="header-actions">
            <button
              className="btn btn-secondary mr-3"
              title="Filter screenList by status">
              <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
              </svg>
              Filter
            </button>
            <button
              className="btn btn-primary"
              onClick={handleAddNewScreen}>
              <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              Add New Screen
            </button>
          </div>
        </div>

        <div className="search-bar">
          <div className="search-input-container">
            <svg className="search-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              placeholder="Search Screen List"
              className="search-input"
              value={searchTerm}
              onChange={handleSearchChange}
            />
          </div>
          <div className="search-results-info">
            Showing {filteredScreenList.length} of {screenList.length} screenList
          </div>
        </div>

        <div className="table-container">
          <table className="table">
            <thead>
            <tr>
              <th
                className={`sortable-column ${sortColumn === 'name' ? 'active-sort' : ''}`}
                onClick={() => handleSort('name')}
              >
                Name
                {sortColumn === 'name' && (
                  <span className="sort-indicator">
                      {sortDirection === 'asc' ? ' ↑' : ' ↓'}
                    </span>
                )}
              </th>
              <th>Wireframe</th>
              <th>Design</th>
              <th>Front End</th>
              <th>Server API</th>
              <th
                className={`sortable-column ${sortColumn === 'lastUpdated' ? 'active-sort' : ''}`}
                onClick={() => handleSort('lastUpdated')}
              >
                Last Updated
                {sortColumn === 'lastUpdated' && (
                  <span className="sort-indicator">
                      {sortDirection === 'asc' ? ' ↑' : ' ↓'}
                    </span>
                )}
              </th>
            </tr>
            </thead>
            <tbody>
            {sortedScreenList.map((screen) => (
              <tr
                key={screen.screenUuid}
                className="screen-row"
                onClick={() => handleViewScreen(screen.screenUuid)}
              >
                <td className="screen-name">{screen.name}</td>
                <td><StatusIndicator completed={screen.wireframe} /></td>
                <td><StatusIndicator completed={screen.design} /></td>
                <td><StatusIndicator completed={screen.frontEnd} /></td>
                <td><StatusIndicator completed={screen.serverApi} /></td>
                <td className="last-updated">{screen.lastUpdated}</td>
              </tr>
            ))}
            {sortedScreenList.length === 0 && (
              <tr>
                <td colSpan="6" className="empty-state">
                  No screenList found matching your search criteria
                </td>
              </tr>
            )}
            </tbody>
          </table>
        </div>

        <div className="pagination">
          <div className="pagination-info">
            Showing <span className="font-medium">{Math.min(1, total)}</span> to <span className="font-medium">{Math.min(perPage, total)}</span> of <span className="font-medium">{total}</span> results
          </div>
          <div className="pagination-controls">
            <button className="pagination-btn" disabled={page === 1}>
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
              </svg>
            </button>
            <button className="pagination-btn" disabled={page === 1}>
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <button className="pagination-btn active">1</button>
            <button className="pagination-btn" disabled={page === Math.ceil(total / perPage)}>
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </button>
            <button className="pagination-btn" disabled={page === Math.ceil(total / perPage)}>
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 5l7 7-7 7M5 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ScreenList;