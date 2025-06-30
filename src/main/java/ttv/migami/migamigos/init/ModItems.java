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
            () -> new ForgeSpawnEggItem(ModEntities.COCOGOAT, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> WAVELYN_SPAWN_COOKIE = REGISTER.register("wavelyn_spawn_cookie",
            () -> new ForgeSpawnEggItem(ModEntities.WAVELYN, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> CLAYMORE_SPAWN_TORTILLA = REGISTER.register("claymore_spawn_tortilla",
            () -> new ForgeSpawnEggItem(ModEntities.CLAYMORE, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> SHYSAW_SPAWN_TEDDY_BEAR = REGISTER.register("shysaw_spawn_teddy_bear",
            () -> new ForgeSpawnEggItem(ModEntities.SHYSAW, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> POLLYPOUNCE_SPAWN_FISH = REGISTER.register("pollypounce_spawn_fish",
            () -> new ForgeSpawnEggItem(ModEntities.POLLYPOUNCE, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));
}