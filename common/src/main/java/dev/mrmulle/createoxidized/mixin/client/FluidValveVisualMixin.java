package dev.mrmulle.createoxidized.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlockEntity;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.mrmulle.createoxidized.client.OxidizedPartialModels;
import dev.mrmulle.createoxidized.client.OxidizedPartialModels.Part;

@Mixin(targets = "com.simibubi.create.content.fluids.pipes.valve.FluidValveVisual", remap = false)
abstract class FluidValveVisualMixin {
    // flywheels fast render path needs the same pointer swap as the normal renderer
    @Redirect(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/AllPartialModels;FLUID_VALVE_POINTER:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"
        )
    )
    private PartialModel createoxidized$oxidizePointer(
        VisualizationContext visualizationContext,
        FluidValveBlockEntity blockEntity,
        float partialTicks
    ) {
        return OxidizedPartialModels.forState(
            Part.FLUID_VALVE_POINTER,
            blockEntity.getBlockState(),
            com.simibubi.create.AllPartialModels.FLUID_VALVE_POINTER
        );
    }
}
