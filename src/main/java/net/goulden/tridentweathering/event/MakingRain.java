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
import net.minecraft.world.level.levelgen.Heightmap;
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
    private static void makeRain(LivingEntityUseItemEvent.Tick event) {

        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (event.getItem().getItem() != Items.TRIDENT) return;

        int eventTick = EVENT_START_AT_TICK - event.getDuration();
        CompoundTag entityData = entity.getPersistentData();
        ServerLevel serverLevel = ((ServerLevel) entity.level());
        if (entityData.getIntOr(TAG_START_TICK, 0) > eventTick
                || entity.getXRot() > -80
                || serverLevel.isRaining()
                || serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) entity.getX(), (int) entity.getZ()) > entity.getY() + entity.getEyeHeight()
                || serverLevel.getBiome(entity.blockPosition()).value().getPrecipitationAt(entity.blockPosition(), 0) == Biome.Precipitation.NONE
        ) {
            entityData.remove(TAG_START_TICK);
            return;
        }
        if (!entityData.contains(TAG_START_TICK)) {
            entityData.putInt(TAG_START_TICK, eventTick);
        }

        int workingTicks = eventTick - entityData.getIntOr(TAG_START_TICK, 0);
        if (workingTicks % 20 != 1) return;
        int rainTriggers = workingTicks / 20 + 1 - wait;
        if (rainTriggers < 1) return;

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        boolean isLastTrigger = rainTriggers == triggers;

        sendCosmetics(serverLevel, x, y, z, entity.getEyeHeight(), rainTriggers, isLastTrigger);

        if (isLastTrigger) serverLevel.getServer().setWeatherParameters(0, ServerLevel.RAIN_DURATION.sample(serverLevel.getRandom()) , true, false);

    }

    private static void sendCosmetics(ServerLevel serverLevel, double x, double y, double z, double yMax, int rainTriggers, boolean lastTrigger) {

        for (int i = 1; i <= rainTriggers; i++) {

            double triggerProgressForRing = (double) i / triggers;
            int particleCount = (int) (20 + 30 * triggerProgressForRing);
            double radius = 0.4 + 0.8 * triggerProgressForRing;

            for (int j = 0; j < particleCount; j++) {

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

        serverLevel.sendParticles(
                ParticleTypes.FALLING_DRIPSTONE_WATER,
                x,
                y + yMax * 10,
                z,
                15,
                0.5, 2, 0.5,
                0
        );

        double triggerProgress = (double) rainTriggers / triggers;

        serverLevel.sendParticles(
                ParticleTypes.DUST_PLUME,
                x, y, z,
                20,
                0.01, 0, 0.01,
                0.1 + (0.1 * triggerProgress)
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

        if (lastTrigger) {
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
}