package ttv.migami.migamigos;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import com.mrcrayfish.framework.api.event.PlayerEvents;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;
import ttv.migami.migamigos.client.ClientHandler;
import ttv.migami.migamigos.client.CustomAmigoManager;
import ttv.migami.migamigos.client.KeyBinds;
import ttv.migami.migamigos.client.MetaLoader;
import ttv.migami.migamigos.common.NetworkAmigoManager;
import ttv.migami.migamigos.datagen.AmigoGen;
import ttv.migami.migamigos.datagen.EntityTagGen;
import ttv.migami.migamigos.entity.client.StunEntityRenderer;
import ttv.migami.migamigos.entity.client.effect.GroundMarkRenderer;
import ttv.migami.migamigos.entity.client.migamigo.AmigoRenderer;
import ttv.migami.migamigos.entity.client.projectile.GenericArrowRenderer;
import ttv.migami.migamigos.entity.client.projectile.cocogoat.IceConeRenderer;
import ttv.migami.migamigos.entity.client.projectile.wavelyn.SoulFireballRenderer;
import ttv.migami.migamigos.entity.client.summon.IceLotusRenderer;
import ttv.migami.migamigos.entity.client.summon.IceShowerRenderer;
import ttv.migami.migamigos.event.ModCommonEventBus;
import ttv.migami.migamigos.event.ServerTickHandler;
import ttv.migami.migamigos.init.*;
import ttv.migami.migamigos.network.PacketHandler;

import java.util.concurrent.CompletableFuture;

@Mod(Reference.MOD_ID)
public class Migamigos {
    public static boolean debugging = false;
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

    public Migamigos() {
        //ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        //ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PlayerEvents.class);
        MinecraftForge.EVENT_BUS.register(ModCommonEventBus.class);
        ModEntities.REGISTER.register(bus);
        ModEffects.REGISTER.register(bus);
        ModEnchantments.REGISTER.register(bus);
        ModItems.REGISTER.register(bus);
        ModBlocks.REGISTER.register(bus);
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
            ClientHandler.onRegisterCreativeTab(bus);
            bus.addListener(KeyBinds::registerKeyMappings);
        });
        MinecraftForge.EVENT_BUS.register(ServerTickHandler.class);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientHandler::setup);
        EntityRenderers.register(ModEntities.COCOGOAT.get(), AmigoRenderer::new);
        EntityRenderers.register(ModEntities.WAVELYN.get(), AmigoRenderer::new);
        EntityRenderers.register(ModEntities.CLAYMORE.get(), AmigoRenderer::new);
        EntityRenderers.register(ModEntities.SHYSAW.get(), AmigoRenderer::new);

        EntityRenderers.register(ModEntities.GENERIC_ARROW.get(), GenericArrowRenderer::new);
        EntityRenderers.register(ModEntities.SOUL_FIREBALL.get(), SoulFireballRenderer::new);

        // Cocogoat
        EntityRenderers.register(ModEntities.ICE_LOTUS.get(), IceLotusRenderer::new);
        EntityRenderers.register(ModEntities.ICE_SHOWER.get(), IceShowerRenderer::new);
        EntityRenderers.register(ModEntities.ICE_CONE.get(), IceConeRenderer::new);

        EntityRenderers.register(ModEntities.STUN_ENTITY.get(), StunEntityRenderer::new);
        EntityRenderers.register(ModEntities.SCORCH_MARK.get(), GroundMarkRenderer::new);
        EntityRenderers.register(ModEntities.GROUND_CRACKS.get(), GroundMarkRenderer::new);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
        {
            PacketHandler.init();
            PacketHandler.register();
            FrameworkAPI.registerLoginData(new ResourceLocation(Reference.MOD_ID, "network_amigo_manager"), NetworkAmigoManager.LoginData::new);
            FrameworkAPI.registerLoginData(new ResourceLocation(Reference.MOD_ID, "custom_amigo_manager"), CustomAmigoManager.LoginData::new);
        });
    }

    private void onGatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        EntityTagGen entityTagGen = new EntityTagGen(output, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), entityTagGen);
        generator.addProvider(event.includeServer(), new AmigoGen(output, lookupProvider));
    }

}
