import React, {Component} from 'react';
import './App.css';
import SplitPane from "react-split-pane";
import SongListComponent from "./SongListComponent";
import UpNextComponent from "./UpNextComponent";
import PlayerComponent from "./PlayerComponent";

class App extends Component {

    constructor(props) {
        super(props);
        this.state = {
            loadingSongs: false,
            loadedSongs: false,
        };
    }

    addToPlaylist = (song) => {
        if (song) {
            let upNext = Object.assign([], this.state.upNext);
            // upNext.push({
            //     name: song.title,
            //     singer: song.artist,
            //     musicSrc: window.location.origin + "/media" + song.location
            // });
            upNext.push(song);

            this.setState({
                upNext: upNext
            });
        }
    };

    defaultFilterMethod = (filter, row, column) => {
        const id = filter.pivotId || filter.id;
        return row[id] !== undefined ? String(row[id]).toLowerCase().includes(filter.value.toLowerCase()) : true
    };

    getCurrentSongSrc = () => {
        return this.state.upNext ? window.location.origin + "/media" + this.state.upNext[0].location : 'fart';
    };

    onCurrentSongEnd = (audioElement) => {
        let upNext = Object.assign([], this.state.upNext);
        upNext.shift(); // remove current song
        this.setState({
            upNext: upNext
        }, () => audioElement.play()); // callback to start playing the next song
    };

    render() {
        return (
            <div>
                <SplitPane split="horizontal" defaultSize="8%">
                    <PlayerComponent
                        currentSongSrc={this.getCurrentSongSrc}
                        onSongEnd={this.onCurrentSongEnd}
                    />
                    <div>
                        <SplitPane split="vertical" defaultSize="15%">
                            <div>navigation</div>
                            <SplitPane split="vertical" defaultSize="70%">
                                <div>
                                    <SongListComponent
                                        addToPlaylist={this.addToPlaylist}
                                        defaultFilterMethod={this.defaultFilterMethod}
                                    />
                                </div>
                                <div>
                                    <UpNextComponent
                                        upNext={this.state.upNext}
                                        defaultFilterMethod={this.defaultFilterMethod}
                                    />
                                </div>
                            </SplitPane>
                        </SplitPane>
                    </div>
                </SplitPane>
            </div>
        );
    }
}

export default App;
