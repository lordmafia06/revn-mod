# REVN - Phase 1 (Forge 1.20.1)

Phase 1: core skeleton + Herobrine manifestation + per-player encounter memory + config.

## Build (needs JDK 17)
1. Download the official Forge **1.20.1 MDK** (files.minecraftforge.net -> 1.20.1 -> Mdk).
2. From the MDK copy ONLY: `gradlew`, `gradlew.bat`, and the `gradle/` folder into this project folder.
3. Open a terminal here and run:  `gradlew build`
4. Your mod: `build/libs/revn-0.1.0.jar` -> put in `.minecraft/mods` (Forge 1.20.1, version 47.x)
Test in dev: `gradlew runClient`

## Test in game
- `/revn herobrine spawn` - force a manifestation on yourself (op only)
- `/revn herobrine memory` - show encounter count
- `/revn herobrine forget` - wipe his memory of you
- Config: `config/revn-common.toml` (cooldowns, distance, visible time = 40 ticks = 2s)

## Behaviour
- Appears 8-24 blocks away (in your view, with line of sight when possible), stares for exactly 40 ticks, vanishes in smoke.
- Every encounter is saved on the player. More encounters -> shorter cooldowns and closer appearances (caps at 20).
