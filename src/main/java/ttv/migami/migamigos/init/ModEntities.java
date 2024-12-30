package ttv.migami.migamigos.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.Companion;
import ttv.migami.migamigos.entity.projectile.GenericArrow;
import ttv.migami.migamigos.entity.summon.IceLotus;
import ttv.migami.migamigos.entity.summon.IceShower;

/**
 * Author: MigaMi
 */
public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Reference.MOD_ID);

    public static final RegistryObject<EntityType<Companion>> COMPANION = REGISTER.register("companion", () -> EntityType.Builder.of(Companion::new, MobCategory.MISC).sized(0.6F, 1.8F).build("companion"));

    public static final RegistryObject<EntityType<GenericArrow>> GENERIC_ARROW = REGISTER.register("generic_arrow", () -> EntityType.Builder.<GenericArrow>of(GenericArrow::new, MobCategory.MISC).sized(0.5F, 0.5F).noSave().noSummon().build("generic_arrow"));

    // Cocogoat
    public static final RegistryObject<EntityType<IceLotus>> ICE_LOTUS = REGISTER.register("ice_lotus", () -> EntityType.Builder.<IceLotus>of(IceLotus::new, MobCategory.MISC).sized(1.0F, 1.0F).noSummon().noSave().build("ice_lotus"));
    public static final RegistryObject<EntityType<IceShower>> ICE_SHOWER = REGISTER.register("ice_shower", () -> EntityType.Builder.<IceShower>of(IceShower::new, MobCategory.MISC).sized(1.0F, 1.0F).noSummon().noSave().build("ice_shower"));
}
