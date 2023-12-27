#!/bin/sh

echo "[INFO] Starting setup script..."

# This script file is expected to be run through a Docker container only.
if [ "${PHANTAZM_IS_DOCKER_CONTAINER}" != "true" ]; then
  echo "[ERROR] This script file can only be executed in Phantazm's development container!"
  exit 1
fi

gdrive_dl() {
  gd_filename="$1"
  gd_fileid="$2"
  curl -L -s -o "${gd_filename}" "https://drive.google.com/uc?export=download&id=${gd_fileid}"
}

dl_to_if_not_exists() {
  filename="$1"
  fileid="$2"
  target_dir="$3"
  output_dir="$4"

  if [ ! -d "${target_dir}/${output_dir}" ]; then
    mkdir -p "./tmp"

    echo "[INFO] Downloading world file ${filename}..."
    gdrive_dl "./tmp/${filename}" "$fileid"
    unzip "./tmp/${filename}" -d "$target_dir"
  fi
}

# Create run folder
mkdir -p "./run"

# First-time setup
if [ ! -d "./run/server-1" ]; then
  echo "[INFO] Doing first-time setup..."

  mkdir -p "./run/server-1"
  if [ -n "${PHANTAZM_CONF_REPO_URL}" ]; then
    echo "Cloning into configuration repository."

    # Clone the configuration repository, if applicable (environment variable should be supplied via docker-compose.yml)
    git clone "${PHANTAZM_CONF_REPO_URL}" ./run/server-1
  else
    echo "[WARNING] No configuration repository has been provided!"
    echo "[WARNING] Phantazm maps will not work correctly."
  fi

  cp -a "./defaultRunData/server-1/." "./run/server-1"
fi

# Automatically download worlds
if [ "${PHANTAZM_AUTO_DL_WORLDS}" = "true" ]; then
  # Delete ./tmp if it already exists (maybe setup proc was terminated before it could clean up?)
  if [ -d "./tmp" ]; then
      rm -rf "./tmp"
  fi

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

  # Clean up after ourselves
  if [ -d "./tmp" ]; then
    rm -rf "./tmp"
  fi
fi

# Now that we've constructed everything else, build the project, copy the libraries, and run the server
./gradlew -PskipBuild=zombies-mapeditor,zombies-timer,snbt-builder phantazm-server:copyJar --no-daemon
cd "./run/server-1" || exit

if [ "${PHANTAZM_DEBUG_ENABLED}" != "true" ]; then
  echo "[INFO] Debugging not enabled."
  java -Dfile.encoding=UTF-8 -Dminestom.packet-queue-size=-1 -Dminestom.keep-alive-kick=-1 -jar server.jar unsafe
else
  debug_args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
  echo "[INFO] Debugging enabled."
  java -Dfile.encoding=UTF-8 -Dminestom.packet-queue-size=-1 -Dminestom.keep-alive-kick=-1 -jar "${debug_args}" server.jar unsafe
fi