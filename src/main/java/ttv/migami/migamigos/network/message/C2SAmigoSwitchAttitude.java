package ttv.migami.migamigos.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.migamigos.common.network.ServerPlayHandler;

import java.util.UUID;

public class C2SAmigoSwitchAttitude extends PlayMessage<C2SAmigoSwitchAttitude>
{
    private UUID uuid;
    private int attitude;

    public C2SAmigoSwitchAttitude() {}

    public C2SAmigoSwitchAttitude(UUID uuid, int attitude)
    {
        this.uuid = uuid;
        this.attitude = attitude;
    }

    @Override
    public void encode(C2SAmigoSwitchAttitude message, FriendlyByteBuf buffer)
    {
        buffer.writeUUID(message.uuid);
        buffer.writeInt(message.attitude);
    }

    @Override
    public C2SAmigoSwitchAttitude decode(FriendlyByteBuf buffer)
    {
        return new C2SAmigoSwitchAttitude(buffer.readUUID(), buffer.readInt());
    }

    @Override
    public void handle(C2SAmigoSwitchAttitude message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.amigoAttitudeSwitch(player, message.uuid, message.attitude);
            }
        });
        context.setHandled(true);
    }
}
