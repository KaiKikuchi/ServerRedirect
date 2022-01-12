## ServerRedirect
This Forge mod provides a way for servers to transfer players to another server address, without the use of a proxy server.
By using this mod, server owners can avoid mod incompatibility issues caused by proxy servers like Bungeecord, Waterfall, and Velocity.

ServerRedirect supports Forge, Bukkit/Spigot and SpongeVanilla/SpongeForge servers.

## How to install this mod on your Minecraft client
- [Download ServerRedirect-Forge for your specific Minecraft version](https://github.com/KaiKikuchi/ServerRedirect/releases)
- Put the downloaded jar file in your "mods" folder

## How to install this mod on your Minecraft server
- If you are running Bukkit (Spigot, Paper, Cauldron, Mohist, etc.), Sponge (SpongeVanilla or SpongeForge), [download the Plugin version of this mod](https://github.com/KaiKikuchi/ServerRedirect/releases), then put the downloaded jar in the plugins folder.
- If you are running a Forge server, [download the Forge mod version for your specific Minecraft version](https://github.com/KaiKikuchi/ServerRedirect/releases), then put the downloaded jar in the mods folder.

## Commands
- `/redirect <Target> <server address>`: redirects the specified target player(s) to the specified address
- `/fallback <Target> <server address>`: sets the fallback server address for the specified target player(s)

The "Target" can be one of the following:
- `Player Name/UUID`: the specified player on the server
- `Target Selector`: a [target selector](https://minecraft.fandom.com/wiki/Target_selectors). Examples: `@a` (all players), `@a[distance=..10]` (all players within 10 blocks from the command sender)
- `r=N`: all players within N blocks from the command sender, or from the overworld spawn if run by console. Example: `r=6` (all players within 6 blocks from the command sender). Plugin version only.
- `*`: all players on the server. Plugin version only.

The command sender can be a Command Block.

## Permissions
- `/redirect`: serverredirect.command.redirect
- `/fallback`: serverredirect.command.fallback

These permissions are **not supposed to be granted to players**. These are console/command blocks commands only.  
These permissions are valid for the plugin version only. If you are using the Forge mod server-side, OP level 2 permission (or higher) is required.

## FAQ
### How this mod redirects players
- By running the /redirect command, the server will send a "transfer" packet containing the specified server address to the specified players.
- All players receiving the packet that have the ServerRedirect mod installed will disconnect from the server, and automatically direct connect to the specified server address.

Any server address that is reachable by the players with the "Direct Connect" button can be used.
The "transfer" packet will not affect players without the mod installed: they will simply stay connected on the current server.  
This behaves like the transfer packet present in "Minecraft: Bedrock Edition", feature included by Mojang by default on that client. Regrettably, "Minecraft: Java Edition" is missing this feature entirely.  
Both the redirect and fallback commands are not supposed to be run by players. They are intended for being run by the console, command blocks and other plugins. Command aliases and "server selector" GUIs are recommended (see BossShop or VirtualChest).

### What the fallback command is for
The fallback command tells the clients which server address they should connect to in case they get forcefully disconnected from the server (e.g. the server crashes).

Server owners want to use this by running the fallback command automatically by the server for every player that joins the server. If the server shuts down for any reason, the players will automatically direct connect to the fallback server address specified in the fallback command.

### Reasons for using this mod instead of a proxy like Bungeecord, Waterfall or Velocity
Forge mods don't always expect that the player is being moved from one server to another, and when a proxy transfers a player to a different server, it can cause glitches or crashes. Forge mods commonly expect to be disconnected from the server before connecting to another one. Additionally, Forge 1.13+ does not support proxies server switching. This mod solves the issue by properly disconnecting players from the server, and then connecting them to another server. This way, mods can properly handle players switching from a server to another.

Server owners can also use a combination of a proxy and Server Redirect if they want to use all the other features proxies can provide. Still, it is suggested to look into replacing the proxy with cross-server plugins and mods.

### Players with this mod can join servers without this mod
Any combination of presence/absence of this mod will let players connect to servers. Players with this mod can connect to Vanilla servers as well, and players with vanilla Minecraft can also connect to servers with this mod.

### This mod can be added in modpacks
Feel free to add this modpack in public and private modpacks. Asking for permission is not required. Although this mod shouldn't have any conflict with any other mod, feel free to [report conflicts here](https://github.com/KaiKikuchi/ServerRedirect/issues).  
This project is also available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/server-redirect).

### License and improvements to the project
This project is MIT licensed. Feel free to fork this project and/or suggest new features on [the Issues page](https://github.com/KaiKikuchi/ServerRedirect/issues)! If you want to push changes, please stick with the Java style (Eclipse style) I am using: tabs instead of spaces, open brackets on the same line.
