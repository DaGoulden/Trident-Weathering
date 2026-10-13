package net.goulden.tridentweathering.event;

import net.goulden.tridentweathering.TridentWeathering;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TridentWeathering.MODID)
public class MakingClear {

    public static int delay;

    private static final String TAG_IN_CONDUIT_FIRST_TICK = "TridentWeathering.InConduitFirstTick";

    @SubscribeEvent
    public static void makeClear(EntityTickEvent.Post event) {

        if (!(event.getEntity() instanceof ThrownTrident trident)) return;
        if (trident.level().isClientSide) return;
        if (!(trident.level().getBlockEntity(trident.getOnPos()) instanceof ConduitBlockEntity conduit)) return;
        if (!(conduit.isActive())) return;
        CompoundTag tData = trident.getPersistentData();
        if (!tData.contains(TAG_IN_CONDUIT_FIRST_TICK)) {
            tData.putInt(TAG_IN_CONDUIT_FIRST_TICK, trident.tickCount);
            return;
        }
        if ((trident.tickCount - tData.getInt(TAG_IN_CONDUIT_FIRST_TICK)) % delay != 1) return;

        ServerLevel serverLevel = ((ServerLevel) trident.level());
        sendCosmetics(serverLevel, trident.getBlockX() + 0.5, trident.getBlockY() + 0.5, trident.getBlockZ() + 0.5);
        trident.lerpMotion(0, 0, 0);
        serverLevel.setWeatherParameters(ServerLevel.RAIN_DELAY.sample(serverLevel.getRandom()), 0, false, false);
    }

    private static void sendCosmetics(ServerLevel serverLevel, double x, double y, double z) {

        serverLevel.sendParticles(ParticleTypes.BUBBLE_POP,
                x, y, z,
                25,
                0.1, 0.1, 0.1,
                0.3
        );

        if (serverLevel.getRainLevel(1) == 1) {

            serverLevel.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP,
                    x, y, z,
                    25,
                    1, 1, 1,
                    0.5
            );

            int particleCount = 15;
            double radius = 0.4;
            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double particleX = x + radius * Math.cos(angle);
                double particleZ = z + radius * Math.sin(angle);

                serverLevel.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        particleX, y, particleZ,
                        0,
                        0, 1, 0,
                        0.05
                );
            }

            serverLevel.playSound(null,
                    x, y, z,
                    SoundEvents.SOUL_ESCAPE,
                    SoundSource.AMBIENT,
                    10f,
                    1.5f
            );
        }

        serverLevel.playSound(null,
                x, y, z,
                SoundEvents.BOAT_PADDLE_WATER,
                SoundSource.AMBIENT,
                0.5f,
                1.7f
        );
    }
}