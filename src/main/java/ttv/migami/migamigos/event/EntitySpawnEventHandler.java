package ttv.migami.migamigos.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.amigo.Shysaw;
import ttv.migami.migamigos.init.ModEntities;

import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntitySpawnEventHandler {

    @SubscribeEvent
    public static void onSpecialSpawn(MobSpawnEvent.FinalizeSpawn event) {
        ServerLevel level = (ServerLevel) event.getEntity().level();
        if (event.getEntity() instanceof Vindicator vindicator) {
            if (!isInsideWoodlandMansion(level, vindicator.blockPosition())) return;

            AABB searchBox = vindicator.getBoundingBox().inflate(500);

            List<Shysaw> existingEntities = level.getEntitiesOfClass(
                    Shysaw.class, searchBox
            );

            if (existingEntities.isEmpty() && level.random.nextFloat() < 0.15F) {
                event.setCanceled(true);

                Shysaw shysaw = new Shysaw(ModEntities.SHYSAW.get(), level);
                shysaw.setEnemigo(true);
                shysaw.moveTo(vindicator.getX(), vindicator.getY(), vindicator.getZ(), vindicator.getYRot(), vindicator.getXRot());
                level.addFreshEntity(shysaw);
            }
        }

    }

    private static boolean isInsideWoodlandMansion(ServerLevel level, BlockPos pos) {
        return level.structureManager().getStructureAt(
                pos,
                Objects.requireNonNull(level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                        .get(BuiltinStructures.WOODLAND_MANSION))
                ).isValid();
    }
}