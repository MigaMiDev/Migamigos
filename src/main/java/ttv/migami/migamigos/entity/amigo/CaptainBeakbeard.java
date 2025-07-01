package ttv.migami.migamigos.entity.amigo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.level.Level;
import ttv.migami.migamigos.init.ModEntities;

public class CaptainBeakbeard extends Parrot {

    public CaptainBeakbeard(EntityType<? extends Parrot> type, Level level) {
        super(ModEntities.CAPTAIN_BEAKBEARD.get(), level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 5, true, false, this::isTargetValid));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
    }

    private boolean isTargetValid(LivingEntity entity) {
        return entity == this.getTarget();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean flag = super.doHurtTarget(target);
        if (flag && target instanceof LivingEntity living) {
            living.hurt(damageSources().mobAttack(this), 2.0F);
        }
        return flag;
    }
}