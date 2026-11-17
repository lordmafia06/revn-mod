package com.revn.mod.herobrine;

import com.revn.mod.Revn;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Revn.MODID);

    public static final RegistryObject<EntityType<HerobrineEntity>> HEROBRINE = ENTITY_TYPES.register("herobrine",
            () -> EntityType.Builder.of(HerobrineEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .noSave()
                    .build("herobrine"));

    private ModEntities() {}
}
