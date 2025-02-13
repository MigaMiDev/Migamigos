package ttv.migami.migamigos.client;

import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.client.screen.AmigoScreen;
import ttv.migami.migamigos.init.ModContainers;
import ttv.migami.migamigos.init.ModItems;

import java.lang.reflect.Field;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {
    private static Field mouseOptionsField;

    public static void setup() {
        registerScreenFactories();
    }

    private static void registerScreenFactories() {
        MenuScreens.register(ModContainers.AMIGO_CONTAINER.get(), AmigoScreen::new);
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof MouseSettingsScreen) {
            MouseSettingsScreen screen = (MouseSettingsScreen) event.getScreen();
            if (mouseOptionsField == null) {
                mouseOptionsField = ObfuscationReflectionHelper.findField(MouseSettingsScreen.class, "f_96218_");
                mouseOptionsField.setAccessible(true);
            }
            try {
                OptionsList list = (OptionsList) mouseOptionsField.get(screen);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void onRegisterCreativeTab(IEventBus bus)
    {
        DeferredRegister<CreativeModeTab> register = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MOD_ID);
        CreativeModeTab.Builder builder = CreativeModeTab.builder();
            builder.title(Component.translatable("itemGroup." + Reference.MOD_ID));
            builder.icon(() -> {
                ItemStack stack = new ItemStack(ModItems.COCOGOAT_SPAWN_FLOWER.get());
                return stack;
            });
        builder.displayItems((flags, output) ->
        {
            ModItems.REGISTER.getEntries().forEach(registryObject ->
            {
                output.accept(registryObject.get());
            });
        });
        register.register("creative_tab", builder::build);
        register.register(bus);
    }

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.Key event) {
        /*if (KeyBinds.BLESSING_MENU.isDown()) {
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageBlessings());
        }*/
    }

}
