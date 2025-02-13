package ttv.migami.migamigos.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import ttv.migami.migamigos.entity.AmigoEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AmigoDataHandler {

    public static final String USED_AMIGO_SLOTS_KEY = "usedAmigoSlots";
    public static final String AMIGO_UUIDS_KEY = "amigoUUIDs";

    @Nullable
    public static AmigoEntity getAmigoByUUID(Player player, UUID uuid) {
        double distance = 10.0;
        return player.getCommandSenderWorld().getEntitiesOfClass(AmigoEntity.class, new AABB(player.getX() - distance, player.getY() - distance, player.getZ() - distance, player.getX() + distance, player.getY() + distance, player.getZ() + distance), (entity) -> {
            return entity.getUUID().equals(uuid);
        }).stream().findAny().orElse(null);
    }

    public static int getUsedAmigoSlots(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getInt(USED_AMIGO_SLOTS_KEY);
    }

    public static void setUsedAmigoSlots(Player player, int slots) {
        CompoundTag data = player.getPersistentData();
        data.putInt(USED_AMIGO_SLOTS_KEY, slots);
    }

    public static List<UUID> getAmigoUUIDs(Player player) {
        CompoundTag data = player.getPersistentData();
        ListTag uuidListTag = data.getList(AMIGO_UUIDS_KEY, StringTag.valueOf("").getId());
        List<UUID> uuidList = new ArrayList<>();

        for (int i = 0; i < uuidListTag.size(); i++) {
            String uuidString = uuidListTag.getString(i);
            uuidList.add(UUID.fromString(uuidString));
        }
        return uuidList;
    }

    public static void addAmigoUUID(Player player, UUID uuid) {
        CompoundTag data = player.getPersistentData();
        ListTag uuidListTag = data.getList(AMIGO_UUIDS_KEY, StringTag.valueOf("").getId());

        if (!containsAmigoUUID(player, uuid)) {
            uuidListTag.add(StringTag.valueOf(uuid.toString()));
            data.put(AMIGO_UUIDS_KEY, uuidListTag);
        }
    }

    public static void removeAmigoUUID(Player player, UUID uuid) {
        CompoundTag data = player.getPersistentData();
        ListTag uuidListTag = data.getList(AMIGO_UUIDS_KEY, StringTag.valueOf("").getId());

        uuidListTag.removeIf(tag -> tag.getAsString().equals(uuid.toString()));
        data.put(AMIGO_UUIDS_KEY, uuidListTag);
    }

    public static boolean containsAmigoUUID(Player player, UUID uuid) {
        return getAmigoUUIDs(player).contains(uuid);
    }

    public static void clearAllAmigos(Player player) {
        CompoundTag data = player.getPersistentData();
        data.remove(USED_AMIGO_SLOTS_KEY);
        data.remove(AMIGO_UUIDS_KEY);
    }

}
