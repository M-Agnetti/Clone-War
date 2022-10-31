import React from 'react'
import APIService from './APIService'

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
                <table className="table table-striped">
                    <thead>
                    <tr>
                        <th>GroupId</th>
                        <th>ArtefactId</th>
                    </tr>
                    </thead>
                    <tbody>
                    {
                        this.state.artefacts.map(artefact =>
                            <tr>
                                <td>{artefact.groupId}</td>
                                <td>{artefact.artefactId}</td>
                            </tr>
                        )
                    }
                    </tbody>
                </table>
            </div>
        )
    }
}

export default MyComponent