package dev.mrmulle.createoxidized;

import net.minecraft.util.StringRepresentable;

public enum OxidationStage implements StringRepresentable {
    UNAFFECTED("unaffected"),
    EXPOSED("exposed"),
    WEATHERED("weathered"),
    OXIDIZED("oxidized");

    private final String serializedName;

    OxidationStage(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public OxidationStage next() {
        // clamp at each end so callers dont need special cases everywhere
        return values()[Math.min(ordinal() + 1, values().length - 1)];
    }

    public OxidationStage previous() {
        return values()[Math.max(ordinal() - 1, 0)];
    }

    public static OxidationStage fromSerializedName(String name) {
        for (OxidationStage stage : values()) {
            if (stage.serializedName.equals(name)) {
                return stage;
            }
        }
        return UNAFFECTED;
    }
}

