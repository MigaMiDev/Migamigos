package ttv.migami.migamigos.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.migamigos.entity.AmigoEntity;

@Mod.EventBusSubscriber
public class CommonEntityEvents {
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (event.isMounting()) {
            return;
        }

        if (event.getEntityMounting() instanceof ServerPlayer) {
            Entity vehicle = event.getEntityBeingMounted();

            if (vehicle == null) return;

            for (Entity entity : vehicle.getPassengers()) {
                if (entity instanceof AmigoEntity amigo && amigo.isFollowing()) {
                    amigo.stopRiding();
                }
            }
        }
    }
}
