# FlagWar-SW Plugin

**FlagWar-SW** is an advanced Towny warfare plugin designed to provide a more dynamic and strategic experience in Minecraft. It introduces innovative features like border ally invitations, dynamic attacks and defenses, preparation times, and war-point teleportation. This plugin transforms the way wars are fought in the Towny world, making battles more engaging and strategic.

## Features

- **Border Ally Invitations**: Invite your allied towns to join forces and defend your borders. You can strategically expand your defense network by including your allies in any conflict occurring near your territory.
  
- **Dynamic Attacks**: Initiate attacks that evolve over time. Players will need to adapt their strategies to changing battle conditions, ensuring that no two wars play out the same way.

- **Dynamic Defense**: Defend your town in real-time as attacks shift and evolve. Your defenses are just as important as your offense, and you'll need to coordinate with allies to ensure the safety of your town.

- **Preparation Time**: Gives towns and nations a set time to prepare for war once it is declared. Use this time wisely to strategize, gather resources, and prepare your troops for the coming battle.

- **War Point Teleportation**: Quickly teleport to the active war zone, ensuring that players can engage in the conflict without losing precious time traveling.

## Installation

1. Download the latest version of the plugin from the [Releases](https://github.com/your-repo/releases) page.
2. Place the `FlagWar-SW.jar` file into your server’s `plugins` folder.
3. Restart the server to load the plugin.
4. Configure the plugin by editing the `config.yml` located in the `plugins/FlagWar-SW/` directory.

## Configuration

The `config.yml` file allows server administrators to customize the behavior of the plugin to suit their specific needs.

```yaml
# Configuration for FlagWar-SW
war-duration: 60 # Duration of the war in minutes
preparation-time: 15 # Preparation time before war starts in minutes
allow-border-invites: true # Allows border invitations to allies
enable-war-teleportation: true # Enables teleportation to war points
max-allies: 5 # Maximum number of allies that can be invited
```

## Commands

- `/fw invite <town>` – Invite an allied town to defend your borders.
- `/fw start` – Start a FlagWar event.
- `/fw defend` – Engage in defensive action during an attack.
- `/fw tp` – Teleport to the war zone.
  
## Permissions

- `flagwar.admin` – Grants access to all FlagWar commands.
- `flagwar.use` – Allows regular players to participate in wars.
- `flagwar.invite` – Allows players to invite allies for border defense.

## How It Works

1. **Declare War**: A town can declare war on another town or nation. Once declared, both sides have a specified preparation time.
   
2. **Border Defense**: The attacked town can invite allied towns within a certain radius to help defend its borders. Allies can accept the invite and join the defense.

3. **Dynamic Attacks and Defenses**: War evolves dynamically, with shifting attack points and defenses that need to be constantly maintained. Attackers must focus on breaking defenses, while defenders must fortify their positions in real time.

4. **War Teleportation**: Once the war begins, players can use `/fw tp` to teleport to the active war zone.

5. **Victory Conditions**: The war ends either when one side is completely overrun or when the set time for the war has expired.

## Requirements

- Minecraft 1.17+ (or compatible versions)
- Towny Plugin (latest version)
- Java 8 or higher

## Contributing

We welcome contributions! If you'd like to contribute to FlagWar-SW, please follow these steps:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/YourFeature`).
3. Commit your changes (`git commit -am 'Add new feature'`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Create a new Pull Request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

---

This `README.md` provides a detailed overview of the plugin's features, installation steps, configuration options, commands, permissions, and instructions for contributing.
