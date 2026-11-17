package com.revn.mod;

import com.mojang.logging.LogUtils;
import com.revn.mod.command.RevnCommands;
import com.revn.mod.herobrine.HerobrineManager;
import com.revn.mod.herobrine.ModEntities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Revn.MODID)
public class Revn {
    public static final String MODID = "revn";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final HerobrineManager HEROBRINE = new HerobrineManager();

    public Revn() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITY_TYPES.register(modBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RevnConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(HEROBRINE);
        MinecraftForge.EVENT_BUS.addListener(RevnCommands::register);
    }
}
