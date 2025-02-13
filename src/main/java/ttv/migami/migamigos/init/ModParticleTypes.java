package ttv.migami.migamigos.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.migamigos.Reference;

/**
 * Author: MigaMi
 */
public class ModParticleTypes {

    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Reference.MOD_ID);

    public static final RegistryObject<SimpleParticleType> FROST_GLINT = REGISTER.register("frost_glint", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FREEZE_BREEZE = REGISTER.register("freeze_breeze", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SMALL_FREEZE_BREEZE = REGISTER.register("small_freeze_breeze", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> SOUL_LAVA_PARTICLE = REGISTER.register("soul_lava", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> SMOKE = REGISTER.register("smoke", () -> new SimpleParticleType(true));

}
