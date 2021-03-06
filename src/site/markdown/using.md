## Connecting
Create a connection to MPD using the builder.

Using default values of localhost port 6600

```
MPD mpd = new MPD.Builder().build();
```

or build to your environment

```
MPD mpd = new MPD.Builder()
        .server("yourserver")
        .port(yourport)
        .password(yourpassword)
        .build();
```

Almost everything you'll need will be gotten from this object.

The connection remains open for the life of the MPD object so take 
care not to create new MPD objects each time you want to do something.
Call close on the object when you are done.

##Searching
Searching for songs is done via the SongSearcher class.

```
mpd.getSongSearcher();
```

##Querying
MPD database access is accomplished using the appropriate access object from the MPDMusicDatabase.

Get the database from MPD.

```
mpd.getMusicDatabase();
```

There is a database for the following:

- Songs
- Albums
- Artists
- Genres
- Years
- Playlists
- Files


##Logging
[slf4j](http://www.slf4j.org/) is used for logging allowing you to use any compatible logging framework.
The following examples use [logback](http://logback.qos.ch/).

Lots of debugging information can be obtained from debug level logging.

```
...
<root level="debug">
    <appender-ref ref="STDOUT"/>
</root>
...
```

##Monitoring
MPDStandAloneMonitor to monitor events
bitrate can be turned off since chatty

##Server status
load the server status by getting the status from MPD.  It has an default expiry interval of 5 seconds, this can be
overridden by setting the interval using setExpiryInterval, use 0 to always call the server for each method.  Updates
can be force using forceUpdate

##Server statistics
load the server statistics by getting the status from MPD.  It has an default expiry interval of 60 seconds, this can be
overridden by setting the interval using setExpiryInterval, use 0 to always call the server for each method.  Updates
can be force using forceUpdate
