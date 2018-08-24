#!/bin/sh
set -ue
SOURCE=$1
cd code
mvn clean compile package
mv target/ffmpeg-pipe-twitch-1.0-SNAPSHOT.jar ../ffmpeg-pipe-twitch.jar
cd ..
docker run --rm -it --volume $(pwd):/opt/ffmpeg-pipe-twitch:cached ffmpeg-pipe-image /bin/bash /opt/ffmpeg-pipe-twitch/script.sh "$SOURCE"
