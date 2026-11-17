package dev.mrmulle.createoxidized.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.mrmulle.createoxidized.client.OxidizedPartialModels;
import dev.mrmulle.createoxidized.client.OxidizedPartialModels.Part;
import net.minecraft.client.renderer.MultiBufferSource;

@Mixin(targets = "com.simibubi.create.content.fluids.tank.FluidTankRenderer", remap = false)
abstract class FluidTankRendererMixin {
    @Redirect(
        method = "renderAsBoiler",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/AllPartialModels;BOILER_GAUGE:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"
        )
    )
    private PartialModel createoxidized$oxidizeGauge(
        FluidTankBlockEntity blockEntity,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int light,
        int overlay
    ) {
        return OxidizedPartialModels.forState(
            Part.BOILER_GAUGE,
            blockEntity.getBlockState(),
            com.simibubi.create.AllPartialModels.BOILER_GAUGE
        );
    }

    @Redirect(
        method = "renderAsBoiler",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/AllPartialModels;BOILER_GAUGE_DIAL:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"
        )
    )
    private PartialModel createoxidized$oxidizeGaugeDial(
        FluidTankBlockEntity blockEntity,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int light,
        int overlay
    ) {
        return OxidizedPartialModels.forState(
            Part.BOILER_GAUGE_DIAL,
            blockEntity.getBlockState(),
            com.simibubi.create.AllPartialModels.BOILER_GAUGE_DIAL
        );
    }
}
