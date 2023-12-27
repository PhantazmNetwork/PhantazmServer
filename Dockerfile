FROM alpine:3.19.0 AS java_base
LABEL authors="steank"

RUN apk --no-cache add openjdk17
RUN addgroup -S container -g 1000
RUN adduser -S container -G container -u 1000

USER my_user

FROM java_base AS server_base
LABEL authors="steank"

USER root
RUN apk --no-cache add git curl unzip
USER container

FROM mariadb:11.2.2 AS mariadb