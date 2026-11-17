package dev.mrmulle.createoxidized.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.mrmulle.createoxidized.client.OxidizedPartialModels;
import dev.mrmulle.createoxidized.client.OxidizedPartialModels.Part;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(targets = "com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceRenderer", remap = false)
abstract class PortableStorageInterfaceRendererMixin {
    @Inject(method = "getTopForState", at = @At("RETURN"), cancellable = true)
    private static void createoxidized$oxidizeTop(
        BlockState state,
        CallbackInfoReturnable<PartialModel> callback
    ) {
        callback.setReturnValue(OxidizedPartialModels.forState(
            Part.PORTABLE_INTERFACE_TOP,
            state,
            callback.getReturnValue()
        ));
    }

    @Inject(method = "getMiddleForState", at = @At("RETURN"), cancellable = true)
    private static void createoxidized$oxidizeMiddle(
        BlockState state,
        boolean powered,
        CallbackInfoReturnable<PartialModel> callback
    ) {
        callback.setReturnValue(OxidizedPartialModels.forState(
            powered ? Part.PORTABLE_INTERFACE_MIDDLE_POWERED : Part.PORTABLE_INTERFACE_MIDDLE,
            state,
            callback.getReturnValue()
        ));
    }
}
