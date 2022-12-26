import {Link} from "react-router-dom";
import React from "react";

function Animation() {

    return (
        <div className="relative grid place-items-center h-screen bg-[url('/public/hacker2.jpg')] bg-cover bg-no-repeat bg-center">
            <div className="h-3/5 aspect-square bg-[url('/public/hacker2.png')] bg-cover bg-no-repeat bg-center animate-flip"></div>


                    <button className="absolute top-5 right-5 inline-flex bg-blue-500 hover:bg-blue-400 text-white font-bold text-xl py-4 px-5 border-b-4 border-blue-700 hover:border-blue-500 rounded">
                        <Link to={`/home`} > Enter </Link>
                    </button>

        </div>
    );
}

export default Animation;