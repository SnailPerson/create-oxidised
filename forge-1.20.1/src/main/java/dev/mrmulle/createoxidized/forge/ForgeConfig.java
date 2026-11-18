package dev.mrmulle.createoxidized.forge;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ForgeConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue OXIDATION_ENABLED;
    public static final ForgeConfigSpec.DoubleValue OXIDATION_SPEED_FACTOR;
    public static final ForgeConfigSpec.BooleanValue CLUTTER_REDUCER_3000;

    static {
        // keep names the same as neoforge so configs are easy to move between versions
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("oxidation");
        OXIDATION_ENABLED = builder
            .comment("Master switch for Create-part oxidation, waxing, scraping, and lightning cleaning.")
            .define("enabled", true);
        OXIDATION_SPEED_FACTOR = builder
            .comment("Multiplier for vanilla copper's oxidation attempt rate. 0 stops aging; 1 is vanilla speed.")
            .defineInRange("speedFactor", 1.0D, 0.0D, 100.0D);
        builder.pop();
        CLUTTER_REDUCER_3000 = builder
            .comment("copper items were always a meme and a cheap way of saying 'i added 9000 items', so if you (understandably) dont like every oxidised pipe variant in history plus the waxed ones in your inventory, consider setting thsi to true. Every oxidised part simply drops as its original state.")
            .define("clutterReducer3000", false);
        SPEC = builder.build();
    }

    private ForgeConfig() {
    }
}

