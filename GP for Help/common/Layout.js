// File: Layout.js

import React from 'react';
import NavBar from './NavBar';
import { Outlet } from 'react-router-dom';

const Layout = () => {
    return (
        <>
            <NavBar />
            <div className="content">
                <Outlet /> {/* Renders child components */}
            </div>
        </>
    );
};

export default Layout;
