import React, {Component} from 'react';
import './App.css';
import ReactAudioPlayer from 'react-audio-player';

class PlayerComponent extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (this.props.currentSongSrc() ?
            <ReactAudioPlayer
                controls
                src={this.props.currentSongSrc()}
                autoplay
                onEnded={() => this.props.onSongEnd(this.audioPlayer.audioEl)}
                ref={(element) => { this.audioPlayer = element; }}
                style={{"width":"100%"}}
            >
                Your browser does not support the
                <code>audio</code> element.
            </ReactAudioPlayer> : null
        )
    }
}

export default PlayerComponent;