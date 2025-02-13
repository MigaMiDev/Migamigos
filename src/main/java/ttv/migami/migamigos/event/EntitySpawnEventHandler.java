package ttv.migami.migamigos.event;

import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.AmigoEntity;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntitySpawnEventHandler {

    @SubscribeEvent
    public static void onSpecialSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getEntity() instanceof AmigoEntity amigoEntity) {
            //amigoEntity.loadAmigo();
            //amigoEntity.refreshData();
        }
    }
}