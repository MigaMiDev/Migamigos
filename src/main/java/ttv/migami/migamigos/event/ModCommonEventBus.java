package ttv.migami.migamigos.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.Companion;
import ttv.migami.migamigos.entity.summon.IceLotus;
import ttv.migami.migamigos.entity.summon.IceShower;
import ttv.migami.migamigos.init.ModEntities;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCommonEventBus {

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.COMPANION.get(), Companion.createAttributes().build());
        event.put(ModEntities.ICE_LOTUS.get(), IceLotus.createAttributes().build());
        event.put(ModEntities.ICE_SHOWER.get(), IceShower.createAttributes().build());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Companion attacker && event.getEntity() instanceof Companion target) {
            if (attacker.getPlayer() != null && attacker.getPlayer().equals(target.getPlayer())) {
                event.setCanceled(true);
            }
        }
    }

}