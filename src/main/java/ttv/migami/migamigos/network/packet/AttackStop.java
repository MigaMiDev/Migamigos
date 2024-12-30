package ttv.migami.migamigos.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import ttv.migami.migamigos.entity.Companion;

import java.util.function.Supplier;

public class AttackStop {
    private final int entityId;

    public AttackStop(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(AttackStop msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
    }

    public static AttackStop decode(FriendlyByteBuf buffer) {
        return new AttackStop(buffer.readInt());
    }

    public static void handle(AttackStop msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Level world = player.level();
                Entity entity = world.getEntity(msg.entityId);
                if (entity instanceof Companion companion) {
                    companion.stopAttacks();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}