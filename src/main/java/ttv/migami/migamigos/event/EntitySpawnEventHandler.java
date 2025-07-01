package ttv.migami.migamigos.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.guardvillagers.GuardEntityType;
import ttv.migami.migamigos.Migamigos;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.amigo.Shysaw;
import ttv.migami.migamigos.init.ModCommands;
import ttv.migami.migamigos.init.ModEntities;

import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntitySpawnEventHandler {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        if (event.getEntity().getType() == EntityType.IRON_GOLEM || event.getEntity().getType() == EntityType.SNOW_GOLEM) {
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                    mob,
                    AmigoEntity.class,
                    10,
                    true,
                    false,
                    entity -> entity instanceof AmigoEntity amigo && (amigo.isHeartless() || amigo.isEnemigo())
            ));
        }

        if (Migamigos.guardsLoaded && event.getEntity().getType() == GuardEntityType.GUARD.get()) {
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                    mob,
                    AmigoEntity.class,
                    10,
                    true,
                    false,
                    entity -> entity instanceof AmigoEntity amigo && (amigo.isHeartless() || amigo.isEnemigo())
            ));
        }

        /*if (Migamigos.recruitsLoaded &&
                (event.getEntity().getType() == ModEntityTypes.RECRUIT.get() ||
                        event.getEntity().getType() == ModEntityTypes.RECRUIT.get() ||
                        event.getEntity().getType() == ModEntityTypes.RECRUIT_SHIELDMAN.get() ||
                        event.getEntity().getType() == ModEntityTypes.BOWMAN.get() ||
                        event.getEntity().getType() == ModEntityTypes.CAPTAIN.get() ||
                        event.getEntity().getType() == ModEntityTypes.CROSSBOWMAN.get() ||
                        event.getEntity().getType() == ModEntityTypes.NOMAD.get() ||
                        event.getEntity().getType() == ModEntityTypes.PATROL_LEADER.get() ||
                        event.getEntity().getType() == ModEntityTypes.SCOUT.get())) {
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                    mob,
                    AmigoEntity.class,
                    10,
                    true,
                    false,
                    entity -> entity instanceof AmigoEntity amigo && (amigo.isHeartless() || amigo.isEnemigo())
            ));
        }*/

        /*if (event.getEntity().getType().builtInRegistryHolder().is(ModTags.GOLEMS)) {
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                    mob,
                    AmigoEntity.class,
                    10,
                    true,
                    false,
                    entity -> ((AmigoEntity) entity).isHeartless()
            ));
        }*/
    }

    @SubscribeEvent
    public static void onSpecialSpawn(MobSpawnEvent.FinalizeSpawn event) {
        ServerLevel level = (ServerLevel) event.getEntity().level();

        if (event.getEntity() instanceof Raider) {
            if (level.random.nextFloat() < 0.1F) {
                if (event.getEntity() instanceof Vindicator vindicator && vindicator.getCurrentRaid() != null &&
                        (event.getSpawnType().equals(MobSpawnType.NATURAL) || event.getSpawnType().equals(MobSpawnType.EVENT))) {
                    Shysaw shysaw = new Shysaw(ModEntities.SHYSAW.get(), level);
                    shysaw.setEnemigo(true);
                    shysaw.moveTo(vindicator.getX(), vindicator.getY(), vindicator.getZ(), vindicator.getYRot(), vindicator.getXRot());
                    level.addFreshEntity(shysaw);

                    List<Player> playersInRaid = getRaidPlayers(vindicator.getCurrentRaid());

                    vindicator.discard();

                    ModCommands.summonLightingBolt(level, shysaw.getPosition(1F));
                    ModCommands.summonGroundCracks(level, shysaw.getPosition(1F));

                    if (!playersInRaid.isEmpty()) {
                        Player player = playersInRaid.get(level.random.nextInt(0, playersInRaid.size()));
                        shysaw.setTarget(player);

                        for (Player playerMessage : playersInRaid) {
                            Component message = Component.translatable("chat.migamigos.foe_appeared")
                                    .withStyle(ChatFormatting.RED);
                            playerMessage.displayClientMessage(message, true);
                        }
                    }
                }
                if (event.getEntity() instanceof Witch witch &&
                        (event.getSpawnType().equals(MobSpawnType.NATURAL) || event.getSpawnType().equals(MobSpawnType.EVENT))) {
                    Shysaw shysaw = new Shysaw(ModEntities.SHYSAW.get(), level);
                    shysaw.setEnemigo(true);
                    shysaw.moveTo(witch.getX(), witch.getY(), witch.getZ(), witch.getYRot(), witch.getXRot());
                    level.addFreshEntity(shysaw);

                    if (witch.getCurrentRaid() != null) {
                        List<Player> playersInRaid = getRaidPlayers(witch.getCurrentRaid());

                        if (!playersInRaid.isEmpty()) {
                            Player player = playersInRaid.get(level.random.nextInt(0, playersInRaid.size()));
                            shysaw.setTarget(player);

                            for (Player playerMessage : playersInRaid) {
                                Component message = Component.translatable("chat.migamigos.foe_appeared")
                                        .withStyle(ChatFormatting.RED);
                                playerMessage.displayClientMessage(message, true);
                            }
                        }
                    }

                    witch.discard();

                    ModCommands.summonLightingBolt(level, shysaw.getPosition(1F));
                    ModCommands.summonGroundCracks(level, shysaw.getPosition(1F));
                }
            }
        }

        if (event.getEntity() instanceof Vindicator vindicator) {
            if (!isInsideWoodlandMansion(level, vindicator.blockPosition())) return;

            AABB searchBox = vindicator.getBoundingBox().inflate(500);

            List<Shysaw> existingEntities = level.getEntitiesOfClass(
                    Shysaw.class, searchBox
            );

            if (existingEntities.isEmpty() && level.random.nextFloat() < 0.15F) {
                event.setCanceled(true);

                Shysaw shysaw = new Shysaw(ModEntities.SHYSAW.get(), level);
                shysaw.setHeartless(true);
                shysaw.moveTo(vindicator.getX(), vindicator.getY(), vindicator.getZ(), vindicator.getYRot(), vindicator.getXRot());
                level.addFreshEntity(shysaw);
            }
        }

    }

    public static List<Player> getRaidPlayers(Raid raid) {
        Level level = raid.getLevel();
        BlockPos center = raid.getCenter();
        int raidRadius = 80;

        AABB raidArea = new AABB(
                center.getX() - raidRadius, level.getMinBuildHeight(),
                center.getZ() - raidRadius,
                center.getX() + raidRadius, level.getMaxBuildHeight(),
                center.getZ() + raidRadius
        );

        List<Player> playersInRaid = level.getEntitiesOfClass(
                Player.class,
                raidArea,
                player -> !player.isSpectator()
        );

        return playersInRaid;
    }

    private static boolean isInsideWoodlandMansion(ServerLevel level, BlockPos pos) {
        return level.structureManager().getStructureAt(
                pos,
                Objects.requireNonNull(level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                        .get(BuiltinStructures.WOODLAND_MANSION))
                ).isValid();
    }

    private static boolean isInsideSwampHut(ServerLevel level, BlockPos pos) {
        return level.structureManager().getStructureAt(
                pos,
                Objects.requireNonNull(level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                        .get(BuiltinStructures.SWAMP_HUT))
        ).isValid();
    }
}