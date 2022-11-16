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


    onFileChange1 = event => {
        this.setState({ selectedFile1: event.target.files[0] });
    };
    onFileChange2 = event => {
        this.setState({ selectedFile2: event.target.files[0] });
    };

    onFileUpload = () => {

        const fileData = new FormData();

        fileData.append(
            "class",
            this.state.selectedFile1,
            this.state.selectedFile1.name
        );

        fileData.append(
            "sources",
            this.state.selectedFile2,
            this.state.selectedFile2.name
        );

        fetch("http://localhost:8080/class",{
            method: 'post',
            body: fileData
        });
           // .then(res => res.json());
        //this.setState({ selectedFile1: null, selectedFile2: null});
        window.location.reload();
    };

    render() {
        console.log("hey");
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
