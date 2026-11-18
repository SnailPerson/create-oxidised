package dev.mrmulle.createoxidized.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.content.kinetics.crank.HandCrankBlockEntity;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.mrmulle.createoxidized.client.OxidizedPartialModels;
import dev.mrmulle.createoxidized.client.OxidizedPartialModels.Part;

@Mixin(targets = "com.simibubi.create.content.kinetics.crank.ValveHandleVisual", remap = false)
abstract class ValveHandleVisualMixin {
    // valve handles are instanced by flywheel and never pass the normal baked block model
    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/AllPartialModels;VALVE_HANDLE:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"
        )
    )
    private PartialModel createoxidized$oxidizeHandle(
        VisualizationContext visualizationContext,
        HandCrankBlockEntity blockEntity,
        float partialTicks
    ) {
        return OxidizedPartialModels.forState(
            Part.VALVE_HANDLE,
            blockEntity.getBlockState(),
            com.simibubi.create.AllPartialModels.VALVE_HANDLE
        );
    }
}
