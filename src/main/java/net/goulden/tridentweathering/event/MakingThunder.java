package net.goulden.tridentweathering.event;

import net.goulden.tridentweathering.TridentWeathering;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
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
    private static void makeThunder(EntityTickEvent.Pre event) {

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

        if (!(tData.getBooleanOr(TAG_ACTIVE, false) && tLevel.canSeeSky(trident.blockPosition()))) return;

        int triggers = (trident.tickCount - 1) / 10;
        if (triggers > wait
                && triggers <= wait + bolts
                && tLevel.getRandom().nextDouble() < probability
        ) {
            LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(tLevel, EntitySpawnReason.TRIGGERED);
            if (bolt != null) {
                bolt.setPos(trident.position());
                bolt.getPersistentData().putBoolean(TAG_ACTIVE, true);
                bolt.setVisualOnly(true);
                tLevel.addFreshEntity(bolt);
            }
            tData.putInt(TAG_LIGHTNING_BOLTS_TRIGGERED, tData.getIntOr(TAG_LIGHTNING_BOLTS_TRIGGERED, 0) + 1);
        }
        if (triggers == wait + bolts && tData.getIntOr(TAG_LIGHTNING_BOLTS_TRIGGERED, 0) == bolts) {
            ServerLevel serverLevel = ((ServerLevel) tLevel);
            serverLevel.playSound(null,
                    trident.getOnPos(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.WEATHER,
                    10000.0f,
                    10f
            );
            serverLevel.getServer().setWeatherParameters(0, ServerLevel.THUNDER_DURATION.sample(serverLevel.getRandom()), true, true);
            tData.putBoolean(TAG_ACTIVE, false);
        }
    }

    @SubscribeEvent
    private static void shortLightningBolt(EntityTickEvent.Pre event) {

        if (!(event.getEntity() instanceof LightningBolt bolt)) return;
        if (bolt.level().isClientSide()) return;
        if (!bolt.getPersistentData().getBooleanOr(TAG_ACTIVE, false)) return;

        if (bolt.tickCount >= 1) bolt.discard();

    }

    @SubscribeEvent
    private static void cancelMakeThunder(ProjectileImpactEvent event) {

        if (!(event.getProjectile() instanceof ThrownTrident trident)) return;
        if (trident.level().isClientSide()) return;

        trident.getPersistentData().putBoolean(TAG_ACTIVE, false);

    }
}