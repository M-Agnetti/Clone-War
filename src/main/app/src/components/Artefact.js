import React, {useEffect, useState} from 'react'
import {Link, useParams} from "react-router-dom";

const Artefact = () => {

    const [artefacts, setArtefact] = useState([]);
    const params = useParams();


    useEffect(()=>{
        fetch('http://localhost:8080/artefact/' + params.id,{
            method: 'get',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json',
            },
            'credentials': 'same-origin'
            }).then(res => res.json())
            .then(data => {
                console.log(data);
                setArtefact(data);
            })
            .catch(err => console.log(err))
    },[]);

    return (
        <div>
            {
                artefacts.map(artefact =>
                    <div className="bg-white rounded w-6/12">
                        <div className="shadow-lg hover:shadow-xl transform transition duration-500 hover:scale-105 p-6 rounded">

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
                                <Link to={`/`} >Revenir sur la page d'accueil</Link>
                            </button>
                        </div>
                    </div>

                )
            }
        </div>
    )
}

export default Artefact