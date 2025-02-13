package ttv.migami.migamigos.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.init.ModEntities;
import ttv.migami.migamigos.init.ModTags;

import java.util.concurrent.CompletableFuture;

public class EntityTagGen extends EntityTypeTagsProvider
{
    public EntityTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        this.tag(ModTags.Entities.AMIGO)
                .add(ModEntities.COCOGOAT.get())
                .add(ModEntities.WAVELYN.get());

        this.tag(ModTags.Entities.UNDEAD)
                .add(EntityType.ZOMBIE)
                .add(EntityType.DROWNED)
                .add(EntityType.HUSK)
                .add(EntityType.PHANTOM)
                .add(EntityType.SKELETON)
                .add(EntityType.SKELETON_HORSE)
                .add(EntityType.STRAY)
                .add(EntityType.WITHER)
                .add(EntityType.WITHER_SKELETON)
                .add(EntityType.ZOGLIN)
                .add(EntityType.ZOMBIE)
                .add(EntityType.ZOMBIE_HORSE)
                .add(EntityType.ZOMBIFIED_PIGLIN)
                .add(EntityType.ZOMBIE_VILLAGER);
    }
}
