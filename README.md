# PhantazmServer
This is the main repository for all Phantazm-related content. Code for our minigames can be found here.

### Reporting bugs
If you have a bug to report, head on over to the `Issues` tab and create a new issue. Make sure you follow the guidelines, and provide plenty of information.

### Making suggestions
If you have an idea for a new feature, or change to an existing one, you can make an issue for that too. However, it's recommended that you first join our Discord and get feedback from the community first.

# Development

All paths used here should be considered relative to the project directory.


### Building
To build Phantazm binaries from source, run the following commands: \
`git clone https://github.com/PhantazmNetwork/PhantazmServer.git` \
`cd PhantazmServer` \
`./gradlew build` 

If you're going to be making changes to the code, run the `setupServer` task: \
`./gradlew setupServer` 

This will automatically set up a locally-hosted server and Velocity proxy you can use to test your changes. Files are contained in `./run/server-1` and `./run/velocity`, for server and proxy respectively, relative to the project directory. 

Generally, you'll want to build from the `master` branch, unless you're testing a specific feature. The `patch` branch is used to stage minor fixes and is regularly merged.

### Running

**You should use the latest Java 17 build to run Phantazm.**

The proxy and server use the BungeeCord protocol to communicate by default. This requires you to set up a shared secret (string) that the proxy will use to authenticate itself to the server. The server *will refuse to run* with the default secret (an empty string), for security reasons. You'll need to configure your own secret.

First, come up with an appropriate secret. You can use a password manager, or a generation tool like `apg` or `pwgen`. If you're just hosting Phantazm locally, with no intention of port-forwarding, you don't have to worry too much about password strength. However, if your server is accessible from the Internet, make sure it's suitably complex. Keep in mind that your secret isn't something you need to actually *remember*, and thus there's no real drawback to making it as long as you want.

**Warning**: The secret is stored, in plaintext, in both the server and proxy configuration files. This is, regrettably, something we can't do much about. Make sure access to your backend servers is properly secured.

Next, set the `velocitySecret` field in `./run/server-1/server-config.toml`, and the `forwarding-secret` field in `./run/velocity/server-config.toml`, to the same string (your secret). 

The proxy will (by default) bind to `0.0.0.0:25565` and the server to `0.0.0.0:25567`, so you can connect through the proxy by adding the `LOCALHOST` server address in your Minecraft client.

If you're using IntelliJ, you can launch this testing server using the `Run server` run configuration, or the server and proxy at once using the `Run server + Velocity` configuration.

You can also run the server and proxy from the command line as follows: \
`java -jar ./run/server-1/server.jar` \
`java -jar ./run/velocity/velocity.jar`


### Contributing

Pull requests are welcome! Please read the contributor's guide before starting.






