#!/bin/bash
set -ue
SOURCE=$1
cd /opt/ffmpeg-pipe-twitch
java -jar ffmpeg-pipe-twitch.jar "$SOURCE"
