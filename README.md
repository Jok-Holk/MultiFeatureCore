# MultiFeatureCore

Custom Bukkit/Paper plugin for a private **Purpur 26.1.2** server. Bundles fast travel, a rank system, builder tools, and special weapons into one core plugin.

---

## Requirements

| | |
|---|---|
| Server | Purpur / Paper 26.1.x |
| Java | 25 |
| API | Paper 26.1.2.build.70-stable |
| Optional | FAWE (FastAsyncWorldEdit), FAVS (FastAsyncVoxelSniper) |

---

## Features

### Rank System
Five ranks: **GUEST -> BUILDER -> ADMIN -> OWNER -> DEVELOPER**

Each rank has its own color prefix in chat, nametag, scoreboard, and join/quit messages. Ranks persist across restarts. OWNER gets a special entrance with lightning and a custom message.

### Fast Travel (`/travel`)
GUI-based checkpoint system. Save up to 9 named checkpoints per player. Teleport with a countdown that cancels on movement.

### Scoreboard (`/scoreboard`)
Personal toggle -- shows player name, rank, and coordinates. Updates every 5 seconds.

### Height Lock (`/heightlock`)
Lock your Y level for precise horizontal building. Toggle on/off or jump to a specific Y coordinate. Works in any gamemode except Spectator.

### Measure Tool (`/measure`)
Equip a compass and click two blocks to measure distance (X/Y/Z + 2D + 3D) or find the exact center point. Center result includes a clickable chat button to teleport there.

### Speed Fly (`/speedfly`)
Toggle speed-boosted flight on/off. Set speed from 1 (vanilla) to 5 (5x). Available to BUILDER+.
- `/speedfly` -- toggle on/off; shows current speed and hint when enabling
- `/speedfly <1-5>` -- set speed level
- `/speedfly tool` -- receive a Speed Wing feather; right-click to toggle without typing

### God Mace (`/godmace`)
Right-click to launch upward. Falling on a player below executes divine judgment. Item is owner-locked -- anyone else who picks it up gets kicked.

### Abyssal Sovereign (`/abyssal`)
A sea-themed trident. Throws at 3x normal speed with extreme base damage. On hit: lightning strike + explosion + splash AoE damage to surrounding entities. Returns to owner after hitting a block. Hit a player in Survival: instant execution. Owner-locked like the God Mace.

**Wet-condition bonus** -- if the target is in water or exposed to rain:
- Direct hit damage doubles (40 -> 80)
- Splash AoE expands from 3x3 to 6x6 blocks
- Explosion radius doubles; 6 fireworks instead of 3

### Glass Shortcut (`/glass`)
Places a glass block at your feet instantly -- for temporary scaffolding.

### Day Length (`/daylength`)
Control how long a Minecraft day takes in real minutes.
- `/daylength 45` -- set to 45 minutes per cycle
- `/daylength` -- show current setting
- `/daylength reset` -- restore vanilla default (20 minutes)

---

## Commands

| Command | Description | Permission |
|---|---|---|
| `/travel` | Open fast travel GUI | `multifeature.travel` |
| `/rank <player> <rank>` | Set player rank | ADMIN+ |
| `/scoreboard [on\|off]` | Toggle personal scoreboard | -- |
| `/heightlock [<y>\|on\|off]` | Lock Y level | `multifeature.heightlock` |
| `/measure <distance\|center>` | Compass measurement tool | `multifeature.measure` |
| `/glass` | Place glass at feet | `multifeature.glass` |
| `/daylength [<minutes>\|reset]` | Set day cycle length | `multifeature.daylength` |
| `/speedfly [1-5\|tool]` | Toggle speed fly / set level / get feather | `multifeature.speedfly` |
| `/godmace` | Summon GOD MACE | ADMIN/OWNER/DEVELOPER |
| `/abyssal` | Summon ABYSSAL SOVEREIGN | ADMIN/OWNER/DEVELOPER |

---

## Rank Permissions

| Rank | Permissions |
|---|---|
| GUEST | Travel, checkpoints -- forced Adventure mode |
| BUILDER | + heightlock, measure, glass, speedfly, worldedit.\*, voxelsniper.\* |
| ADMIN | All (`*`) |
| OWNER | All (`*`) |
| DEVELOPER | All (`*`) |

---

## Build

```bash
mvn clean package
```

Output: `target/multifeaturecore-<version>.jar`

> **Note:** Uses `maven-jar-plugin` (not shade). The plugin has no runtime dependencies -- Paper API is `provided`.

---

## Version History

| Version | Summary |
|---|---|
| 4.5.5 | Prevent duplicate special items: GodMace, Abyssal, Measure compass, Speed Wing |
| 4.5.0 | Add /speedfly: speed-boosted flight toggle + Speed Wing feather item |
| 4.4.0 | Abyssal Sovereign sound rebalance: IMPACT replaces THUNDER, Elder Guardian dominant |
| 4.3.5 | Abyssal Sovereign wet-condition system: double damage + wider splash AoE in water/rain |
| 4.3.0 | Fix Abyssal Sovereign: inventory removal in Creative, trident return after entity hit |
| 4.2.5 | Add FAVS (`voxelsniper.*`) permissions; fix JAR build (shade -> jar-plugin) |
| 4.2.0 | Add Abyssal Sovereign trident; fix deprecated `DO_DAYLIGHT_CYCLE` API |
| 4.1.0 | Add `/glass` and `/daylength` |
| 4.0.0 | Major rewrite -- HeightLock, Measure, GodMace, rank system overhaul |
| 2.1.1 | Fix scoreboard global state |
| 2.1.0 | Rename `/menu` -> `/travel`, rework travel system |
| 2.0.0 | Fast travel system rewrite |
