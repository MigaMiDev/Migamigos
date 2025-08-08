package ttv.migami.migamigos;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    /**
     * Common config options
     */
    public static class Common
    {
        public final Gameplay gameplay;

        public Common(ForgeConfigSpec.Builder builder)
        {
            builder.push("common");
            {
                this.gameplay = new Gameplay(builder);
            }
            builder.pop();
        }
    }

    /**
     * Gameplay related config options
     */
    public static class Gameplay
    {
        public final ForgeConfigSpec.BooleanValue friendlyFire;
        public final ForgeConfigSpec.BooleanValue rideWithPlayer;

        public Gameplay(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to gameplay").push("gameplay");
            {
                this.friendlyFire = builder.comment("If enabled, Amigos will receive damage from your attacks.").define("friendlyFire", false);
                this.rideWithPlayer = builder
                    .comment("If enabled, Amigos will mount the entity the player is riding when interacting. Rideable entities")
                    .comment("that support this behavior are configured via the #migamigos:ride_with_player entity_type tag")
                    .define("rideWithPlayer", true);
            }
            builder.pop();
        }
    }

    /*static final ForgeConfigSpec clientSpec;
    public static final Client CLIENT;*/

    static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;

    /*static final ForgeConfigSpec serverSpec;
    public static final Config.Server SERVER;*/

    static
    {
        /*final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();*/

        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();

        /*final Pair<Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        serverSpec = serverSpecPair.getRight();
        SERVER = serverSpecPair.getLeft();*/
    }

    /*public static void saveClientConfig()
    {
        clientSpec.save();
    }*/
}
