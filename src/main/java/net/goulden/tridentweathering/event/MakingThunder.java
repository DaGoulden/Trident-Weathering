package net.goulden.tridentweathering.event;

import net.goulden.tridentweathering.TridentWeathering;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Objects;

@EventBusSubscriber(modid = TridentWeathering.MODID)
public class MakingThunder {

    public static int wait;
    public static int bolts;
    public static double probability;

    private static final String TAG_ACTIVE = "TridentWeathering.Active";
    private static final String TAG_LIGHTNING_BOLTS_TRIGGERED = "TridentWeathering.LightningBoltsTriggered";

    @SubscribeEvent
    public static void makeThunder(EntityTickEvent.Pre event) {

        if (!(event.getEntity() instanceof ThrownTrident trident)) return;
        Level tLevel = trident.level();
        if (tLevel.isClientSide()) return;
        if (!(trident.tickCount % 10 == 1)) return;
        if (!(tLevel.isRainingAt(trident.blockPosition()))) return;
        if (Objects.requireNonNull(
                trident.getPickupItemStackOrigin().get(DataComponents.ENCHANTMENTS))
                    .getLevel(tLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.CHANNELING))
        < 1) return;

        CompoundTag tData = trident.getPersistentData();
        if (trident.getXRot() > 85 && trident.tickCount == 1) {
            tData.putBoolean(TAG_ACTIVE, true);
            tData.putInt(TAG_LIGHTNING_BOLTS_TRIGGERED, 0);
        }

        if (tData.getBoolean(TAG_ACTIVE) && tLevel.canSeeSkyFromBelowWater(trident.blockPosition())) {
            int triggers = (trident.tickCount - 1) / 10;
            if (triggers > wait && triggers <= wait + bolts) {
                if (tLevel.random.nextDouble() < probability) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(tLevel);
                    if (bolt != null) {
                        bolt.setPos(trident.position());
                        bolt.getPersistentData().putBoolean(TAG_ACTIVE, true);
                        bolt.setVisualOnly(true);
                        tLevel.addFreshEntity(bolt);
                    }
                    tData.putInt(TAG_LIGHTNING_BOLTS_TRIGGERED, tData.getInt(TAG_LIGHTNING_BOLTS_TRIGGERED) + 1);
                }
            }
            if (triggers == wait + bolts && tData.getInt(TAG_LIGHTNING_BOLTS_TRIGGERED) == bolts) {
                ServerLevel serverLevel = ((ServerLevel) tLevel);
                serverLevel.playSound(null,
                        trident.getOnPos(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.WEATHER,
                        10000.0f,
                        10f
                );
                serverLevel.setWeatherParameters(0, ServerLevel.THUNDER_DURATION.sample(serverLevel.getRandom()), true, true);
                tData.putBoolean(TAG_ACTIVE, false);
            }
        }
    }

    @SubscribeEvent
    private static void shortLightningBolt(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LightningBolt bolt)) return;
        if (bolt.level().isClientSide()) return;
        if (!bolt.getPersistentData().getBoolean(TAG_ACTIVE)) return;
        if (bolt.tickCount >= 1) {
            bolt.discard();
        }
    }

    @SubscribeEvent
    private static void cancelMakeThunder(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownTrident trident)) return;
        if (trident.level().isClientSide()) return;
        trident.getPersistentData().putBoolean(TAG_ACTIVE, false);
    }
}