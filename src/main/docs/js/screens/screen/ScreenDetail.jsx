import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from "axios";
import TabGeneralInfo from './TabGeneralInfo';
import TabAssets from './TabAssets';
import TabEndpoints from './TabEndpoints';
import Optional from "../../controls/Optional.jsx";

const ScreenDetail = () => {
  const { screenUuid } = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('general');

  // Modified to support multiple files and include fileName
  const [screenData, setScreenData] = useState({
    screenUuid: undefined,
    name: 'New Screen',
    fileName: '', // Added fileName field
    details: '',
    screenEndpointList: [],
    wireframeFiles: [], // Array for multiple wireframe files
    screenshotFiles: [], // Array for multiple screenshot files
    activeWireframeIndex: 0, // Track active wireframe
    activeScreenshotIndex: 0 // Track active screenshot
  });

  const [newEndpoint, setNewEndpoint] = useState({
    name: '',
    method: 'GET',
    controller: false,
    service: false,
    dao: false,
    tests: false,
    modelObject: '', // Added model object field
    description: ''  // Added description field
  });

  // For demonstration purposes, load screen data based on ID
  useEffect(() => {
    if(screenUuid === 'new'){
      setScreenData({
        screenUuid: undefined,
        name: 'New Screen',
        fileName: '', // Added fileName field
        details: '',
        screenEndpointList: [],
        wireframeFiles: [],
        screenshotFiles: [],
        activeWireframeIndex: 0,
        activeScreenshotIndex: 0
      });
      loadNewScreenUuid().then();
    }else{
      loadScreen(screenUuid).then();
    }
  }, [screenUuid]);

  const loadNewScreenUuid = async () => {
    try {
      const response = await axios.post(`/rest/api/1/screen/new/uuid/`,{});
      setScreenData({
        ...screenData,
        screenUuid: response?.data?.uuid
      });
    } catch (error) {
    }
  }

  // Updated to handle multiple files
  const loadScreen = async (screenUuid) => {
    try {
      const response = await axios.get(`/rest/api/1/screen/${screenUuid}`);
      const loadedData = response.data;

      // Initialize arrays if they don't exist in the response
      loadedData.wireframeFiles = loadedData.wireframeFileList || [];
      loadedData.screenshotFiles = loadedData.screenshotFileList || [];

      // Set active indices
      loadedData.activeWireframeIndex = loadedData.wireframeFiles.length > 0 ? 0 : 0;
      loadedData.activeScreenshotIndex = loadedData.screenshotFiles.length > 0 ? 0 : 0;

      // Set wireframe preview to active file if available
      if (loadedData.wireframeFiles.length > 0) {
        loadedData.wireframePreviewFileUuid = loadedData.wireframeFiles[0].fileUuid;
        loadedData.wireframePreviewFileUrl = loadedData.wireframeFiles[0].fileUrl;
      }

      // Set screenshot preview to active file if available
      if (loadedData.screenshotFiles.length > 0) {
        loadedData.screenshotPreviewFileUuid = loadedData.screenshotFiles[0].fileUuid;
        loadedData.screenshotPreviewFileUrl = loadedData.screenshotFiles[0].fileUrl;
      }

      setScreenData(loadedData);
    } catch (error) {
    }
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setScreenData({
      ...screenData,
      [name]: value
    });
  };

  const handleNewEndpointChange = (e) => {
    const { name, value } = e.target;
    setNewEndpoint({
      ...newEndpoint,
      [name]: value
    });
  };

  const handleCheckboxChange = (e) => {
    const { name, checked } = e.target;
    setNewEndpoint({
      ...newEndpoint,
      [name]: checked
    });
  };

  const handleAddEndpoint = async () => {
    if (!newEndpoint.path) return;

    // Update local state
    setScreenData({
      ...screenData,
      screenEndpointList: [
        ...(screenData.screenEndpointList || []),
        newEndpoint
      ]
    });

    console.log(JSON.stringify(newEndpoint));

    try {
      // Post to the API
      if (screenData.screenUuid) {
        await axios.post(
          `/rest/api/1/screen/${screenData.screenUuid}/endpoint/`,
          newEndpoint
        );
      }
    } catch (error) {
      console.error('Error saving endpoint:', error);
      // Could add error handling UI here
    }

    // Reset the form
    setNewEndpoint({
      name: '',
      method: 'GET',
      controller: false,
      service: false,
      dao: false,
      tests: false,
      modelObject: '',
      description: ''
    });
  };

  const handleRemoveEndpoint = (endpointUuid) => {
    setScreenData({
      ...screenData,
      screenEndpointList: screenData?.screenEndpointList?.filter(endpoint => endpoint.endpointUuid !== endpointUuid)
    });
  };

  // New functions for handling multiple files
  const selectWireframePreview = (index) => {
    setScreenData({
      ...screenData,
      activeWireframeIndex: index,
      wireframePreviewFileUuid: screenData.wireframeFiles[index].fileUuid,
      wireframePreviewFileUrl: screenData.wireframeFiles[index].fileUrl
    });
  };

  const selectScreenshotPreview = (index) => {
    setScreenData({
      ...screenData,
      activeScreenshotIndex: index,
      screenshotPreviewFileUuid: screenData.screenshotFiles[index].fileUuid,
      screenshotPreviewFileUrl: screenData.screenshotFiles[index].fileUrl
    });
  };

  // Updated to handle multiple files
  const handleFileUpload = async (assetType, e) => {
    const file = e.target.files[0];
    if (!file) return;

    try {
      // Get presigned URL from the server
      const response = await axios.get(`/rest/api/1/s3/upload/url`);
      const { url, s3Bucket, s3Key } = response.data;

      // Upload the file to the presigned URL
      await axios.put(url, file, {
        headers: {
          'Content-Type': file.type
        }
      });

      // Communicate with the server that the asset has been uploaded using the endpoint /rest/api/1/screen/{screenUuid}/asset/upload/
      axios.post(`/rest/api/1/screen/${screenData.screenUuid}/asset/upload/`, {
        s3Bucket: s3Bucket,
        s3Key: s3Key,
        name: file.name,
        contentType: file.type,
        size: file.size,
        assetType: assetType
      })
        .then(response => {
          let s3UploadComplete = response.data;

          loadFile(s3UploadComplete.fileUuid)
            .then(response => {
              const fileUrl = response?.data?.url;

              if(assetType === 'WIREFRAME') {
                const newFile = {
                  fileUuid: s3UploadComplete.fileUuid,
                  fileName: file.name,
                  s3Bucket:s3UploadComplete.s3Bucket,
                  s3Key:s3UploadComplete.s3Key,
                  fileUrl: fileUrl
                };

                const updatedFiles = [...(screenData.wireframeFiles || []), newFile];
                const newIndex = updatedFiles.length - 1;

                setScreenData({
                  ...screenData,
                  wireframeFiles: updatedFiles,
                  wireframePreviewFileUuid: s3UploadComplete.fileUuid,
                  wireframePreviewFileUrl: fileUrl,
                  wireframe: true,
                  activeWireframeIndex: newIndex
                });
              }
              else if(assetType === 'SCREENSHOT') {
                const newFile = {
                  fileUuid: s3UploadComplete.fileUuid,
                  fileName: file.name,
                  s3Bucket:s3UploadComplete.s3Bucket,
                  s3Key:s3UploadComplete.s3Key,
                  fileUrl: fileUrl
                };

                const updatedFiles = [...(screenData.screenshotFiles || []), newFile];
                const newIndex = updatedFiles.length - 1;

                setScreenData({
                  ...screenData,
                  screenshotFiles: updatedFiles,
                  screenshotPreviewFileUuid: s3UploadComplete.fileUuid,
                  screenshotPreviewFileUrl: fileUrl,
                  design: true,
                  activeScreenshotIndex: newIndex
                });
              }
            });
        });
    } catch (error) {
      console.error('Error uploading file:', error);
    }
  };

  // New function to remove a wireframe file
  const removeWireframeFile = (index) => {
    const newFiles = [...screenData.wireframeFiles];
    newFiles.splice(index, 1);

    if (newFiles.length === 0) {
      setScreenData({
        ...screenData,
        wireframeFiles: [],
        wireframePreviewFileUuid: undefined,
        wireframePreviewFileUrl: undefined,
        wireframe: false,
        activeWireframeIndex: 0
      });
    } else {
      // If we removed the active file, select the previous one or the first one
      let newIndex = screenData.activeWireframeIndex;
      if (index === screenData.activeWireframeIndex) {
        newIndex = Math.max(0, index - 1);
      } else if (index < screenData.activeWireframeIndex) {
        newIndex = screenData.activeWireframeIndex - 1;
      }

      setScreenData({
        ...screenData,
        wireframeFiles: newFiles,
        wireframePreviewFileUuid: newFiles[newIndex].fileUuid,
        wireframePreviewFileUrl: newFiles[newIndex].fileUrl,
        activeWireframeIndex: newIndex
      });
    }
  };

  // New function to remove a screenshot file
  const removeScreenshotFile = (index) => {
    const newFiles = [...screenData.screenshotFiles];
    newFiles.splice(index, 1);

    if (newFiles.length === 0) {
      setScreenData({
        ...screenData,
        screenshotFiles: [],
        screenshotPreviewFileUuid: undefined,
        screenshotPreviewFileUrl: undefined,
        design: false,
        activeScreenshotIndex: 0
      });
    } else {
      // If we removed the active file, select the previous one or the first one
      let newIndex = screenData.activeScreenshotIndex;
      if (index === screenData.activeScreenshotIndex) {
        newIndex = Math.max(0, index - 1);
      } else if (index < screenData.activeScreenshotIndex) {
        newIndex = screenData.activeScreenshotIndex - 1;
      }

      setScreenData({
        ...screenData,
        screenshotFiles: newFiles,
        screenshotPreviewFileUuid: newFiles[newIndex].fileUuid,
        screenshotPreviewFileUrl: newFiles[newIndex].fileUrl,
        activeScreenshotIndex: newIndex
      });
    }
  };

  const loadFile = (fileUuid) => {
    return axios.get(`/rest/api/1/s3/file/${fileUuid}/download`);
  }

  const handleSaveChanges = () => {
    // In real app, save to server
    // Prepare data for saving - convert wireframeFiles to wireframeFileList
    const dataToSave = {
      ...screenData,
      wireframeFileList: screenData.wireframeFiles,
      screenshotFileList: screenData.screenshotFiles
    };

    if(screenUuid == 'new'){
      axios.post(`/rest/api/1/screen/`, dataToSave)
        .then(response=>{
          setScreenData({
            ...response.data,
            wireframeFiles: response.data.wireframeFileList || [],
            screenshotFiles: response.data.screenshotFileList || [],
            activeWireframeIndex: screenData.activeWireframeIndex,
            activeScreenshotIndex: screenData.activeScreenshotIndex
          });
          navigate(`/screen/${response.data.screenUuid}/detail`);
        })
    }else{
      dataToSave.screenUuid = screenUuid;
      axios.put(`/rest/api/1/screen/${screenUuid}`, dataToSave)
        .then(response=>{
          setScreenData({
            ...response.data,
            wireframeFiles: response.data.wireframeFileList || [],
            screenshotFiles: response.data.screenshotFileList || [],
            activeWireframeIndex: screenData.activeWireframeIndex,
            activeScreenshotIndex: screenData.activeScreenshotIndex
          });
        })
    }
  };

  const handleDeleteScreen = () => {
    if (window.confirm('Are you sure you want to delete this screen?')) {
      // In real app, delete from server
      navigate('/screen/');
    }
  };

  // These functions are no longer needed with the new implementation
  // but keeping them to maintain compatibility with existing code
  const clearWireframeImage = () => {
    setScreenData({
      ...screenData,
      wireframe: false,
      wireframePreviewFileUuid: undefined,
      wireframePreviewFileUrl: undefined,
      wireframeFiles: []
    });
  }

  const clearScreenshotImage = () => {
    setScreenData({
      ...screenData,
      screenshotPreviewFileUuid: undefined,
      screenshotPreviewFileUrl: undefined,
      screenshotFiles: []
    });
  }

  const StatusBadge = ({ completed, label }) => (
    <span className={`status-badge ${completed ? 'status-complete' : 'status-pending'}`}>
      {completed ? (
        <>
          <svg className="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
          </svg>
          {label || 'Complete'}
        </>
      ) : (
        <>{label || 'Pending'}</>
      )}
    </span>
  );

  return (
    <div className="screen-detail-page">
      {/* Back button and title */}
      <div className="detail-header">
        <div>
          <button
            className="back-link"
            onClick={() => navigate('/screen/')}
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back to Screen List
          </button>
          <h1 className="page-title">Screen Detail - {screenData.name}</h1>
        </div>
        <div className="status-badges">
          <StatusBadge completed={screenData.wireframe} label="Wireframe Ready" />
          <StatusBadge completed={screenData.design} label="Design Ready" />
        </div>
      </div>

      {/* Tabs */}
      <div className="card">
        <div className="tabs-container">
          <button
            className={`tab ${activeTab === 'general' ? 'active' : ''}`}
            onClick={() => setActiveTab('general')}
          >
            General Info
          </button>
          <button
            className={`tab ${activeTab === 'assets' ? 'active' : ''}`}
            onClick={() => setActiveTab('assets')}
          >
            Assets
          </button>
          <button
            className={`tab ${activeTab === 'endpoints' ? 'active' : ''}`}
            onClick={() => setActiveTab('endpoints')}
          >
            API Endpoints
          </button>
        </div>

        <div className="card-body">
          {/* Render appropriate tab component based on activeTab using Optional */}
          <Optional show={activeTab === 'general'}>
            <TabGeneralInfo
              screenData={screenData}
              handleInputChange={handleInputChange}
            />
          </Optional>

          <Optional show={activeTab === 'assets'}>
            <TabAssets
              screenData={screenData}
              handleFileUpload={handleFileUpload}
              selectWireframePreview={selectWireframePreview}
              selectScreenshotPreview={selectScreenshotPreview}
              removeWireframeFile={removeWireframeFile}
              removeScreenshotFile={removeScreenshotFile}
            />
          </Optional>

          <Optional show={activeTab === 'endpoints'}>
            <TabEndpoints
              screenData={screenData}
              newEndpoint={newEndpoint}
              handleNewEndpointChange={handleNewEndpointChange}
              handleCheckboxChange={handleCheckboxChange}
              handleAddEndpoint={handleAddEndpoint}
              handleRemoveEndpoint={handleRemoveEndpoint}
            />
          </Optional>
        </div>

        <div className="card-footer">
          <div className="form-actions">
            <button className="btn btn-primary" onClick={handleSaveChanges}>
              <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" />
              </svg>
              Save Changes
            </button>
            <button className="btn btn-danger" onClick={handleDeleteScreen}>
              <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              Delete Screen
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ScreenDetail;