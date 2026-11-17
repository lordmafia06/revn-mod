package com.revn.mod.herobrine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/**
 * Per-player memory stored in the player's persistent NBT (survives death and relogs).
 * Phase 2+ will add anger, provocation counters, etc. here.
 */
public final class PlayerMemory {
    private static final String ROOT = "revn";

    private PlayerMemory() {}

    public static CompoundTag data(Player player) {
        CompoundTag all = player.getPersistentData();
        CompoundTag persisted = all.getCompound(Player.PERSISTED_NBT_TAG);
        if (!all.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            all.put(Player.PERSISTED_NBT_TAG, persisted);
        }
        CompoundTag mine = persisted.getCompound(ROOT);
        if (!persisted.contains(ROOT, Tag.TAG_COMPOUND)) {
            persisted.put(ROOT, mine);
        }
        return mine;
    }

    public static int encounters(Player player) {
        return data(player).getInt("encounters");
    }

    public static long lastEncounter(Player player) {
        return data(player).getLong("lastEncounter");
    }

    public static void recordEncounter(Player player, long gameTime) {
        CompoundTag d = data(player);
        d.putInt("encounters", d.getInt("encounters") + 1);
        d.putLong("lastEncounter", gameTime);
    }

    public static void forget(Player player) {
        CompoundTag all = player.getPersistentData();
        if (all.contains(Player.PERSISTED_NBT_TAG, Tag.TAG_COMPOUND)) {
            all.getCompound(Player.PERSISTED_NBT_TAG).remove(ROOT);
        }
    }
}
