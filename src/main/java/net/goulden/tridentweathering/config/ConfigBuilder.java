package net.goulden.tridentweathering.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigBuilder {

    public static final ModConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SERVER = new Server(builder);
        SERVER_SPEC = builder.build();
    }

    public static class Server {

        protected final ModConfigSpec.IntValue clearTriggerDelay;
        protected final ModConfigSpec.IntValue rainWaitTriggers;
        protected final ModConfigSpec.IntValue rainTriggers;
        protected final ModConfigSpec.IntValue thunderWaitTriggers;
        protected final ModConfigSpec.IntValue thunderBoltsNeeded;
        protected final ModConfigSpec.DoubleValue thunderBoltProbability;

        Server(ModConfigSpec.Builder builder) {

            clearTriggerDelay = builder
                    .comment(" Delay (in ticks) between triggers to clear the weather when the trident is in the conduit.")
                    .defineInRange("clearTriggerDelay", 50, 1, 10000);

            rainWaitTriggers = builder
                    .comment(" Wait (in seconds) before starting with the triggers.")
                    .defineInRange("rainWaitTriggers", 1, 0, Integer.MAX_VALUE);

            rainTriggers = builder
                    .comment(" Amount of triggers, with the last one making it rain.")
                    .defineInRange("rainTriggers", 3, 1, Integer.MAX_VALUE);

            thunderWaitTriggers = builder
                    .comment(" Wait (in seconds) before starting with the lightning bolts triggers. (Change it only if needed)")
                    .defineInRange("thunderWaitTriggers", 2, 0, Integer.MAX_VALUE);

            thunderBoltsNeeded = builder
                    .comment(" Amount of lightning bolts that can strike and that are needed for making it thunder.")
                    .defineInRange("thunderBoltsNeeded", 3, 1, 5);

            thunderBoltProbability = builder
                    .comment(" Probability of striking a lightning bolt.")
                    .defineInRange("thunderBoltProbability", 0.85, 0, 1);
        }
    }
}