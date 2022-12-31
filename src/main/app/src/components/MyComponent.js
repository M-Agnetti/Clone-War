import React from 'react'
import './MyComponent.css';
import {Link} from "react-router-dom";
import UploadFiles from "./UploadFiles";
import CanvasJSReact from './canvasjs.react';

const CanvasJSChart = CanvasJSReact.CanvasJSChart;

class MyComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            artefacts: []
        };
    }


    componentDidMount(){

        const art = fetch('http://localhost:8080/artefacts',{
            method: 'get',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
            },
            'credentials': 'same-origin'
        }).then(res => res.json())
            .catch(function (ex) {
                console.log('Response parsing failed. Error: ', ex);
            });

        fetch('http://localhost:8080/all-scores',{
            method: 'get',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
            },
            'credentials': 'same-origin'
        }).then(res => res.json())
            .then((data) => {
                console.log(data);
                let tmp = [];
                art.then((arts) => {
                    for(let pos = 0 ; pos < arts.length && pos < data.length ; pos++){
                        let points = [];
                        for (let i = 0; i < data.at(pos).length ; i++) {
                            points.push({label: data.at(pos).at(i).artefact.name, y:data.at(pos).at(i).score});
                        }
                        console.log(points);
                        tmp.push({
                            artefact: arts.at(pos),
                            options: {
                                animationEnabled: true,
                                theme: "light2",
                                title: {
                                    text: "Highest scores"
                                },
                                axisX: {
                                    title: "Artifacts",
                                },
                                axisY: {
                                    title: "Pourcentage",
                                },
                                data: [
                                    {
                                        type: "column",
                                        dataPoints: points
                                    }
                                ]
                            }
                        });
                    }
                    this.setState({artefacts: tmp});
                });
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
                                this.state.artefacts.map(elem =>
                                    <div className="bg-white rounded">
                                        <div className="shadow-lg hover:shadow-xl transform transition duration-500 hover:scale-105 p-6 rounded">

                                            <span className="text-blue-500 block mb-5">{elem.artefact.name}</span>
                                            <h2 className="text-xl font-medium text-gray-700">{new Date(elem.artefact.dateAdd).toLocaleDateString("fr")}</h2>

                                            <a href={elem.artefact.url} className="text-blue-500 block mb-5">{elem.artefact.url}</a>

                                            <button
                                                className="mt-12 w-full text-center bg-yellow-400 py-2 rounded-lg">
                                                <Link to={`/artefact/${elem.artefact.id}`} >Read more</Link>
                                            </button>

                                            <div className="mb-8 shadow-lg mt-5">
                                                <CanvasJSChart options = {elem.options} />
                                            </div>
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
