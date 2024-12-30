package ttv.migami.migamigos.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import ttv.migami.migamigos.common.network.ServerPlayHandler;

import java.util.UUID;

public class C2SCompanionSwitchAttitude extends PlayMessage<C2SCompanionSwitchAttitude>
{
    private UUID uuid;
    private int attitude;

    public C2SCompanionSwitchAttitude() {}

    public C2SCompanionSwitchAttitude(UUID uuid, int attitude)
    {
        this.uuid = uuid;
        this.attitude = attitude;
    }

    @Override
    public void encode(C2SCompanionSwitchAttitude message, FriendlyByteBuf buffer)
    {
        buffer.writeUUID(message.uuid);
        buffer.writeInt(message.attitude);
    }

    @Override
    public C2SCompanionSwitchAttitude decode(FriendlyByteBuf buffer)
    {
        return new C2SCompanionSwitchAttitude(buffer.readUUID(), buffer.readInt());
    }

    @Override
    public void handle(C2SCompanionSwitchAttitude message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.companionAttitudeSwitch(player, message.uuid, message.attitude);
            }
        });
        context.setHandled(true);
    }
}
