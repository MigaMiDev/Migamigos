package ttv.migami.migamigos.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import ttv.migami.migamigos.Migamigos;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.annotation.Validator;
import ttv.migami.migamigos.entity.AmigoEntity;

import java.io.*;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BasicAmigoManager
{
    private static final Gson GSON_INSTANCE = Util.make(() -> {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ResourceLocation.class, JsonDeserializers.RESOURCE_LOCATION);
        builder.excludeFieldsWithModifiers(Modifier.TRANSIENT);
        return builder.create();
    });

    public static void loadAmigoDataFromDatapack(ResourceLocation id, ResourceManager resourceManager, AmigoEntity amigoEntity) {
        List<ResourceLocation> resources = new ArrayList<>(resourceManager.listResources("amigos", (fileName) -> fileName.getPath().endsWith(id.getPath() + ".json")).keySet());
        resources.sort((r1, r2) -> {
            if(r1.getNamespace().equals(r2.getNamespace())) return 0;
            return r2.getNamespace().equals(Reference.MOD_ID) ? 1 : -1;
        });
        resources.forEach(resourceLocation -> {

            // Also check if the mod id matches with the amigo's registered namespace
            if (!resourceLocation.getNamespace().equals(id.getNamespace()))
                return;

            resourceManager.getResource(resourceLocation).ifPresent(resource ->
            {
                try (Reader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8)))
                {
                    Amigo amigo = GsonHelper.fromJson(GSON_INSTANCE, reader, Amigo.class);
                    if (amigo != null && Validator.isValidObject(amigo))
                    {
                        amigoEntity.setAmigo(amigo);
                    }
                    else
                    {
                        Migamigos.LOGGER.error("Couldn't load data file {} as it is missing or malformed. Using default amigo data", resourceLocation);
                        amigoEntity.setAmigo(amigo);
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
}