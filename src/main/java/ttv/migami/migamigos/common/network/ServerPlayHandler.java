package ttv.migami.migamigos.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import ttv.migami.migamigos.entity.Companion;
import ttv.migami.migamigos.init.ModSounds;

import javax.annotation.Nullable;
import java.util.UUID;

public class ServerPlayHandler
{
    public static void closeCompanionInventory(ServerPlayer player, UUID companionUUID) {
        Companion companion = getCompanionByUUID(player, companionUUID);
        if (companion != null) {
            companion.setContainerOpen(false);
        }
        companion.level().playSound(null, companion, ModSounds.BACKPACK_CLOSING.get(), SoundSource.PLAYERS, 0.3F, 1.0F);
    }

    public static void companionAttitudeSwitch(ServerPlayer player, UUID companionUUID, int attitude) {
        Companion companion = getCompanionByUUID(player, companionUUID);
        if (companion != null) {
            switch (attitude) {
                case 0: {
                    companion.setAttackingAnyEnemy(!companion.isAttackingAnyEnemy());
                    companion.setTarget(null);
                    break;
                }
                case 1: {
                    companion.setDefendPlayerOnly(!companion.isDefendingPlayerOnly());
                    break;
                }
                case 2: {
                    companion.setFocusOnMainTarget(!companion.isFocusingOnMainTarget());
                    break;
                }
                case 3: {
                    companion.setAllowWandering(!companion.canWander());
                    break;
                }
                case 4: {
                    companion.setFollowing(!companion.isFollowing());
                    companion.getNavigation().stop();

                    if (!companion.isFollowing()) {
                        companion.setPostPos(companion.getOnPos().getCenter().toVector3f());
                    }
                    break;
                }
            }
        }
    }

    /*public static void basicAttack(ServerPlayer player, int companionID) {
        Companion companion = getCompanionByUUID(player.level(), companionID);

        companion.basicAttack();
    }*/

    @Nullable
    public static Companion getCompanionByUUID(Player player, UUID uuid) {
        double distance = 10.0;
        return player.getCommandSenderWorld().getEntitiesOfClass(Companion.class, new AABB(player.getX() - distance, player.getY() - distance, player.getZ() - distance, player.getX() + distance, player.getY() + distance, player.getZ() + distance), (entity) -> {
            return entity.getUUID().equals(uuid);
        }).stream().findAny().orElse(null);
    }
}

