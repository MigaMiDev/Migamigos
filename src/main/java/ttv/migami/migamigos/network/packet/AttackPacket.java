package ttv.migami.migamigos.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import ttv.migami.migamigos.entity.AmigoEntity;

import java.util.function.Supplier;

public class AttackPacket {
    private final int entityId;

    public AttackPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(AttackPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
    }

    public static AttackPacket decode(FriendlyByteBuf buffer) {
        return new AttackPacket(buffer.readInt());
    }

    public static void handle(AttackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Level world = player.level();
                Entity entity = world.getEntity(msg.entityId);
                if (entity instanceof AmigoEntity amigoEntity) {
                    amigoEntity.basicAttack();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}