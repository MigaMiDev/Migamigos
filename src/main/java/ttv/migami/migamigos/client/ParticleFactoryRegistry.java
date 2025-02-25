package ttv.migami.migamigos.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.client.particle.GlintParticle;
import ttv.migami.migamigos.client.particle.SmokeParticle;
import ttv.migami.migamigos.client.particle.SoulLavaParticle;
import ttv.migami.migamigos.client.particle.SparkParticle;
import ttv.migami.migamigos.init.ModParticleTypes;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleFactoryRegistry
{
    @SubscribeEvent
    public static void onRegisterParticleFactory(RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(ModParticleTypes.FROST_GLINT.get(), GlintParticle.LightBlueProvider::new);
        event.registerSpriteSet(ModParticleTypes.FREEZE_BREEZE.get(), SmokeParticle.BreezeProvider::new);
        event.registerSpriteSet(ModParticleTypes.SMALL_FREEZE_BREEZE.get(), SmokeParticle.SmallBreezeProvider::new);

        event.registerSpriteSet(ModParticleTypes.SOUL_LAVA_PARTICLE.get(), SoulLavaParticle.Provider::new);

        event.registerSpriteSet(ModParticleTypes.SPARK.get(), SparkParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SMOKE.get(), SmokeParticle.Provider::new);
    }
}
