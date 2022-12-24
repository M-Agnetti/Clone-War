import React from 'react'
import './MyComponent.css';
import {Link} from "react-router-dom";
import UploadFiles from "./UploadFiles";

class MyComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            artefacts: []
        };
    }

    componentDidMount(){
        fetch('http://localhost:8080/artefacts',{
            method: 'get',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
            },
            'credentials': 'same-origin'
        }).then(res => res.json())
        .then((data) => {
            this.setState({ artefacts: data});
        })
        .catch(function (ex) {
            console.log('Response parsing failed. Error: ', ex);
        });
    }


    render() {
        return (
            <div>
                <div className="bg-[url('/public/hacker2.jpg')] bg-no-repeat bg-cover bg-center flow-root justify-center min-h-screen bg-orange-600 flex justify-center items-center py-20">

            <UploadFiles />

            <div className="container mx-auto p-12 bg-gray-100 rounded-xl">
                <h1 className="text-4xl font-mono from-current mb-8">Artefacts</h1>
                <div className="content-around sm:grid sm:grid-cols-2 lg:grid-cols-3 gap-4 space-y-4 sm:space-y-0">
                        {
                        this.state.artefacts.map(artefact =>
                                <div className="bg-white rounded">
                                        <div className="shadow-lg hover:shadow-xl transform transition duration-500 hover:scale-105 p-6 rounded">

                                            <span className="text-blue-500 block mb-5">{artefact.name}</span>
                                            <h2 className="text-xl font-medium text-gray-700">{new Date(artefact.dateAdd).toLocaleDateString("fr")}</h2>

                                            <a href={artefact.url} className="text-blue-500 block mb-5">{artefact.url}</a>

                                                <button
                                                        className="mt-12 w-full text-center bg-yellow-400 py-2 rounded-lg">
                                                    <Link to={`/artefact/${artefact.id}`} >Read more</Link>
                                                    </button>
                                                </div>
                                        </div>

                        )
                    }
                </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default MyComponent