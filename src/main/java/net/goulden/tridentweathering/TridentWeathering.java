package net.goulden.tridentweathering;

import net.goulden.tridentweathering.config.ConfigBuilder;
import net.neoforged.fml.ModContainer;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(TridentWeathering.MODID)
public class TridentWeathering {

    public static final String MODID = "tridentweathering";

    public TridentWeathering(ModContainer modContainer) {

        modContainer.registerConfig(ModConfig.Type.SYNCED, ConfigBuilder.SERVER_SPEC);

    }
}