import React, { Component } from 'react';
import SwaggerUI from "swagger-ui-react"
import "swagger-ui-react/swagger-ui.css"

export default class SwaggerOpenApi extends Component {
    render() {
        return (
            <div>
                < SwaggerUI url={"http://localhost:8080/swagger.json"} />
            </div>
        );
    }
}