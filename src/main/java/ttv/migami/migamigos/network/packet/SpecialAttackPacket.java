package ttv.migami.migamigos.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import ttv.migami.migamigos.entity.Companion;

import java.util.function.Supplier;

public class SpecialAttackPacket {
    private final int entityId;

    public SpecialAttackPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(SpecialAttackPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
    }

    public static SpecialAttackPacket decode(FriendlyByteBuf buffer) {
        return new SpecialAttackPacket(buffer.readInt());
    }

    public static void handle(SpecialAttackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Level world = player.level();
                Entity entity = world.getEntity(msg.entityId);
                if (entity instanceof Companion companion) {
                    companion.specialAttack();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}