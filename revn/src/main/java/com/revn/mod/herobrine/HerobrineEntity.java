package com.revn.mod.herobrine;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Herobrine as a short-lived apparition. Phase 1: appears, stares at his target
 * for a fixed number of ticks, then vanishes. Not saved to disk, never despawns early.
 */
public class HerobrineEntity extends PathfinderMob {
    private int lifetimeTicks = 40; // 40 ticks = exactly 2 seconds
    private UUID targetId;

    public HerobrineEntity(EntityType<? extends HerobrineEntity> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.setInvulnerable(true);
        this.setSilent(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    public void setup(Player target, int lifetimeTicks) {
        this.targetId = target.getUUID();
        this.lifetimeTicks = lifetimeTicks;
        faceTarget(target);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) return;

        Player target = targetId == null ? null : this.level.getPlayerByUUID(targetId);
        if (target == null || !target.isAlive()) {
            this.discard();
            return;
        }
        faceTarget(target);
        if (this.tickCount >= lifetimeTicks) {
            vanish();
        }
    }

    private void faceTarget(Player target) {
        this.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        this.setYBodyRot(this.getYRot());
        this.setYHeadRot(this.getYRot());
    }

    private void vanish() {
        if (this.level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.0, getZ(), 14, 0.3, 0.6, 0.3, 0.01);
        }
        this.discard();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }
}
