package dev.mrmulle.createoxidized.client;

import java.util.EnumMap;
import java.util.Map;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.mrmulle.createoxidized.OxidationProperties;
import dev.mrmulle.createoxidized.OxidationStage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

// moving gauges pointers etc arent in the main block model so they need their own versions
public final class OxidizedPartialModels {
    private static final Map<OxidationStage, Map<Part, PartialModel>> MODELS = createModels();

    private OxidizedPartialModels() {
    }

    // makes sure these exist before flywheel gathers all its partial models
    public static void init() {
    }

    public static PartialModel forState(Part part, BlockState state, PartialModel original) {
        if (state == null || !OxidationProperties.hasProperties(state)) {
            return original;
        }
        Map<Part, PartialModel> stageModels = MODELS.get(state.getValue(OxidationProperties.STAGE));
        return stageModels == null ? original : stageModels.getOrDefault(part, original);
    }

    private static Map<OxidationStage, Map<Part, PartialModel>> createModels() {
        // these json models point at the same generated stage textures as normal blocks
        Map<OxidationStage, Map<Part, PartialModel>> result = new EnumMap<>(OxidationStage.class);
        for (OxidationStage stage : OxidationStage.values()) {
            if (stage == OxidationStage.UNAFFECTED) {
                continue;
            }
            Map<Part, PartialModel> stageModels = new EnumMap<>(Part.class);
            for (Part part : Part.values()) {
                ResourceLocation location = ResourceLocation.tryParse(
                    "createoxidized:block/" + stage.getSerializedName() + "/" + part.path
                );
                stageModels.put(part, PartialModel.of(location));
            }
            result.put(stage, stageModels);
        }
        return result;
    }

    public enum Part {
        BOILER_GAUGE("steam_engine/gauge"),
        BOILER_GAUGE_DIAL("steam_engine/gauge_dial"),
        PORTABLE_INTERFACE_TOP("portable_fluid_interface/block_top"),
        PORTABLE_INTERFACE_MIDDLE("portable_fluid_interface/block_middle"),
        PORTABLE_INTERFACE_MIDDLE_POWERED("portable_fluid_interface/block_middle_powered"),
        FLUID_VALVE_POINTER("fluid_valve/pointer"),
        VALVE_HANDLE("valve_handle");

        private final String path;

        Part(String path) {
            this.path = path;
        }
    }
}
