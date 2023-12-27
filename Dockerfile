FROM alpine:3.19.0 AS java_base
LABEL authors="steank"

RUN apk --no-cache add openjdk17

FROM java_base AS server_base
LABEL authors="steank"

RUN apk --no-cache add git curl unzip

FROM mariadb:11.2.2 AS mariadb