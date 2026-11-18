package dev.mrmulle.createoxidized;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

public final class CreateOxidizedCommon {
    public static final String MOD_ID = "createoxidized";

    // common code cant read either loaders config directly so they plug these in on startup
    private static BooleanSupplier enabled = () -> true;
    private static DoubleSupplier speedFactor = () -> 1.0D;
    private static BooleanSupplier clutterReducer3000 = () -> false;

    private CreateOxidizedCommon() {
    }

    public static void installConfig(
        BooleanSupplier enabledSupplier,
        DoubleSupplier speedSupplier,
        BooleanSupplier clutterReducerSupplier
    ) {
        enabled = enabledSupplier;
        speedFactor = speedSupplier;
        clutterReducer3000 = clutterReducerSupplier;
    }

    public static boolean enabled() {
        return enabled.getAsBoolean();
    }

    public static double speedFactor() {
        // negative speed would do wierd things to the chance maths
        return Math.max(0.0D, speedFactor.getAsDouble());
    }

    public static boolean clutterReducer3000() {
        return clutterReducer3000.getAsBoolean();
    }
}

