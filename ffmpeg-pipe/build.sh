#!/bin/sh
set -ue
cd docker
docker build -t ffmpeg-pipe-image .
