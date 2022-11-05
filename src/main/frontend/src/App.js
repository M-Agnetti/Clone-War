import './App.css';
import MyComponent from "./components/MyComponent";
import React, {Component} from 'react';

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
        const formData = new FormData();

        formData.append(
            "classes",
            this.state.selectedFile1,
            this.state.selectedFile1.name
        );
        formData.append(
            "sources",
            this.state.selectedFile2,
            this.state.selectedFile2.name
        );

        // Request made to the backend api
        // Send formData object
        fetch("http://localhost:8080/",{
            method: 'post',
            body: formData
        });
           // .then(res => res.json());
        //this.setState({ selectedFile1: null, selectedFile2: null});
        window.location.reload();
    };

    render() {
        console.log("Render");

        return (

            <div className="App">
                    <input type="file"  onChange={this.onFileChange1} required />
                    <input type="file" onChange={this.onFileChange2} required />
                    <button onClick={this.onFileUpload} >
                        Upload!
                    </button>

                <MyComponent/>
            </div>

        );
    }

}

export default App;
