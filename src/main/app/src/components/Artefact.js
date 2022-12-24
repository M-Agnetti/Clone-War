import React, {useEffect, useState} from 'react'
import {Link, useParams} from "react-router-dom";
import CanvasJSReact from './canvasjs.react';

const CanvasJS = CanvasJSReact.CanvasJS;
const CanvasJSChart = CanvasJSReact.CanvasJSChart;

const Artefact = () => {

    const [artefacts, setArtefact] = useState([]);
    const [sources, setSources] = useState([]);
    const params = useParams();

    const options = {
        title: {
            text: "Highest scores"
        },
        axisX:{
            title:"Artifacts",
        },
        axisY:{
            title:"Pourcentage",
        },
        data: [
            {
                // Change type to "doughnut", "line", "splineArea", etc.
                type: "column",
                dataPoints: [
                    { label: "Art1",  y: 10  },
                    { label: "Art2", y: 15  },
                    { label: "Art3", y: 25  },
                    { label: "Art4",  y: 28  },
                    { label: "Art5",  y: 38  }
                ]
            }
        ]
    };


    useEffect(()=>{
        fetch('http://localhost:8080/artefact/' + params.id,{
            method: 'get',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
            },
            'credentials': 'same-origin'
            })
            .then(res => res.json())
            .then(data => {
                console.log(data);
                setArtefact(data);
            })
            .catch(err => console.log(err));

        fetch('http://localhost:8080/clone/' + params.id,{
            method: 'get',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
            },
            'credentials': 'same-origin'
        })
            .then(res => res.json())
            .then(data => {
                setSources(data);
                console.log(sources);
            })
            .catch(err => console.log(err));
    },[]);

    return (
        <div>
            <div className="bg-[url('/public/hacker2.jpg')] bg-no-repeat bg-cover bg-center flow-root justify-center min-h-screen bg-orange-600 flex justify-center items-center py-20">
            <div className="container mx-auto p-12 bg-gray-100 rounded-xl">
            {
                artefacts.map(artefact =>
                    <div className="bg-white rounded mb-8 shadow-lg px-4 py-4">

                            <h2 className="text-xl font-medium text-gray-700">
                                groupId : {artefact.groupId}
                            </h2>
                            <h2 className="text-xl font-medium text-gray-700">
                                artifactId : {artefact.artifactId}
                            </h2>
                            <h2 className="text-xl font-medium text-gray-700">
                                version : {artefact.version}
                            </h2>

                            <button className="mt-12 w-full text-center bg-yellow-400 py-2 rounded-lg">
                                <Link to={`/home`} >Revenir sur la page d'accueil</Link>
                            </button>
                    </div>
                )
            }
                <div className="mb-8 shadow-lg">
                    <CanvasJSChart options = {options} />
                </div>


            <div className="content-around sm:grid sm:grid-cols-2 lg:grid-cols-2 gap-4 space-y-4 sm:space-y-0 bg-center">
                {
                    sources.map(source => source.map(code =>
                        <div className="bg-slate-800 rounded px-3 py-3 mb-5">
                            {
                                code.map(lines => lines.map(line =>
                                        <p className="text-left text-white font-mono text-base">
                                            {line}
                                        </p>
                                    )
                                )
                            }
                        </div>
                    )
                    )
                }
            </div>
        </div>
            </div>
            </div>
    )
}

export default Artefact