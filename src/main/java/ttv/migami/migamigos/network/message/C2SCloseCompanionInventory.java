package ttv.migami.migamigos.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.migamigos.common.network.ServerPlayHandler;

import java.util.UUID;

public class C2SCloseCompanionInventory extends PlayMessage<C2SCloseCompanionInventory>
{
    private UUID uuid;

    public C2SCloseCompanionInventory() {}

    public C2SCloseCompanionInventory(UUID uuid)
    {
        this.uuid = uuid;
    }

    @Override
    public void encode(C2SCloseCompanionInventory message, FriendlyByteBuf buffer)
    {
        buffer.writeUUID(message.uuid);
    }

    @Override
    public C2SCloseCompanionInventory decode(FriendlyByteBuf buffer)
    {
        return new C2SCloseCompanionInventory(buffer.readUUID());
    }

    @Override
    public void handle(C2SCloseCompanionInventory message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.closeCompanionInventory(player, message.uuid);
            }
        });
        context.setHandled(true);
    }
}
