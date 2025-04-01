import React from 'react';
import Optional from "../../controls/Optional.jsx";
import FileUpload from "../../controls/FileUpload.jsx";
import AssetPreview from "../../controls/AssetPreview.jsx";

const TabAssets = ({
                     screenData,
                     handleFileUpload,
                     selectWireframePreview,
                     selectScreenshotPreview,
                     removeWireframeFile,
                     removeScreenshotFile
                   }) => {
  return (
    <div className="assets-tab">
      <div className="assets-columns">
        <div className="asset-column">
          <div className="form-group">
            <label className="form-label">Wireframe</label>
            <Optional show={screenData.wireframeFiles && screenData.wireframeFiles.length > 0}>
              <AssetPreview
                files={screenData.wireframeFiles}
                activeIndex={screenData.activeWireframeIndex}
                onSelectFile={selectWireframePreview}
                onRemoveFile={removeWireframeFile}
                onAddFile={(e) => handleFileUpload('WIREFRAME', e)}
                uploadId="wireframe-upload"
                acceptedTypes=".pdf,.png,.jpg,.jpeg"
                altText="Wireframe"
              />
            </Optional>
            <Optional show={!screenData.wireframeFiles || screenData.wireframeFiles.length === 0}>
              <FileUpload
                id="wireframe-upload"
                acceptedTypes=".pdf,.png,.jpg,.jpeg"
                onFileSelected={(e) => handleFileUpload('WIREFRAME', e)}
                hint="PDF, PNG, JPG up to 10MB"
              />
            </Optional>
          </div>

          <div className="form-group">
            <label className="form-label">Design Screenshot</label>
            <Optional show={screenData.screenshotFiles && screenData.screenshotFiles.length > 0}>
              <AssetPreview
                files={screenData.screenshotFiles}
                activeIndex={screenData.activeScreenshotIndex}
                onSelectFile={selectScreenshotPreview}
                onRemoveFile={removeScreenshotFile}
                onAddFile={(e) => handleFileUpload('SCREENSHOT', e)}
                uploadId="screenshot-upload"
                acceptedTypes=".png,.jpg,.jpeg"
                altText="Design Screenshot"
              />
            </Optional>
            <Optional show={!screenData.screenshotFiles || screenData.screenshotFiles.length === 0}>
              <FileUpload
                id="screenshot-upload"
                acceptedTypes=".png,.jpg,.jpeg"
                onFileSelected={(e) => handleFileUpload('SCREENSHOT', e)}
                hint="PNG, JPG up to 10MB"
              />
            </Optional>
          </div>
        </div>

        <div className="asset-column">
          <div className="code-implementation">
            <div className="code-section">
              <div className="code-header">
                <h3 className="code-title">Front End Components</h3>
                <button className="code-action-btn">
                  <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                  </svg>
                  Generate Scaffolding
                </button>
              </div>
              <div className="code-block">
                <div className="code-comment"># Component Structure</div>
                <div className="code-line">LoginForm.jsx</div>
                <div className="code-line">GoogleAuthButton.jsx</div>
                <div className="code-line">LoginPage.jsx</div>
              </div>
            </div>

            <div className="code-section">
              <div className="code-header">
                <h3 className="code-title">Backend Implementation</h3>
                <button className="code-action-btn">
                  <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                  </svg>
                  Generate Controllers
                </button>
              </div>
              <div className="code-block">
                <div className="code-comment"># Java Implementation Status</div>
                <div className="code-file">
                  <span>UserController.java</span>
                  <a href="docs/js/screens/screen/ScreenDetail.jsx#" className="code-view-link">View</a>
                </div>
                <div className="code-file">
                  <span>UserService.java</span>
                  <a href="docs/js/screens/screen/ScreenDetail.jsx#" className="code-view-link">View</a>
                </div>
              </div>
            </div>
          </div>

          <div className="ai-generation">
            <button className="btn btn-primary">
              <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
              Generate with AI
            </button>
            <span className="ai-hint">Use AI to generate assets based on description</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TabAssets;