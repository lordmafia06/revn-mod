package com.revn.mod.herobrine;

import com.revn.mod.Revn;
import com.revn.mod.RevnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Schedules and performs Herobrine manifestations for each online player. */
public class HerobrineManager {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final int RETRY_SECONDS = 15;
    private static final int FAMILIARITY_CAP = 20;

    private final Map<UUID, Long> nextAppearance = new HashMap<>();
    private int tickCounter = 0;

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            schedule(sp, sp.serverLevel().getGameTime(), false);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        nextAppearance.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (++tickCounter % CHECK_INTERVAL_TICKS != 0) return;
        if (!RevnConfig.ENABLED.get()) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!eligible(player)) continue;

            long now = player.serverLevel().getGameTime();
            Long due = nextAppearance.get(player.getUUID());
            if (due == null) {
                schedule(player, now, false);
                continue;
            }
            if (now < due) continue;

            boolean ok = manifest(player);
            schedule(player, now, !ok);
        }
    }

    private boolean eligible(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) return false;
        if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE && !RevnConfig.ENABLE_IN_CREATIVE.get()) {
            return false;
        }
        return !RevnConfig.OVERWORLD_ONLY.get() || player.serverLevel().dimension() == Level.OVERWORLD;
    }

    private float familiarity(ServerPlayer player) {
        if (!RevnConfig.LEARN_FROM_ENCOUNTERS.get()) return 0f;
        return Math.min(PlayerMemory.encounters(player), FAMILIARITY_CAP) / (float) FAMILIARITY_CAP;
    }

    private void schedule(ServerPlayer player, long now, boolean retry) {
        RandomSource rng = player.getRandom();
        long ticks;
        if (retry) {
            ticks = RETRY_SECONDS * 20L;
        } else {
            int min = RevnConfig.MIN_COOLDOWN_SECONDS.get();
            int max = Math.max(min, RevnConfig.MAX_COOLDOWN_SECONDS.get());
            double seconds = min + rng.nextInt(max - min + 1);
            seconds *= 1.0 - 0.5 * familiarity(player); // he gets bolder with every encounter
            ticks = Math.max(100L, (long) (seconds * 20));
        }
        nextAppearance.put(player.getUUID(), now + ticks);
    }

    /** Spawns Herobrine near the player. Returns false if no suitable spot was found. */
    public boolean manifest(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double minD = RevnConfig.MIN_DISTANCE.get();
        double maxD = Math.max(minD + 1, RevnConfig.MAX_DISTANCE.get());
        double effMax = Mth.lerp(familiarity(player), maxD, Math.min(maxD, minD + 4));

        Optional<Vec3> spot = findSpot(player, level, minD, effMax);
        if (spot.isEmpty()) return false;

        HerobrineEntity herobrine = ModEntities.HEROBRINE.get().create(level);
        if (herobrine == null) return false;

        Vec3 p = spot.get();
        herobrine.moveTo(p.x, p.y, p.z, 0.0F, 0.0F);
        herobrine.setup(player, RevnConfig.VISIBLE_TICKS.get());
        level.addFreshEntity(herobrine);

        if (RevnConfig.PLAY_SOUND.get()) {
            level.playSound(null, p.x, p.y, p.z, SoundEvents.ENDERMAN_STARE, SoundSource.AMBIENT, 0.5F, 0.6F);
        }
        PlayerMemory.recordEncounter(player, level.getGameTime());
        Revn.LOGGER.debug("Herobrine manifested for {} at {}", player.getGameProfile().getName(), p);
        return true;
    }

    private Optional<Vec3> findSpot(ServerPlayer player, ServerLevel level, double minD, double maxD) {
        RandomSource rng = player.getRandom();
        Vec3 eye = player.getEyePosition();

        for (int attempt = 0; attempt < 24; attempt++) {
            boolean strict = attempt < 14;
            double dist = Mth.lerp(rng.nextDouble(), minD, maxD);
            double yawDeg = (RevnConfig.PREFER_FRONT_OF_PLAYER.get() && strict)
                    ? player.getYRot() + (rng.nextDouble() * 120.0 - 60.0)
                    : rng.nextDouble() * 360.0;
            double rad = Math.toRadians(yawDeg);
            double x = player.getX() - Math.sin(rad) * dist;
            double z = player.getZ() + Math.cos(rad) * dist;

            BlockPos column = BlockPos.containing(x, player.getY(), z);
            if (!level.hasChunkAt(column)) continue;

            BlockPos stand = findStandable(level, column, player.blockPosition().getY());
            if (stand == null) continue;

            Vec3 pos = new Vec3(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);

            if (strict && RevnConfig.REQUIRE_LINE_OF_SIGHT.get()) {
                Vec3 headPos = pos.add(0, 1.6, 0);
                HitResult hit = level.clip(new ClipContext(eye, headPos,
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                if (hit.getType() != HitResult.Type.MISS) continue;
            }
            return Optional.of(pos);
        }
        return Optional.empty();
    }

    /** Looks for solid ground with 2 free blocks above, within +-4 blocks of the player's height. */
    private BlockPos findStandable(ServerLevel level, BlockPos column, int playerY) {
        for (int y = playerY + 4; y >= playerY - 4; y--) {
            BlockPos feet = new BlockPos(column.getX(), y, column.getZ());
            BlockPos below = feet.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
            if (!level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()) continue;
            if (!level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()) continue;
            if (!level.getFluidState(feet).isEmpty()) continue;
            return feet;
        }
        return null;
    }
}
