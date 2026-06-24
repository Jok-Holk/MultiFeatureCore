# MultiFeatureCore

Custom Bukkit/Paper plugin for a private **Purpur 26.1.2** server. Bundles fast travel, a rank system, bilingual language support, builder tools, PvP kits, a personal war horse, and ten special weapons into one core plugin.

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

### Language System (`/language`)
Each player chooses their preferred language. All plugin messages display in that language. Persists across restarts.
- `/language english` -- switch to English
- `/language vietnamese` -- switch to Vietnamese

### Rank System
Five ranks: **GUEST -> BUILDER -> ADMIN -> OWNER -> DEVELOPER**

Each rank has its own color prefix in chat, nametag, scoreboard, and join/quit messages. Ranks persist across restarts. OWNER gets a special entrance with lightning and a custom message. All rank-related messages respect each player's language setting.

### Fast Travel (`/travel`)
GUI-based checkpoint system. Each player sets their own slot count (1-54). Save named checkpoints and teleport with a countdown that cancels on movement. Customize each checkpoint's icon with any Minecraft item.
- `/travel slots <1-54>` -- set how many slots you want (safe-checks your highest used slot)
- `/travel icon <checkpointN> <material|reset>` -- set the icon for a slot (accepts `minecraft:` namespace format)

### Scoreboard (`/scoreboard`)
Personal toggle -- shows player name, rank, and coordinates. Updates every 5 seconds.

### Height Lock (`/heightlock`)
Lock your Y level for precise horizontal building. Toggle on/off or jump to a specific Y coordinate. Works in any gamemode except Spectator.

### Measure Tool (`/measure`)
Equip a compass and click two blocks to measure distance (X/Y/Z + 2D + 3D) or find the exact center point. Center result includes a clickable chat button to teleport there.

### Speed Fly (`/speedfly`)
Toggle speed-boosted flight on/off. Set speed from 1 (vanilla) to 10 (max). Available to BUILDER+.
- `/speedfly` -- toggle on/off; shows current speed and hint when enabling
- `/speedfly <1-10>` -- set speed level
- `/speedfly tool` -- receive a Speed Wing feather; right-click to toggle without typing

### God Mace (`/godmace`)
Right-click to launch upward. Falling on a player below executes divine judgment. Item is owner-locked -- anyone else who picks it up gets kicked.

### Abyssal Sovereign (`/trident`)
A sea-themed trident. Throws at 3x normal speed with extreme base damage. On hit: lightning strike + explosion + splash AoE damage to surrounding entities. Returns to owner after hitting a block. Hit a player in Survival: instant execution. Owner-locked like the God Mace.

**Wet-condition bonus** -- if the target is in water or exposed to rain:
- Direct hit damage doubles (40 -> 80)
- Splash AoE expands from 3x3 to 6x6 blocks
- Explosion radius doubles; 6 fireworks instead of 3

### PvP Kits (`/kits`)
9-slot GUI with themed combat kits. Each kit provides a complete loadout: weapons, armor, potions, food, and utility items. Inventory check before applying -- if you have items, a clickable confirm/cancel prompt appears.
- `/kits` -- open the kit selection GUI
- `/kits <name>` -- apply a kit directly (warrior, juggernaut, spear, archer, survivor, berserker, ghost, alchemist, pantheon)
- `/kits confirm` / `/kits cancel` -- handle the inventory-clear prompt

### War Horse (`/horse`)
Spawn a personal war horse bound to your UUID. Other players cannot ride it. All armor materials give identical max protection (netherite-level stats via entity attributes) -- the material is cosmetic only.
- `/horse <breed> [armor] [name]` -- spawn horse (breed: white, black, gray, chestnut, creamy, brown, dark\_brown, skeleton, zombie, donkey, mule)
- `/horse dismiss` -- despawn your current horse
- `/horse confirm` / `/horse cancel` -- handle the replace-existing-horse prompt

### Divine Weapons
Ten legendary weapons, each UUID-locked to its owner. Anyone else who picks one up gets kicked. ADMIN/OWNER/DEVELOPER only. Each weapon has a custom resource-pack model linked via `setItemModel`.

**Charge-and-release** (right-click once to start charging, again to release):

| Command | Item | Mechanic |
|---|---|---|
| `/excalibur` | NETHERITE\_SWORD -- Dark Excalibur | Charge 10s -> multi-wave darkness beam; width, length, and damage scale with charge; full charge broadcasts server-wide |
| `/ragnarok` | NETHERITE\_AXE -- Ragnarok | Charge 5s -> horizontal sweep in front, breaks blocks + damages all entities in the arc |
| `/ignis` | NETHERITE\_PICKAXE -- Ignis Core | Charge 8s -> forward cylinder drill, breaks stone-tier blocks, ignites entities |
| `/void` | BOW -- Void Constellation | Charge 5s -> ray-cast to target, summons particle ring, fires 5-25 arrows with spread |
| `/nothan` | CROSSBOW -- Divine Crossbow | Charge 4s -> forward cone of divine force; stagger + weaken targets in range; cone angle and damage scale with charge |
| `/spear` | NETHERITE\_SPEAR -- Spear of Justice | Charge 3s -> lunge forward at high speed; hits ALL entities along the path (2.5 block radius); +15-45 damage + Slowness III + Blindness + Glowing; golden particle trail during lunge; kicks SURVIVAL player on contact |

**Mode-based** (sneak+right-click to cycle area, right-click to activate):

| Command | Item | Mechanic |
|---|---|---|
| `/verdant` | NETHERITE\_HOE -- Verdant Cipher | Cycle area 1x1 -> 3x3 -> 5x5 -> 9x9 -> 15x15; right-click to till soil and ripen crops in selected area |
| `/grave` | NETHERITE\_SHOVEL -- Grave Sovereign | Cycle area 1x1 -> 3x3 -> 5x5 -> 9x9 -> 15x15 (with depth 3-10 blocks); right-click to dig a circle downward, apply Wither II + Slowness to nearby entities, soul particle storm |

**God weapons** (owner-locked; their own interaction mechanic):

| Command | Item | Mechanic |
|---|---|---|
| `/godmace` | MACE -- GOD MACE | Right-click launch upward + falling-strike verdict |
| `/trident` | TRIDENT -- Abyssal Sovereign | 3x-speed throw + AoE + wet bonus |

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
| `/language <english\|vietnamese>` | Set preferred language | any player |
| `/travel` | Open fast travel GUI | `multifeature.travel` |
| `/travel slots <1-54>` | Set personal slot count | `multifeature.travel` |
| `/travel icon <checkpoint> <material\|reset>` | Set checkpoint icon | `multifeature.checkpoint.rename` |
| `/rank <player> <rank>` | Set player rank | ADMIN+ |
| `/scoreboard [on\|off]` | Toggle personal scoreboard | -- |
| `/kits [name]` | Open PvP kit GUI or apply kit directly | `multifeature.kits` |
| `/heightlock [<y>\|on\|off]` | Lock Y level | `multifeature.heightlock` |
| `/measure <distance\|center>` | Compass measurement tool | `multifeature.measure` |
| `/glass` | Place glass at feet | `multifeature.glass` |
| `/daylength [<minutes>\|reset]` | Set day cycle length | `multifeature.daylength` |
| `/speedfly [1-10\|tool]` | Toggle speed fly / set level / get feather | `multifeature.speedfly` |
| `/godmace` | Summon GOD MACE | ADMIN/OWNER/DEVELOPER |
| `/trident` | Summon ABYSSAL SOVEREIGN | ADMIN/OWNER/DEVELOPER |
| `/horse <breed> [armor] [name]` | Spawn personal war horse | `multifeature.horse` |
| `/horse dismiss` | Despawn your horse | `multifeature.horse` |
| `/excalibur` | Summon Dark Excalibur | ADMIN/OWNER/DEVELOPER |
| `/ragnarok` | Summon Ragnarok | ADMIN/OWNER/DEVELOPER |
| `/ignis` | Summon Ignis Core | ADMIN/OWNER/DEVELOPER |
| `/grave` | Summon Grave Sovereign | ADMIN/OWNER/DEVELOPER |
| `/verdant` | Summon Verdant Cipher | ADMIN/OWNER/DEVELOPER |
| `/void` | Summon Void Constellation | ADMIN/OWNER/DEVELOPER |
| `/spear` | Summon Spear of Justice | ADMIN/OWNER/DEVELOPER |
| `/nothan` | Summon Divine Crossbow | ADMIN/OWNER/DEVELOPER |

---

## Rank Permissions

| Rank | Color | Permissions |
|---|---|---|
| GUEST | gray | Travel, checkpoints -- forced Adventure mode |
| BUILDER | green | + heightlock, measure, glass, speedfly, kits, horse, worldedit.\*, voxelsniper.\* |
| ADMIN | yellow | All (`*`) |
| OWNER | red | All (`*`) + special join/quit message + lightning sound |
| DEVELOPER | purple | All (`*`) |

---

## Resource Pack

The `src/main/resources/pack-contents/` directory contains the resource pack for all divine weapons. Copy the contents of `pack-contents/` to your server's resource pack and reload.

- Pack format: 55 (supported formats 46-99)
- All 10 divine weapons have custom models under `assets/multifeature/models/item/`
- Textures under `assets/multifeature/textures/item/`
- Models are linked in-game via `ItemMeta.setItemModel(NamespacedKey)` -- no minecraft namespace overrides needed

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
| 5.0.0 | Bilingual language system (/language english\|vietnamese); resource pack model links for all 10 divine weapons (setItemModel); package modularization into 7 subpackages; Spear of Justice rewritten to charge-and-release lunge with path AoE; Grave Sovereign rewritten to mode-based area control (sneak+click to cycle 1x1->15x15) |
| 4.9.6 | Full weapon overhaul + 2 new divine weapons: Spear of Justice and Divine Crossbow (No Than) |
| 4.9.5 | Add 6 Divine Weapons: Excalibur, Ragnarok, Ignis Core, Grave Sovereign, Verdant Cipher, Void Constellation -- all UUID-locked, ADMIN+ only, shared charge/release mechanic |
| 4.9.4 | Fix Abyssal block-hit re-embed: replace fly-back animation with immediate item return; fix Pantheon kit Spear of Justice (TRIDENT -> NETHERITE\_SPEAR) |
| 4.9.3 | Fix Spear Master kit: TRIDENT -> NETHERITE\_SPEAR, correct armor tier, LUNGE+IMPALING enchants for right-click thrust |
| 4.9.2 | Fix Abyssal Sovereign: block-hit trident return -- store item at launch, use UUID lookup, add 4s timeout, fix detection radius |
| 4.9.1 | Fix Abyssal Sovereign: trident not returned to owner after hitting an entity |
| 4.8.5 | Rename /abyssal -> /trident |
| 4.8.0 | Add /horse: personal war horse with owner lock, max stats, cosmetic armor |
| 4.7.5 | Kits inventory check: confirm/cancel before clearing inventory |
| 4.7.0 | Per-player travel slot count (1-54) + per-checkpoint icon customization |
| 4.6.0 | Add /kits: 9-kit PvP GUI with direct give; extend /speedfly to level 10; fix /glass Y offset |
| 4.5.5 | Prevent duplicate special items: GodMace, Abyssal, Measure compass, Speed Wing |
| 4.5.0 | Add /speedfly: speed-boosted flight toggle + Speed Wing feather item |
| 4.4.0 | Abyssal Sovereign sound rebalance: IMPACT replaces THUNDER, Elder Guardian dominant |
| 4.3.5 | Abyssal Sovereign wet-condition system: double damage + wider splash AoE in water/rain |
| 4.3.0 | Fix Abyssal Sovereign: inventory removal in Creative, trident return after entity hit |
| 4.2.5 | Add FAVS (voxelsniper.\*) permissions; fix JAR build (shade -> jar-plugin) |
| 4.2.0 | Add Abyssal Sovereign trident; fix deprecated DO\_DAYLIGHT\_CYCLE API |
| 4.1.0 | Add /glass and /daylength |
| 4.0.0 | Major rewrite -- HeightLock, Measure, rank system overhaul |
| 3.0.0 | Add GodMace weapon -- right-click launch upward, owner lock, falling strike on player below |
| 2.1.1 | Fix scoreboard global state |
| 2.1.0 | Rename /menu -> /travel, rework travel system |
| 2.0.0 | Fast travel system rewrite |
| 1.5.0 | Scoreboard -- right-side info panel showing name, rank, coordinates |
| 1.0.0 | Initial release -- five-tier rank system (GUEST -> DEVELOPER) with chat rank prefix and color-coded nametags |
