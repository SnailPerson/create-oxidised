package dev.mrmulle.createoxidized.mixin;

import dev.mrmulle.createoxidized.OxidationProperties;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StateDefinition.Builder.class)
public abstract class StateDefinitionBuilderMixin<O, S extends StateHolder<O, S>> {
    @Shadow
    @Final
    private O owner;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "create", at = @At("HEAD"))
    private void createoxidized$addOxidationProperties(
        CallbackInfoReturnable<StateDefinition<O, S>> callback
    ) {
        // doing it here avoids replacing every Create block with a custom subclass
        if (OxidationProperties.classCanCarryProperties(owner)) {
            ((StateDefinition.Builder) (Object) this).add(
                OxidationProperties.STAGE,
                OxidationProperties.WAXED
            );
        }
    }
}

