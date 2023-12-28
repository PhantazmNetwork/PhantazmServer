#!/bin/sh

user_id=${DOCKER_USER_ID:-"1000"}
group_id=${DOCKER_GROUP_ID:-"1000"}

echo "Creating user and group with UID ${user_id} and GID ${group_id}"
addgroup -g "${group_id}" -S container
adduser -G container -S -u "${user_id}"

su -l container