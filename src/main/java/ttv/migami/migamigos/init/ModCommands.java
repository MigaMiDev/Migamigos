package ttv.migami.migamigos.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.AmigoEntityType;
import ttv.migami.migamigos.entity.fx.GroundCracksEntity;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(

                Commands.literal("migamigos")
                        .then(Commands.literal("summon")
                                .then(Commands.literal("amigo")
                                        .then(Commands.argument("entityType", ResourceLocationArgument.id())
                                                .suggests((context, builder) -> {
                                                    return SharedSuggestionProvider.suggestResource(
                                                            ForgeRegistries.ENTITY_TYPES.getKeys().stream()
                                                                    .filter(key -> ForgeRegistries.ENTITY_TYPES.getValue(key) instanceof AmigoEntityType),
                                                            builder
                                                    );
                                                })
                                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                                        .executes(context -> {
                                                            CommandSourceStack source = context.getSource();
                                                            ResourceLocation entityId = ResourceLocationArgument.getId(context, "entityType");
                                                            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityId);

                                                            if (!(type instanceof AmigoEntityType)) {
                                                                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
                                                                        .create("Entity type must be an Amigo!");
                                                            }

                                                            Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                            return executeSpawnAmigo(source, (AmigoEntityType<?>) type, pos);
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("enemigo")
                                        .then(Commands.argument("entityType", ResourceLocationArgument.id())
                                                .suggests((context, builder) -> {
                                                    return SharedSuggestionProvider.suggestResource(
                                                            ForgeRegistries.ENTITY_TYPES.getKeys().stream()
                                                                    .filter(key -> ForgeRegistries.ENTITY_TYPES.getValue(key) instanceof AmigoEntityType),
                                                            builder
                                                    );
                                                })
                                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                                        .executes(context -> {
                                                            CommandSourceStack source = context.getSource();
                                                            ResourceLocation entityId = ResourceLocationArgument.getId(context, "entityType");
                                                            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityId);

                                                            if (!(type instanceof AmigoEntityType)) {
                                                                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
                                                                        .create("Entity type must be an Amigo!");
                                                            }

                                                            Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                            return executeSpawnEnemigo(source, (AmigoEntityType<?>) type, pos);
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("heartless")
                                        .then(Commands.argument("entityType", ResourceLocationArgument.id())
                                                .suggests((context, builder) -> {
                                                    return SharedSuggestionProvider.suggestResource(
                                                            ForgeRegistries.ENTITY_TYPES.getKeys().stream()
                                                                    .filter(key -> ForgeRegistries.ENTITY_TYPES.getValue(key) instanceof AmigoEntityType),
                                                            builder
                                                    );
                                                })
                                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                                        .executes(context -> {
                                                            CommandSourceStack source = context.getSource();
                                                            ResourceLocation entityId = ResourceLocationArgument.getId(context, "entityType");
                                                            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityId);

                                                            if (!(type instanceof AmigoEntityType)) {
                                                                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
                                                                        .create("Entity type must be an Amigo!");
                                                            }

                                                            Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                            return executeSummonHeartless(source, (AmigoEntityType<?>) type, pos);
                                                        })
                                                )
                                        )
                                )
                        )

        );
    }

    private static int executeSpawnAmigo(CommandSourceStack source, AmigoEntityType<?> amigo, Vec3 pos) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.nullToEmpty("You do not have permission to execute this command."));
            return 0;
        }

        ServerLevel level = source.getLevel();

        AmigoEntity amigoEntity = amigo.create(source.getLevel());
        spawnAmigo(level, amigoEntity, pos);

        source.sendSuccess(() -> Component.nullToEmpty("Say hello to the Amigo!"), true);
        return 1;
    }

    public static void spawnAmigo(ServerLevel level, AmigoEntity amigoEntity, Vec3 pos) {
        amigoEntity.setPos(pos.x, pos.y, pos.z);
        level.addFreshEntity(amigoEntity);
    }

    public static void summonLightingBolt(Level level, Vec3 pos) {
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        lightningBolt.setPos(pos);
        lightningBolt.setVisualOnly(true);
        level.addFreshEntity(lightningBolt);
    }

    public static void summonGroundCracks(Level level, Vec3 pos) {
        GroundCracksEntity groundCracks = new GroundCracksEntity(level, BlockPos.containing(pos), 200, 6);
        groundCracks.setPos(pos);
        level.addFreshEntity(groundCracks);
    }


    private static int executeSpawnEnemigo(CommandSourceStack source, AmigoEntityType<?> amigo, Vec3 pos) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.nullToEmpty("You do not have permission to execute this command."));
            return 0;
        }

        ServerLevel level = source.getLevel();

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            source.sendFailure(Component.nullToEmpty("Enemigos can't spawn in Peaceful!"));
            return 0;
        }

        AmigoEntity amigoEntity = amigo.create(source.getLevel());
        spawnEnemigo(level, amigoEntity, pos);

        source.sendSuccess(() -> Component.nullToEmpty("Spawned an Enemigo!"), true);
        return 1;
    }

    public static void spawnEnemigo(ServerLevel level, AmigoEntity amigoEntity, Vec3 pos) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("IsEnemigo", true);
        amigoEntity.load(tag);

        amigoEntity.setPos(pos.x, pos.y, pos.z);
        level.addFreshEntity(amigoEntity);

        summonLightingBolt(level, amigoEntity.getPosition(1F));
        summonGroundCracks(level, amigoEntity.getPosition(1F));
    }

    private static int executeSummonHeartless(CommandSourceStack source, AmigoEntityType<?> amigo, Vec3 pos) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.nullToEmpty("You do not have permission to execute this command."));
            return 0;
        }

        ServerLevel level = source.getLevel();

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            source.sendFailure(Component.nullToEmpty("Enemigos can't spawn in Peaceful!"));
            return 0;
        }

        AmigoEntity amigoEntity = amigo.create(source.getLevel());
        summonHeartless(level, amigoEntity, pos);

        source.sendSuccess(() -> Component.nullToEmpty("Summoned a Heartless"), true);
        return 1;
    }

    public static void summonHeartless(ServerLevel level, AmigoEntity amigoEntity, Vec3 pos) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("IsHeartless", true);
        amigoEntity.load(tag);

        amigoEntity.setPos(pos.x, pos.y, pos.z);
        level.addFreshEntity(amigoEntity);

        amigoEntity.heartlessParticles(level);
    }
}