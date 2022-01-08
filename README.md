## ServerRedirect
This Forge mod provides a way for servers to transfer players to another server address, without a proxy server.
By using this mod, server owners can avoid mod incompatibility issues caused by proxy servers like Bungeecord, Waterfall, and Velocity.

ServerRedirect supports Forge (1.7.10, 1.10.2, 1.12.2, 1.16.5, 1.18.1), Bukkit/Spigot and SpongeVanilla/SpongeForge servers.

## How to install this mod on your Minecraft client
- [Download ServerRedirect-Forge for your specific Minecraft version](https://github.com/KaiKikuchi/ServerRedirect/releases)
- Put the downloaded jar file in your "mods" folder

## Commands and permissions
- `/redirect [player] [server address]` (permission: serverredirect.command.redirect): redirects the specified player to the specified address
- `/redirect r=15 [server address]` (permission: serverredirect.command.redirect): redirects all players within the specified radius from the command sender to the specified address (useful for command blocks)
- `/redirect [target selector] [server address]` (permission: serverredirect.command.redirect): redirects all players matching the target selector (e.g. @e) to the specified address
- `/redirect * [server address]` (permission: serverredirect.command.redirect): redirects all players on the server to the specified address

- `/fallback [player] [server address]` (permission: serverredirect.command.fallback): sets the fallback server address for the specified player
- `/fallback r=15 [server address]` (permission: serverredirect.command.fallback): sets the fallback server address for all players within the specified radius from the command sender (useful for command blocks)
- `/fallback [target selector] [server address]` (permission: serverredirect.command.fallback): sets the fallback server address for all players matching the target selector (e.g. @e)
- `/fallback * [server address]` (permission: serverredirect.command.fallback): sets the fallback server address for all players on the server

## Questions & Answers
### Why should I use this mod instead of a proxy server?
- It is easier to use
- It is safer to use
- It is lightweight
- It works with all mods
- It supports a wide range of Minecraft versions
- You can transfer players without having to worry about the latency between proxy and Minecraft server

#### How is it easier to use?
ServerRedirect does not require any configuration file!

Anything that can run the `/redirect` command as OP/Console will work:
- Command blocks
- Command aliases plugins (custom /hub or /lobby command)
- Chest/Text GUI plugins (like BossShop and VirtualChest) to make a fancy server selector!

#### How is it lightweight?
A proxy handles all connections from clients and forwards them to the servers. In order to make clients and servers communicate correctly, some packet rewriting is needed. This causes some connection latency and use of server memory/CPU. ServerRedirect will not do that: players will connect directly to the Minecraft servers.

#### How is it safer?
If the proxy has an issue or is victim of a denial of service (DDoS) attack, all your Minecraft servers will be unreachable! ServerRedirect allows you to redistribute your Minecraft servers traffic between different dedicated servers with different IP addresses and ports, making it harder for all your servers to be unreachable!

#### How is it working with all mods?
Sometimes, mods have issues caused by proxies. This requires developers and server owners to find workarounds and fixes for those issues. ServerRedirect will not interfere with mods as all it does it disconnecting players from the current server and connecting them to the server address sent by the server to the client.

#### How does it support a wide range of Minecraft versions?
Proxy servers may support a limited range of Minecraft versions. ServerRedirect will work as long as the server supports the client connection. This makes it possible to have a hub/lobby that allows connection from clients with version from 1.4.7 to 1.18.1 with plugins like ProtocolSupport and ViaVersion.

Currently, ServerRedirect supports Minecraft 1.7.10 and newer versions.

#### What latency between proxy and Minecraft server?
Although this is not the case for the majority of the networks using proxies, sometimes server owners need to put Minecraft servers in different physical servers. This could add latency, in particular in case of network congestion or if the physical server hosting the proxy is overloaded! Sometimes, networks have servers across different regions (e.g. NA and EU servers), and due to the very high latency between regions proxies are not used to communicate between servers across different regions.

### It seems very nice... but what are the drawbacks of using this mod instead of a proxy?
- Your players will only be transferred to other servers if they have this mod in their mods folder. All other players will still be able to connect and play just fine, but they won't be transferred to another server when the server asks them to.
- This mod does not provide a way to exchange data between the servers on your network. You must rely on other software to make that happen.

### What happens if my server crashes? With proxies, players will be connected to the fallback server automatically!
Make sure to use the /fallback command feature! When a player joins the server, make the server run the /fallback command so that every player with ServerRedirect will have a fallback server they will automatically connect to if the server crashes.

### Players do not want to install this mod manually
It would be much easier if this mod was included in your modpack! It would be even better if this mod was part of Forge or Minecraft! Maybe, someday! :)

### Will it affect Vanilla clients?
No, it will not. Vanilla clients can still connect to your server and play just fine!

### Can players with this mod join servers without this mod or without the plugin?
Yes, they can! Any combination of presence/absence of this mod will let players connect to servers.

### How should I set up my server with this mod?
There are many ways to set a server up with this mod. If you are running a simple Forge modpack server, add the mod to your mods folder. If you are running Spigot, Paper, Cauldron, Mohist, or SpongeForge, use the server plugin instead!

For networks with multiple modpacks, it is recommended to have a hub/lobby running Spigot (or Paper). Plugins to allow multiple Minecraft versions like ViaVersion/ViaBackwards and ProtocolSupport are supported and we encourage using them!

Custom commands, custom GUIs (e.g. BossShop and VirtualChest), or even Command Blocks can be used to run the /redirect and /fallback commands.

### I am a modpack developer and I would like to add this mod to my modpack!
That would be highly appreciated! You do not have to ask for any permission to use this mod, feel free to add it! I am sure all server owners will appreciate it!

### I am a mods/plugins developer. Can I use this mod/plugin for my mod/plugin?
Yes, you can! Include the mod or plugin on your build path, then check the ServerRedirect class. The mod/plugin will call the PlayerRedirectEvent event (cancellable) before sending a player to another server.

## License and improvements to the project
This project is MIT licensed. Feel free to fork this project and/or suggest new features on the Issues page! If you want to push changes, please stick with the Java style (Eclipse style) I am using: tabs instead of spaces, open brackets on the same line.
