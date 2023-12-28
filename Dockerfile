FROM alpine:3.19.0 AS java_base
LABEL authors="steank"

ARG uid=1000
ARG gid=1000

ENV PHANTAZM_UID_D $uid
ENV PHANTAZM_GID_D $gid

RUN apk --no-cache add openjdk17
RUN addgroup -g "${PHANTAZM_GID_D}" -S container
RUN adduser -S -G container -u "${PHANTAZM_UID_D}" container

USER container

FROM java_base AS server_base
LABEL authors="steank"

USER root
RUN apk --no-cache add git curl unzip
USER container

FROM mariadb:11.2.2 AS mariadb