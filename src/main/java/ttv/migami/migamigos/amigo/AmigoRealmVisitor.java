package ttv.migami.migamigos.amigo;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.AmigoEntityType;

import java.util.Optional;

public class AmigoRealmVisitor implements CustomSpawner {
    private int nextTick;

    public AmigoRealmVisitor() {
    }

    @Override
    public int tick(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies) {
        /*if (!Config.COMMON.gunnerMobs.gunnerMobPatrols.get()) {
            return 0;
        }*/

        RandomSource random = level.random;
        --this.nextTick;

        if (this.nextTick > 0) {
            return 0;
        }

        /*int fixedDaysInterval = Config.COMMON.gunnerMobs.patrolIntervalDays.get();
        int randomIntervalMin = Config.COMMON.gunnerMobs.randomIntervalMinTicks.get();
        int randomIntervalMax = Config.COMMON.gunnerMobs.randomIntervalMaxTicks.get();*/

        int fixedDaysInterval = 0;
        int randomIntervalMin = 100;
        int randomIntervalMax = 100;

        if (fixedDaysInterval > 0) {
            this.nextTick += fixedDaysInterval * 24000;
        } else {
            this.nextTick += randomIntervalMin + random.nextInt(randomIntervalMax - randomIntervalMin + 1);
        }

        // Adds a chance for the Patrol to spawn in either Daytime or Nighttime
        this.nextTick += random.nextInt(200);

        long dayTime = level.getDayTime() / 24000L;

        //int minimumDays = Config.COMMON.gunnerMobs.minimumDaysForPatrols.get();
        int minimumDays = 0;
        if (dayTime < minimumDays) {
            return 0;
        }

        int playerCount = level.players().size();
        if (playerCount < 1) {
            return 0;
        }

        Player randomPlayer = level.players().get(random.nextInt(playerCount));
        if (randomPlayer.isSpectator() || level.isCloseToVillage(randomPlayer.blockPosition(), 2)) {
            return 0;
        }

        BlockPos.MutableBlockPos spawnPos = randomPlayer.blockPosition().mutable()
            .move((24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1),
                  0,
                  (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1));

        if (!level.hasChunksAt(spawnPos.getX() - 10, spawnPos.getZ() - 10, spawnPos.getX() + 10, spawnPos.getZ() + 10)) {
            return 0;
        }

        Holder<Biome> biome = level.getBiome(spawnPos);
        if (biome.is(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
            return 0;
        }

        Optional<EntityType<?>> amigo = ForgeRegistries.ENTITY_TYPES.getValues().stream().filter(entityType -> entityType instanceof AmigoEntityType<?>).findAny();

        if (amigo.isEmpty()) {
            return 0;
        }

        AmigoEntity amigoEntity = (AmigoEntity) amigo.get().create(level);

        if (amigoEntity != null) {
            amigoEntity.setPos(spawnPos.getCenter());
            level.addFreshEntity(amigoEntity);

            BlockPos.MutableBlockPos walkTo = randomPlayer.blockPosition().mutable()
                    .move((5 + random.nextInt(15)) * (random.nextBoolean() ? -1 : 1),
                            0,
                            (5 + random.nextInt(15)) * (random.nextBoolean() ? -1 : 1));

            amigoEntity.getNavigation().moveTo(walkTo.getX(), walkTo.getY(), walkTo.getZ(), 1.0F);

            Component message = Component.translatable("broadcast.migamigos.amigo_visitor").withStyle(ChatFormatting.WHITE);
            level.getServer().getPlayerList().broadcastSystemMessage(message, false);
        }

        return 1;
    }
}