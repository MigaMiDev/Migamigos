package ttv.migami.migamigos.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.AmigoEntity;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class TameTargetEventHandler {

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity newTarget = event.getNewTarget();

        if (newTarget instanceof AmigoEntity amigo) {
            if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
                if (tamable.getOwnerUUID() != null && amigo.getPlayer() != null && tamable.getOwnerUUID().equals(amigo.getPlayer().getUUID()))
                    event.setCanceled(true);
            }
        }
    }
}