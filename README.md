# PhantazmServer

[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)

Phantazm is a Minecraft server network focused on creating PvE minigames.

This repository contains:

1. The code for our minigames
2. Supporting libraries such as `phantazm-commons`
3. Advanced utilities for creating games, such as `phantazm-zombies-mapeditor`

## Table of Contents

- [Background](#background)
- [Install](#install)
- [Usage](#usage)
- [Maintainers](#maintainers)
- [Contributing](#contributing)
- [License](#license)

## Background

This project started out of a perceived lack of PvE-focused Minecraft servers. We believe that this is an area of untapped potential in the space.

## Install

To build Phantazm binaries from source, run the following commands: \
`git clone --recurse-submodules https://github.com/PhantazmNetwork/PhantazmServer.git` \
`cd PhantazmServer` \
`./gradlew build`

If you're going to be making changes to the code, run the `setupServer` task: \
`./gradlew setupServer`

This will automatically set up a locally-hosted server and Velocity proxy you can use to test your changes. Files are contained in `./run/server-1` and `./run/velocity`, for server and proxy respectively, relative to the project directory.

Generally, you'll want to build from the `master` branch, unless you're testing a specific feature. The `patch` branch is used to stage minor fixes and is regularly merged.

## Usage

You should use the latest Java 17 build to run Phantazm. The Minestom server by itself runs on 1.19.3. When connecting through the proxy, clients can use any Minecraft version at or later than 1.7.

### For development

The proxy and server use BungeeCord with BungeeGuard handshakes to communicate by default. This requires the use of a shared secret (string). This string is used to authenticate the proxy and ensure that no players can connect directly to the underlying server.

When running locally-hosted development builds, you can launch the server in "unsafe" mode (by specifying the `unsafe` program argument). This will disable the check that would normally prevent the server from running while using the default secret. This allows you to fire up Phantazm instances for local testing without needing to configure a unique shared secret first.

When launching Phantazm through IntelliJ (such as by using the `Run server` or `Run server + Velocity` run configurations), `unsafe` mode is **enabled**, so you don't have to do anything extra.

**Warning**: Make sure your locally-hosted development server & proxy are not visible from the Internet if you're launching in unsafe mode! Check your system (or gateway) firewall.

When debugging things, it's sometimes very useful to use breakpoints to analyze state and execute code line-by-line. Doing this on a Minecraft server works, but is tricky with a vanilla client due to the read timeout delay. As such, it is recommended to use a mod, like [this one](https://www.curseforge.com/minecraft/mc-mods/timeoutout-fabric), to increase this value to whatever you want. This will prevent your client from disconnecting itself due to a nonresponsive server.

### For production

If the `unsafe` argument is not specified, the server *will refuse to run* with the default secret, for security reasons. You'll need to configure your own secret.

First, come up with an appropriate string. You can use a password manager, or a generation tool like `apg` or `pwgen`. If your server is accessible from the Internet, make sure it's suitably complex. Keep in mind that your secret isn't something you need to actually *remember*, and thus there's no real drawback to making it as long as you want.

Next, set the `proxySecret` field in `./run/server-1/server-config.toml`, and the `forwarding-secret` field in `./run/velocity/velocity.toml`, to the same string (your secret).

**Warning**: The secret is stored, in plaintext, in both the server and proxy configuration files. This is, regrettably, something we can't do much about. Make sure access to your backend servers is properly secured.

### Running

The proxy will (by default) bind to `0.0.0.0:25565` and the server to `0.0.0.0:25567`, so you can connect through the proxy by adding the `localhost` server address in your Minecraft client.

If you're using IntelliJ, you can launch this testing server using the `Run server` run configuration (the server will use unsafe mode), or the server and proxy at once using the `Run server + Velocity` configuration.

You can also run the server from the command line as follows: \
`./gradlew setupServer copyJar` \
`cd ./run/server-1` \
`java -jar server.jar` 

And the proxy: \
`cd ./run/velocity` \
`java -jar velocity.jar`

This setup will generate the required binaries to run the server. Dependency artifacts are stored in the `./libs` folder in a hierarchical structure according to their group name. 

### Configuration

After running, Phantazm will generate two files, named `lobbies-config.toml` and `server-config.toml`, in the same directory as the `server.jar` file. You can use these to set up basic server parameters, such as the IP address and port to bind to. 

Details and tutorials for how to configure more complicated aspects of Phantazm are included in the [wiki](https://github.com/PhantazmNetwork/PhantazmServer/wiki). **These are currently out-of-date, pending a more stable codebase.**

## Maintainers

[Steank](https://github.com/Steanky) 

## Contributing

See the [contributions document](https://github.com/PhantazmNetwork/.github/blob/main/CONTRIBUTING.md) for more detailed information. Pull requests are welcome!

If editing the README, please conform to the [standard-readme](https://github.com/RichardLitt/standard-readme) specification.


Phantazm follows the [Contributor Covenant](http://contributor-covenant.org/version/1/3/0/) Code of Conduct.

### Reporting bugs
If you have a bug to report, head on over to the `Issues` tab and create a new issue. Make sure you follow the guidelines, and provide plenty of information.

### Making suggestions
If you have an idea for a new feature, or change to an existing one, you can make an issue for that too. However, it's recommended that you first join our Discord and get feedback from the community first.

## License

[GNU General Public License v3](LICENSE)
