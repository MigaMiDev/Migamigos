package ttv.migami.migamigos.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import ttv.migami.migamigos.AmigoAnimations;
import ttv.migami.migamigos.Migamigos;
import ttv.migami.migamigos.common.Amigo;
import ttv.migami.migamigos.common.amigo.Action;
import ttv.migami.migamigos.common.container.AmigoContainer;
import ttv.migami.migamigos.entity.ai.*;
import ttv.migami.migamigos.init.ModSounds;
import ttv.migami.migamigos.network.PacketHandler;
import ttv.migami.migamigos.network.packet.AttackStop;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

public class AmigoEntity extends PathfinderMob implements GeoEntity {
    //private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private Amigo amigo = new Amigo();
    protected SoundEvent chime = null;
    private Action currentAction;
    private long comboStartTime;
    protected Item defaultItem = Items.IRON_SWORD;
    protected Item lastItem = Items.AIR;

    public final static int EMOTE_COOLDOWN = 100;

    private static final EntityDataAccessor<String> DATA_NAME = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> DATA_IS_ENEMIGO = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_HEARTLESS = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_SHOW_HELMET = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SHOW_CHESTPLATE = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SHOW_LEGGINGS = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SHOW_BOOTS = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_EYE_EXPRESSION = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HAS_PLAYER = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TOLERANCE = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> DATA_IS_PLAYING_ANIMATION = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_AMIGO_STATE = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> DATA_IS_ATTACKING = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COMBO_COOLDOWN = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SPECIAL_COOLDOWN = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ULTIMATE_COOLDOWN = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> DATA_CONTAINER_OPEN = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_IS_FARMING = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_HARVESTING = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> DATA_EMOTE = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EMOTE_TIMER = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EMOTE_COOLDOWN = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_EXPERIENCE = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LEVEL = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.INT);

    /**
     * These values are additional values. They add to the data values.
     */
    private static final EntityDataAccessor<Float> EXTRA_HEALTH = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> EXTRA_POWER = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.FLOAT);
    // This value is only for actions. For example, Farming.
    private static final EntityDataAccessor<Float> EXTRA_SPEED = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.FLOAT);

    /**
     *  If set to true, the Amigo will attack any Entity instanceof Enemy by default.
     *  If set to false, the Amigo will only attack Targets.
     */
    private static final EntityDataAccessor<Boolean> DATA_ATTACK_ANY_ENEMY = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     *  If set to true, the Amigo will defend the Player from attacks.
     *  If set to false, the Amigo will also defend the Player and other Amigos from the same Player.
     */
    private static final EntityDataAccessor<Boolean> DEFEND_PLAYER_ONLY = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     *  If set to true, the Amigo will focus on attacking the Player's target.
     *  If set to false, the Amigo will focus on attacking it's target, defending and supporting.
     */
    private static final EntityDataAccessor<Boolean> DATA_FOCUS_ON_MAIN_TARGET = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     *  If set to false, the Amigo will not be allowed to wander.
     */
    private static final EntityDataAccessor<Boolean> DATA_ALLOW_WANDERING = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     *  If set to false, the Amigo will not follow the Player, and Stay instead.
     */
    private static final EntityDataAccessor<Boolean> DATA_FOLLOWING = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     *  The position of this Amigo's Post (where the Amigo will go back after being set not to follow).
     */
    private static final EntityDataAccessor<Vector3f> DATA_POST_POS = SynchedEntityData.defineId(AmigoEntity.class, EntityDataSerializers.VECTOR3);


    private final SimpleContainer inventory = new SimpleContainer(14);

    private double recruitChance = 0.25;

    private Player followingPlayer = null;
    private UUID followingPlayerUUID = null;

    private int maxTolerance = 3;
    private int maxHealingTimer = 60;

    public int particleTick = 0;
    public int healingTimer = 0;

    public int blinkTimer = 0;

    public AmigoEntity(EntityType<? extends AmigoEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);

        this.setLeftHanded(false);

        if (pEntityType instanceof AmigoEntityType<?> amigoEntityType) {
            this.amigo = amigoEntityType.getAmigo();
        }

        this.refreshData();
    }

    public void refreshData() {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.amigo.getGeneral().getHealth());
        this.setHealth(this.amigo.getGeneral().getHealth() + this.getExtraHealth());
        this.maxHealingTimer = this.amigo.getGeneral().getHealingTimer();
        this.getAttribute(Attributes.ARMOR).setBaseValue(this.amigo.getGeneral().getArmor());
        this.recruitChance = this.amigo.getGeneral().getRecruitingChance();
        this.maxTolerance = this.amigo.getGeneral().getTolerance();
        this.setTolerance(this.maxTolerance);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.amigo.getGeneral().getSpeed());
    }

    public void setAmigo(Amigo amigo) {
        this.amigo = amigo;
    }

    public Amigo getAmigo() {
        return this.amigo;
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0F)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2F)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    public static boolean checkAmigoSpawnRules(EntityType<? extends AmigoEntity> amigo, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
        return pLevel.getBlockState(pPos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PersonalSpaceGoal(this));
        //this.goalSelector.addGoal(5, new AmigoMeleeAttackGoal(this, 1.6, true));
        //this.goalSelector.addGoal(5, new AmigoRangedAttackGoal<>(this, 1.6));
        this.goalSelector.addGoal(4, new AmigoEatOrDrinkGoal(this));
        this.goalSelector.addGoal(6, new MoveBackToPostGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new FollowPlayerGoal(this, 1.6D, 8.0F, 100.0F, false));
        //this.goalSelector.addGoal(7, new AmigoFarmGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new WaterAvoidingAmigoStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new AmigoLookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new PlayerHurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new AmigoHurtByPlayerGoal(this));
        this.targetSelector.addGoal(2, new PlayerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new AmigoHurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(7, new NearestAttackableAmigoTargetGoal<>(this, LivingEntity.class, 5, true, true, (livingEntity) -> livingEntity instanceof Enemy && !(livingEntity instanceof Creeper) && !(livingEntity instanceof EnderMan) && !(livingEntity instanceof Ravager)));
    }

    public void playChime() {
        if (this.chime != null) {
            this.level().playSound(null, this, this.chime, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isHeartless() || this.isEnemigo()) return InteractionResult.SUCCESS;

        ItemStack itemstack = player.getItemInHand(hand);
        Random random = new Random();

        this.lookAt(EntityAnchorArgument.Anchor.FEET, player.getEyePosition(1F));
        if (this.getEmoteCooldown() <= 0) {
            this.setActiveEmote(AmigoEmotes.WAVE);
        }
        this.playChime();

        Item favoriteItem = ForgeRegistries.ITEMS.getValue(this.amigo.getGeneral().getFavoriteItem());
        boolean canOpen = true;

        if ((this.getHealth() < this.getMaxHealth() || this.getTolerance() < this.maxTolerance)) {
            canOpen = !player.getMainHandItem().is(favoriteItem);
        }
        if ((this.getHealth() >= this.getMaxHealth() && this.getTolerance() >= this.maxTolerance)) {
            canOpen = true;
        }

        if (canOpen)  {
            if (player.getUUID().equals(this.followingPlayerUUID)) {
                this.setContainerOpen(true);
                if (!player.level().isClientSide) {
                    this.setTarget(null);
                    this.stopAttacks();
                    this.navigation.stop();
                    this.level().playSound(null, this, ModSounds.BACKPACK_OPENING.get(), SoundSource.PLAYERS, 0.3F, 1.0F);
                    NetworkHooks.openScreen(
                            (ServerPlayer) player,
                            new SimpleMenuProvider(
                                    (id, inventory, serverPlayer) -> new AmigoContainer(id, inventory, this),
                                    this.getDisplayName()
                            ),
                            buf -> buf.writeUUID(this.getUUID())
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (itemstack.is(favoriteItem)) {

            // Recruit Amigo & Heal
            if (this.getTarget() != player && !this.isAttacking()) {

                // Heal
                if (this.getTolerance() < this.maxTolerance || this.getHealth() < this.getMaxHealth()) {
                    if (this.level() instanceof ServerLevel serverLevel && !this.isEnemigo() && !this.isHeartless()) {
                        for (int i = 0; i < 3; i++) {
                            serverLevel.sendParticles(ParticleTypes.HEART,
                                    this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                    this.getY() + random.nextDouble() * this.getBbHeight(),
                                    this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                    1, 0, 0, 0, 0.1);
                        }
                    }
                    this.setHealth(this.getHealth() + this.getHealth() / 4);
                    this.setTolerance(this.getTolerance() + 1);
                    itemstack.shrink(1);
                    return InteractionResult.SUCCESS;
                }

                // Recruit
                if (!this.hasPlayer() && !this.isEnemigo() && !this.isHeartless()) {

                    //if (player.getPersistentData().getInt(USED_AMIGO_SLOTS_KEY) < 5) {
                        itemstack.shrink(1);
                        if (random.nextDouble() < this.recruitChance) {
                            this.followingPlayer = player;
                            this.followingPlayerUUID = player.getUUID();
                            this.setHasPlayer(true);
                            this.setTarget(null);
                            this.stopAttacks();
                            this.setAttackingAnyEnemy(false);
                            this.setTolerance(this.maxTolerance);
                            if (this.level() instanceof ServerLevel serverLevel) {
                                for (int i = 0; i < 7; i++) {
                                    serverLevel.sendParticles(ParticleTypes.HEART,
                                            this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                            this.getY() + random.nextDouble() * this.getBbHeight(),
                                            this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                            1, 0, 0, 0, 0.1);
                                }
                            }
                            /*CompoundTag playerData = player.getPersistentData();
                            ListTag amigoUUIDs = playerData.getList(AMIGO_UUIDS_KEY, Tag.TAG_STRING);

                            amigoUUIDs.add(StringTag.valueOf(this.getUUID().toString()));
                            playerData.put(AMIGO_UUIDS_KEY, amigoUUIDs);
                            player.getPersistentData().putInt(USED_AMIGO_SLOTS_KEY, player.getPersistentData().getInt(USED_AMIGO_SLOTS_KEY) + 1);*/
                        } else {
                            if (this.level() instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.HEART,
                                        this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                        this.getY() + random.nextDouble() * this.getBbHeight(),
                                        this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                        1, 0, 0, 0, 0.1);
                            }
                        }
                        return InteractionResult.SUCCESS;
                    //}
                }
            }
        } else if (!this.hasPlayer()) {
            Component item = Component.translatable(favoriteItem.getDescriptionId()).withStyle(ChatFormatting.GOLD);
            player.displayClientMessage(Component.translatable("chat.migamigos.favorite_item", item).withStyle(ChatFormatting.WHITE), true);
        }
        return super.mobInteract(player, hand);
    }

    public void blink() {
        this.setEyeExpression(2);
        this.blinkTimer = 3;
    }

    private void performAttack(Entity attacker, int stage) {
        attacker.level().players().forEach(player -> {
            player.displayClientMessage(Component.literal("Attack stage: " + stage), true);
        });
    }

    public void heartlessParticles(ServerLevel serverLevel) {
        serverLevel.sendParticles(ParticleTypes.SQUID_INK,
                this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                this.getY() + random.nextDouble() * this.getBbHeight(),
                this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                20, this.getBbWidth() / 2, this.getBbHeight() / 2, this.getBbWidth() / 2, 0.1);
    }

    public void heartlessDiscard(ServerLevel serverLevel) {
        this.heartlessParticles(serverLevel);
        this.discard();
    }


    public void setActiveEmote(String emote) {
        if (!emote.equals(AmigoEmotes.NONE)) {
            this.setAmigoState(AmigoState.EMOTING);
        }
        if (emote.equals(AmigoEmotes.NONE)) {
            this.setEmote(0);
            this.setEmoteTimer(0);
            this.setEmoteCooldown(0);
        } else if (emote.equals(AmigoEmotes.WAVE)) {
            this.setEmote(1);
            this.setEmoteTimer(40);
            this.setEmoteCooldown(EMOTE_COOLDOWN);
        }
    }

    @Override
    public void tick() {
        super.tick();

        Migamigos.LOGGER.atInfo().log(this.getAmigoState() + " From: " + this);

        if (this.isLeftHanded()) {
            this.setLeftHanded(false);
        }
        if (this.getMainHandItem() != this.defaultItem.getDefaultInstance() && this.tickCount < 20) {
            this.setItemSlot(EquipmentSlot.MAINHAND, this.getDefaultItem().getDefaultInstance());
        }

        if (this.isPassenger()) {
            this.setAmigoState(AmigoState.SITTING);
        }

        if (this.isEnemigo()) {
            if (this.level().getDifficulty().equals(Difficulty.PEACEFUL)) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    this.heartlessDiscard(serverLevel);
                }
            }
        }
        if (this.isHeartless()) {
            if (this.level().getDifficulty().equals(Difficulty.PEACEFUL)) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    this.heartlessDiscard(serverLevel);
                }
            }
            if (isDeadOrDying()) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    this.heartlessDiscard(serverLevel);
                }
            }
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 0, false, false));
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 0, false, false));
        }
        if (this.isEnemigo()) {
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 1, false, false));
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 1, false, false));
        }

        if (this.getEmoteTimer() > 0) {
            this.setEmoteTimer(this.getEmoteTimer() - 1);
        }
        else {
            this.setEmote(0);
        }
        if (this.getEmoteCooldown() > 0) {
            this.setEmoteCooldown(this.getEmoteCooldown() - 1);
        }

        if (this.hurtTime == 3) {
            this.setEyeExpression(0);
        }
        if (this.isDeadOrDying() || (this.hurtTime > 3)) {
            this.setEyeExpression(2);
        } else if (!this.isAttacking() && this.random.nextInt(80) == 0) {
            this.blink();
        }
        if (!this.isAttacking() && --this.blinkTimer == 0) {
            this.setEyeExpression(0);
        }

        if (this.getTarget() != null && this.getTarget() == this.getPlayer()) {
            this.setTarget(null);
        }

        if (this.getTarget() != null && this.getTarget().isDeadOrDying()) {
            this.setTarget(null);
        }

        if (this.getTarget() instanceof AmigoEntity amigoEntityTarget && amigoEntityTarget.getPlayer() == this.getPlayer()) {
            if (this.hasPlayer() && amigoEntityTarget.hasPlayer()) {
                this.setTarget(null);
            }
        }

        if (this.currentAction != null) {
            long elapsedTicks = this.level().getGameTime() - comboStartTime;

            if (elapsedTicks >= this.currentAction.getNextKeyframe()) {
                this.currentAction.executeNextStage(this);
            }

            if (this.currentAction.isComplete()) {
                this.currentAction = null;
            }
            if (!this.isAttacking() &&
                    !this.getAmigoState().equals(AmigoState.COMBO_ATTACKING) &&
                    !this.getAmigoState().equals(AmigoState.SPECIAL_ATTACKING) &&
                    !this.getAmigoState().equals(AmigoState.ULTIMATE_ATTACKING)) {
                this.currentAction = null;
            }
        }

        // Heal if outside of combat and not full health
        if (!this.isHeartless()) {
            if (this.getHealth() < this.getMaxHealth() && !this.isAttacking() && !this.isDeadOrDying()) {
                this.healingTimer--;
                if (this.healingTimer <= 0) {
                    if (this.level() instanceof ServerLevel serverLevel && !this.isEnemigo()) {
                        for (int i = 0; i < 1; i++) {
                            serverLevel.sendParticles(ParticleTypes.HEART,
                                    this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                    this.getY() + random.nextDouble() * this.getBbHeight(),
                                    this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                    1, 0, 0, 0, 0.1);
                        }
                    }
                    this.setHealth(this.getHealth() + 2);
                    this.healingTimer = maxHealingTimer;
                }
            } else {
                this.healingTimer = maxHealingTimer;
            }
        }

        // Update Player every tick
        if (this.followingPlayer == null && this.followingPlayerUUID != null && !this.level().isClientSide) {
            if (this.level() instanceof ServerLevel serverLevel) {
                Player player = serverLevel.getPlayerByUUID(this.followingPlayerUUID);
                if (player != null) {
                    this.followingPlayer = player;
                    this.setHasPlayer(true);
                }
            }
        }

        this.setComboCooldown(this.getComboCooldown() - 1);
        this.setSpecialCooldown(this.getSpecialCooldown() - 1);
        this.setUltimateCooldown(this.getUltimateCooldown() - 1);
    }

    @Override
    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
        //return this.hasPlayer();
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);

        dropAllInventoryItems();
    }

    private void dropAllInventoryItems() {
        if (!this.level().isClientSide && this.getInventory() != null) {
            for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
                ItemStack stack = this.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    this.spawnAtLocation(stack);
                    this.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    public void setDefaultItem(Item item) {
        this.defaultItem = item;
    }

    public Item getDefaultItem() {
        return this.defaultItem;
    }

    public void stopAttacks() {
        this.setAmigoState(AmigoState.IDLE);
        this.setPlayingAnimation(false);
    }

    public void startAttacking(Action action) {
        this.startAction(action);
        this.setAttacking(true);
        this.setPlayingAnimation(true);
        this.getNavigation().stop();
    }

    public void startAction(Action combo) {
        this.currentAction = combo;
        this.comboStartTime = this.level().getGameTime();
    }

    public Action createExampleCombo() {
        List<Integer> keyframeTimings = List.of(10, 30, 55);
        List<Consumer<AmigoEntity>> attackActions = List.of(
                attacker -> performAttack(attacker, 1),
                attacker -> performAttack(attacker, 2),
                attacker -> performAttack(attacker, 3)
        );
        return new Action(keyframeTimings, attackActions);
    }

    public Action basicAction() {
        return createExampleCombo();
    }

    public void basicAttack() {
    }

    public Action specialAction() {
        return createExampleCombo();
    }

    public void specialAttack() {
    }

    public Action ultimateAction() {
        return createExampleCombo();
    }

    public void ultimateAttack() {
    }

    public int getExperience() {
        return this.entityData.get(DATA_EXPERIENCE);
    }

    public void setExperience(int experience) {
        this.entityData.set(DATA_EXPERIENCE, experience);
    }

    public int getAmigoLevel() {
        return this.entityData.get(DATA_LEVEL);
    }

    public void setLevel(int level) {
        this.entityData.set(DATA_LEVEL, level);
    }

    /**
     * Calculates the required experience for the next mastery level.
     * Formula: base * (growthFactor ^ currentLevel)
     *
     * @param currentLevel The current mastery level.
     * @return The experience required for the next level.
     */
    public static int getExperienceForNextLevel(int currentLevel) {
        int base = 10;
        double growthFactor = 1.1;
        return (int) (base * Math.pow(growthFactor, currentLevel));
        //return base + (currentLevel) + 1;
    }

    public String getAmigoName() {
        return this.entityData.get(DATA_NAME);
    }

    public void setAmigoName(String name) {
        this.entityData.set(DATA_NAME, name);
    }

    public boolean isEnemigo() {
        return this.entityData.get(DATA_IS_ENEMIGO);
    }

    public void setEnemigo(boolean enemigo) {
        if (enemigo) {
            this.clearPlayer();
            this.setAttackingAnyEnemy(true);
        }
        this.entityData.set(DATA_IS_ENEMIGO, enemigo);
    }

    public boolean isHeartless() {
        return this.entityData.get(DATA_IS_HEARTLESS);
    }

    public void setHeartless(boolean heartless) {
        if (heartless) {
            this.clearPlayer();
            this.setAttackingAnyEnemy(true);
        }
        this.entityData.set(DATA_IS_HEARTLESS, heartless);
    }

    public boolean isShowingHelmet() {
        return this.entityData.get(DATA_SHOW_HELMET);
    }

    public void setShowHelmet(boolean showHelmet) {
        this.entityData.set(DATA_SHOW_HELMET, showHelmet);
    }

    public boolean isShowingChestplate() {
        return this.entityData.get(DATA_SHOW_CHESTPLATE);
    }

    public void setShowChestplate(boolean showChestplate) {
        this.entityData.set(DATA_SHOW_CHESTPLATE, showChestplate);
    }

    public boolean isShowingLeggings() {
        return this.entityData.get(DATA_SHOW_LEGGINGS);
    }

    public void setShowLeggings(boolean showLeggings) {
        this.entityData.set(DATA_SHOW_LEGGINGS, showLeggings);
    }

    public boolean isShowingBoots() {
        return this.entityData.get(DATA_SHOW_BOOTS);
    }

    public void setShowBoots(boolean showBoots) {
        this.entityData.set(DATA_SHOW_BOOTS, showBoots);
    }

    public int getEyeExpression() {
        return this.entityData.get(DATA_EYE_EXPRESSION);
    }

    public void setEyeExpression(int eyeExpression) {
        this.entityData.set(DATA_EYE_EXPRESSION, eyeExpression);
    }

    public boolean hasPlayer() {
        return this.entityData.get(DATA_HAS_PLAYER);
    }

    public void setHasPlayer(boolean hasPlayer) {
        this.entityData.set(DATA_HAS_PLAYER, hasPlayer);
    }

    public void clearPlayer() {
        this.setHasPlayer(false);
        this.followingPlayer = null;
        this.followingPlayerUUID = null;
    }

    public boolean isPlayingAnimation() {
        return this.entityData.get(DATA_IS_PLAYING_ANIMATION);
    }

    public void setPlayingAnimation(boolean playingAnimation) {
        this.entityData.set(DATA_IS_PLAYING_ANIMATION, playingAnimation);
    }

    public String getAmigoState() {
        return this.entityData.get(DATA_AMIGO_STATE);
    }

    public void setAmigoState(String state) {
        this.entityData.set(DATA_AMIGO_STATE, state);
    }

    public boolean isAttacking() {
        return this.entityData.get(DATA_IS_ATTACKING);
    }

    public void setAttacking(boolean att) {
        this.entityData.set(DATA_IS_ATTACKING, att);
    }

    public int getComboCooldown() {
        return this.entityData.get(DATA_COMBO_COOLDOWN);
    }

    public void setComboCooldown(int cooldown) {
        this.entityData.set(DATA_COMBO_COOLDOWN, cooldown);
    }

    public int getSpecialCooldown() {
        return this.entityData.get(DATA_SPECIAL_COOLDOWN);
    }

    public void setSpecialCooldown(int cooldown) {
        this.entityData.set(DATA_SPECIAL_COOLDOWN, cooldown);
    }

    public int getUltimateCooldown() {
        return this.entityData.get(DATA_ULTIMATE_COOLDOWN);
    }

    public void setUltimateCooldown(int cooldown) {
        this.entityData.set(DATA_ULTIMATE_COOLDOWN, cooldown);
    }

    public boolean wantsToAttack(LivingEntity pTarget, LivingEntity pOwner) {
        return true;
    }

    public int getTolerance() {
        return this.entityData.get(DATA_TOLERANCE);
    }

    public void setTolerance(int tolerance) {
        this.entityData.set(DATA_TOLERANCE, tolerance);
    }

    public boolean isContainerOpen() {
        return this.entityData.get(DATA_CONTAINER_OPEN);
    }

    public void setContainerOpen(boolean open) {
        this.entityData.set(DATA_CONTAINER_OPEN, open);
    }

    public boolean isFarming() {
        return this.entityData.get(DATA_IS_FARMING);
    }

    public void setIsFarming(boolean farming) {
        this.entityData.set(DATA_IS_FARMING, farming);
    }

    public boolean isHarvesting() {
        return this.entityData.get(DATA_IS_HARVESTING);
    }

    public void setIsHarvesting(boolean harvesting) {
        this.entityData.set(DATA_IS_HARVESTING, harvesting);
    }

    public int getEmote() {
        return this.entityData.get(DATA_EMOTE);
    }

    public void setEmote(int emote) {
        this.entityData.set(DATA_EMOTE, emote);
    }

    public int getEmoteTimer() {
        return this.entityData.get(DATA_EMOTE_TIMER);
    }

    public void setEmoteTimer(int emote) {
        this.entityData.set(DATA_EMOTE_TIMER, emote);
    }

    public int getEmoteCooldown() {
        return this.entityData.get(DATA_EMOTE_COOLDOWN);
    }

    public void setEmoteCooldown(int emote) {
        this.entityData.set(DATA_EMOTE_COOLDOWN, emote);
    }

    // Additional values
    public void setExtraHealth(float extraHealth) {
        this.entityData.set(EXTRA_HEALTH, extraHealth);
    }

    public float getExtraHealth() {
        return this.entityData.get(EXTRA_HEALTH);
    }

    public void setExtraPower(float extraDamage) {
        this.entityData.set(EXTRA_POWER, extraDamage);
    }

    public float getExtraPower() {
        return this.entityData.get(EXTRA_POWER);
    }


    // Amigo commands
    public boolean isAttackingAnyEnemy() {
        return this.entityData.get(DATA_ATTACK_ANY_ENEMY);
    }

    public void setAttackingAnyEnemy(boolean value) {
        this.entityData.set(DATA_ATTACK_ANY_ENEMY, value);
    }

    public boolean isDefendingPlayerOnly() {
        return this.entityData.get(DEFEND_PLAYER_ONLY);
    }

    public void setDefendPlayerOnly(boolean value) {
        this.entityData.set(DEFEND_PLAYER_ONLY, value);
    }

    public boolean isFocusingOnMainTarget() {
        return this.entityData.get(DATA_FOCUS_ON_MAIN_TARGET);
    }

    public void setFocusOnMainTarget(boolean value) {
        this.entityData.set(DATA_FOCUS_ON_MAIN_TARGET, value);
    }

    public boolean canWander() {
        return this.entityData.get(DATA_ALLOW_WANDERING);
    }

    public void setAllowWandering(boolean value) {
        this.entityData.set(DATA_ALLOW_WANDERING, value);
    }

    public boolean isFollowing() {
        return this.entityData.get(DATA_FOLLOWING);
    }

    public void setFollowing(boolean value) {
        this.entityData.set(DATA_FOLLOWING, value);
    }

    public Vector3f getPostPos() {
        return this.entityData.get(DATA_POST_POS);
    }

    public void setPostPos(Vector3f pos) {
        this.entityData.set(DATA_POST_POS, pos);
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Nullable
    public LivingEntity getPlayer() {
        UUID uuid = this.followingPlayerUUID;
        return uuid == null ? null : this.level().getPlayerByUUID(uuid);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isHeartless() || this.isEnemigo()) {
            return null;
        }
        return this.chime;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 300;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_NAME, "Amigo");

        this.entityData.define(DATA_AMIGO_STATE, AmigoState.IDLE);

        this.entityData.define(DATA_IS_ENEMIGO, false);
        this.entityData.define(DATA_IS_HEARTLESS, false);

        this.entityData.define(DATA_SHOW_HELMET, true);
        this.entityData.define(DATA_SHOW_CHESTPLATE, true);
        this.entityData.define(DATA_SHOW_LEGGINGS, true);
        this.entityData.define(DATA_SHOW_BOOTS, true);
        this.entityData.define(DATA_EYE_EXPRESSION, 0);
        this.entityData.define(DATA_EXPERIENCE, 0);
        this.entityData.define(DATA_LEVEL, 1);
        this.entityData.define(DATA_HAS_PLAYER, false);
        this.entityData.define(DATA_TOLERANCE, maxTolerance);
        this.entityData.define(DATA_IS_PLAYING_ANIMATION, false);
        this.entityData.define(DATA_IS_ATTACKING, false);
        this.entityData.define(DATA_COMBO_COOLDOWN, 0);
        this.entityData.define(DATA_SPECIAL_COOLDOWN, 0);
        this.entityData.define(DATA_ULTIMATE_COOLDOWN, 0);
        this.entityData.define(DATA_CONTAINER_OPEN, false);
        this.entityData.define(DATA_IS_FARMING, false);
        this.entityData.define(DATA_IS_HARVESTING, false);
        this.entityData.define(DATA_EMOTE, 0);
        this.entityData.define(DATA_EMOTE_TIMER, 0);
        this.entityData.define(DATA_EMOTE_COOLDOWN, 0);

        this.entityData.define(EXTRA_HEALTH, 0.0F);
        this.entityData.define(EXTRA_POWER, 0.0F);

        this.entityData.define(DATA_ATTACK_ANY_ENEMY, true);
        this.entityData.define(DEFEND_PLAYER_ONLY, false);
        this.entityData.define(DATA_FOCUS_ON_MAIN_TARGET, false);
        this.entityData.define(DATA_ALLOW_WANDERING, true);
        this.entityData.define(DATA_FOLLOWING, true);
        this.entityData.define(DATA_POST_POS, new Vector3f(0, 0, 0));
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putBoolean("IsEnemigo", this.isEnemigo());
        compound.putBoolean("IsHeartless", this.isHeartless());

        // Experience & Level
        compound.putInt("AmigoExperience", this.getExperience());
        compound.putInt("AmigoLevel", this.getAmigoLevel());

        // Additional values
        compound.putFloat("AmigoExtraHealth", this.getExtraHealth());
        compound.putFloat("AmigoExtraDamage", this.getExtraPower());

        // Inventory
        ListTag items = new ListTag();
        for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemStack = this.inventory.getItem(i);
            if (!itemStack.isEmpty()) {
                CompoundTag slot = new CompoundTag();
                slot.putByte("Slot", (byte)i);
                itemStack.save(slot);
                items.add(slot);
            }
        }
        compound.put("Items", items);

        if (this.followingPlayerUUID != null) {
            compound.putUUID("FollowingPlayerUUID", this.followingPlayerUUID);
        }

        // Hide/Show Armor
        compound.putBoolean("ShowHelmet", this.isShowingHelmet());
        compound.putBoolean("ShowChestplate", this.isShowingChestplate());
        compound.putBoolean("ShowLeggings", this.isShowingLeggings());
        compound.putBoolean("ShowBoots", this.isShowingBoots());

        // Save Amigo Commands
        compound.putBoolean("AttackAnyEnemy", this.isAttackingAnyEnemy());
        compound.putBoolean("DefendPlayerOnly", this.isDefendingPlayerOnly());
        compound.putBoolean("FocusOnMainTarget", this.isFocusingOnMainTarget());
        compound.putBoolean("AllowWandering", this.canWander());
        compound.putBoolean("Following", this.isFollowing());

        // PostPos
        Vector3f postPos = this.getPostPos();
        compound.putFloat("PostPosX", postPos.x());
        compound.putFloat("PostPosY", postPos.y());
        compound.putFloat("PostPosZ", postPos.z());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("IsEnemigo")) {
            this.setEnemigo(compound.getBoolean("IsEnemigo"));
        }
        if (compound.contains("IsHeartless")) {
            this.setHeartless(compound.getBoolean("IsHeartless"));
        }

        // Experience & Level
        if (compound.contains("AmigoExperience")) {
            this.setExperience(compound.getInt("AmigoExperience"));
        }
        if (compound.contains("AmigoLevel")) {
            this.setLevel(compound.getInt("AmigoLevel"));
        }

        // Additional values
        if (compound.contains("AmigoExtraHealth")) {
            this.setExtraHealth(compound.getFloat("AmigoExtraHealth"));
        }
        if (compound.contains("AmigoExtraDamage")) {
            this.setExtraPower(compound.getFloat("AmigoExtraDamage"));
        }

        // Inventory
        ListTag items = compound.getList("Items", 10);
        for(int i = 0; i < items.size(); ++i) {
            CompoundTag itemsCompound = items.getCompound(i);
            int slot = itemsCompound.getByte("Slot") & 255;
            if (slot < this.inventory.getContainerSize()) {
                this.inventory.setItem(slot, ItemStack.of(itemsCompound));
            }
        }

        if (compound.hasUUID("FollowingPlayerUUID")) {
            this.followingPlayerUUID = compound.getUUID("FollowingPlayerUUID");
        }

        // Hide/Show Armor
        if (compound.contains("ShowHelmet")) {
            this.setShowHelmet(compound.getBoolean("ShowHelmet"));
        }
        if (compound.contains("ShowChestplate")) {
            this.setShowChestplate(compound.getBoolean("ShowChestplate"));
        }
        if (compound.contains("ShowLeggings")) {
            this.setShowLeggings(compound.getBoolean("ShowLeggings"));
        }
        if (compound.contains("ShowBoots")) {
            this.setShowBoots(compound.getBoolean("ShowBoots"));
        }

        // Load Amigo Commands
        if (compound.contains("AttackAnyEnemy")) {
            this.setAttackingAnyEnemy(compound.getBoolean("AttackAnyEnemy"));
        }
        if (compound.contains("DefendPlayerOnly")) {
            this.setDefendPlayerOnly(compound.getBoolean("DefendPlayerOnly"));
        }
        if (compound.contains("FocusOnMainTarget")) {
            this.setFocusOnMainTarget(compound.getBoolean("FocusOnMainTarget"));
        }
        if (compound.contains("AllowWandering")) {
            this.setAllowWandering(compound.getBoolean("AllowWandering"));
        }
        if (compound.contains("Following")) {
            this.setFollowing(compound.getBoolean("Following"));
        }

        // PostPos
        if (compound.contains("PostPosX") && compound.contains("PostPosY") && compound.contains("PostPosZ")) {
            float x = compound.getFloat("PostPosX");
            float y = compound.getFloat("PostPosY");
            float z = compound.getFloat("PostPosZ");
            this.setPostPos(new Vector3f(x, y, z));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(AmigoAnimations.genericWalkIdleController(this));
        controllers.add(AmigoAnimations.genericEmoteController(this));
        controllers.add(AmigoAnimations.genericEatController(this));
        controllers.add(AmigoAnimations.genericFarmController(this));
        controllers.add(AmigoAnimations.genericAttackAnimation(this)
                .setParticleKeyframeHandler((customInstructionKeyframeEvent -> {
                    String instruction = customInstructionKeyframeEvent.getKeyframeData().getEffect()   ;
                    switch(instruction) {
                        case "eyesOpen" -> this.setEyeExpression(0);
                        case "eyesSemiClosed" -> this.setEyeExpression(1);
                        case "eyesClosed" -> this.setEyeExpression(2);
                        case "eyesWinkRight" -> this.setEyeExpression(3);
                        case "eyesWinkLeft" -> this.setEyeExpression(4);
                    }
                }))
                .setCustomInstructionKeyframeHandler((customInstructionKeyframeEvent -> {
                    String instruction = customInstructionKeyframeEvent.getKeyframeData().getInstructions();
                    switch(instruction) {
                        case "stopAttacking;" -> PacketHandler.INSTANCE.sendToServer(new AttackStop(this.getId()));
                        /*case "basicAttack;" -> PacketHandler.INSTANCE.sendToServer(new AttackPacket(this.getId()));
                        case "attackSpecial;" -> PacketHandler.INSTANCE.sendToServer(new SpecialAttackPacket(this.getId()));
                        case "attackUltimate;" -> PacketHandler.INSTANCE.sendToServer(new UltimateAttackPacket(this.getId()));*/
                    }
                })));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}