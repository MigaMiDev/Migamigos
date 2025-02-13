package ttv.migami.migamigos.common;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.migamigos.Migamigos;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.annotation.Validator;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class CustomAmigoLoader extends SimpleJsonResourceReloadListener
{
    private static final Gson GSON_INSTANCE = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ResourceLocation.class, JsonDeserializers.RESOURCE_LOCATION);
        builder.registerTypeAdapter(ItemStack.class, JsonDeserializers.ITEM_STACK);
        return builder.create();
    });

    private static CustomAmigoLoader instance;

    private Map<ResourceLocation, CustomAmigo> customAmigoMap = new HashMap<>();

    public CustomAmigoLoader()
    {
        super(GSON_INSTANCE, "custom_amigos");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler)
    {
        ImmutableMap.Builder<ResourceLocation, CustomAmigo> builder = ImmutableMap.builder();
        objects.forEach((resourceLocation, object) ->
        {
            try
            {
                CustomAmigo customAmigo = GSON_INSTANCE.fromJson(object, CustomAmigo.class);
                if(customAmigo != null && Validator.isValidObject(customAmigo))
                {
                    builder.put(resourceLocation, customAmigo);
                }
                else
                {
                    Migamigos.LOGGER.error("Couldn't load data file {} as it is missing or malformed", resourceLocation);
                }
            }
            catch(InvalidObjectException e)
            {
                Migamigos.LOGGER.error("Missing required properties for {}", resourceLocation);
                e.printStackTrace();
            }
            catch(IllegalAccessException e)
            {
                e.printStackTrace();
            }
        });
        this.customAmigoMap = builder.build();
    }

    /**
     * Writes all custom amigos into the provided packet buffer
     *
     * @param buffer a packet buffer get
     */
    public void writeCustomAmigos(FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(this.customAmigoMap.size());
        this.customAmigoMap.forEach((id, amigo) -> {
            buffer.writeResourceLocation(id);
            buffer.writeNbt(amigo.serializeNBT());
        });
    }

    /**
     * Reads all registered amigos from the provided packet buffer
     *
     * @param buffer a packet buffer get
     * @return a map of registered amigos from the server
     */
    public static ImmutableMap<ResourceLocation, CustomAmigo> readCustomAmigos(FriendlyByteBuf buffer)
    {
        int size = buffer.readVarInt();
        if(size > 0)
        {
            ImmutableMap.Builder<ResourceLocation, CustomAmigo> builder = ImmutableMap.builder();
            for(int i = 0; i < size; i++)
            {
                ResourceLocation id = buffer.readResourceLocation();
                CustomAmigo customAmigo = new CustomAmigo();
                customAmigo.deserializeNBT(buffer.readNbt());
                builder.put(id, customAmigo);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event)
    {
        CustomAmigoLoader customAmigoLoader = new CustomAmigoLoader();
        event.addListener(customAmigoLoader);
        CustomAmigoLoader.instance = customAmigoLoader;
    }

    @Nullable
    public static CustomAmigoLoader get()
    {
        return instance;
    }
}
