# Music (API)

A Spring Boot Java web application, which provides useful APIs for music management and consumption.

This is intended to be the single source of truth when using the associated music clients.

I built this after not being able to find another music program that satisfied all of my requirements: ability to allow syncing to other devices (including other computers), single source metadata management (including on the fly play count updates), and no requirement to run on Windows (ahem MediaMonkey).

## Clients:
- Docker: https://hub.docker.com/r/stevenmassaro/music-api
- Web client: https://github.com/StevenMassaro/music-client
- Android app: URL TBD (currently not on GitHub)

## Features:
- ID3 metadata management
- individual device profiles
- play count tracking (per profile)
- skip count tracking (per profile)
- import existing play count data from MediaMonkey database
- conversion of music to new format (as specified in the device profile), via [FFmpeg](https://ffmpeg.org/)
- (basic) websocket implementation for live updates of long running tasks
- "smart" playlists which update on the fly as track metadata changes
- metadata changes are cached, and written to disk at a later time (when requested) - to avoid unnecessary hard drive spin ups as metadata is modified
- local folder monitoring, where newly added files are automatically uploaded

## OpenAPI specifications

[http://localhost:8080/Music/v2/api-docs](http://localhost:8080/Music/v2/api-docs)

[http://localhost:8080/Music/swagger-ui/index.html](http://localhost:8080/Music/swagger-ui/index.html)

## Reverse proxy via nginx
```
location /Music {
	# CORS requires unauthenticated OPTIONS calls to succeed
	limit_except OPTIONS {
		auth_basic "Restricted content";
		auth_basic_user_file .htpasswd;
	}
	include /config/nginx/proxy.conf;
	proxy_pass http://host:port;
	# WebSocket connection requires this
	proxy_set_header Upgrade $http_upgrade;
	proxy_set_header Connection "upgrade";
}
```

## To-do
- cache album art changes (currently album art changes get applied directly to disk)
- generate swagger/OpenAPI specifications