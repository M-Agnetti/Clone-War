import './App.css';
import MyComponent from "./components/MyComponent";
import React, {Component} from 'react';
import {Route, Routes} from "react-router-dom";
import Artefact from "./components/Artefact";

class App extends Component {
    state = {
        selectedFile1: null,
        selectedFile2:null
    };

    fetchData = async (fileData) => {
        const response = await fetch("http://localhost:8080/class",
            {
                method: 'post',
                body: fileData
            })
        if (!response.ok) {
            throw new Error('Data coud not be fetched!')
        } else {
            return response.json()
        }
    }

    onFileChange1 = event => {
        this.setState({ selectedFile1: event.target.files[0] });
    };
    onFileChange2 = event => {
        this.setState({ selectedFile2: event.target.files[0] });
    };

    onFileUpload = () => {

        const fileData = new FormData();

        fileData.append(
            "classes",
            this.state.selectedFile1,
            this.state.selectedFile1.name
        );

        fileData.append(
            "sources",
            this.state.selectedFile2,
            this.state.selectedFile2.name
        );
/*
        fetch("http://localhost:8080/class",{
            method: 'post',
            body: fileData
        });

 */
        this.fetchData(fileData);
        window.location.reload();
    };

    render() {
        return (
            <div className="App">
                    <input type="file"  onChange={this.onFileChange1} required />
                    <input type="file" onChange={this.onFileChange2} required />
                    <button onClick={this.onFileUpload} >
                        Upload!
                    </button>

                <Routes>
                    <Route index element={<MyComponent />} />
                    <Route path="/artefact/:id" element={<Artefact />} />
                </Routes>
            </div>
        );
    }

}

export default App;
