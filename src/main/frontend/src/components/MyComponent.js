import React from 'react'
import APIService from './APIService'
import './MyComponent.css';

class MyComponent extends React.Component {

    constructor(props) {
        super(props)
        this.state = {
            artefacts: []
        }
    }

    componentDidMount(){
        APIService.getArtefacts().then((data) => {
            this.setState({ artefacts: data});
            console.log(this.state.artefacts);
        })
            .catch(function (ex) {
                console.log('Response parsing failed. Error: ', ex);
            });
    }

    render() {
        return (
            <div>
                <h2 className="text-center">Artefacts</h2>

                    {
                        this.state.artefacts.map(artefact =>
                            <div className="Div">
                                <p>{artefact.groupId}</p>
                                <p>{artefact.artefactId}</p>
                            </div>
                        )
                    }

            </div>
        )
    }
}

export default MyComponent