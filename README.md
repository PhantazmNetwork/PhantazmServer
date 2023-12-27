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
All three may be launched at once by running `docker compose up` in the project root. You can also use the provided
IntelliJ run configuration `Launch Phantazm`, which does the same thing.

By default, launching this way will attempt to download the Minecraft world files we use on our official server. If you
are not developing for our network, you can disable this by editing `docker-compose.yml`.

In addition to the files included in this repository, to properly run Phantazm you will need access to a Git repository
containing valid configuration files, as these define most aspects of gameplay and are essential. Whether you are
developing for our network or not, you must add a specify to the configuration repository. You must do this by including
a file named `docker-compose.override.yml` in the project root (it will be ignored by Git). The file should initially
look
something like this:

```yml
services:
    phantazm_server: &server
        environment:
            PHANTAZM_CONF_REPO_URL: "https://[your-github-username]:[your-github-access-token]@[repository]"
    phantazm_server_debug:
        <<: *server 
```

An example `PHANTAZM_CONF_REPO_URL` would
be `https://steanky:[token-redacted]@github.com/PhantazmNetwork/Configuration`.

### Additional setup (required for Linux users)

Depending on how your Docker installation is configured, and as a consequence of how Linux file permissions work, you
may run into errors such as files being rendered unmodifiable due to having their user set by the Docker container.
These are fixable, although they unfortunately require some user-specific configuration.

First, open up your terminal of choice and run the command `id`. You will get an output resembling this:

```
uid=1000(steank) gid=1000(steank)
```

Where `steank` is replaced by your current username. There might be additional output pertaining to *user groups*; we
don't care about those. Just note your `uid` and `gid`. In my case, those are both the same number — 1000.

Now, in your `docker-compose.override.yml` (you should have created one already in order to set up a configuration
repository), you will have to add some additional options:

```yml
services:
    phantazm_server: &server
        environment:
            PHANTAZM_CONF_REPO_URL: "https://[your-github-username]:[your-github-access-token]@[repository]"
        user: "[uid]:[gid]"
    phantazm_server_debug:
        <<: *server
    phantazm_proxy:
        user: "[uid]:[gid]"
```

Replace `[uid]` with the UID you found from running `id`, and `[gid]` with the GID. At the end, your file should look
something like this (obviously the actual numbers may vary):

```yml
services:
    phantazm_server: &server
        environment:
            PHANTAZM_CONF_REPO_URL: "https://[your-github-username]:[your-github-access-token]@[repository]"
        user: "1000:1000"
    phantazm_server_debug:
        <<: *server
    phantazm_proxy:
        user: "1000:1000"
```

### Joining the local development server

Once you've set up your development environment and run `docker compose up`, you should have a running proxy, server,
and database. You can connect to the server (through the proxy) by joining `localhost` on a vanilla 1.19.4 Minecraft
client.

### Debugging

Up until now, the examples have shown the project being run without debugging enabled. However, there is an alternate
Docker profile you can enable that will allow you to connect to the Minecraft server with a JVM debugger. Just
run `docker compose --profile debug up`,
or use the IntelliJ run configuration `Launch Phantazm (debug)`. Then, you can connect the debugger to `localhost:5005`,
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
