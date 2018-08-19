#!/bin/sh
set -ue
SOURCE=$1
cd code
mvn clean compile package
mv target/ffmpeg-pipe-1.0-SNAPSHOT.jar ../ffmpeg-pipe.jar
cd ..
docker run --rm -it --volume $(pwd):/opt/ffmpeg-pipe:cached -p 127.0.0.1:8090:8090 -p 127.0.0.1:8091:8091  ffmpeg-pipe-image /bin/bash /opt/ffmpeg-pipe/script.sh "$SOURCE"
