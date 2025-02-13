package ttv.migami.migamigos.client;

import com.mrcrayfish.framework.api.data.login.ILoginData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.Validate;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.common.CustomAmigo;
import ttv.migami.migamigos.common.CustomAmigoLoader;
import ttv.migami.migamigos.network.message.S2CMessageUpdateAmigos;

import java.util.Map;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class CustomAmigoManager
{
    private static Map<ResourceLocation, CustomAmigo> customAmigoMap;

    public static boolean updateCustomAmigos(S2CMessageUpdateAmigos message)
    {
        return updateCustomAmigos(message.getCustomAmigos());
    }

    private static boolean updateCustomAmigos(Map<ResourceLocation, CustomAmigo> customAmigoMap)
    {
        CustomAmigoManager.customAmigoMap = customAmigoMap;
        return true;
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event)
    {
        customAmigoMap = null;
    }

    public static class LoginData implements ILoginData
    {
        @Override
        public void writeData(FriendlyByteBuf buffer)
        {
            Validate.notNull(CustomAmigoLoader.get());
            CustomAmigoLoader.get().writeCustomAmigos(buffer);
        }

        @Override
        public Optional<String> readData(FriendlyByteBuf buffer)
        {
            Map<ResourceLocation, CustomAmigo> customAmigos = CustomAmigoLoader.readCustomAmigos(buffer);
            CustomAmigoManager.updateCustomAmigos(customAmigos);
            return Optional.empty();
        }
    }
}