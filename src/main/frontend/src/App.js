import {useRef, useState} from "react";
import logo from './logo.svg';
import './App.css';
import {Artefact} from './interfaces.ts';
import MyComponent from './components/MyComponent.js';

function addArtefact({name}){
}

function display(){
    let DATA:Artefact[] = [
        {name:"art1"},
        {name:"art2"},
        {name:"art2"}
        ];

}

function App() {
    const baseURL = "http://localhost:8080/api";
    const get_id = useRef(null);
  const get_title = useRef(null);

  const [getResult, setGetResult] = useState(null);

  const formatResponse = (res) => {
    return JSON.stringify(res, null, 2);
  }

    let DATA:Artefact[] = [
            {name:"art1"},
            {name:"art2"},
            {name:"art2"}
            ];

  return (

    <div>
        <h1 class="flex font-semibold text-purple-600">
          Hello <span class="animate-waving-hand">👋🏻</span>, LogRocket Blog
        </h1>

        {DATA.map(elem => (

            <MyComponent name={elem.name} />
        ))}
    </div>

  );
}

export default App;
