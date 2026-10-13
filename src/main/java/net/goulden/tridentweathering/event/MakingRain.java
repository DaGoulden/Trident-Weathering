package net.goulden.tridentweathering.event;

import net.goulden.tridentweathering.TridentWeathering;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = TridentWeathering.MODID)
public class MakingRain {

    public static int wait;
    public static int triggers;

    private static final String TAG_START_TICK = "TridentWeathering.StartTick";

    // event starts at 72000 and then goes down
    private static final int EVENT_START_AT_TICK = 72000;

    @SubscribeEvent
    public static void makeRain(LivingEntityUseItemEvent.Tick event) {

        Entity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (event.getItem().getItem() != Items.TRIDENT) return;
        if (entity.level().getBiome(entity.blockPosition()).value().getPrecipitationAt(entity.blockPosition()) == Biome.Precipitation.NONE) return;

        int eventTick = EVENT_START_AT_TICK - event.getDuration();
        CompoundTag entityData = entity.getPersistentData();
        if (entity.getXRot() > -80
                || entityData.getInt(TAG_START_TICK) > eventTick
                || entity.level().isRaining()
        ) {
            entityData.remove(TAG_START_TICK);
            return;
        }
        if (!entityData.contains(TAG_START_TICK)) {
            entityData.putInt(TAG_START_TICK, eventTick);
        }

        int workingTicks = eventTick - entityData.getInt(TAG_START_TICK);
        if (workingTicks % 20 != 1) return;
        int rainTriggers = workingTicks / 20 + 1 - wait;
        if (rainTriggers < 1) return;

        ServerLevel serverLevel = ((ServerLevel) entity.level());
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        boolean lastTrigger = rainTriggers == triggers;

        drawRings(serverLevel, x, y, z, entity.getEyeHeight() * 1.8, rainTriggers, lastTrigger);
        sendCosmetics(serverLevel, x, y, z, (double) rainTriggers / triggers);

        if (lastTrigger) {
            serverLevel.setWeatherParameters(0, ServerLevel.RAIN_DURATION.sample(serverLevel.getRandom()) , true, false);
            serverLevel.playSound(
                    null,
                    x, y, z,
                    SoundEvents.ELDER_GUARDIAN_CURSE,
                    SoundSource.PLAYERS,
                    0.03f,
                    1.7f
            );
        }
    }

    static void drawRings(ServerLevel serverLevel, double x, double y, double z, double yMax, int rainTriggers, boolean lastTrigger) {

        for (int i = 1; i <= rainTriggers; i++) {

            double triggerProgressForRing = (double) i / triggers;
            int particleCount = (int) (20 + 30 * triggerProgressForRing);

            for (int j = 0; j < particleCount; j++) {

                double radius = 0.4 + 0.8 * triggerProgressForRing;
                double angle = (2 * Math.PI * j) / particleCount;

                serverLevel.sendParticles(
                        ParticleTypes.FISHING,
                        x + radius * Math.cos(angle),
                        y + yMax * (1 - triggerProgressForRing),
                        z + radius * Math.sin(angle),
                        0,
                        0, lastTrigger ? 0.1 : 0, 0,
                        lastTrigger ? 10 * (1 - triggerProgressForRing) : 0
                );
            }
        }
    }

    static void sendCosmetics(ServerLevel serverLevel, double x, double y, double z, double triggerProgress) {

        serverLevel.sendParticles(
                ParticleTypes.DUST_PLUME,
                x, y, z,
                20,
                0.01, 0, 0.01,
                0.1 + (0.15 * triggerProgress)
        );

        serverLevel.playSound(
                null,
                x, y, z,
                SoundEvents.ALLAY_THROW,
                SoundSource.PLAYERS,
                0.3f,
                1f + (float) triggerProgress
        );

        serverLevel.playSound(
                null,
                x, y, z,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS,
                0.15f,
                1.2f + (float) triggerProgress / 2
        );
    }
}