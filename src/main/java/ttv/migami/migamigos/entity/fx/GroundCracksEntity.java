package ttv.migami.migamigos.entity.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import ttv.migami.migamigos.init.ModEntities;

public class GroundCracksEntity extends GroundMarkEntity {
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(GroundCracksEntity.class, EntityDataSerializers.FLOAT);

    public GroundCracksEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public GroundCracksEntity(Level pLevel, BlockPos blockPos, int life, float size) {
        super(ModEntities.GROUND_CRACKS.get(), pLevel, blockPos, life, size);
    }
}

