package ttv.migami.migamigos.entity;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlagSet;
import ttv.migami.migamigos.common.Amigo;
import ttv.migami.migamigos.common.NetworkAmigoManager;

public class AmigoEntityType<A extends AmigoEntity> extends EntityType<AmigoEntity> {
    private Amigo amigo = new Amigo();

    public AmigoEntityType(EntityFactory pFactory, MobCategory pCategory, boolean pSerialize, boolean pSummon, boolean pFireImmune, boolean pCanSpawnFarFromPlayer, ImmutableSet pImmuneTo, EntityDimensions pDimensions, int pClientTrackingRange, int pUpdateInterval, FeatureFlagSet pRequiredFeatures) {
        super(pFactory, pCategory, pSerialize, pSummon, pFireImmune, pCanSpawnFarFromPlayer, pImmuneTo, pDimensions, pClientTrackingRange, pUpdateInterval, pRequiredFeatures);
        this.amigo = this.getAmigo();
    }

    public void setAmigo(NetworkAmigoManager.Supplier supplier) {
        this.amigo = supplier.getAmigo();
    }

    public Amigo getAmigo() {
        return this.amigo;
    }
}
