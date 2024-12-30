package ttv.migami.migamigos;

import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import com.mrcrayfish.framework.api.event.PlayerEvents;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;
import ttv.migami.migamigos.client.ClientHandler;
import ttv.migami.migamigos.client.KeyBinds;
import ttv.migami.migamigos.client.MetaLoader;
import ttv.migami.migamigos.entity.client.companion.CompanionRenderer;
import ttv.migami.migamigos.entity.client.projectile.GenericArrowRenderer;
import ttv.migami.migamigos.entity.client.summon.IceLotusRenderer;
import ttv.migami.migamigos.entity.client.summon.IceShowerRenderer;
import ttv.migami.migamigos.event.ModCommonEventBus;
import ttv.migami.migamigos.init.*;
import ttv.migami.migamigos.network.PacketHandler;

@Mod(Reference.MOD_ID)
public class Migamigos {
    public static boolean debugging = false;
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
    public static boolean jegLoaded = false;

    public Migamigos() {
        //ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        //ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PlayerEvents.class);
        MinecraftForge.EVENT_BUS.register(ModCommonEventBus.class);
        ModEffects.REGISTER.register(bus);
        ModEnchantments.REGISTER.register(bus);
        ModItems.REGISTER.register(bus);
        ModBlocks.REGISTER.register(bus);
        ModEntities.REGISTER.register(bus);
        ModParticleTypes.REGISTER.register(bus);
        ModSounds.REGISTER.register(bus);
        ModContainers.REGISTER.register(bus);
        bus.addListener(this::onCommonSetup);
        bus.addListener(this::onClientSetup);
        bus.addListener(this::onGatherData);

        // OooOoOh spooky!
        GeckoLib.initialize();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FrameworkClientAPI.registerDataLoader(MetaLoader.getInstance());
            //ClientHandler.onRegisterCreativeTab(bus);
            bus.addListener(KeyBinds::registerKeyMappings);
        });
        jegLoaded = ModList.get().isLoaded("jeg");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientHandler::setup);
        EntityRenderers.register(ModEntities.COMPANION.get(), CompanionRenderer::new);

        EntityRenderers.register(ModEntities.GENERIC_ARROW.get(), GenericArrowRenderer::new);

        // Cocogoat
        EntityRenderers.register(ModEntities.ICE_LOTUS.get(), IceLotusRenderer::new);
        EntityRenderers.register(ModEntities.ICE_SHOWER.get(), IceShowerRenderer::new);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
        {
            PacketHandler.init();
            PacketHandler.register();
        });
    }

    private void onGatherData(GatherDataEvent event)
    {

    }

}
