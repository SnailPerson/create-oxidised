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
        "create:copper_casing",
        "create:fluid_pipe",
        "create:encased_fluid_pipe",
        "create:glass_fluid_pipe",
        "create:mechanical_pump",
        "create:smart_fluid_pipe",
        "create:fluid_valve",
        "create:copper_valve_handle",
        "create:fluid_tank",
        "create:hose_pulley",
        "create:item_drain",
        "create:spout",
        "create:portable_fluid_interface",
        "create:steam_engine",
        "create:copper_ladder",
        "create:copper_bars",
        "create:copper_scaffolding",
        "create:copper_door",
        "create:copper_table_cloth",
        "createdeco:blue_copper_lamp",
        "createdeco:copper_bars",
        "createdeco:copper_bars_overlay",
        "createdeco:copper_catwalk",
        "createdeco:copper_catwalk_railing",
        "createdeco:copper_catwalk_stairs",
        "createdeco:copper_coinstack",
        "createdeco:copper_hull",
        "createdeco:copper_mesh_fence",
        "createdeco:copper_sheet_metal",
        "createdeco:copper_support",
        "createdeco:copper_support_wedge",
        "createdeco:copper_window",
        "createdeco:copper_window_pane",
        "createdeco:green_copper_lamp",
        "createdeco:locked_copper_door",
        "createdeco:red_copper_lamp",
        "createdeco:yellow_copper_lamp",
        "createdieselgenerators:chemical_turret",
        "createdieselgenerators:diesel_engine",
        "createdieselgenerators:distillation_tank",
        "createdieselgenerators:pumpjack_hole",
        "createdieselgenerators:white_concrete_encased_fluid_pipe",
        "createdieselgenerators:orange_concrete_encased_fluid_pipe",
        "createdieselgenerators:magenta_concrete_encased_fluid_pipe",
        "createdieselgenerators:light_blue_concrete_encased_fluid_pipe",
        "createdieselgenerators:yellow_concrete_encased_fluid_pipe",
        "createdieselgenerators:lime_concrete_encased_fluid_pipe",
        "createdieselgenerators:pink_concrete_encased_fluid_pipe",
        "createdieselgenerators:gray_concrete_encased_fluid_pipe",
        "createdieselgenerators:light_gray_concrete_encased_fluid_pipe",
        "createdieselgenerators:cyan_concrete_encased_fluid_pipe",
        "createdieselgenerators:purple_concrete_encased_fluid_pipe",
        "createdieselgenerators:blue_concrete_encased_fluid_pipe",
        "createdieselgenerators:brown_concrete_encased_fluid_pipe",
        "createdieselgenerators:green_concrete_encased_fluid_pipe",
        "createdieselgenerators:red_concrete_encased_fluid_pipe",
        "createdieselgenerators:black_concrete_encased_fluid_pipe",
        "create_connected:fluid_vessel",
        "dndecor:copper_boiler",
        "dndecor:copper_catwalk",
        "dndecor:copper_cross_bolt",
        "dndecor:copper_dash_bolt",
        "dndecor:copper_dot_bolt",
        "dndecor:copper_flat_bolt",
        "dndecor:copper_frontlight",
        "dndecor:copper_lamp",
        "dndecor:copper_sheet_metal",
        "dndecor:large_copper_chain",
        "dndesires:fluid_gauge",
        "dndesires:fluid_hatch",
        "dndesires:hydraulic_press",
        "dndesires:spud_sentry"
    );
    public static final Set<String> BLOCK_IDS = Set.copyOf(ORDERED_BLOCK_IDS);

    private static final Set<String> EXTRA_MODEL_IDS = Set.of(
        "dndesires:block/fluid_gauge/needle",
        "dndesires:block/hydraulic_press/head",
        "dndesires:block/spud_sentry/base",
        "dndesires:block/spud_sentry/cannon",
        "dndesires:block/spud_sentry/gear"
    );

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
        "com.simibubi.create.content.logistics.tableCloth.TableClothBlock",
        "com.simibubi.create.content.decoration.palettes.WindowBlock",
        "com.simibubi.create.content.decoration.palettes.ConnectedGlassPaneBlock",
        "com.simibubi.create.content.decoration.palettes.ConnectedPillarBlock",
        "net.minecraft.world.level.block.DoorBlock",
        "com.github.talrey.createdeco.blocks.CageLampBlock",
        "com.github.talrey.createdeco.blocks.CatwalkBlock",
        "com.github.talrey.createdeco.blocks.CatwalkRailingBlock",
        "com.github.talrey.createdeco.blocks.CatwalkStairBlock",
        "com.github.talrey.createdeco.blocks.CoinStackBlock",
        "com.github.talrey.createdeco.blocks.HullBlock",
        "com.github.talrey.createdeco.blocks.MeshFenceBlock",
        "com.github.talrey.createdeco.blocks.SupportBlock",
        "com.github.talrey.createdeco.blocks.SupportWedgeBlock",
        "com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlock",
        "com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock",
        "com.jesz.createdieselgenerators.content.distillation.DistillationTankBlock",
        "com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlock",
        "com.jesz.createdieselgenerators.content.concrete.ConcreteEncasedFluidPipeBlock",
        "com.hlysine.create_connected.content.fluidvessel.FluidVesselBlock",
        "dev.lopyluna.dndecor.content.blocks.boiler.BoilerBlock",
        "dev.lopyluna.dndecor.content.blocks.catwalk.CatwalkBlock",
        "dev.lopyluna.dndecor.content.blocks.bolt.BoltBlock",
        "dev.lopyluna.dndecor.content.blocks.frontlight.FrontlightBlock",
        "dev.lopyluna.dndecor.content.blocks.lamp.LampBlock",
        "dev.lopyluna.dndecor.content.blocks.LargeChain",
        "dev.lopyluna.dndesires.content.blocks.logistics.fluid_gauge.FluidGaugeBlock",
        "dev.lopyluna.dndesires.content.blocks.logistics.fluid_hatch.FluidHatchBlock",
        "dev.lopyluna.dndesires.content.blocks.kinetics.hydraulic_press.HydraulicPressBlock",
        "dev.lopyluna.dndesires.content.blocks.kinetics.spud_sentry.SpudSentryBlock"
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

    public static boolean isSupportedBlockId(String namespace, String path) {
        return BLOCK_IDS.contains(namespace + ":" + path);
    }

    public static boolean isSupportedModelId(String namespace, String path) {
        String id = namespace + ":" + path;
        return BLOCK_IDS.contains(id) || EXTRA_MODEL_IDS.contains(id);
    }

    public static boolean isOxidizable(BlockState state) {
        return hasProperties(state) && state.is(OXIDIZABLE_BLOCKS);
    }
}

