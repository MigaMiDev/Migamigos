package ttv.migami.migamigos.network;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import com.mrcrayfish.framework.api.network.MessageDirection;
import net.minecraft.resources.ResourceLocation;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.network.message.C2SAmigoSwitchAttitude;
import ttv.migami.migamigos.network.message.C2SCloseAmigoInventory;
import ttv.migami.migamigos.network.message.C2SMessageToggleArmorPiece;
import ttv.migami.migamigos.network.message.S2CMessageUpdateAmigos;

public class PacketHandler
{
    private static FrameworkNetwork playChannel;

    public static void init()
    {
        playChannel = FrameworkAPI.createNetworkBuilder(new ResourceLocation(Reference.MOD_ID, "play"), 1)
                .registerPlayMessage(S2CMessageUpdateAmigos.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(C2SCloseAmigoInventory.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SAmigoSwitchAttitude.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageToggleArmorPiece.class, MessageDirection.PLAY_SERVER_BOUND)
                .build();
    }

    public static FrameworkNetwork getPlayChannel()
    {
        return playChannel;
    }
}
