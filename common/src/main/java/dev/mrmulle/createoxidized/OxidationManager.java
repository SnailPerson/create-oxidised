package dev.mrmulle.createoxidized;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public final class OxidationManager {
    // same first roll vanilla copper uses before it checks the nearby blocks
    private static final double VANILLA_ATTEMPT_CHANCE = 0.05688889D;
    // keeping the block in place matters for tanks and brackets attached to pipes
    private static final int IN_PLACE_UPDATE = Block.UPDATE_ALL | Block.UPDATE_MOVE_BY_PISTON;

    private OxidationManager() {
    }

    public static void randomTick(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (!CreateOxidizedCommon.enabled() || !OxidationProperties.isOxidizable(state)
            || state.getValue(OxidationProperties.WAXED)
            || state.getValue(OxidationProperties.STAGE) == OxidationStage.OXIDIZED) {
            return;
        }

        double factor = CreateOxidizedCommon.speedFactor();
        if (factor <= 0.0D) {
            return;
        }
        double adjustedChance = 1.0D - Math.pow(1.0D - VANILLA_ATTEMPT_CHANCE, factor);
        if (random.nextDouble() >= adjustedChance) {
            return;
        }

        OxidationStage current = state.getValue(OxidationProperties.STAGE);
        int sameAge = 0;
        int older = 0;

        // this is basically WeatheringCopper logic but it also counts our addon blocks
        for (BlockPos nearby : BlockPos.withinManhattan(pos, 4, 4, 4)) {
            if (nearby.distManhattan(pos) > 4) {
                break;
            }
            if (nearby.equals(pos)) {
                continue;
            }
            BlockState other = level.getBlockState(nearby);
            int otherAge;
            if (OxidationProperties.isOxidizable(other)) {
                if (other.getValue(OxidationProperties.WAXED)) {
                    continue;
                }
                otherAge = other.getValue(OxidationProperties.STAGE).ordinal();
            } else if (other.getBlock() instanceof WeatheringCopper weatheringCopper) {
                otherAge = weatheringCopper.getAge().ordinal();
            } else {
                continue;
            }
            if (otherAge < current.ordinal()) {
                return;
            }
            if (otherAge == current.ordinal()) {
                sameAge++;
            } else {
                older++;
            }
        }

        float ratio = (float) (older + 1) / (float) (older + sameAge + 1);
        float ageModifier = current == OxidationStage.UNAFFECTED ? 0.75F : 1.0F;
        if (random.nextFloat() < ageModifier * ratio * ratio) {
            setStage(level, pos, state, current.next());
        }
    }

    public static boolean wax(Level level, BlockPos pos, BlockState state) {
        return wax(level, pos, state, true);
    }

    public static boolean wax(Level level, BlockPos pos, BlockState state, boolean playEffect) {
        if (!CreateOxidizedCommon.enabled() || !OxidationProperties.isOxidizable(state)
            || state.getValue(OxidationProperties.WAXED)) {
            return false;
        }
        setState(level, pos, state.setValue(OxidationProperties.WAXED, true));
        if (playEffect) {
            level.levelEvent(3003, pos, 0);
        }
        return true;
    }

    public static boolean oxidize(Level level, BlockPos pos, BlockState state) {
        if (!CreateOxidizedCommon.enabled() || !OxidationProperties.isOxidizable(state)
            || state.getValue(OxidationProperties.WAXED)) {
            return false;
        }
        OxidationStage stage = state.getValue(OxidationProperties.STAGE);
        if (stage == OxidationStage.OXIDIZED) {
            return false;
        }
        setStage(level, pos, state, stage.next());
        return true;
    }

    public static ScrapeResult scrape(Level level, BlockPos pos, BlockState state) {
        if (!CreateOxidizedCommon.enabled() || !OxidationProperties.isOxidizable(state)) {
            return ScrapeResult.NONE;
        }
        if (state.getValue(OxidationProperties.WAXED)) {
            setState(level, pos, state.setValue(OxidationProperties.WAXED, false));
            level.levelEvent(3004, pos, 0);
            level.playSound(
                null,
                pos,
                scrapeSound(ScrapeResult.WAX_REMOVED),
                SoundSource.BLOCKS,
                1.0F,
                1.0F
            );
            return ScrapeResult.WAX_REMOVED;
        }
        OxidationStage stage = state.getValue(OxidationProperties.STAGE);
        if (stage == OxidationStage.UNAFFECTED) {
            return ScrapeResult.NONE;
        }
        setStage(level, pos, state, stage.previous());
        level.levelEvent(3005, pos, 0);
        level.playSound(
            null,
            pos,
            scrapeSound(ScrapeResult.SCRAPED),
            SoundSource.BLOCKS,
            1.0F,
            1.0F
        );
        return ScrapeResult.SCRAPED;
    }

    public static SoundEvent scrapeSound(ScrapeResult result) {
        return result == ScrapeResult.WAX_REMOVED ? SoundEvents.AXE_WAX_OFF : SoundEvents.AXE_SCRAPE;
    }

    public static void lightningStrike(Level level, BlockPos strikePos) {
        if (level.isClientSide || !CreateOxidizedCommon.enabled()) {
            return;
        }

        BlockState strikeState = level.getBlockState(strikePos);
        BlockPos first = strikeState.getBlock() instanceof LightningRodBlock
            ? strikePos.relative(strikeState.getValue(LightningRodBlock.FACING).getOpposite())
            : strikePos;
        boolean customAnchor = fullyDeoxidize(level, first);
        boolean vanillaAnchor = level.getBlockState(first).getBlock() instanceof WeatheringCopper;
        if (!customAnchor && !vanillaAnchor) {
            return;
        }

        // vanilla lightning cleans a few short random walks instead of one giant area
        RandomSource random = level.random;
        int walks = random.nextInt(3) + 3;
        for (int walk = 0; walk < walks; walk++) {
            BlockPos.MutableBlockPos cursor = first.mutable();
            int steps = random.nextInt(8) + 1;
            for (int step = 0; step < steps; step++) {
                BlockPos next = randomStepCleaningCopper(level, cursor);
                if (next == null) {
                    break;
                }
                cursor.set(next);
            }
        }
    }

    private static boolean fullyDeoxidize(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (OxidationProperties.isOxidizable(state) && !state.getValue(OxidationProperties.WAXED)) {
            setState(level, pos, state.setValue(OxidationProperties.STAGE, OxidationStage.UNAFFECTED));
            return true;
        }
        return false;
    }

    private static BlockPos randomStepCleaningCopper(Level level, BlockPos pos) {
        for (BlockPos candidate : BlockPos.randomInCube(level.random, 10, pos, 1)) {
            BlockState state = level.getBlockState(candidate);
            if (!OxidationProperties.isOxidizable(state) || state.getValue(OxidationProperties.WAXED)) {
                continue;
            }
            OxidationStage stage = state.getValue(OxidationProperties.STAGE);
            if (stage != OxidationStage.UNAFFECTED) {
                setStage(level, candidate, state, stage.previous());
            }
            level.levelEvent(3002, candidate, -1);
            return candidate.immutable();
        }
        return null;
    }

    private static void setStage(Level level, BlockPos pos, BlockState state, OxidationStage stage) {
        setState(level, pos, state.setValue(OxidationProperties.STAGE, stage));
    }

    private static void setState(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, IN_PLACE_UPDATE);
    }

    public enum ScrapeResult {
        NONE,
        WAX_REMOVED,
        SCRAPED
    }
}
