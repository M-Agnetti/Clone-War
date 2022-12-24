import './App.css';
import MyComponent from "./components/MyComponent";
import React from 'react';
import {Route, Routes} from "react-router-dom";
import Artefact from "./components/Artefact";
import Animation from "./components/Animation";

function App() {

    return (
        <div className="App">
            <Routes>
                <Route index element={<Animation />} />
                <Route path="/home" element={<MyComponent />} />
                <Route path="/artefact/:id" element={<Artefact />} />
            </Routes>
        </div>
    );
}

export default App;
