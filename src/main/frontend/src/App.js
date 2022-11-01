import logo from './logo.svg';
import './App.css';
import PostList from "./components/PostList";
import MyComponent from "./components/MyComponent";

function App() {

  return (

    <div className="App">
        <form>
            <input type="file" accept=".jar" required />
            <input type="file" accept=".jar" required />
            <button type="submit">Ajouter</button>
        </form>
        <MyComponent/>
    </div>

  );
}

export default App;
