package ttv.migami.migamigos.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.AmigoEntity;

import static ttv.migami.migamigos.common.network.ServerPlayHandler.sendParticlesToAll;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityKillEventHandler {

    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        LivingEntity entity = event.getEntity();
        LivingEntity killer = entity.getKillCredit();

        if (killer == null) {
            return;
        }

        // In the sad case an Amigo dies
        if (entity instanceof AmigoEntity amigoEntity) {
            Component deathMessage;
            if (killer instanceof Player) {
                deathMessage = Component.translatable(
                        "amigo.death.by_player",
                        entity.getDisplayName(),
                        killer.getDisplayName()
                );
            } else if (killer != null) {
                deathMessage = Component.translatable(
                        "amigo.death.by_entity",
                        entity.getDisplayName(),
                        killer.getDisplayName()
                );
            } else {
                deathMessage = Component.translatable(
                        "amigo.death.generic",
                        entity.getDisplayName()
                );
            }

            if (amigoEntity.hasPlayer()) {
                if (killer instanceof ServerPlayer player) {
                    player.sendSystemMessage(deathMessage);
                } else {
                    entity.getServer().getPlayerList().broadcastSystemMessage(deathMessage, false);
                }
            }
        }

        // Amigo level-up
        if (killer instanceof AmigoEntity amigoEntity) {
            int currentExperience = amigoEntity.getExperience();
            int currentLevel = amigoEntity.getAmigoLevel();
            int experienceReward = entity.getExperienceReward();

            currentExperience += experienceReward;

            while (currentLevel < 99 && currentExperience >= AmigoEntity.getExperienceForNextLevel(currentLevel)) {
                currentExperience -= AmigoEntity.getExperienceForNextLevel(currentLevel);
                currentLevel++;

                if (currentLevel % 2 == 0) {
                    amigoEntity.setExtraHealth(amigoEntity.getExtraHealth() + 1);
                    amigoEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(amigoEntity.getAmigo().getGeneral().getHealth() + amigoEntity.getExtraHealth());
                }

                amigoEntity.setExtraPower((float) (amigoEntity.getExtraPower() + 0.1D));

                if (amigoEntity.level() instanceof ServerLevel serverLevel) {
                    sendParticlesToAll(
                            serverLevel,
                            ParticleTypes.HAPPY_VILLAGER,
                            true,
                            amigoEntity.getX(),
                            amigoEntity.getY() + 0.1,
                            amigoEntity.getZ(),
                            16,
                            amigoEntity.getBbWidth(), amigoEntity.getBbHeight(), amigoEntity.getBbWidth(),
                            0.1
                    );
                    serverLevel.playSound(amigoEntity, amigoEntity.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }

            if (currentLevel >= 99) {
                currentLevel = 99;
                currentExperience = AmigoEntity.getExperienceForNextLevel(currentLevel);
            }

            amigoEntity.setExperience(currentExperience);
            amigoEntity.setLevel(currentLevel);
        }
    }
}