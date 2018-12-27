FROM golang:alpine as builder

RUN apk update && apk upgrade && apk add --no-cache curl git
RUN go get github.com/prep/pubsubc

FROM google/cloud-sdk:alpine

COPY --from=builder /go/bin/pubsubc   /usr/bin

RUN apk --update add openjdk8-jre netcat-openbsd && gcloud components install beta pubsub-emulator

EXPOSE 8086

ENTRYPOINT ["sh", "/run.sh"]