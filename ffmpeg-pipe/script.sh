#!/bin/bash
set -ue
SOURCE=$1
cd /opt/ffmpeg-pipe
ffserver -f ffserver.conf &
sleep 2
java -jar ffmpeg-pipe.jar "$SOURCE"
