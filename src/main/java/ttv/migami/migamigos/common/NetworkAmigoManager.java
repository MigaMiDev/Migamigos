package ttv.migami.migamigos.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrcrayfish.framework.api.data.login.ILoginData;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;
import ttv.migami.migamigos.Migamigos;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.annotation.Validator;
import ttv.migami.migamigos.entity.AmigoEntityType;
import ttv.migami.migamigos.init.ModTags;
import ttv.migami.migamigos.network.PacketHandler;
import ttv.migami.migamigos.network.message.S2CMessageUpdateAmigos;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class NetworkAmigoManager extends SimplePreparableReloadListener<Map<AmigoEntityType<?>, Amigo>>
{
    private static final int FILE_TYPE_LENGTH_VALUE = ".json".length();
    private static final Gson GSON_INSTANCE = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ResourceLocation.class, JsonDeserializers.RESOURCE_LOCATION);
        builder.excludeFieldsWithModifiers(Modifier.TRANSIENT);
        return builder.create();
    });

    private static final List<AmigoEntityType<?>> clientRegisteredAmigos = new ArrayList<>();
    private static NetworkAmigoManager instance;

    private Map<ResourceLocation, Amigo> registeredAmigos = new HashMap<>();

    @Override
    protected Map<AmigoEntityType<?>, Amigo> prepare(ResourceManager manager, ProfilerFiller profiler)
    {
        Map<AmigoEntityType<?>, Amigo> map = new HashMap<>();
        ForgeRegistries.ENTITY_TYPES.getValues().stream().filter(entityType -> entityType instanceof AmigoEntityType<?>).forEach(amigoEntityType ->
        {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(amigoEntityType);
            if (id != null)
            {
                List<ResourceLocation> resources = new ArrayList<>(manager.listResources("amigos", (fileName) -> fileName.getPath().endsWith(id.getPath() + ".json")).keySet());
                resources.sort((r1, r2) -> {
                    if(r1.getNamespace().equals(r2.getNamespace())) return 0;
                    return r2.getNamespace().equals(Reference.MOD_ID) ? 1 : -1;
                });
                resources.forEach(resourceLocation ->
                {
                    Migamigos.LOGGER.atInfo().log("Found an Amigo: " + resourceLocation);
                    String path = resourceLocation.getPath().substring(0, resourceLocation.getPath().length() - FILE_TYPE_LENGTH_VALUE);
                    String[] splitPath = path.split("/");

                    // Makes sure the file name matches exactly with the id of the amigo
                    if(!id.getPath().equals(splitPath[splitPath.length - 1]))
                        return;

                    // Also check if the mod id matches with the amigo's registered namespace
                    if (!id.getNamespace().equals(resourceLocation.getNamespace()))
                        return;

                    manager.getResource(resourceLocation).ifPresent(resource ->
                    {
                        try (Reader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8)))
                        {
                            Amigo amigo = GsonHelper.fromJson(GSON_INSTANCE, reader, Amigo.class);
                            if (amigo != null && Validator.isValidObject(amigo))
                            {
                                map.put((AmigoEntityType<?>) amigoEntityType, amigo);
                            }
                            else
                            {
                                Migamigos.LOGGER.error("Couldn't load data file {} as it is missing or malformed. Using default amigo data", resourceLocation);
                                map.putIfAbsent((AmigoEntityType<?>) amigoEntityType, new Amigo());
                            }
                        }
                        catch (InvalidObjectException e)
                        {
                            Migamigos.LOGGER.error("Missing required properties for {}", resourceLocation);
                            e.printStackTrace();
                        }
                        catch (IOException e)
                        {
                            Migamigos.LOGGER.error("Couldn't parse data file {}", resourceLocation);
                        }
                        catch (IllegalAccessException e)
                        {
                            e.printStackTrace();
                        }
                    });
                });
            }
        });
        return map;
    }

    @Override
    protected void apply(Map<AmigoEntityType<?>, Amigo> objects, ResourceManager resourceManager, ProfilerFiller profiler)
    {
        ImmutableMap.Builder<ResourceLocation, Amigo> builder = ImmutableMap.builder();
        objects.forEach((amigoEntity, amigo) -> {
            builder.put(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(amigoEntity)), amigo);
            amigoEntity.setAmigo(new Supplier(amigo));
        });
        this.registeredAmigos = builder.build();
    }

    /**
     * Writes all registered amigos into the provided packet buffer
     *
     * @param buffer a packet buffer get
     */
    public void writeRegisteredAmigos(FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(this.registeredAmigos.size());
        this.registeredAmigos.forEach((id, amigo) -> {
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
    public static ImmutableMap<ResourceLocation, Amigo> readRegisteredAmigos(FriendlyByteBuf buffer)
    {
        int size = buffer.readVarInt();
        if(size > 0)
        {
            ImmutableMap.Builder<ResourceLocation, Amigo> builder = ImmutableMap.builder();
            for(int i = 0; i < size; i++)
            {
                ResourceLocation id = buffer.readResourceLocation();
                Amigo amigo = Amigo.create(buffer.readNbt());
                builder.put(id, amigo);
            }
            return builder.build();
        }
        return ImmutableMap.of();
    }

    public static boolean updateRegisteredAmigos(S2CMessageUpdateAmigos message)
    {
        return updateRegisteredAmigos(message.getRegisteredAmigos());
    }

    /**
     * Updates registered amigos from data provided by the server
     *
     * @return true if all registered amigos were able to update their corresponding amigo item
     */
    private static boolean updateRegisteredAmigos(Map<ResourceLocation, Amigo> registeredAmigos)
    {
        clientRegisteredAmigos.clear();
        if(registeredAmigos != null)
        {
            for(Map.Entry<ResourceLocation, Amigo> entry : registeredAmigos.entrySet())
            {
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entry.getKey());
                if(entityType != null && !(entityType.is(ModTags.Entities.AMIGO)))
                {
                    return false;
                }
                ((AmigoEntityType<?>) entityType).setAmigo(new Supplier(entry.getValue()));
                clientRegisteredAmigos.add((AmigoEntityType<?>) entityType);
            }
            return true;
        }
        return false;
    }

    /**
     * Gets a map of all the registered amigos objects. Note, this is an immutable map.
     *
     * @return a map of registered amigo objects
     */
    public Map<ResourceLocation, Amigo> getRegisteredAmigos()
    {
        return this.registeredAmigos;
    }

    /**
     * Gets a list of all the amigos registered on the client side. Note, this is an immutable list.
     *
     * @return a map of amigos registered on the client
     */
    public static List<AmigoEntityType<?>> getClientRegisteredAmigos()
    {
        return ImmutableList.copyOf(clientRegisteredAmigos);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event)
    {
        NetworkAmigoManager.instance = null;
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event)
    {
        NetworkAmigoManager networkAmigoManager = new NetworkAmigoManager();
        event.addListener(networkAmigoManager);
        NetworkAmigoManager.instance = networkAmigoManager;
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event)
    {
        if(event.getPlayer() == null)
        {
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateAmigos());
        }
    }

    /**
     * Gets the network amigo manager. This will be null if the client isn't running an integrated
     * server or the client is connected to a dedicated server.
     *
     * @return the network amigo manager
     */
    @Nullable
    public static NetworkAmigoManager get()
    {
        return instance;
    }

    /**
     * A simple wrapper for a amigo object to pass to AmigoEntityType<?>. This is to indicate to developers that
     * Amigo instances shouldn't be changed on AmigoEntityType<?>s as they are controlled by NetworkAmigoManager.
     * Changes to amigo properties should be made through the JSON file.
     */
    public static class Supplier
    {
        private final Amigo amigo;

        private Supplier(Amigo amigo)
        {
            this.amigo = amigo;
        }

        public Amigo getAmigo()
        {
            return this.amigo;
        }
    }

    public static class LoginData implements ILoginData
    {
        @Override
        public void writeData(FriendlyByteBuf buffer)
        {
            Validate.notNull(NetworkAmigoManager.get());
            NetworkAmigoManager.get().writeRegisteredAmigos(buffer);
        }

        @Override
        public Optional<String> readData(FriendlyByteBuf buffer)
        {
            Map<ResourceLocation, Amigo> registeredAmigos = NetworkAmigoManager.readRegisteredAmigos(buffer);
            NetworkAmigoManager.updateRegisteredAmigos(registeredAmigos);
            return Optional.empty();
        }
    }
}