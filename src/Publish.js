import React from 'react';
import Axios from 'axios';

class Publish extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            workspaceName: "eee",
            datastoreName: "",
            layerName: "",
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChange(event) {
        this.setState({
            workspaceName: document.getElementById("publish_workspace").value,
            datastoreName: document.getElementById("publish_datastore").value,
            layerName: document.getElementById("publish_layer").value,
        });
    }

    // workspaceName: "testWS",
    // layerName: "testFT",
    // datastoreName: "testDS",
    // featuretypeName: "testFT"
    handleSubmit(event) {
        Axios.post('http://localhost:8899/bcgis/mapservice/wms/publish', {
            workspaceName: document.getElementById("publish_workspace").value,
            datastoreName: document.getElementById("publish_datastore").value,
            featuretypeName: document.getElementById("publish_layer").value          
        })
            .then(function (response) {
                // ReactDOM.render(<MapList response={JSON.stringify(response)}/>, document.getElementById('map'))
                console.log(response);
            })
            .catch(function (error) {
                console.log(error);
            });
        alert(this.state.workspaceName + "->" + this.state.datastoreName + "->" + this.state.layerName);
        event.preventDefault();
    }

    render() {
        return (
            <form id="publish_form" onSubmit={this.handleSubmit}>
                <label>工作空间名称：</label> <input type="text" id="publish_workspace" onChange={this.handleChange} value="testWS"></input> <br />
                <label>数据存储名称：</label> <input type="text" id="publish_datastore" onChange={this.handleChange} value="testDS"></input> <br />
                <label>图层名称：</label> <input type="text" id="publish_layer" onChange={this.handleChange} value="testFT"></input> <br />
                <input type="submit" value="发布" />
            </form>
        );
    }
}

export default Publish;