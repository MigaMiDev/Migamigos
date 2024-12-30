package ttv.migami.migamigos.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Author: MrCrayfish
 */
public class KeyBinds
{
    public static final KeyMapping KEY_Z_ACTION = new KeyMapping("key.mdf.z_action", GLFW.GLFW_KEY_Z, "key.categories.mdf");
    public static final KeyMapping KEY_X_ACTION = new KeyMapping("key.mdf.x_action", GLFW.GLFW_KEY_X, "key.categories.mdf");
    public static final KeyMapping KEY_C_ACTION = new KeyMapping("key.mdf.c_action", GLFW.GLFW_KEY_C, "key.categories.mdf");
    public static final KeyMapping KEY_V_ACTION = new KeyMapping("key.mdf.v_action", GLFW.GLFW_KEY_V, "key.categories.mdf");
    public static final KeyMapping KEY_R_ACTION = new KeyMapping("key.mdf.r_action", GLFW.GLFW_KEY_R, "key.categories.mdf");
    public static final KeyMapping BLESSING_MENU = new KeyMapping("key.mdf.blessing_menu", GLFW.GLFW_KEY_B, "key.categories.mdf");

    public static void registerKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.register(KEY_Z_ACTION);
        event.register(KEY_X_ACTION);
        event.register(KEY_C_ACTION);
        event.register(KEY_V_ACTION);
        event.register(KEY_R_ACTION);
        event.register(BLESSING_MENU);
    }

}
