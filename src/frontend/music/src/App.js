import React, {Component} from 'react';
import './App.css';
import ReactJkMusicPlayer from "react-jinke-music-player";
import ReactTable from "react-table";
import "react-table/react-table.css";
import "react-jinke-music-player/assets/index.css";

class App extends Component {

    constructor(props) {
        super(props);
        this.state = {
            loadingSongs: false,
            loadedSongs: false,
            playlist: []
        };
    }


    componentDidMount() {
        this.listSongs();
    }

    listSongs = () => {
        this.setState({
            loadingSongs: true,
            loadedSongs: false
        });
        fetch("./track/")
            .then(res => res.json())
            .then(
                (result) => {
                    this.setState({
                        loadingSongs: false,
                        loadedSongs: true,
                        songs: result
                    });
                },
                // Note: it's important to handle errors here
                // instead of a catch() block so that we don't swallow
                // exceptions from actual bugs in components.
                (error) => {
                    this.setState({
                        loadingSongs: false,
                        loadedSongs: true,
                        error
                    });
                }
            );
    };

    addToPlayList = (song) => {
        if (song) {
            let playlist = Object.assign([], this.state.playlist);
            playlist.push({
                name: song.title,
                singer: song.artist,
                musicSrc: window.location.origin + "/media" + song.location
            });

            this.setState({
                playlist: playlist
            });
        }
    };

    onAudioListsChange = (currentPlayId, audioLists, audioInfo) => {
        this.setState({
            playlist: audioLists
        });
    };

    defaultFilterMethod = (filter, row, column) => {
        const id = filter.pivotId || filter.id;
        return row[id] !== undefined ? String(row[id]).toLowerCase().includes(filter.value.toLowerCase()) : true
    };

    render() {

        // const audioLists = [
        //     {
        //         name: "02 - The Wreck Of The Edmund Fitzgerald.flac",
        //         singer: "Gordon",
        //         cover: "//cdn.lijinke.cn/nande.jpg",
        //         musicSrc: "./song/download"
        //     }];

        const {error, loadedSongs, songs} = this.state;

        return (
            <div>
                <ReactTable
                    data={songs}
                    pivotBy={[
                        // 'artist'
                        // 'artist', 'album'
                        // 'album'
                    ]}
                    getTdProps={(state, rowInfo, column, instance) => {
                        return {
                            onClick: (e, handleOriginal) => {
                                this.addToPlayList(rowInfo.original);
                                // IMPORTANT! React-Table uses onClick internally to trigger
                                // events like expanding SubComponents and pivots.
                                // By default a custom 'onClick' handler will override this functionality.
                                // If you want to fire the original onClick handler, call the
                                // 'handleOriginal' function.
                                if (handleOriginal) {
                                    handleOriginal();
                                }
                            }
                        };
                    }}
                    columns={[
                        {
                            Header: "D",
                            accessor: "discNumber",
                            maxWidth: 25
                        },
                        {
                            Header: "T",
                            accessor: "trackNumber",
                            maxWidth: 50
                        },
                        {
                            Header: "Title",
                            accessor: "title",
                            maxWidth: 175
                        },
                        {
                            Header: "Artist",
                            accessor: "artist",
                            maxWidth: 175
                        },
                        {
                            Header: "Album",
                            accessor: "album",
                            maxWidth: 175
                        },
                        {
                            Header: "Genre",
                            accessor: "genre",
                            maxWidth: 175
                        },
                        {
                            Header: "Plays",
                            accessor: "playCounter",
                            maxWidth: 50
                        },
                        {
                            Header: "Rating",
                            accessor: "rating",
                            maxWidth: 50
                        }
                    ]}
                    defaultPageSize={1000}
                    minRows={0}
                    noDataText={loadedSongs ? (error ? error : "No songs in database.") : "Loading songs..."}
                    filterable={true}
                    className="-striped -highlight"
                    defaultFilterMethod={this.defaultFilterMethod}

                />
                <ReactJkMusicPlayer
                    audioLists={this.state.playlist}
                    autoPlay={true}
                    mode={"full"}
                    toggleMode={false}
                    preload={false}
                    onAudioListsChange={this.onAudioListsChange}
                />
            </div>

        );
    }
}

export default App;
