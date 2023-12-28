FROM alpine:3.19.0 AS java_base
LABEL authors="steank"

ARG DOCKER_UID=1000
ARG DOCKER_GID=1000

ENV DOCKER_USER_ID=$DOCKER_UID
ENV DOCKER_GROUP_ID=$DOCKER_GID

RUN apk --no-cache add openjdk17
RUN addgroup -g "${DOCKER_GROUP_ID}" -S container
RUN adduser -S -G container -u "${DOCKER_USER_ID}" container

USER container

FROM java_base AS server_base
LABEL authors="steank"

USER root
RUN apk --no-cache add git curl unzip
USER container

FROM mariadb:11.2.2 AS mariadb