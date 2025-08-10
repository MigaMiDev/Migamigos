package ttv.migami.migamigos.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import ttv.migami.migamigos.Reference;

public class ModTags
{

    public static class Entities
    {
        public static final TagKey<EntityType<?>> AMIGO = tag("amigo");
        public static final TagKey<EntityType<?>> UNDEAD = tag("undead");
        public static final TagKey<EntityType<?>> RIDE_WITH_PLAYER = tag("ride_with_player");

        public static TagKey<EntityType<?>> tag(String name)
        {
            return TagKey.create(Registries.ENTITY_TYPE,new ResourceLocation(Reference.MOD_ID, name));
        }
    }
}
