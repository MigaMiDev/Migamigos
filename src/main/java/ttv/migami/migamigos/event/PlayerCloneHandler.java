package ttv.migami.migamigos.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.common.AmigoDataHandler;
import ttv.migami.migamigos.entity.AmigoEntity;

import java.util.List;
import java.util.UUID;

import static ttv.migami.migamigos.common.AmigoDataHandler.getAmigoByUUID;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class PlayerCloneHandler {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData();
        CompoundTag newData = event.getEntity().getPersistentData();

        if (oldData.contains(AmigoDataHandler.USED_AMIGO_SLOTS_KEY)) {
            newData.putInt(AmigoDataHandler.USED_AMIGO_SLOTS_KEY,
                    oldData.getInt(AmigoDataHandler.USED_AMIGO_SLOTS_KEY));
        }

        if (oldData.contains(AmigoDataHandler.AMIGO_UUIDS_KEY)) {
            newData.put(AmigoDataHandler.AMIGO_UUIDS_KEY,
                    oldData.get(AmigoDataHandler.AMIGO_UUIDS_KEY));
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            teleportAmigosToPlayer(player);
        }

        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            teleportAmigosToPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            teleportAmigosToPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            teleportAmigosToPlayer(player);
        }
    }

    private static void teleportAmigosToPlayer(ServerPlayer player) {
        List<UUID> amigoUUIDs = AmigoDataHandler.getAmigoUUIDs(player);

        for (UUID uuid : amigoUUIDs) {
            AmigoEntity amigo = getAmigoByUUID(player, uuid);

            if (amigo != null) {
                amigo.teleportTo(player.getX(), player.getY(), player.getZ());
                amigo.setTarget(null);
                amigo.getNavigation().stop();
            }
        }
    }
}