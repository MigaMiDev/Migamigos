package ttv.migami.migamigos.init;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.AmigoEntityType;
import ttv.migami.migamigos.entity.StunEntity;
import ttv.migami.migamigos.entity.amigo.Claymore;
import ttv.migami.migamigos.entity.amigo.Cocogoat;
import ttv.migami.migamigos.entity.amigo.Wavelyn;
import ttv.migami.migamigos.entity.fx.GroundCracksEntity;
import ttv.migami.migamigos.entity.fx.ScorchMarkEntity;
import ttv.migami.migamigos.entity.projectile.GenericArrow;
import ttv.migami.migamigos.entity.projectile.cocogoat.IceCone;
import ttv.migami.migamigos.entity.projectile.wavelyn.SoulFireball;
import ttv.migami.migamigos.entity.summon.HailShower;
import ttv.migami.migamigos.entity.summon.IceLotus;

/**
 * Author: MigaMi
 */
public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Reference.MOD_ID);

    public static final RegistryObject<AmigoEntityType<Cocogoat>> COCOGOAT = REGISTER.register("cocogoat",
            () -> new AmigoEntityType<>(
                    Cocogoat::new,
                    MobCategory.CREATURE,
                    true,
                    true,
                    false,
                    false,
                    ImmutableSet.of(),
                    EntityDimensions.scalable(0.6F, 1.8F),
                    80,
                    1,
                    FeatureFlags.DEFAULT_FLAGS
            )
    );
    public static final RegistryObject<AmigoEntityType<Wavelyn>> WAVELYN = REGISTER.register("wavelyn",
            () -> new AmigoEntityType<>(
                    Wavelyn::new,
                    MobCategory.CREATURE,
                    true,
                    true,
                    false,
                    false,
                    ImmutableSet.of(),
                    EntityDimensions.scalable(0.6F, 1.8F),
                    80,
                    1,
                    FeatureFlags.DEFAULT_FLAGS
            )
    );
    public static final RegistryObject<AmigoEntityType<Claymore>> CLAYMORE = REGISTER.register("claymore",
            () -> new AmigoEntityType<>(
                    Claymore::new,
                    MobCategory.CREATURE,
                    true,
                    true,
                    false,
                    false,
                    ImmutableSet.of(),
                    EntityDimensions.scalable(0.6F, 1.8F),
                    80,
                    1,
                    FeatureFlags.DEFAULT_FLAGS
            )
    );

    public static final RegistryObject<EntityType<GenericArrow>> GENERIC_ARROW = REGISTER.register("generic_arrow", () -> EntityType.Builder.<GenericArrow>of(GenericArrow::new, MobCategory.MISC).sized(0.8F, 0.8F).noSave().noSummon().build("generic_arrow"));

    // Cocogoat
    public static final RegistryObject<EntityType<IceLotus>> ICE_LOTUS = REGISTER.register("ice_lotus", () -> EntityType.Builder.<IceLotus>of(IceLotus::new, MobCategory.MISC).sized(1.0F, 1.0F).noSummon().noSave().build("ice_lotus"));
    public static final RegistryObject<EntityType<HailShower>> ICE_SHOWER = REGISTER.register("ice_shower", () -> EntityType.Builder.<HailShower>of(HailShower::new, MobCategory.MISC).sized(1.0F, 1.0F).noSummon().noSave().build("ice_shower"));
    public static final RegistryObject<EntityType<IceCone>> ICE_CONE = REGISTER.register("ice_cone", () -> EntityType.Builder.<IceCone>of(IceCone::new, MobCategory.MISC).sized(2.0F, 2.0F).noSave().noSummon().build("ice_cone"));

    // Wavelyn
    public static final RegistryObject<EntityType<SoulFireball>> SOUL_FIREBALL = REGISTER.register("soul_fireball", () -> EntityType.Builder.<SoulFireball>of(SoulFireball::new, MobCategory.MISC).sized(0.8F, 0.8F).noSave().noSummon().build("soul_fireball"));

    public static final RegistryObject<EntityType<StunEntity>> STUN_ENTITY = REGISTER.register("stun_entity", () -> EntityType.Builder.<StunEntity>of(StunEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).noSave().noSummon().build("stun_entity"));
    public static final RegistryObject<EntityType<ScorchMarkEntity>> SCORCH_MARK = REGISTER.register("scorch_mark", () -> EntityType.Builder.<ScorchMarkEntity>of(ScorchMarkEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).noSave().noSummon().build("scorch_mark"));
    public static final RegistryObject<EntityType<GroundCracksEntity>> GROUND_CRACKS = REGISTER.register("ground_cracks", () -> EntityType.Builder.<GroundCracksEntity>of(GroundCracksEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).noSave().noSummon().build("ground_cracks"));
}
