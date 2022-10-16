import logo from './logo.svg';
import './App.css';
import Artefact from './interfaces.ts';
import MyComponent from './components/MyComponent.js';


function display(){
    let DATA:Artefact[] = [
        {name:"art1"},
        {name:"art2"},
        {name:"art2"}
        ];

        DATA.map(elem => <p >{`artefact : ${elem.name} `}</p>);
}

function App() {

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />


      </header>

      <div className="artefactApp">
       {display()}
      </div>
    </div>



  );
}

export default App;
