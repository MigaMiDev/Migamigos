package ttv.migami.migamigos.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.migamigos.Reference;

public class ModItems {

    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);

    public static final RegistryObject<Item> COCOGOAT_SPAWN_FLOWER = REGISTER.register("cocogoat_spawn_flower",
            () -> new ForgeSpawnEggItem(ModEntities.COMPANION, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

}