package ttv.migami.migamigos.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ttv.migami.migamigos.init.ModEntities;

import java.util.List;

public class StunEntity extends Entity {
    protected int life = 200;
    protected AmigoEntity owner;
    protected float damage = 5.0F;

    public StunEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public StunEntity(AmigoEntity owner, Level pLevel, Vec3 targetPos, float damage, int life) {
        super(ModEntities.STUN_ENTITY.get(), pLevel);
        this.setPos(targetPos);
        this.owner = owner;
        this.life = life;
        this.damage = damage;
        this.teleportToGroundOrAir();
    }

    private void teleportToGroundOrAir() {
        BlockPos currentPos = this.blockPosition();
        Level level = this.level();

        while (currentPos.getY() > level.getMinBuildHeight() && level.getBlockState(currentPos.below()).isAir()) {
            currentPos = currentPos.below();
        }

        while (!level.getBlockState(currentPos).isAir() && currentPos.getY() < level.getMaxBuildHeight()) {
            currentPos = currentPos.above();
        }

        this.setPos(currentPos.getX() + 0.5, currentPos.getY(), currentPos.getZ() + 0.5);
    }

    @Override
    public void tick() {
        super.tick();

        Level level = this.level();

        if (this.tickCount >= (this.life - 10)) {
            this.ejectPassengers();
        }

        if (this.tickCount >= this.life) {
            this.ejectPassengers();
            this.remove(RemovalReason.KILLED);
            return;
        }

        if (!level.isClientSide)
        {
            if ((this.tickCount <= (this.life - 10))) {
                List<Entity> collidedEntities = level.getEntities(this, this.getBoundingBox());
                for (Entity entity : collidedEntities) {
                    if ((entity instanceof Enemy || entity.equals(this.owner.getTarget())) && entity != this.owner) {
                        if (this.getPassengers().isEmpty()) {
                            entity.startRiding(this, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }
}
