FROM ubuntu:18.04

# can be used for cache-busting
ENV REFRESHED_AT 2018-05-09

# avoid regenerating initrd since it's not used anyways
ENV INITRD No
# don't interactively prompt during package installs
ENV DEBIAN_FRONTEND noninteractive

RUN \
        apt-get update && \
        apt-get dist-upgrade -y && \
        apt-get install -y default-jre ffmpeg && \
	apt-get clean && rm -rf /var/cache/apt/archives/* /var/lib/apt/lists/*

# prevents the "dumb terminal when using docker exec" problem
RUN echo "export TERM=xterm" >> /root/.bashrc

EXPOSE 8090/tcp 8091/tcp
