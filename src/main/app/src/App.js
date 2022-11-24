import './App.css';
import MyComponent from "./components/MyComponent";
import React, {useEffect, useState} from 'react';
import {Route, Routes, useNavigate } from "react-router-dom";
import Artefact from "./components/Artefact";
import Animation from "./components/Animation";

const chunkSize = 5000;

function App() {
    const [file1, setFile1] = useState(null);
    const [file2, setFile2] = useState(null);
    const [typeJar, setTypeJar] = useState("classes");
    const [counter, setCounter] = useState(1)
    const [fileToBeUpload, setFileToBeUpload] = useState({})
    const [beginingOfTheChunk, setBeginingOfTheChunk] = useState(0)
    const [endOfTheChunk, setEndOfTheChunk] = useState(chunkSize)
    const [progress, setProgress] = useState(0)
    const [fileGuid, setFileGuid] = useState("")
    const [fileSize, setFileSize] = useState(0)
    const [chunkCount, setChunkCount] = useState(0)
    const navigate = useNavigate();

    const sleep = ms => new Promise(
        resolve => setTimeout(resolve, ms)
    );

    const resetChunkProperties = () => {
        setProgress(0)
        setCounter(1)
        setBeginingOfTheChunk(0)
        setEndOfTheChunk(chunkSize)
    }

    const getFile1 = (e) => {
        setFile1(e.target.files[0]);
    }
    const getFile2 = (e) => {
        setFile2(e.target.files[0]);
    }

    const getFileContext = (_file) => {
        console.log("GET FILE CONTEXT");
        resetChunkProperties();
        setFileSize(_file.size)
        console.log("file size : " + _file.size);
        const _totalCount = _file.size % chunkSize == 0 ? _file.size / chunkSize : Math.floor(_file.size / chunkSize) + 1; // Total count of chunks will have been upload to finish the file
        setChunkCount(_totalCount)
        setFileToBeUpload(_file)
        const _fileID = _file.name.split('.').pop();
        setFileGuid(_fileID)
        console.log("chunkSize : " + chunkSize + " chunkCount : " + chunkCount);
    }

    const fileUpload = () => {
        console.log("FILE UPLOAD");
        setCounter(counter + 1);
        if (counter <= chunkCount) {
            let chunk = fileToBeUpload.slice(beginingOfTheChunk, endOfTheChunk);
            uploadChunk(chunk);
        }
    }

    const uploadChunk = async (chunk) => {
        console.log("UPLOAD CHUNK " + typeJar);
        try {
            const response = await fetch("http://localhost:8080/post/" + typeJar,
                {
                    method: 'post',
                    body: chunk,
                    headers: { 'Content-Type': 'application/json' }
                })
            const data = response.data;
            if (response.ok) {
                console.log("UPLOAD CHUNK RESPONSE OK ");
                setBeginingOfTheChunk(endOfTheChunk);
                setEndOfTheChunk(endOfTheChunk + chunkSize);
                if (counter == chunkCount) {
                    if(typeJar == "classes"){
                        console.log("fini avec le file 1, on commence le file 2 ");
                        setTypeJar("sources");
                        setFileToBeUpload(file2);
                        getFileContext(file2);
                    }
                    else{
                        console.log('Process is complete, counter', counter)
                        await uploadCompleted();
                    }
                    //console.log('Process is complete, counter', counter)
                    //await uploadCompleted();
                } else {
                    let percentage = (counter / chunkCount) * 100;
                    setProgress(percentage);
                }
            } else {
                console.log('Error Occurred:', data.errorMessage)
            }
        } catch (error) {
            console.log('error', error)
        }
    }

    const uploadCompleted = async () => {
        console.log("UPLOAD COMPLETE");
        setFileToBeUpload(null);
        const response = await fetch("http://localhost:8080/class/UploadComplete", {
            method: 'post'
        });
        if (response.ok) {
            setProgress(100);
            setTimeout(() => {  navigate('/home') }, 1000);
        }
    }

    useEffect(() => {
        if (fileToBeUpload != null && fileSize > 0) {
            console.log("USE EFFECT");
            console.log("counter : " + counter + " chunkCount : " + chunkCount);
            setCounter(counter + 1);
            console.log("counter : " + counter);
            if (counter <= chunkCount) {
                let chunk = fileToBeUpload.slice(beginingOfTheChunk, endOfTheChunk);
                uploadChunk(chunk);
            }
        }
    }, [fileToBeUpload, progress])


    const onFileUpload = () => {
        console.log("ON FILE UPLOAD");
        console.log("type : " + typeJar);
        setTypeJar("classes");
        getFileContext(file1);
    };

    function DisplayUpload() {
        if (progress == 0) {
            if(file1 == null || file2 == null){
                return <div>
                    <button className="bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded inline-flex items-center opacity-50 cursor-not-allowed">
                        <svg className="fill-current w-4 h-4 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                            <path d="M13 8V2H7v6H2l8 8 8-8h-5zM0 18h20v2H0v-2z"/>
                        </svg>
                        <span>Upload</span>
                    </button>
                </div>;
            }
            return <div>
                <button className="bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded inline-flex items-center"
                        onClick={onFileUpload}>
                    <svg className="fill-current w-4 h-4 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                        <path d="M13 8V2H7v6H2l8 8 8-8h-5zM0 18h20v2H0v-2z"/>
                    </svg>
                    <span>Upload</span>
                </button>
            </div>;
        }

        return <div className="w-6/12 bg-gray-200 rounded-full">
            <div className="bg-red-600 text-xs font-medium text-red-100 text-center p-0.5 leading-none rounded-l-full"
                 style={{ width: Math.floor(progress) + "%" }}>
                Loading {typeJar}... {Math.floor(progress)}%
            </div>
        </div>;

    }

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
