import {Link} from "react-router-dom";
import React from "react";

function Animation() {

    return (
        <div className="relative grid place-items-center h-screen bg-[url('/public/hacker2.jpg')] bg-cover bg-no-repeat bg-center">
            <div className="h-3/5 aspect-square bg-[url('/public/hacker2.png')] bg-cover bg-no-repeat bg-center animate-flip"></div>


            <div className="absolute bottom-0 w-full bg-black inline-flex" >
                <p className="text-white">Authors : SAIDI Soumia-AGNETTI Marc</p>
                <div>
                    <button className="right-0 bg-blue-500 hover:bg-blue-400 text-white font-bold py-2 px-4 border-b-4 border-blue-700 hover:border-blue-500 rounded">
                        <Link to={`/home`} > Enter </Link>
                    </button>
                </div>

            </div>


        </div>
    );
}

export default Animation;