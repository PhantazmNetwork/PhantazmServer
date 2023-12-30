# PhantazmServer

[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)

Phantazm is a Minecraft server network focused on creating PvE minigames.

This repository contains:

1. The code for our minigames
2. Supporting libraries such as `phantazm-commons`
3. Advanced utilities for creating games, such as `phantazm-zombies-mapeditor`
4. Build files, setup scripts, and some essential configuration files

If you want to interact with the community, please join our [Discord server](https://discord.gg/Rb6NkK4EQ8)!

## Table of Contents

- [Background](#background)
- [Install a local development build](#install-a-local-development-build)
- [Joining the local development server](#joining-the-local-development-server)
- [Configuration](#configuration)
- [Maintainers](#maintainers)
- [Contributing](#contributing)
- [License](#license)

## Background

This project started out of a perceived lack of PvE-focused Minecraft servers. We believe that this is an area of
untapped potential in the space.

## Downloading

Run `git clone --recurse-submodules https://github.com/PhantazmNetwork/PhantazmServer` to download the source code. If
you skip the `--recurse-submodules` flag the project will not compile!

## Install a local development build

**Warning!** _You must only use this to set up an environment for local testing!_ Much of the components are insecure,
including the database and Minecraft server itself, due to using **default credentials**.

Local development builds may be started with [Docker Compose](https://docs.docker.com/compose/), which is a tool for
orchestrating multiple virtual containers. If you are on
Windows, [Docker Desktop](https://docs.docker.com/desktop/install/windows-install/) should be all you need to install
beforehand. If you are on Linux, you can also set up Docker Desktop (although it may be more complicated depending on
your distribution). If you do **not** go with Docker Desktop, make sure you have installed
both [Docker Engine](https://docs.docker.com/engine/) and Docker Compose.

If you are a Windows user, it is additionally very important to ensure that CRLF line endings won't cause Docker to have
issues building the project. Open up Git Bash and run the command `git config --global core.autocrlf false` — or
manually update the config file — to prevent issues from arising.

Phantazm currently makes use of three separate Docker containers — a database, Velocity proxy, and the Minestom server.
All three may be launched at once by running `docker compose up` in the project root. However, it is preferable to
instead use the IntelliJ run configuration `Launch Phantazm`, as it will appropriately manage the lifecycle of the
containers as well as provide convenient terminal access.

By default, launching this way will attempt to download the Minecraft world files we use on our official server. If you
are not developing for our network, you can disable this by adding a file named `.override.env` in the root directory of
the project, and adding the line `PHANTAZM_AUTO_DL_WORLDS='false'`.

In addition to the files included in this repository, to properly run Phantazm you will need access to a Git repository
containing valid configuration files, as these define most aspects of gameplay and are essential. Whether you are
developing for our network or not, you must specify a configuration repository. The first time you
run `docker compose up` (or `Launch Phantazm`), the setup script will prompt you to enter a URL, which will be appended
automatically to
your `.override.env` file. An example of such a URL is below:

```
https://steanky:[token-redacted]@github.com/PhantazmNetwork/Configuration
```

If you need to re-generate the files in `./run` for any reason, you can simply delete it and it will be recreated the
next time you launch a development build. This can help if you are running into errors related to the development
environment setup.

### Additional setup (Linux users only)

Many Linux users will not need to go through with this additional setup. To determine if it is necessary for you, run
the `id` command. This will output some text like the following:

```
uid=1000(steank) gid=1000(steank)
```

Make note of your current user name (the one that owns all the project files; in this example it is `steank`) and
its `uid` and `gid`. _If `uid` and `gid` are both 1000, you do not need to do anything else._ However, if one differs,
you will need to add some additional configuration to `.override.env`:

```
# This example assumes your UID and GID are 1001
PHANTAZM_UID='1001'
PHANTAZM_GID='1001'
```

In other words, set `PHANTAZM_UID` to your `uid` and `PHANTAZM_GID` to your `gid`.

### Joining the local development server

Once you've set up your development environment and run `Launch Phantazm`, you should have a running proxy, server,
and database. You can connect to the server (through the proxy) by joining `localhost` on a vanilla 1.19.4 Minecraft
client.

### Debugging

Up until now, the examples have shown the project being run without debugging enabled. However, there is an alternate
Docker profile you can enable that will allow you to connect to the Minecraft server with a JVM debugger. Just
run `Launch Phantazm (debug)`. Then, you can connect the debugger to `localhost:5005`,
or run the `Debug Phantazm` configuration in IntelliJ, and set breakpoints as usual.

### Configuration

Details and tutorials for how to configure more complicated aspects of Phantazm are included in
the [wiki](https://github.com/PhantazmNetwork/PhantazmServer/wiki). **These are currently out-of-date, pending a more
stable codebase.**

## Maintainers

[Steank](https://github.com/Steanky)

## Contributing

See the [contributions document](https://github.com/PhantazmNetwork/.github/blob/main/CONTRIBUTING.md) for more detailed
information. Pull requests are welcome!

If editing the README, please conform to the [standard-readme](https://github.com/RichardLitt/standard-readme)
specification.

Phantazm follows the [Contributor Covenant](http://contributor-covenant.org/version/1/3/0/) Code of Conduct.

### Reporting bugs

If you have a bug to report, head on over to the `Issues` tab and create a new issue. Make sure you follow the
guidelines, and provide plenty of information.

### Making suggestions

If you have an idea for a new feature, or change to an existing one, you can make an issue for that too. However, it's
recommended that you first join our Discord and get feedback from the community first.

## License

[GNU General Public License v3](LICENSE)
