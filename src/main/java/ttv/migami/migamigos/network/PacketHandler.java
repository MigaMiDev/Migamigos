package ttv.migami.migamigos.network;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import com.mrcrayfish.framework.api.network.MessageDirection;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.network.message.C2SAmigoSwitchAttitude;
import ttv.migami.migamigos.network.message.C2SCloseAmigoInventory;
import ttv.migami.migamigos.network.message.C2SMessageToggleArmorPiece;
import ttv.migami.migamigos.network.message.S2CMessageUpdateAmigos;
import ttv.migami.migamigos.network.packet.AttackPacket;
import ttv.migami.migamigos.network.packet.AttackStop;
import ttv.migami.migamigos.network.packet.SpecialAttackPacket;
import ttv.migami.migamigos.network.packet.UltimateAttackPacket;

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

    // Non-Framework

    public static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel INSTANCE;

    public static void register() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(Reference.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        int id = 0;
        INSTANCE.registerMessage(id++, AttackStop.class, AttackStop::encode, AttackStop::decode, AttackStop::handle);
        INSTANCE.registerMessage(id++, AttackPacket.class, AttackPacket::encode, AttackPacket::decode, AttackPacket::handle);
        INSTANCE.registerMessage(id++, SpecialAttackPacket.class, SpecialAttackPacket::encode, SpecialAttackPacket::decode, SpecialAttackPacket::handle);
        INSTANCE.registerMessage(id++, UltimateAttackPacket.class, UltimateAttackPacket::encode, UltimateAttackPacket::decode, UltimateAttackPacket::handle);
    }
}
