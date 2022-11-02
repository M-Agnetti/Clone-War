import React from 'react'
import APIService from './APIService'
import './MyComponent.css';

class Artefact extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            artefacts: []
        };
        this.button = false;
    }
/*
    componentDidMount(){
        APIService.getArtefacts().then((data) => {
            this.setState({ artefacts: data});
            console.log(this.state.artefacts);
        })
            .catch(function (ex) {
                console.log('Response parsing failed. Error: ', ex);
            });
    }
*/
    render() {
        return (
            <div>
                <h2>hello</h2>
            </div>
        )
    }
}

export default Artefact