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

    public static final RegistryObject<Item> SHYBROOM_SPAWN_BROOM = REGISTER.register("shybroom_spawn_broom",
            () -> new ForgeSpawnEggItem(ModEntities.SHYBROOM, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> POLLYPOUNCE_SPAWN_PARROT_PLUSHIE = REGISTER.register("pollypounce_spawn_parrot_plushie",
            () -> new ForgeSpawnEggItem(ModEntities.POLLYPOUNCE, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));
}