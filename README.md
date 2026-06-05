# PowerMobs

PowerMobs is a Paper/Spigot plugin for creating **enhanced custom mobs** with configurable 
stats, abilities, equipment, drops, and spawn rules—plus **randomly generated “PowerMobs”** for variety.
It is to help create vanilla+ content that can be used in a normal survival playthrough without getting in the way.

## What you can do

### Custom / predefined PowerMobs
Define custom mobs in `mobsconfig.yml` with options like:
- **Base mob type** (ZOMBIE, SKELETON, etc.)
- **Custom names** (color codes supported)
- **Stats** (health, damage, speed) with single values or ranges
- **Abilities**
- **Equipment** (vanilla items or custom items)
- **Drops** (items, amounts, chances, enchantments, exp, and more!)
- **Spawn conditions** (dimension, biome groups, time of day, coordinate bounds, delays, and much more!)

## Configuration files

### Global values and Random Mobs
Controls plugin-wide behavior in `config.yml` such as:
- Debug flags
- Spawn chance and announcements
- Spawn timer rules / bypasses
- Random-mob generation rules (abilities, equipment, drops, weights, ranges, etc.)

### Custom items (weapons, armor, uniques)
Define items in `itemsconfig.yml`

Each item can include:
- Material, name, lore, glow, model data, unbreakable
- Enchantments and attributes
- A flexible **effects system** (potion effects, particles, sounds, knockback, AoE effects, immunity effects, and more!)

### Spawn blockers
Define spawn blocks in `spawnblocksconfig.yml`
- Material
- Name and lore
- Chunk range

### Spawn keys
Define spawn keys in `spawnkeysconfig.yml`
- Enable or disable
- Material, name, lore
- Spawn ids, require context, context failure text
- Involve timers, timer restriction text
- Announcement text, announcement text interval
- Sound effects, particle effects

### Predefined Mobs
Customize your mobs in `mobsconfig.yml` (Can be done in game UI)
- Each mob can define stats, abilities, equipment, drops, and detailed spawn conditions.

### Abilities
Defines ability defaults in `abilitiesconfig.yml` such as:
- Radius, chances, cooldowns, damage, durations, and more

You can also override these settings per mob:

- **Predefined mobs** (`mobsconfig.yml`): `abilities:` can be either a basic list of IDs or a map of ability settings for each ability ID.

- **Random mobs** (`config.yml`): `possible-abilities:` for selection.

### Kill Commands

Optionally define commands to run when custom mobs are killed (`kill-commands:`), such as:

  - `say §3Frost Archer was defeated`
  - `summon lightning ~ ~ ~` 
  - Any available commands (including other plugins!), this is a generic integration hook that can trigger quests, economy, broadcasts, etc.

Where the relative coordinates (if applicable) are defined by the `at:` block as either the killer or mob.

Each `kill-commands:` entry supports:
- `command`: the command text (a leading `/` is allowed)
- `chance`: probability this entry fires, 0.0–1.0 (default 1.0)
- `at`: position anchor for `~ ~ ~` and selectors — `mob` (default) or `killer`

Block-level options:
- `player-required`: only fire the block when a player killed the mob (default true)

Placeholders substituted in the command text:
- `{player}`, `{player_uuid}` — the killer (empty if none)
- `{world}`, `{dimension}` — death location's world name and dimension key
- `{x}`, `{y}`, `{z}` — integer block coords of death
- `{mob_id}`, `{mob_name}` — config key and display name (color codes stripped)

All entries dispatch with console-level / operator permissions; `at:` only sets the position context for `~ ~ ~` and selectors. See the `# --- KILL COMMANDS ---` block at the top of `mobsconfig.yml` for the full schema, and the `bounty-bandit` mob at the bottom of that file for a working example.

## In-game UI
Most random-mob settings and many mob options can be edited in-game through the GUI pages.

## Debugging
Enable debug layers in `config.yml`:

## Compatibility notes

### LeveledMobs compatibility
In `externalplugins.yml` add:
`power-mobs:
  friendly-name: "power-mobs"
  plugin-name: "PowerMobs"
  key-name: "powermob.id"
  key-type: "metadata"
  requirement: "exists"`

Also, in LeveledMobs `rules.yml` under **External Plugins with Vanilla Stats and Minimized Nametags**, add `power-mobs` to the list.

### InfernalMobs compatibility
In InfernalMobs `config.yml`, under `enabledSpawnReasons:`, remove:
- `- CUSTOM`