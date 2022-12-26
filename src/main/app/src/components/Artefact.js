import React, {useEffect, useState} from 'react'
import {Link, useParams} from "react-router-dom";
import CanvasJSReact from './canvasjs.react';

const CanvasJSChart = CanvasJSReact.CanvasJSChart;

const Artefact = () => {

    const [artefacts, setArtefact] = useState([]);
    const [sources, setSources] = useState([]);
    const [options, setOptions] = useState({
        animationEnabled: true,
        theme: "light2",
        title: { text: "Highest scores" },
        axisX:{ title:"Artifacts",},
        axisY:{ title:"Pourcentage",},
        data: [
            {
                type: "column",
                dataPoints: []
            }
        ]
    });
    const params = useParams();

    async function fetchs() {
        await fetch('http://localhost:8080/artefact/' + params.id,{
            method: 'get',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
            },
            'credentials': 'same-origin'
        })
            .then(res => res.json())
            .then(data => {
                setArtefact(data);
            })
            .catch(err => console.log(err));


        await fetch('http://localhost:8080/clones/' + params.id,{
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
            })
            .catch(err => console.log(err));

        await fetch('http://localhost:8080/scores/' + params.id,{
            method: 'get',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
            },
            'credentials': 'same-origin'
        })
            .then(res => res.json())
            .then(data => {
                let points = [];
                for (let i = 0; i < data.length ; i++) {
                    points.push({label: data.at(i).artefact.name, y:data.at(i).score});
                }
                setOptions({
                    animationEnabled: true,
                    theme: "light2",
                    title: { text: "Highest scores" },
                    axisX:{ title:"Artifacts",},
                    axisY:{ title:"Pourcentage",},
                    data: [
                        {
                            type: "column",
                            dataPoints: points
                        }
                    ]
                });
            })
            .catch(err => console.log(err));
    }


    useEffect(()=> {
        fetchs();
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

                <div className="mb-8 shadow-lg mt-5">
                    <CanvasJSChart options = {options} />
                </div>

            <div className="content-around sm:grid sm:grid-cols-2 lg:grid-cols-2 gap-4 space-y-4 sm:space-y-0 bg-center">
                {
                    sources.map(source => source.map(code =>
                        <div className="bg-slate-800 rounded px-3 py-3 mb-5 shadow-lg">
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