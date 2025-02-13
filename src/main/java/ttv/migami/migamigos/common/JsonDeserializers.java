package ttv.migami.migamigos.common;

import com.google.gson.JsonDeserializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

public class JsonDeserializers {
    public static final JsonDeserializer<ItemStack> ITEM_STACK = (json, typeOfT, context) -> CraftingHelper.getItemStack(json.getAsJsonObject(), true);
    public static final JsonDeserializer<ResourceLocation> RESOURCE_LOCATION = (json, typeOfT, context) -> new ResourceLocation(json.getAsString());

    public JsonDeserializers() {
    }
}