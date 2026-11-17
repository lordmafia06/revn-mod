package com.revn.mod;

import net.minecraftforge.common.ForgeConfigSpec;

/** Config file: config/revn-common.toml */
public final class RevnConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.BooleanValue ENABLE_IN_CREATIVE;
    public static final ForgeConfigSpec.BooleanValue OVERWORLD_ONLY;
    public static final ForgeConfigSpec.BooleanValue PREFER_FRONT_OF_PLAYER;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_LINE_OF_SIGHT;
    public static final ForgeConfigSpec.BooleanValue LEARN_FROM_ENCOUNTERS;
    public static final ForgeConfigSpec.BooleanValue PLAY_SOUND;
    public static final ForgeConfigSpec.IntValue MIN_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue MAX_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.IntValue VISIBLE_TICKS;
    public static final ForgeConfigSpec.IntValue MIN_DISTANCE;
    public static final ForgeConfigSpec.IntValue MAX_DISTANCE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("herobrine");
        ENABLED = b.comment("Master switch for random Herobrine manifestations.")
                .define("enabled", true);
        ENABLE_IN_CREATIVE = b.comment("Also haunt players in creative mode.")
                .define("enableInCreative", false);
        OVERWORLD_ONLY = b.comment("Only manifest in the Overworld.")
                .define("overworldOnly", true);
        MIN_COOLDOWN_SECONDS = b.comment("Minimum seconds between manifestations.")
                .defineInRange("minCooldownSeconds", 120, 5, 86400);
        MAX_COOLDOWN_SECONDS = b.comment("Maximum seconds between manifestations.")
                .defineInRange("maxCooldownSeconds", 600, 5, 86400);
        VISIBLE_TICKS = b.comment("How long he stays visible. 40 ticks = exactly 2 seconds.")
                .defineInRange("visibleTicks", 40, 1, 400);
        MIN_DISTANCE = b.comment("Closest he can appear to the player (blocks).")
                .defineInRange("minDistance", 8, 3, 64);
        MAX_DISTANCE = b.comment("Farthest he can appear from the player (blocks).")
                .defineInRange("maxDistance", 24, 4, 96);
        PREFER_FRONT_OF_PLAYER = b.comment("Try to appear inside the player's field of view first.")
                .define("preferFrontOfPlayer", true);
        REQUIRE_LINE_OF_SIGHT = b.comment("Try to appear where the player has a clear line of sight.")
                .define("requireLineOfSight", true);
        LEARN_FROM_ENCOUNTERS = b.comment("Each encounter is remembered; later ones come sooner and closer.")
                .define("learnFromEncounters", true);
        PLAY_SOUND = b.comment("Play a quiet sound when he manifests.")
                .define("playSound", true);
        b.pop();
        SPEC = b.build();
    }

    private RevnConfig() {}
}
