package dev.mrmulle.createoxidized.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlockEntity;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.mrmulle.createoxidized.client.OxidizedPartialModels;
import dev.mrmulle.createoxidized.client.OxidizedPartialModels.Part;
import net.minecraft.client.renderer.MultiBufferSource;

@Mixin(targets = "com.simibubi.create.content.fluids.pipes.valve.FluidValveRenderer", remap = false)
abstract class FluidValveRendererMixin {
    // fallback renderer used when flywheel isnt drawing the valve
    @Redirect(
        method = "renderSafe(Lcom/simibubi/create/content/fluids/pipes/valve/FluidValveBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/AllPartialModels;FLUID_VALVE_POINTER:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"
        )
    )
    private PartialModel createoxidized$oxidizePointer(
        FluidValveBlockEntity blockEntity,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int light,
        int overlay
    ) {
        return OxidizedPartialModels.forState(
            Part.FLUID_VALVE_POINTER,
            blockEntity.getBlockState(),
            com.simibubi.create.AllPartialModels.FLUID_VALVE_POINTER
        );
    }
}
