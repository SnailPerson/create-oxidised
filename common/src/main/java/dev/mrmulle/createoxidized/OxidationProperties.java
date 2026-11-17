package dev.mrmulle.createoxidized;

import java.util.List;
import java.util.Set;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class OxidationProperties {
    public static final EnumProperty<OxidationStage> STAGE =
        EnumProperty.create("createoxidized_stage", OxidationStage.class);
    public static final BooleanProperty WAXED = BooleanProperty.create("createoxidized_waxed");

    public static final TagKey<Block> OXIDIZABLE_BLOCKS = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.tryParse(CreateOxidizedCommon.MOD_ID + ":oxidizable_create_blocks")
    );

    public static final List<String> ORDERED_BLOCK_IDS = List.of(
        "copper_casing",
        "fluid_pipe",
        "encased_fluid_pipe",
        "glass_fluid_pipe",
        "mechanical_pump",
        "smart_fluid_pipe",
        "fluid_valve",
        "copper_valve_handle",
        "fluid_tank",
        "hose_pulley",
        "item_drain",
        "spout",
        "portable_fluid_interface",
        "steam_engine",
        "copper_ladder",
        "copper_bars",
        "copper_scaffolding",
        "copper_door",
        "copper_table_cloth"
    );
    public static final Set<String> BLOCK_IDS = Set.copyOf(ORDERED_BLOCK_IDS);

    private static final Set<String> ELIGIBLE_CLASSES = Set.of(
        "com.simibubi.create.content.decoration.encasing.CasingBlock",
        "com.simibubi.create.content.fluids.pipes.FluidPipeBlock",
        "com.simibubi.create.content.fluids.pipes.EncasedPipeBlock",
        "com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock",
        "com.simibubi.create.content.fluids.pump.PumpBlock",
        "com.simibubi.create.content.fluids.pipes.SmartFluidPipeBlock",
        "com.simibubi.create.content.fluids.pipes.valve.FluidValveBlock",
        "com.simibubi.create.content.kinetics.crank.ValveHandleBlock",
        "com.simibubi.create.content.fluids.tank.FluidTankBlock",
        "com.simibubi.create.content.fluids.hosePulley.HosePulleyBlock",
        "com.simibubi.create.content.fluids.drain.ItemDrainBlock",
        "com.simibubi.create.content.fluids.spout.SpoutBlock",
        "com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock",
        "com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock",
        "com.simibubi.create.content.decoration.MetalLadderBlock",
        "net.minecraft.world.level.block.IronBarsBlock",
        "com.simibubi.create.content.decoration.MetalScaffoldingBlock",
        "com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock",
        "com.simibubi.create.content.logistics.tableCloth.TableClothBlock"
    );

    private OxidationProperties() {
    }

    public static boolean classCanCarryProperties(Object owner) {
        if (!(owner instanceof Block)) {
            return false;
        }
        for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass()) {
            if (ELIGIBLE_CLASSES.contains(type.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasProperties(BlockState state) {
        return state.hasProperty(STAGE) && state.hasProperty(WAXED);
    }

    public static boolean isOxidizable(BlockState state) {
        return hasProperties(state) && state.is(OXIDIZABLE_BLOCKS);
    }
}

