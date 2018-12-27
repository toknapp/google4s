FROM upvest/scala-sbt-docker-k8:stable

RUN mkdir /google4s
WORKDIR /google4s

ADD project project
ADD build.sbt .
RUN sbt update

ADD core core
ADD gkms gkms
ADD gpubsub gpubsub
ADD gstorage gstorage

RUN sbt compile it:compile

ENTRYPOINT ["sbt"]
