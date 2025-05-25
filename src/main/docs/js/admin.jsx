import React from 'react';
import {createRoot} from "react-dom/client";
import {HashRouter, Route, Routes} from "react-router";
import ContentList from "./admin/screens/ContentList.jsx";
import Layout from "./admin/AdminLayout.jsx";
import AccountList from "./admin/screens/AccountList.jsx";
import AccountDetail from "./admin/screens/AccountDetail.jsx";
import AssetList from "./admin/screens/AssetList.jsx";
import AssetDetail from "./admin/screens/AssetDetail.jsx";
import ContentDetail from "./admin/screens/ContentDetail.jsx";
import NewsletterList from "./admin/screens/NewsletterList.jsx";
import NewsletterDetail from "./admin/screens/NewsletterDetail.jsx";
import NewsletterSettings from "./admin/screens/NewsletterSettings.jsx";
import TemplateList from "./admin/screens/TemplateList.jsx";
import TemplateDetail from "./admin/screens/TemplateDetail.jsx";
import axios from "axios";

const Homepage = () => {

  // Add an Axios Intercepter to add the Authorization header to all requests
  axios.interceptors.request.use((config) => {
      config.headers.Authorization = 'Bearer ' + localStorage.getItem('token');
      return config;
    },
    error => Promise.reject(error));

  // Add an Axios Intercepter to update the token in local storage when it changes
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
        <Route path="/" element={<Layout />}>
          {/*<Route index element={<Navigate to="/content" replace />} />*/}
          <Route path="content/" element={<ContentList />} />
          <Route path="content/:id/detail" element={<ContentDetail />} />
          <Route path="account/" element={<AccountList />} />
          <Route path="account/:id/detail" element={<AccountDetail />} />
          <Route path="asset/" element={<AssetList />} />
          <Route path="assets/:id/detail" element={<AssetDetail />} />
          <Route path="newsletter/" element={<NewsletterList />} />
          <Route path="newsletter/:id/detail" element={<NewsletterDetail />} />
          <Route path="newsletter/settings" element={<NewsletterSettings />} />
          <Route path="newsletter/settings/templates" element={<TemplateList />} />
          <Route path="newsletter/template/:id/detail" element={<TemplateDetail />} />
        </Route>
      </Routes>
    </HashRouter>

  );
};

export default Homepage;

createRoot(document.getElementById('app_container')).render(<Homepage />);

