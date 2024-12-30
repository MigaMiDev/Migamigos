package ttv.migami.migamigos.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.jetbrains.annotations.Nullable;
import ttv.migami.migamigos.entity.Companion;
import ttv.migami.migamigos.entity.summon.SummonEntity;
import ttv.migami.migamigos.event.CompanionProjectileHitEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class CustomProjectileEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final Predicate<Entity> PROJECTILE_TARGETS = input -> input != null && input.isPickable() && !input.isSpectator();

    public UUID playerUUID;
    public int ownerID;
    public LivingEntity owner;
    public boolean collateral = false;
    public boolean affectedByGravity = false;
    public double modifiedGravity = -0.04;
    public int life = 100;
    public float damage = 51.0F;
    public boolean checkForCollisions = false;
    public double speed = 3.5D;

    public CustomProjectileEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public CustomProjectileEntity(EntityType<?> pEntityType, Level pLevel, LivingEntity owner) {
        this(pEntityType, pLevel);
        if (owner instanceof Companion companion && companion.getPlayer() != null) {
            this.playerUUID = companion.getPlayer().getUUID();
        }
        this.ownerID = owner.getId();
        this.owner = owner;
        this.life = 60;

        /* Get speed and set motion */
        Vec3 dir = this.getDirection(owner);
        double speed = this.speed;
        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();

        /* Spawn the projectile half way between the previous and current position */
        double posX = owner.xOld + (owner.getX() - owner.xOld) / 2.0;
        double posY = owner.yOld + (owner.getY() - owner.yOld) / 2.0 + owner.getEyeHeight();
        double posZ = owner.zOld + (owner.getZ() - owner.zOld) / 2.0;
        this.setPos(posX, posY, posZ);
    }

    public LivingEntity getOwner() {
        return this.owner;
    }

    @Override
    public void tick()
    {
        super.tick();

        this.updateHeading();
        this.onProjectileTick();

        Vec3 startVec = this.position();
        Vec3 endVec = startVec.add(this.getDeltaMovement());
        HitResult result = rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if(result.getType() != HitResult.Type.MISS)
        {
            endVec = result.getLocation();
        }

        List<EntityResult> hitEntities = null;
        if(!this.collateral)
        {
            EntityResult entityResult = this.findEntityOnPath(startVec, endVec);
            if(entityResult != null)
            {
                hitEntities = Collections.singletonList(entityResult);
            }
        }
        else
        {
            hitEntities = this.findEntitiesOnPath(startVec, endVec);
        }

        if(hitEntities != null && hitEntities.size() > 0)
        {
            for(EntityResult entityResult : hitEntities)
            {
                result = new EntityHitResult(entityResult.getEntity());
                if(((EntityHitResult) result).getEntity() instanceof Player)
                {
                    Player player = (Player) ((EntityHitResult) result).getEntity();

                    if(this.owner instanceof Player && !((Player) this.owner).canHarmPlayer(player))
                    {
                        result = null;
                    }
                }
                if(result != null)
                {
                    this.onHit(result, startVec, endVec);
                }
            }
        }
        else
        {
            this.onHit(result, startVec, endVec);
        }

        if (this.checkForCollisions) {
            AABB boundingBox = this.getBoundingBox();
            List<Entity> nearbyEntities = this.level().getEntities(this, boundingBox);

            for (Entity entity : nearbyEntities) {
                if (entity != this && entity != this.owner && this.getBoundingBox().intersects(entity.getBoundingBox())) {
                    this.onHitEntity(entity);

                    if (!this.collateral) {
                        this.remove(RemovalReason.KILLED);
                    }

                    entity.invulnerableTime = 0;
                }
            }
        }

        double nextPosX = this.getX() + this.getDeltaMovement().x();
        double nextPosY = this.getY() + this.getDeltaMovement().y();
        double nextPosZ = this.getZ() + this.getDeltaMovement().z();
        this.setPos(nextPosX, nextPosY, nextPosZ);

        if(this.affectedByGravity)
        {
            this.setDeltaMovement(this.getDeltaMovement().add(0, this.modifiedGravity, 0));
        }

        if(this.tickCount >= this.life)
        {
            if(this.isAlive())
            {
                this.onExpired();
            }
            this.remove(RemovalReason.KILLED);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private EntityResult getHitResult(Entity entity, Vec3 startVec, Vec3 endVec)
    {
        double expandHeight = entity instanceof Player && !entity.isCrouching() ? 0.0625 : 0.0;
        AABB boundingBox = entity.getBoundingBox();
        boundingBox = boundingBox.expandTowards(0, expandHeight, 0);

        Vec3 hitPos = boundingBox.clip(startVec, endVec).orElse(null);
        if(hitPos == null)
        {
            return null;
        }

        return new EntityResult(entity, hitPos);
    }

    protected void onHit(HitResult result, Vec3 startVec, Vec3 endVec)
    {
        if(MinecraftForge.EVENT_BUS.post(new CompanionProjectileHitEvent(result, this)))
        {
            return;
        }

        if(result instanceof BlockHitResult blockHitResult)
        {
            if(blockHitResult.getType() == HitResult.Type.MISS)
            {
                return;
            }

            Vec3 hitVec = result.getLocation();
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState state = this.level().getBlockState(pos);
            Block block = state.getBlock();

            this.onHitBlock(state, pos, blockHitResult.getDirection(), hitVec.x, hitVec.y, hitVec.z);

        }

        if(result instanceof EntityHitResult entityHitResult)
        {
            Entity entity = entityHitResult.getEntity();
            if(entity.getId() == this.ownerID)
            {
                return;
            }
            if (entity instanceof Display.BlockDisplay) {
                return;
            }

            if(this.owner instanceof Player player)
            {
                if(entity.hasIndirectPassenger(player))
                {
                    return;
                }
            }

            if (entity instanceof Companion target &&
                    target.getPlayer() != null && target.getPlayer().getUUID() == this.playerUUID) {
                return;
            }
            if (entity.getUUID() == this.playerUUID) {
                return;
            }
            /*if (entity instanceof SummonEntity summon && summon.getPlayerUUID().isPresent() &&
                    summon.getPlayerUUID().get() == this.playerUUID) {*/
            if (entity instanceof SummonEntity) {
                return;
            }

            this.onHitEntity(entity);

            if (!this.collateral) {
                this.remove(RemovalReason.KILLED);
            }

            entity.invulnerableTime = 0;
        }
    }

    public void updateHeading()
    {
        double horizontalDistance = this.getDeltaMovement().horizontalDistance();
        this.setYRot((float) (Mth.atan2(this.getDeltaMovement().x(), this.getDeltaMovement().z()) * (180D / Math.PI)));
        this.setXRot((float) (Mth.atan2(this.getDeltaMovement().y(), horizontalDistance) * (180D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    private Vec3 getVectorFromRotation(float pitch, float yaw)
    {
        float f = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    /**
     * A simple method to perform logic on each tick of the projectile. This method is appropriate
     * for spawning particles. Override {@link #tick()} to make changes to physics
     */
    protected void onProjectileTick()
    {
    }

    /**
     * Will spawn a particle just before being deleted!
     */
    protected void impactEffect()
    {
    }

    /**
     * Called when the projectile has run out of it's life. In other words, the projectile managed
     * to not hit any blocks and instead aged. The grenade uses this to explode in the air.
     */
    protected void onExpired()
    {
    }

    protected void onHitEntity(Entity entity)
    {
        entity.hurt(this.damageSources().mobProjectile(this, this.owner), this.damage);
        entity.invulnerableTime = 0;
    }

    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z)
    {
    }

    @Override
    public void onRemovedFromWorld()
    {
        this.impactEffect();
    }

    @Nullable
    protected EntityResult findEntityOnPath(Vec3 startVec, Vec3 endVec)
    {
        Vec3 hitVec = null;
        Entity hitEntity = null;
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);
        double closestDistance = Double.MAX_VALUE;
        for(Entity entity : entities)
        {
            if(!entity.equals(this.owner))
            {
                EntityResult result = this.getHitResult(entity, startVec, endVec);
                if(result == null)
                    continue;
                Vec3 hitPos = result.getHitPos();
                double distanceToHit = startVec.distanceTo(hitPos);
                if(distanceToHit < closestDistance)
                {
                    hitVec = hitPos;
                    hitEntity = entity;
                    closestDistance = distanceToHit;
                }
            }
        }
        return hitEntity != null ? new EntityResult(hitEntity, hitVec) : null;
    }

    @Nullable
    protected List<EntityResult> findEntitiesOnPath(Vec3 startVec, Vec3 endVec)
    {
        List<EntityResult> hitEntities = new ArrayList<>();
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);
        for(Entity entity : entities)
        {
            if(!entity.equals(this.owner))
            {
                EntityResult result = this.getHitResult(entity, startVec, endVec);
                if(result == null)
                    continue;
                hitEntities.add(result);
            }
        }
        return hitEntities;
    }

    /**
     * A custom implementation of ray tracing that allows you to pass a predicate to ignore certain
     * blocks when checking for collisions.
     *
     * @param world     the world to perform the ray trace
     * @param context   the ray trace context
     * @return a result of the raytrace
     */
    public static BlockHitResult rayTraceBlocks(Level world, ClipContext context)
    {
        return performRayTrace(context, (rayTraceContext, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            FluidState fluidState = world.getFluidState(blockPos);
            Vec3 startVec = rayTraceContext.getFrom();
            Vec3 endVec = rayTraceContext.getTo();
            VoxelShape blockShape = rayTraceContext.getBlockShape(blockState, world, blockPos);
            BlockHitResult blockResult = world.clipWithInteractionOverride(startVec, endVec, blockPos, blockShape, blockState);
            VoxelShape fluidShape = rayTraceContext.getFluidShape(fluidState, world, blockPos);
            BlockHitResult fluidResult = fluidShape.clip(startVec, endVec, blockPos);
            double blockDistance = blockResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(blockResult.getLocation());
            double fluidDistance = fluidResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(fluidResult.getLocation());
            return blockDistance <= fluidDistance ? blockResult : fluidResult;
        }, (rayTraceContext) -> {
            Vec3 Vector3d = rayTraceContext.getFrom().subtract(rayTraceContext.getTo());
            return BlockHitResult.miss(rayTraceContext.getTo(), Direction.getNearest(Vector3d.x, Vector3d.y, Vector3d.z), BlockPos.containing(rayTraceContext.getTo()));
        });
    }

    public static <T> T performRayTrace(ClipContext context, BiFunction<ClipContext, BlockPos, T> hitFunction, Function<ClipContext, T> p_217300_2_)
    {
        Vec3 startVec = context.getFrom();
        Vec3 endVec = context.getTo();
        if(startVec.equals(endVec))
        {
            return p_217300_2_.apply(context);
        }
        else
        {
            double startX = Mth.lerp(-0.0000001, endVec.x, startVec.x);
            double startY = Mth.lerp(-0.0000001, endVec.y, startVec.y);
            double startZ = Mth.lerp(-0.0000001, endVec.z, startVec.z);
            double endX = Mth.lerp(-0.0000001, startVec.x, endVec.x);
            double endY = Mth.lerp(-0.0000001, startVec.y, endVec.y);
            double endZ = Mth.lerp(-0.0000001, startVec.z, endVec.z);
            int blockX = Mth.floor(endX);
            int blockY = Mth.floor(endY);
            int blockZ = Mth.floor(endZ);
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(blockX, blockY, blockZ);
            T t = hitFunction.apply(context, mutablePos);
            if(t != null)
            {
                return t;
            }

            double deltaX = startX - endX;
            double deltaY = startY - endY;
            double deltaZ = startZ - endZ;
            int signX = Mth.sign(deltaX);
            int signY = Mth.sign(deltaY);
            int signZ = Mth.sign(deltaZ);
            double d9 = signX == 0 ? Double.MAX_VALUE : (double) signX / deltaX;
            double d10 = signY == 0 ? Double.MAX_VALUE : (double) signY / deltaY;
            double d11 = signZ == 0 ? Double.MAX_VALUE : (double) signZ / deltaZ;
            double d12 = d9 * (signX > 0 ? 1.0D - Mth.frac(endX) : Mth.frac(endX));
            double d13 = d10 * (signY > 0 ? 1.0D - Mth.frac(endY) : Mth.frac(endY));
            double d14 = d11 * (signZ > 0 ? 1.0D - Mth.frac(endZ) : Mth.frac(endZ));

            while(d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D)
            {
                if(d12 < d13)
                {
                    if(d12 < d14)
                    {
                        blockX += signX;
                        d12 += d9;
                    }
                    else
                    {
                        blockZ += signZ;
                        d14 += d11;
                    }
                }
                else if(d13 < d14)
                {
                    blockY += signY;
                    d13 += d10;
                }
                else
                {
                    blockZ += signZ;
                    d14 += d11;
                }

                T t1 = hitFunction.apply(context, mutablePos.set(blockX, blockY, blockZ));
                if(t1 != null)
                {
                    return t1;
                }
            }

            return p_217300_2_.apply(context);
        }
    }

    private Vec3 getDirection(LivingEntity shooter)
    {
        return this.getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
    }

    /**
     * Author: MrCrayfish
     */
    public static class EntityResult
    {
        private final Entity entity;
        private final Vec3 hitVec;

        public EntityResult(Entity entity, Vec3 hitVec)
        {
            this.entity = entity;
            this.hitVec = hitVec;
        }

        /**
         * Gets the entity that was hit by the projectile
         */
        public Entity getEntity()
        {
            return this.entity;
        }

        /**
         * Gets the position the projectile hit
         */
        public Vec3 getHitPos()
        {
            return this.hitVec;
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        this.modifiedGravity = compound.getDouble("ModifiedGravity");
        this.life = compound.getInt("MaxLife");
        this.damage = compound.getFloat("Damage");
        this.playerUUID = compound.getUUID("Owner");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        compound.putDouble("ModifiedGravity", this.modifiedGravity);
        compound.putInt("MaxLife", this.life);
        compound.putFloat("Damage", this.damage);
        compound.putUUID("Owner", this.playerUUID);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.ownerID);
        buffer.writeDouble(this.modifiedGravity);
        buffer.writeVarInt(this.life);
        buffer.writeFloat(this.damage);
        buffer.writeUUID(this.playerUUID);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        this.ownerID = buffer.readInt();
        this.modifiedGravity = buffer.readDouble();
        this.life = buffer.readVarInt();
        this.damage = buffer.readFloat();
        this.playerUUID = buffer.readUUID();
    }
}
