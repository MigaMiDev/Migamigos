package ttv.migami.migamigos.entity.projectile.cocogoat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ttv.migami.migamigos.entity.projectile.CustomProjectileEntity;
import ttv.migami.migamigos.init.ModEntities;
import ttv.migami.migamigos.init.ModParticleTypes;

import static ttv.migami.migamigos.common.network.ServerPlayHandler.sendParticlesToAll;

public class IceCone extends CustomProjectileEntity {
    public IceCone(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public IceCone(Level pLevel, LivingEntity pShooter, float damage, float speed) {
        super(ModEntities.ICE_CONE.get(), pLevel, pShooter, damage, speed);
        this.affectedByGravity = true;
        this.checkForCollisions = true;
    }

    @Override
    public void impactEffect()
    {
        if (this.level() instanceof ServerLevel serverLevel) {
            sendParticlesToAll(
                    serverLevel,
                    ModParticleTypes.FROST_GLINT.get(),
                    true,
                    this.getX(),
                    this.getY() + 0.1,
                    this.getZ(),
                    16,
                    this.getBbWidth() / 2, this.getBbHeight(), this.getBbWidth() / 2,
                    0.1
            );
            sendParticlesToAll(
                    serverLevel,
                    ParticleTypes.SNOWFLAKE,
                    true,
                    this.getX(),
                    this.getY() + 0.1,
                    this.getZ(),
                    16,
                    this.getBbWidth() / 2, this.getBbHeight(), this.getBbWidth() / 2,
                    0.1
            );
            serverLevel.playSound(this, this.getOnPos(), SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
