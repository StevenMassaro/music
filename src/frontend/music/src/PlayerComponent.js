import React, {Component} from 'react';
import './App.css';

class PlayerComponent extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <audio
                controls
                src={this.props.currentSongSrc()}
                preload={auto}
            >
                Your browser does not support the
                <code>audio</code> element.
            </audio>
        )
    }
}

export default PlayerComponent;