#!/bin/sh

log_file="$(pwd)/setup_log.txt"
/dev/null > "${log_file}"

log_error() {
  printf "\033[31;1m[ERROR]\033[0m %s\n" "$1"
  echo "[ERROR] $1" >> "${log_file}"
}

log_warning() {
  printf "\033[33;1m[WARNING]\033[0m %s\n" "$1"
  echo "[WARNING] $1" >> "${log_file}"
}

log_info() {
  printf "\033[32;1m[INFO]\033[0m %s\n" "$1"
  echo "[INFO] $1" >> "${log_file}"
}

log_trace() {
  echo "[TRACE] $1" >> "${log_file}"
}

# This script file is expected to be run through a Docker container only.
if [ "${PHANTAZM_IS_DOCKER_CONTAINER}" != "true" ]; then
  log_error "This script file can only be executed in Phantazm's development container!"
  exit 1
fi

log_trace "Waiting for some input from stdin..."

# We wait to receive some input from stdin.
# That way, we don't send anything to stdout before we've had a process attach to this container.
read -r input
code="$?"

current_user="$(whoami)"
current_uid="$(id -u "${current_user}")"
current_gid="$(id -g "${current_user}")"
log_info "Running as user '$current_user' with UID=$current_uid and GID=$current_gid"

log_trace "Got string from 'read': ${input}"
log_trace "'read' operation exit code ${code}"

# Use case here instead of equality comparison because thanks to WINDOWS,
# $input can end up equaling start\r
if ! case "${input}" in start*) true;; esac; then
  log_error "Invalid input; expected 'start', got '${input}'"
  exit 1
fi

log_info "Starting setup script..."

require() {
  name="$1"

  eval "result=\$$name"
  if [ -z "${result}" ]; then
    log_error "Missing required environment variable ${name}!"
    false
  fi

  echo "${result}"
}

# Exit if we're missing any of these environment variables
gradle_args=$(require "PHANTAZM_GRADLE_ARGS") || exit 1
server_jvm_args=$(require "PHANTAZM_SERVER_JVM_ARGS") || exit 1
server_args=$(require "PHANTAZM_SERVER_ARGS") || exit 1
server_file=$(require "PHANTAZM_SERVER_FILE") || exit 1

gdrive_dl() {
  gd_filename="$1"
  gd_fileid="$2"

  if ! curl -L -s -o "${gd_filename}" "https://drive.google.com/uc?export=download&id=${gd_fileid}"; then
    log_error "Failed to download map to ${gd_filename}, it will not be joinable in-game!"
    false
  fi
}

clean_temp() {
  if [ -d "./tmp" ]; then
    log_trace "./tmp existed; removing it"
    rm -rf "./tmp"
  fi
}

clone_repository() {
  if ! git clone -q "$1" ./run/server-1; then
    log_error "Failed to clone from configuration repository $1"
    false
  fi
}

dl_to_if_not_exists() {
  filename="$1"
  fileid="$2"
  target_dir="$3"
  output_dir="$4"

  if [ ! -d "${target_dir}/${output_dir}" ]; then
    mkdir -p "./tmp"

    log_info "Downloading world file ${filename}..."
    if gdrive_dl "./tmp/${filename}" "$fileid"; then
      if ! unzip -qq "./tmp/${filename}" -d "$target_dir"; then
        log_warning "Error unzipping world file ${filename} to ${target_dir}."
        log_warning "Ensure that the output directory is empty and the process has permission to access it."
        log_warning "This map will likely not be joinable in-game!"
      fi
    fi
  fi
}

# Create run folder
mkdir -p "./run"

# First-time setup
if [ ! -d "./run/server-1" ]; then
  log_info "Doing first-time setup..."

  mkdir -p "./run/server-1"
  if [ -n "${PHANTAZM_CONF_REPO_URL}" ]; then
    log_info "Cloning into configuration repository as defined by PHANTAZM_CONF_REPO_URL."
    if ! clone_repository "${PHANTAZM_CONF_REPO_URL}"; then
      log_error "Check that your .override.env file is configured correctly!"
      exit 1
    fi
  else
    printf "You have not provided a configuration repository!\nIf this is your first time launching Phantazm, you will \
have to provide one. It looks like this:\n\nhttps://[git-username]:[git-password-or-access-token]@[repository-url]\n\n\
For example, if you are connecting the GitHub repository PhantazmNetwork/Configuration and your\nusername is steanky, \
your configuration repository would look like this:\n\n\
https://steanky:[access-token-redacted]@github.com/PhantazmNetwork/Configuration\n\nYou can also define the repository \
by setting the PHANTAZM_CONF_REPO_URL environment variable in\n.override.env.\n\n"

    # Keep asking for the repository URL until we clone successfully.
    while true ; do
      printf "Enter your configuration repository URL on the line below, then hit ENTER. Alternatively,\ntype cancel \
to quit.\n"

      read -r repository_url
      if [ "${repository_url}" = "cancel" ]; then
        log_info "Quitting..."
        exit 0
      fi

      if clone_repository "${repository_url}"; then
        if ! echo "PHANTAZM_CONF_REPO_URL='${repository_url}'" >> ".override.env"; then
          log_error "Cloned the repository successfully, but failed to update .override.env. To ensure that the next \
build succeeds, please add the line PHANTAZM_CONF_REPO_URL='[your-repository-url]' to .override.env. If such a file \
does not exist in the root directory of the project, create it."
          exit 1
        fi

        log_info "Successfully cloned repository and updated .override.env!"
        break
      fi
    done
  fi

  if ! cp -a "./defaultRunData/server-1/." "./run/server-1"; then
    log_warning "Failed to copy machine-specific run configs. The server may encounter launch errors."
  else
    log_info "Copied default machine-specific run configs."
  fi
else
  log_trace "Skipping first-time setup because ./run/server-1 already exists."
fi

# Automatically download worlds
if [ "${PHANTAZM_AUTO_DL_WORLDS}" = "true" ]; then
  clean_temp

  mkdir -p "./run/server-1/zombies/instances"
  mkdir -p "./run/server-1/lobbies/instances"

  lobby_instances="./run/server-1/lobbies/instances/"
  zombies_instances="./run/server-1/zombies/instances/"

  main_lobby="main_lobby.zip"

  fallen_grounds="fallen_grounds.zip"
  gau="gau.zip"

  # Only download if the world file is not already present
  dl_to_if_not_exists "$main_lobby" "13xEJ6yBjOASChsEhkWOz3Ppq2WLanSDD" "$lobby_instances" "main"

  dl_to_if_not_exists "$fallen_grounds" "1xWG8JIwvWa0LQtLH-RVvPorY9UhJioCn" "$zombies_instances" "fallen_grounds"
  dl_to_if_not_exists "$gau" "16mxFemqIP9Z0-cgU0rSrloqhINV1z47H" "$zombies_instances" "gau"

  clean_temp
fi

# Now that we've constructed everything else, build the project, copy the libraries, and run the server
# If the Gradle build fails, or we can't change directory, exit immediately
gradle_command="./gradlew ${gradle_args}"
if ! eval "${gradle_command}"; then
  log_error "Gradle build failed. Its output may contain more information."
  log_error "Command used: ${gradle_command}"
  exit 1
fi

if ! cd "./run/server-1"; then
  log_error "Could not cd into the server folder ./run/server-1. Does it exist?"
  exit 1
fi

log_info "Launching server!"
start_command="java ${server_jvm_args} ${server_file} ${server_args}"
if ! eval "${start_command}"; then
  log_error "The server process returned a non-zero exit code!"
  log_error "Command used: ${start_command}"
  exit 1
fi

log_trace "Server shut down normally. Exiting startup script."