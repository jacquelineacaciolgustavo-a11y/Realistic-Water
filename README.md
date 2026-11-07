 Realistic Fluids

**English** | [Português (Brasil)](README.pt-BR.md)

> **This is a modified version of the mod _Realistic Water_, created by Skaffy.**
> It is not the original mod.
> Original mod: https://github.com/sskaffy/Realistic-Water
> All credit for the base mod (fluid simulation, rendering, realistic blocks and physics) goes to **Skaffy**.
> This version is distributed under the same license as the original. See the `LICENÇA` file, which has been kept intact.

This version is meant for **personal use and testing**. Some parts have not been fully tested yet and may contain bugs.

---

## Requirements

- Minecraft **26.3**
- **Fabric Loader**
- **Fabric API**
- A graphics card with **Vulkan** support

## Installation

1. Install Fabric: https://docs.fabricmc.net/players/installing-fabric/
2. Put this mod's `.jar` and Fabric API in your `mods` folder.
3. Open the game, go to **Video Settings**, set the graphics API to **Vulkan**, then restart the game.
4. In the creative inventory, open the **Realistic Fluids** tab to see every item.

If you are updating from an older version, delete the `realistic-water.properties` file from your `config` folder. It overrides the new default values.

---

## What is different from the original mod

- The **`/water` command was removed** (including `/water gui` and `/water remove`).
- **Water and lava buckets** place a single **finite block** of liquid. They no longer create an infinite source.
- New **source blocks** for water, lava and slime, with flow rate, pressure and flood mode (see below).
- A dedicated **creative tab** called Realistic Fluids.
- **Slime** was redesigned with a lava-style look, green and more transparent, and it behaves stickier.
- **Lava + water** now produces **hot obsidian in layers**, which cools down into regular obsidian.
- **Lighter default settings**, so water and lava are less demanding on your computer.
- New mod name and icon, plus Portuguese (pt_br) translations.

---

## Source blocks (Water Source, Lava Source, Slime Source)

These blocks release liquid continuously. When placed, **all outputs start closed**.

| Action | What happens |
| --- | --- |
| **Right-click while standing** | Opens or closes the output on the face you clicked. A message appears, for example "Top output open". |
| **Right-click while sneaking** (empty hand) | Opens the **Source flow** screen. |

An open face shows a colored square on its texture. A closed face stays plain.

### "Source flow" screen

- **Flow (0 to 1000):** in steps of 100. The higher the flow, the **more liquid comes out and the harder it is pushed**, shooting the fluid with more force in the direction of the output.
- **Flood mode:** forces the flow to **1000** and keeps shooting a large amount of liquid in the direction of the open outputs.
- **ALL:** applies the saved settings to **every loaded source block in the world** (water, lava and slime).
- **Save:** stores the settings.

### Source optimization

- A source only works while a player is within 96 blocks of it.
- There is a cap on how many liquid blocks are created at once and on how many jets are active per simulation region.

---

## Lava + water = hot obsidian

When realistic lava meets water, a **Hot Obsidian** block appears:

1. It glows like magma and grows in **layers** while lava and water are still touching it.
2. At 8 layers it starts growing upward.
3. Over time it **cools down** and becomes **regular obsidian**. If it stopped partway, it stays as a cold obsidian layer.
4. If the space is occupied by water and there is a **solid ceiling** above, nothing grows: the fluid just disappears.

Vanilla (non-mod) lava still turns into stone as before.

---

## What the mod contains

Items from the original mod (Skaffy):

- Realistic water, with boiling, steam, heating, cooling and ultra-realistic physics
- Realistic lava (burns things and flammable blocks)
- Realistic slime
- Realistic sand and realistic leaves
- Realistic boat
- Water Pump (moves water from its input to its output when powered by redstone; right-click to change its speed)
- **Realistic Converter**, to turn whole blocks into realistic blocks with gravity
- Realistic rain that feeds water
- Realistic objects react to TNT
- Realistic liquids can travel through portals
- Sodium options menu support
- Vitrail shader loader support (OF/Iris shaders on Vulkan): https://modrinth.com/mod/vitrail-shaders
- MP4 clip rendering with ffmpeg (ffmpeg is not included)

Items and blocks added in this version:

- Realistic water, lava and slime buckets (1 finite block)
- Small realistic water bucket
- Water Source, Lava Source and Slime Source
- Hot Obsidian (created when lava meets water, not obtainable from the inventory)

---

## Building

The project uses Gradle. To build:

```
./gradlew build
```

The mod's `.jar` is created in `build/libs`. Use the file that does **not** end in `-sources`.

The repository also has a GitHub Actions workflow (`.github/workflows/build.yml`) that builds the mod in the cloud and makes the `.jar` available under **Artifacts**.

---

## Credits and license

- **Original mod: Realistic Water by Skaffy** — https://github.com/sskaffy/Realistic-Water
- This modified version keeps the original license. Read the `LICENÇA` file.
- Summary of the license rules (read the full text for details): you may fork and distribute a modified version as long as you credit Skaffy, keep the link to the original project, make clear that it is a modified version, and keep the same license. You **may not** sell the mod, put it behind a paywall, or claim the original work as your own.

The mod is provided "as is", without warranty of any kind.
