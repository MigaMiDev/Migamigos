package ttv.migami.migamigos.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.migamigos.common.network.ServerPlayHandler;

import java.util.UUID;

public class C2SCloseAmigoInventory extends PlayMessage<C2SCloseAmigoInventory>
{
    private UUID uuid;

    public C2SCloseAmigoInventory() {}

    public C2SCloseAmigoInventory(UUID uuid)
    {
        this.uuid = uuid;
    }

    @Override
    public void encode(C2SCloseAmigoInventory message, FriendlyByteBuf buffer)
    {
        buffer.writeUUID(message.uuid);
    }

    @Override
    public C2SCloseAmigoInventory decode(FriendlyByteBuf buffer)
    {
        return new C2SCloseAmigoInventory(buffer.readUUID());
    }

    @Override
    public void handle(C2SCloseAmigoInventory message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.closeAmigoInventory(player, message.uuid);
            }
        });
        context.setHandled(true);
    }
}
