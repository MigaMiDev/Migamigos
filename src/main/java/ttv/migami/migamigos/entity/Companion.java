package ttv.migami.migamigos.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import ttv.migami.migamigos.CompanionAnimations;
import ttv.migami.migamigos.common.container.CompanionContainer;
import ttv.migami.migamigos.entity.ai.*;
import ttv.migami.migamigos.entity.projectile.GenericArrow;
import ttv.migami.migamigos.entity.summon.IceLotus;
import ttv.migami.migamigos.entity.summon.IceShower;
import ttv.migami.migamigos.init.ModParticleTypes;
import ttv.migami.migamigos.init.ModSounds;
import ttv.migami.migamigos.network.PacketHandler;
import ttv.migami.migamigos.network.packet.AttackPacket;
import ttv.migami.migamigos.network.packet.AttackStop;
import ttv.migami.migamigos.network.packet.SpecialAttackPacket;
import ttv.migami.migamigos.network.packet.UltimateAttackPacket;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class Companion extends PathfinderMob implements GeoEntity {
    //private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> DATA_HAS_PLAYER = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TOLERANCE = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_ATTACKING = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_SPECIAL_ATTACKING = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SPECIAL_COOLDOWN = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_ULTIMATE_ATTACKING = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ULTIMATE_COOLDOWN = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CONTAINER_OPEN = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.BOOLEAN);

    /**
     *  If set to true, the Companion will attack any Entity instanceof Enemy by default.
     *  If set to false, the Companion will only attack Targets.
     */
    private static final EntityDataAccessor<Boolean> DATA_ATTACK_ANY_ENEMY = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.BOOLEAN);

    /**
     *  If set to true, the Companion will defend the Player from attacks.
     *  If set to false, the Companion will also defend the Player and other Companions from the same Player.
     */
    private static final EntityDataAccessor<Boolean> DEFEND_PLAYER_ONLY = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.BOOLEAN);

    /**
     *  If set to true, the Companion will focus on attacking the Player's target.
     *  If set to false, the Companion will focus on attacking it's target, defending and supporting.
     */
    private static final EntityDataAccessor<Boolean> DATA_FOCUS_ON_MAIN_TARGET = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.BOOLEAN);

    /**
     *  If set to false, the Companion will not be allowed to wander.
     */
    private static final EntityDataAccessor<Boolean> DATA_ALLOW_WANDERING = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.BOOLEAN);

    /**
     *  If set to false, the Companion will not follow the Player, and Stay instead.
     */
    private static final EntityDataAccessor<Boolean> DATA_FOLLOWING = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.BOOLEAN);

    /**
     *  The position of this Companion's Post (where the Companion will go back after being set not to follow).
     */
    private static final EntityDataAccessor<Vector3f> DATA_POST_POS = SynchedEntityData.defineId(Companion.class, EntityDataSerializers.VECTOR3);


    private final SimpleContainer inventory = new SimpleContainer(14);

    private static final double RECRUIT_CHANCE = 0.25;

    private Player followingPlayer = null;
    private UUID followingPlayerUUID = null;

    private static final int MAX_TOLERANCE = 3;
    private static final int MAX_HEALING_TIMER = 60;

    public int particleTick = 0;
    public int healingTimer = 0;

    public Companion(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2F)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new PersonalSpaceGoal(this));
        //this.goalSelector.addGoal(5, new CompanionMeleeAttackGoal(this, 1.6, true));
        this.goalSelector.addGoal(5, new CompanionRangedAttackGoal<>(this, 1.6));
        this.goalSelector.addGoal(6, new MoveBackToPostGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new FollowPlayerGoal(this, 1.6D, 8.0F, 100.0F, false));
        this.goalSelector.addGoal(7, new CompanionFarmGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new WaterAvoidingCompanionStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new PlayerHurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new CompanionHurtByPlayerGoal(this));
        this.targetSelector.addGoal(2, new PlayerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new CompanionHurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(7, new NearestAttackableCompanionTargetGoal<>(this, Mob.class, 5, true, true, (livingEntity) -> livingEntity instanceof Enemy && !(livingEntity instanceof Creeper)));}

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Random random = new Random();

        if (this.getHealth() >= this.getMaxHealth()||
                (this.getHealth() < this.getMaxHealth() && !player.getMainHandItem().is(Items.AZURE_BLUET)))  {
            if (player.getUUID().equals(this.followingPlayerUUID)) {
                if (!player.level().isClientSide) {
                    NetworkHooks.openScreen(
                            (ServerPlayer) player,
                            new SimpleMenuProvider(
                                    (id, inventory, serverPlayer) -> new CompanionContainer(id, inventory, this),
                                    this.getDisplayName()
                            ),
                            buf -> buf.writeUUID(this.getUUID())
                    );
                    this.setTarget(null);
                    this.stopAttacks();
                    this.setContainerOpen(true);
                    this.navigation.stop();
                    this.level().playSound(null, this, ModSounds.BACKPACK_OPENING.get(), SoundSource.PLAYERS, 0.3F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (itemstack.is(Items.AZURE_BLUET)) {

            // Recruit Companion & Heal
            if (this.getTarget() != player) {

                // Heal
                if (this.getTolerance() < MAX_TOLERANCE || this.getHealth() < this.getMaxHealth()) {
                    itemstack.shrink(1);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 3; i++) {
                            serverLevel.sendParticles(ParticleTypes.HEART,
                                    this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                    this.getY() + random.nextDouble() * this.getBbHeight(),
                                    this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                    1, 0, 0, 0, 0.1);
                        }
                    }
                    this.setHealth(this.getHealth() + this.getHealth() / 2);
                    this.setTolerance(this.getTolerance() + 1);
                    return InteractionResult.SUCCESS;
                }

                // Recruit
                if (!this.hasPlayer()) {
                    itemstack.shrink(1);
                    if (random.nextDouble() < RECRUIT_CHANCE) {
                        this.followingPlayer = player;
                        this.followingPlayerUUID = player.getUUID();
                        this.setHasPlayer(true);
                        this.setTarget(null);
                        this.stopAttacks();
                        this.setAttackingAnyEnemy(false);
                        if (this.level() instanceof ServerLevel serverLevel) {
                            for (int i = 0; i < 7; i++) {
                                serverLevel.sendParticles(ParticleTypes.HEART,
                                        this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                        this.getY() + random.nextDouble() * this.getBbHeight(),
                                        this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                        1, 0, 0, 0, 0.1);
                            }
                        }

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
                }
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getTarget()!= null && this.getTarget().isDeadOrDying()) {
            this.setTarget(null);
        }

        if (this.getTarget() instanceof Companion companionTarget && companionTarget.getPlayer() == this.getPlayer()) {
            this.setTarget(null);
        }

        // Heal if outside of combat and not full health
        if (this.getHealth() < this.getMaxHealth() && !this.isAttacking() && !this.isDeadOrDying()) {
            this.healingTimer--;
            if (this.healingTimer <= 0) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 1; i++) {
                        serverLevel.sendParticles(ParticleTypes.HEART,
                                this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                this.getY() + random.nextDouble() * this.getBbHeight(),
                                this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                                1, 0, 0, 0, 0.1);
                    }
                }
                this.setHealth(this.getHealth() + 2);
                this.healingTimer = MAX_HEALING_TIMER;
            }
        } else {
            this.healingTimer = MAX_HEALING_TIMER;
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

        // Particle Tick
        if (--this.particleTick > 0) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 2; i++) {
                    serverLevel.sendParticles(ModParticleTypes.FROST_GLINT.get(),
                            this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + random.nextDouble() * this.getBbHeight(),
                            this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                            1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }

        this.setSpecialCooldown(this.getSpecialCooldown() - 1);
        this.setUltimateCooldown(this.getUltimateCooldown() - 1);
    }

    @Override
    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        return false;
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

    public void stopAttacks() {
        this.setAttacking(false);
        this.setSpecialAttacking(false);
        this.setUltimateAttacking(false);
    }

    public void basicAttack() {
        if (this.isDeadOrDying()) return;

        LivingEntity target = this.getTarget();
        if (target == null) return;

        this.setTarget(target);

        //Arrow arrow = new Arrow(this.level(), this);
        Vec3 direction = target.position().subtract(this.position()).normalize();
        Vec3 lookAngle = this.getLookAngle();
        GenericArrow arrow = new GenericArrow(this.level(), this);
        //arrow.shoot(direction.x, direction.y, direction.z, 2.0F, 1.0F);
        //arrow.shoot(lookAngle.x, lookAngle.y, lookAngle.z, 2.0F, 1.0F);

        this.level().playSound(null, this, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        this.level().addFreshEntity(arrow);

        Vec3 knockbackDirection = direction.scale(-0.5);
        this.setDeltaMovement(knockbackDirection.x, knockbackDirection.y, knockbackDirection.z);
        this.hasImpulse = true;
    }

    public void specialAttack() {
        if (this.isDeadOrDying()) return;

        this.particleTick = 10;
        this.invulnerableTime = 20;

        IceLotus entity = new IceLotus(this.level(), this);
        entity.setPos(this.getX(), this.getY(), this.getZ());
        this.level().addFreshEntity(entity);

        this.level().playSound(null, this, SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

        Vec3 lookAngle = this.getLookAngle();
        Vec3 knockbackDirection = lookAngle.scale(-2.25);
        this.setDeltaMovement(knockbackDirection.x, 0.1, knockbackDirection.z);
        this.hasImpulse = true;
    }

    public void ultimateAttack() {
        if (this.isDeadOrDying()) return;

        this.invulnerableTime = 60;

        IceShower entity = new IceShower(this.level(), this);
        entity.setPos(this.getX(), this.getY(), this.getZ());
        this.level().addFreshEntity(entity);
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

    public boolean isAttacking() {
        return this.entityData.get(DATA_IS_ATTACKING);
    }

    public void setAttacking(boolean isAttacking) {
        this.entityData.set(DATA_IS_ATTACKING, isAttacking);
    }

    public boolean isSpecialAttacking() {
        return this.entityData.get(DATA_IS_SPECIAL_ATTACKING);
    }

    public void setSpecialAttacking(boolean isAttacking) {
        this.entityData.set(DATA_IS_SPECIAL_ATTACKING, isAttacking);
    }

    public int getSpecialCooldown() {
        return this.entityData.get(DATA_SPECIAL_COOLDOWN);
    }

    public void setSpecialCooldown(int cooldown) {
        this.entityData.set(DATA_SPECIAL_COOLDOWN, cooldown);
    }

    public boolean isUltimateAttacking() {
        return this.entityData.get(DATA_IS_ULTIMATE_ATTACKING);
    }

    public void setUltimateAttacking(boolean isAttacking) {
        this.entityData.set(DATA_IS_ULTIMATE_ATTACKING, isAttacking);
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

    // Companion commands
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HAS_PLAYER, false);
        this.entityData.define(DATA_TOLERANCE, MAX_TOLERANCE);
        this.entityData.define(DATA_IS_ATTACKING, false);
        this.entityData.define(DATA_IS_SPECIAL_ATTACKING, false);
        this.entityData.define(DATA_SPECIAL_COOLDOWN, 0);
        this.entityData.define(DATA_IS_ULTIMATE_ATTACKING, false);
        this.entityData.define(DATA_ULTIMATE_COOLDOWN, 0);
        this.entityData.define(DATA_CONTAINER_OPEN, false);

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

        // Save Companion Commands
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

        // Load Companion Commands
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
        controllers.add(CompanionAnimations.genericWalkIdleController(this));
        controllers.add(CompanionAnimations.genericAttackAnimation(this)
                .setCustomInstructionKeyframeHandler((customInstructionKeyframeEvent -> {
                    String instruction = customInstructionKeyframeEvent.getKeyframeData().getInstructions();
                    switch(instruction) {
                        case "stopAttacking;" -> PacketHandler.INSTANCE.sendToServer(new AttackStop(this.getId()));

                        case "basicAttack;" -> PacketHandler.INSTANCE.sendToServer(new AttackPacket(this.getId()));

                        case "attackSpecial;" -> PacketHandler.INSTANCE.sendToServer(new SpecialAttackPacket(this.getId()));

                        case "attackUltimate;" -> PacketHandler.INSTANCE.sendToServer(new UltimateAttackPacket(this.getId()));
                    }
                })));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}