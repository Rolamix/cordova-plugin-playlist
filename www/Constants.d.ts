/**
 * Enum describing the possible errors that may come from the plugins
 */
export declare enum RmxAudioErrorType {
    RMXERR_NONE_ACTIVE = 0,
    RMXERR_ABORTED = 1,
    RMXERR_NETWORK = 2,
    RMXERR_DECODE = 3,
    RMXERR_NONE_SUPPORTED = 4
}
/**
 * String descriptions corresponding to the RmxAudioErrorType values
 */
export declare const RmxAudioErrorTypeDescriptions: string[];
/**
 * Enumeration of all status messages raised by the plugin.
 * NONE, REGISTER and INIT are structural and probably not useful to you.
 */
export declare enum RmxAudioStatusMessage {
    /**
     * The starting state of the plugin. You will never see this value;
     * it changes before the callbacks are even registered to report changes to this value.
     */
    RMXSTATUS_NONE = 0,
    /**
     * Raised when the plugin registers the callback handler for onStatus callbacks.
     * You will probably not be able to see this (nor do you need to).
     */
    RMXSTATUS_REGISTER = 1,
    /**
     * Reserved for future use
     */
    RMXSTATUS_INIT = 2,
    /**
     * Indicates an error is reported in the 'value' field.
     */
    RMXSTATUS_ERROR = 5,
    /**
     * The reported track is being loaded by the player
     */
    RMXSTATUS_LOADING = 10,
    /**
     * The reported track is able to begin playback
     */
    RMXSTATUS_CANPLAY = 11,
    /**
     * The reported track has loaded 100% of the file (either from disc or network)
     */
    RMXSTATUS_LOADED = 15,
    /**
     * (iOS only): Playback has stalled due to insufficient network
     */
    RMXSTATUS_STALLED = 20,
    /**
     * Reports an update in the reported track's buffering status
     */
    RMXSTATUS_BUFFERING = 25,
    /**
     * The reported track has started (or resumed) playing
     */
    RMXSTATUS_PLAYING = 30,
    /**
     * The reported track has been paused, either by the user or by the system.
     * (iOS only): This value is raised when MP3's are malformed (but still playable).
     * These require the user to explicitly press play again. This can be worked
     * around and is on the TODO list.
     */
    RMXSTATUS_PAUSE = 35,
    /**
     * Reports a change in the reported track's playback position.
     */
    RMXSTATUS_PLAYBACK_POSITION = 40,
    /**
     * The reported track has seeked.
     * On Android, only the plugin consumer can generate this (Notification controls on Android do not include a seek bar).
     * On iOS, the Command Center includes a seek bar so this will be reported when the user has seeked via Command Center.
     */
    RMXSTATUS_SEEK = 45,
    /**
     * The reported track has completed playback.
     */
    RMXSTATUS_COMPLETED = 50,
    /**
     * The reported track's duration has changed. This is raised once, when duration is updated for the first time.
     * For streams, this value is never reported.
     */
    RMXSTATUS_DURATION = 55,
    /**
     * All playback has stopped, probably because the plugin is shutting down.
     */
    RMXSTATUS_STOPPED = 60,
    /**
     * The playlist has skipped forward to the next track.
     * On both Android and iOS, this will be raised if the notification controls/Command Center were used to skip.
     * It is unlikely you need to consume this event: RMXSTATUS_TRACK_CHANGED is also reported when this occurs,
     * so you can generalize your track change handling in one place.
     */
    RMX_STATUS_SKIP_FORWARD = 90,
    /**
     * The playlist has skipped back to the previous track.
     * On both Android and iOS, this will be raised if the notification controls/Command Center were used to skip.
     * It is unlikely you need to consume this event: RMXSTATUS_TRACK_CHANGED is also reported when this occurs,
     * so you can generalize your track change handling in one place.
     */
    RMX_STATUS_SKIP_BACK = 95,
    /**
     * Reported when the current track has changed in the native player. This event contains full data about
     * the new track, including the index and the actual track itself. The type of the 'value' field in this case
     * is OnStatusTrackChangedData.
     */
    RMXSTATUS_TRACK_CHANGED = 100,
    /**
     * The entire playlist has completed playback.
     * After this event has been raised, the current item is set to null and the current index to -1.
     */
    RMXSTATUS_PLAYLIST_COMPLETED = 105,
    /**
     * An item has been added to the playlist. For the setPlaylistItems and addAllItems methods, this status is
     * raised once for every track in the collection.
     */
    RMXSTATUS_ITEM_ADDED = 110,
    /**
     * An item has been removed from the playlist. For the removeItems and clearAllItems methods, this status is
     * raised once for every track that was removed.
     */
    RMXSTATUS_ITEM_REMOVED = 115,
    /**
     * All items have been removed from the playlist
     */
    RMXSTATUS_PLAYLIST_CLEARED = 120,
    /**
     * Just for testing.. you don't need this and in fact can never receive it, the plugin is destroyed before it can be raised.
     */
    RMXSTATUS_VIEWDISAPPEAR = 200
}
/**
 * String descriptions corresponding to the RmxAudioStatusMessage values
 */
export declare const RmxAudioStatusMessageDescriptions: {
    0: string;
    1: string;
    2: string;
    5: string;
    10: string;
    11: string;
    15: string;
    20: string;
    25: string;
    30: string;
    35: string;
    40: string;
    45: string;
    50: string;
    55: string;
    60: string;
    90: string;
    95: string;
    100: string;
    105: string;
    110: string;
    115: string;
    120: string;
    200: string;
};
