## ServerRedirect
This Forge mod provides a way for servers to transfer players to another server address without using a proxy server like Bungeecord.

ServerRedirect supports Forge, Bukkit/Spigot and SpongeVanilla/SpongeForge servers.

## How to install this mod on your Minecraft client
- [Download ServerRedirect-Forge for your specific Minecraft version](https://github.com/KaiKikuchi/ServerRedirect/releases)
- Put the downloaded jar file in your "mods" folder

## Command
- `/redirect <address> <PlayerName|PlayerUUID|"r=[radius]"|"*">`: this command transfers the specified player (all players if * is used instead of the player name, or all players within `[radius]`) to the specified address.

## Permission (plugin only)
- `serverredirect.command.redirect`: allows using the /redirect command

## Questions & Answers
### Why should I use this mod instead of a proxy server?
- It is easier to use
- It is safer to use
- It is lightweight
- It works with all mods
- It supports a wide range of Minecraft versions
- Transfers are not limited to Minecraft servers on your dedicated server

#### How is it easier to use?
ServerRedirect does not require any configuration file!

Anything that can run the `/redirect` command as OP/Console will work:
- Command blocks
- Command aliases plugins (custom /hub or /lobby command)
- Chest/Text GUI plugins (like BossShop and VirtualChest) to make a fancy server selector!

#### How is it lightweight?
A proxy handles all connections from clients and forwards them to the servers. In order to make clients and servers communicate correctly, some packet rewriting is needed. This causes some connection latency. ServerRedirect will not do that: your players will connect directly to the Minecraft servers!

#### How is it safer?
If the proxy has an issue or is victim of a denial of service (DDoS) attack, all your Minecraft servers will be unreachable! ServerRedirect allows you to redistribute your Minecraft servers between different dedicated servers with different IP addresses and ports, making it harder for all your servers to be unreachable!

#### How is it working with all mods?
Sometimes, mods have issues caused by proxies. This requires developers and server owners to find workarounds and fixes for those issues. ServerRedirect will not interfere with any mod.

#### How does it support a wide range of Minecraft versions?
Proxy servers support a limited range of Minecraft versions. ServerRedirect will work as long as the server supports the client connection. This makes it possible to have a hub/lobby that allows connection from clients with version from 1.4.7 to 1.12.2 thanks to ProtocolSupport.

Currently, ServerRedirect supports Minecraft 1.7.10 and newer versions. I may develop ServerRedirect for older versions of Minecraft if I get enough requests.

#### How are transfers not limited to Minecraft servers on my dedicated server?
A proxy server can only transfer clients between servers configured on the same proxy server. ServerRedirect allows you to use any server address! You can transfer players to servers oversea!

### It seems very nice... but what are the drawbacks of using this mod instead of a proxy?
- Clients without this mod will not be transferred to other servers by this mod. They will be able to join the server anyway.
- This mod does not provide a way to exchange data between the servers on your network. You must rely on other software to make that happen (e.g. SynX).

### Players do not want to install this mod manually
It would be much easier if this mod was included in your modpack! It would be even better if this mod was part of Forge or Vanilla Minecraft! Maybe, someday! :)

### Without a proxy server, I can't share chat and run commands on other servers!
There are few plugins that can do that fine without a proxy server. Be sure to check SynX, Sync (send commands between servers) and SynGlobalChannels (chat plugin with chat shared between servers)!

### Will it affect Vanilla clients?
No, it will not. Vanilla clients can still connect to your server, but they will have to manually connect to the desired server address.

### How should I set up my server with this mod?
There are many ways to set up a server with this mod. This is a way to make a Spigot hub/lobby that allows Minecraft clients from version 1.4.7 to 1.12.2 to connect to it:
- Download latest version of Spigot, ProtocolSupport, and ServerRedirect-Plugin.
- Make a hub/lobby with Spigot, and add ProtocolSupport and ServerRedirect-Plugin to the "plugins" folder.
- Make one Command Block for each one of your servers, and set them with the /redirect command. Place a button on them and a Sign with the name of the server. You can also use a plugins like BossShop to make a server selector GUI.
- You can also make command aliases, so /hub and/or /lobby runs the appropriate /redirect command for the player, or a fancy /servers command with the list of all your servers.

### I am a modpack developer and I would like to add this mod to my modpack!
That would be highly appreciated! You do not even have to ask for any permission to use this mod, feel free to add it! I am sure all server owners will appreciate it!

### I am a mods/plugins developer. Can I use this mod/plugin for my mod/plugin?
Yes, you can! Include the mod or plugin on your build path, then check the ServerRedirect class. Also, you can make PlayerRedirectEvent and PlayerWithRedirectJoinEvent event listeners!

## License and improvements to the project
This project is MIT licensed. Feel free to fork this project and/or suggest new features on the Issues page! If you want to push changes, please stick with the Java style (Eclipse style) I am using: tabs instead of spaces, open brackets on the same line.
