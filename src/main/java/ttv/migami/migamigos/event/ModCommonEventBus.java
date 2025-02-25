package ttv.migami.migamigos.event;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.amigo.Claymore;
import ttv.migami.migamigos.entity.amigo.Cocogoat;
import ttv.migami.migamigos.entity.amigo.Shysaw;
import ttv.migami.migamigos.entity.amigo.Wavelyn;
import ttv.migami.migamigos.entity.summon.HailShower;
import ttv.migami.migamigos.entity.summon.IceLotus;
import ttv.migami.migamigos.init.ModEntities;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCommonEventBus {

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.COCOGOAT.get(), AmigoEntity.createAttributes().build());
        event.put(ModEntities.WAVELYN.get(), AmigoEntity.createAttributes().build());
        event.put(ModEntities.CLAYMORE.get(), AmigoEntity.createAttributes().build());
        event.put(ModEntities.SHYSAW.get(), AmigoEntity.createAttributes().build());

        event.put(ModEntities.ICE_LOTUS.get(), IceLotus.createAttributes().build());
        event.put(ModEntities.ICE_SHOWER.get(), HailShower.createAttributes().build());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof AmigoEntity attacker && event.getEntity() instanceof AmigoEntity target) {
            if (attacker.getPlayer() != null && attacker.getPlayer().equals(target.getPlayer())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
                ModEntities.COCOGOAT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                Cocogoat::checkAmigoSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.WAVELYN.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                Wavelyn::checkAmigoSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.CLAYMORE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                Claymore::checkAmigoSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
        event.register(
                ModEntities.SHYSAW.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.WORLD_SURFACE,
                Shysaw::checkAmigoSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR
        );
    }
}