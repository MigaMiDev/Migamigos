package ttv.migami.migamigos.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.migamigos.common.network.ServerPlayHandler;

import java.util.UUID;

public class C2SMessageToggleArmorPiece extends PlayMessage<C2SMessageToggleArmorPiece>
{
    private UUID amigoUUID;
    private int armorPiece = 0;

    public C2SMessageToggleArmorPiece() {}

    public C2SMessageToggleArmorPiece(UUID amigoUUID, int armorPiece) {
        this.amigoUUID = amigoUUID;
        this.armorPiece = armorPiece;
    }

    @Override
    public void encode(C2SMessageToggleArmorPiece message, FriendlyByteBuf buffer)
    {
        buffer.writeUUID(message.amigoUUID);
        buffer.writeInt(message.armorPiece);
    }

    @Override
    public C2SMessageToggleArmorPiece decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageToggleArmorPiece(buffer.readUUID(), buffer.readInt());
    }

    @Override
    public void handle(C2SMessageToggleArmorPiece message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.toggleArmorPiece(player, message.amigoUUID, message.armorPiece);
            }
        });
        context.setHandled(true);
    }
}