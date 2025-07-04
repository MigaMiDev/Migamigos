package ttv.migami.migamigos.init;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.migamigos.Migamigos;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.common.container.AmigoContainer;
import ttv.migami.migamigos.entity.AmigoEntity;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;

public class ModContainers {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Reference.MOD_ID);
    public static final RegistryObject<MenuType<AmigoContainer>> AMIGO_CONTAINER;

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String id, MenuType.MenuSupplier<T> factory) {
        return REGISTER.register(id, () -> new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS));
    }

    static {
        AMIGO_CONTAINER = REGISTER.register("amigo_container", () -> {
            return IForgeMenuType.create((windowId, inv, data) -> {
                try {
                    UUID workerId = data.readUUID();
                    Migamigos.LOGGER.info("{} is opening Amigo container for {}", inv.player.getDisplayName().getString(), workerId);
                    AmigoEntity amigoEntity = getAmigoByUUID(inv.player, workerId);
                    Migamigos.LOGGER.info("Amigo is {}", amigoEntity);
                    return amigoEntity == null ? null : new AmigoContainer(windowId, inv, amigoEntity);
                } catch (Exception var5) {
                    Migamigos.LOGGER.error("Error in amigo_container: ");
                    Migamigos.LOGGER.error(var5.getMessage());
                    Migamigos.LOGGER.error(Arrays.toString(var5.getStackTrace()));
                    return null;
                }
            });
        });
    }

    @Nullable
    public static AmigoEntity getAmigoByUUID(Player player, UUID uuid) {
        double distance = 10.0;
        return player.getCommandSenderWorld().getEntitiesOfClass(AmigoEntity.class, new AABB(player.getX() - distance, player.getY() - distance, player.getZ() - distance, player.getX() + distance, player.getY() + distance, player.getZ() + distance), (entity) -> {
            return entity.getUUID().equals(uuid);
        }).stream().findAny().orElse(null);
    }
}
