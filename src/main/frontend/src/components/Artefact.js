import React, {useEffect, useState} from 'react'
import {Link, useParams} from "react-router-dom";

const Artefact = () => {

    const [artefacts, setArtefact] = useState([]);
    const params = useParams();

    function fetchAnalysis() {
        fetch('http://localhost:8080/index/' + params.id,{
            method: 'put',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
            },
            'credentials': 'same-origin'
        }).then(res => res.json())
      .catch(err => console.log(err))
    }

    useEffect(()=>{
        fetch('http://localhost:8080/artefact/' + params.id,{
            method: 'get',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
            },
            'credentials': 'same-origin'
            }).then(res => res.json())
            .then(data => setArtefact(data))
            .catch(err => console.log(err))
    },[]);

    return (
        <div>
            {
                artefacts.map(artefact =>
                    <div className="bg-white rounded">
                        <div className="shadow-lg hover:shadow-xl transform transition duration-500 hover:scale-105 p-6 rounded">

                            <span className="text-blue-500 block mb-5">{artefact.artefactId}</span>
                            <h2 className="text-xl font-medium text-gray-700">{new Date(artefact.addDate).toLocaleDateString("fr")}</h2>

                            <Link to={`/`} >Revenir sur la page d'accueil</Link>

                            <button className="mt-12 w-full text-center bg-yellow-400 py-2 rounded-lg"
                                onClick={fetchAnalysis}>
                                Launch artefact analysis
                            </button>
                        </div>
                    </div>

                )
            }
        </div>
    )
}

export default Artefact