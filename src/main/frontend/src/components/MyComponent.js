import React from 'react'
import APIService from './APIService'
import './MyComponent.css';
import Artefact from "./Artefact";

class MyComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            artefacts: []
        };
        this.button = false;
    }

    componentDidMount(){
        APIService.getArtefacts().then((data) => {
            this.setState({ artefacts: data});
        })
            .catch(function (ex) {
                console.log('Response parsing failed. Error: ', ex);
            });
    }

    nextPath(path) {
        this.props.history.push(path);
    }

    render() {
        return (
            <div>

        <div className="min-h-screen bg-gray-400 flex justify-center items-center py-20">
            <div className="container mx-auto p-12 bg-gray-100 rounded-xl">
                <h1 className="text-4xl font-mono from-current mb-8">Artefacts</h1>
                <div className="sm:grid sm:grid-cols-2 lg:grid-cols-3 gap-4 space-y-4 sm:space-y-0">
                        {
                        this.state.artefacts.map(artefact =>
                                <div className="bg-white rounded">
                                        <div className="shadow-lg hover:shadow-xl transform transition duration-500 hover:scale-105 p-6 rounded">

                                            <h2 className="text-xl font-medium text-gray-700">{artefact.groupId}</h2>
                                            <span className="text-blue-500 block mb-5">{artefact.artefactId}</span>
                                            <h2 className="text-xl font-medium text-gray-700">{artefact.version}</h2>
                                            <h2 className="text-xl font-medium text-gray-700">{new Date(artefact.addDate).toLocaleDateString("fr")}</h2>

                                                <button
                                                        className="mt-12 w-full text-center bg-yellow-400 py-2 rounded-lg">
                                                    Read more
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