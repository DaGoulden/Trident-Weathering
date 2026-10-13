package net.goulden.tridentweathering.config;

import net.goulden.tridentweathering.TridentWeathering;
import net.goulden.tridentweathering.event.MakingClear;
import net.goulden.tridentweathering.event.MakingRain;
import net.goulden.tridentweathering.event.MakingThunder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

import static net.goulden.tridentweathering.config.ConfigBuilder.SERVER;

@EventBusSubscriber(modid = TridentWeathering.MODID)
public class ConfigEvent {

    @SubscribeEvent
    public static void configLoading(ModConfigEvent.Loading event) {
        refreshConfig();
    }
    @SubscribeEvent
    public static void configReloading(ModConfigEvent.Reloading event) {
        refreshConfig();
    }

    private static void refreshConfig() {
        MakingClear.delay = SERVER.clearTriggerDelay.get();
        MakingRain.wait = SERVER.rainWaitTriggers.get();
        MakingRain.triggers = SERVER.rainTriggers.get();
        MakingThunder.wait = SERVER.thunderWaitTriggers.get();
        MakingThunder.bolts = SERVER.thunderBoltsNeeded.get();
        MakingThunder.probability = SERVER.thunderBoltProbability.get();
    }
}
