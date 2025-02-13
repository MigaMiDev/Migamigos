package ttv.migami.migamigos.entity.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import ttv.migami.migamigos.init.ModEntities;

public class ScorchMarkEntity extends GroundMarkEntity {
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(ScorchMarkEntity.class, EntityDataSerializers.FLOAT);

    public ScorchMarkEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ScorchMarkEntity(Level pLevel, BlockPos blockPos, int life, float size) {
        super(ModEntities.SCORCH_MARK.get(), pLevel, blockPos, life, size);
    }

    @Override
    protected void spawnParticles() {
        level().addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 0, 0.05, 0);
    }
}

