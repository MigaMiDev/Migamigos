package ttv.migami.migamigos.common.network;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.init.ModSounds;

import java.util.UUID;

import static ttv.migami.migamigos.common.AmigoDataHandler.getAmigoByUUID;

public class ServerPlayHandler
{
    public static void tryToHurt(AmigoEntity amigo, LivingEntity target, DamageSource damageSource, float damage) {
        if (target instanceof AmigoEntity amigoTarget) {
            if (amigo.hasPlayer() && amigo.getPlayer() != null && amigo.getPlayer().equals(amigoTarget.getPlayer())) {
                return;
            }
        }
        if (target.equals(amigo.getPlayer())) {
            return;
        }
        if (target instanceof Player && amigo.getTarget() != null && !amigo.getTarget().equals(target)) {
            return;
        }
        if (target instanceof AmigoEntity && amigo.getTarget() != null && !amigo.getTarget().equals(target)) {
            return;
        }
        if (!(target instanceof Enemy) && !target.equals(amigo.getTarget())) {
            return;
        }

        if (amigo.hasEffect(MobEffects.DAMAGE_BOOST)) {
            damage *= amigo.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1;
        }

        target.hurt(damageSource, damage);
        target.invulnerableTime = 0;
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(), target.getZ(), ((int) amigo.getAmigo().getAttackCombo().getPower() / 2), target.getBbWidth() / 2, target.getBbHeight() / 2, target.getBbWidth() / 2, 0.1);
        }
    }

    public static void closeAmigoInventory(ServerPlayer player, UUID amigoUUID) {
        AmigoEntity amigoEntity = getAmigoByUUID(player, amigoUUID);
        if (amigoEntity != null) {
            amigoEntity.setContainerOpen(false);
        }
        amigoEntity.level().playSound(null, amigoEntity, ModSounds.BACKPACK_CLOSING.get(), SoundSource.PLAYERS, 0.3F, 1.0F);
    }

    public static void amigoAttitudeSwitch(ServerPlayer player, UUID amigoUUID, int attitude) {
        AmigoEntity amigoEntity = getAmigoByUUID(player, amigoUUID);
        if (amigoEntity != null) {
            switch (attitude) {
                case 0: {
                    amigoEntity.setAttackingAnyEnemy(!amigoEntity.isAttackingAnyEnemy());
                    amigoEntity.setTarget(null);
                    break;
                }
                case 1: {
                    amigoEntity.setDefendPlayerOnly(!amigoEntity.isDefendingPlayerOnly());
                    break;
                }
                case 2: {
                    amigoEntity.setFocusOnMainTarget(!amigoEntity.isFocusingOnMainTarget());
                    break;
                }
                case 3: {
                    amigoEntity.setAllowWandering(!amigoEntity.canWander());
                    break;
                }
                case 4: {
                    amigoEntity.setFollowing(!amigoEntity.isFollowing());
                    amigoEntity.getNavigation().stop();

                    if (!amigoEntity.isFollowing()) {
                        amigoEntity.setPostPos(amigoEntity.getOnPos().getCenter().toVector3f());
                    }
                    break;
                }
            }
        }
    }

    public static void toggleArmorPiece(ServerPlayer player, UUID amigoUUID, int armorPiece) {
        AmigoEntity amigoEntity = getAmigoByUUID(player, amigoUUID);

        if (amigoEntity != null) {
            switch (armorPiece) {
                case 0: amigoEntity.setShowHelmet(!amigoEntity.isShowingHelmet()); break;
                case 1: amigoEntity.setShowChestplate(!amigoEntity.isShowingChestplate()); break;
                case 2: amigoEntity.setShowLeggings(!amigoEntity.isShowingLeggings()); break;
                case 3: amigoEntity.setShowBoots(!amigoEntity.isShowingBoots()); break;
            }
        }
    }

    /**
     * Sends particles to all players in the ServerLevel using the specified parameters.
     *
     * @param serverLevel   The ServerLevel where the particles will be shown.
     * @param particleType  The particle type to be displayed.
     * @param longDistance  Whether the particles are visible from far away.
     * @param posX          The X-coordinate of the particles.
     * @param posY          The Y-coordinate of the particles.
     * @param posZ          The Z-coordinate of the particles.
     * @param particleCount Number of particles to spawn.
     * @param offsetX       Spread of the particles along the X-axis.
     * @param offsetY       Spread of the particles along the Y-axis.
     * @param offsetZ       Spread of the particles along the Z-axis.
     * @param speed         Speed of the particles.
     */
    public static <T extends ParticleOptions> void sendParticlesToAll(
            ServerLevel serverLevel,
            T particleType,
            boolean longDistance,
            double posX, double posY, double posZ,
            int particleCount,
            double offsetX, double offsetY, double offsetZ,
            double speed
    ) {
        for (ServerPlayer player : serverLevel.players()) {
            serverLevel.sendParticles(
                    player,
                    particleType,
                    longDistance,
                    posX, posY, posZ,
                    particleCount,
                    offsetX, offsetY, offsetZ,
                    speed
            );
        }
    }
}

