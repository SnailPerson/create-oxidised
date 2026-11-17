package dev.mrmulle.createoxidized.forge;

import dev.mrmulle.createoxidized.CreateOxidizedCommon;
import dev.mrmulle.createoxidized.OxidationManager;
import dev.mrmulle.createoxidized.OxidationItemData;
import dev.mrmulle.createoxidized.OxidationManager.ScrapeResult;
import dev.mrmulle.createoxidized.WaxedUpCompat;
import dev.mrmulle.createoxidized.WaxedUpCompat.Treatment;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CreateOxidizedCommon.MOD_ID)
public final class CreateOxidizedForge {
    @SuppressWarnings("removal")
    public CreateOxidizedForge() {
        OxidationItemData.install(new ForgeItemDataAdapter());
        ForgeCreativeTab.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeConfig.SPEC);
        CreateOxidizedCommon.installConfig(
            () -> ForgeConfig.OXIDATION_ENABLED.get(),
            () -> ForgeConfig.OXIDATION_SPEED_FACTOR.get(),
            () -> ForgeConfig.CLUTTER_REDUCER_3000.get()
        );
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(this::onBlockPlaced);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        BlockState state = event.getLevel().getBlockState(event.getPos());
        Treatment treatment = WaxedUpCompat.treatment(stack);
        if (treatment != Treatment.NONE && WaxedUpCompat.canTreat(state, treatment)) {
            if (!event.getLevel().isClientSide
                && WaxedUpCompat.treat(event.getLevel(), event.getPos(), state, treatment)) {
                BlockState treatedState = event.getLevel().getBlockState(event.getPos());
                event.getLevel().gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    event.getPos(),
                    GameEvent.Context.of(event.getEntity(), treatedState)
                );
                WaxedUpCompat.playSprayEffect(stack, event.getLevel(), event.getPos(), treatment);
                consumeSprayUse(event.getEntity(), event.getHand(), stack);
                event.getEntity().swing(event.getHand(), true);
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            return;
        }

        if (event.getLevel().isClientSide) {
            return;
        }

        boolean changed = false;
        boolean damageTool = false;

        if (stack.is(Items.HONEYCOMB)) {
            changed = OxidationManager.wax(event.getLevel(), event.getPos(), event.getLevel().getBlockState(event.getPos()));
            if (changed && !event.getEntity().getAbilities().instabuild) {
                stack.shrink(1);
            }
        } else if (stack.getItem() instanceof AxeItem) {
            ScrapeResult result = OxidationManager.scrape(
                event.getLevel(),
                event.getPos(),
                event.getLevel().getBlockState(event.getPos())
            );
            changed = result != ScrapeResult.NONE;
            damageTool = changed;
        }

        if (!changed) {
            return;
        }

        event.getEntity().awardStat(Stats.ITEM_USED.get(stack.getItem()));
        if (damageTool) {
            stack.hurtAndBreak(1, event.getEntity(), player -> player.broadcastBreakEvent(event.getHand()));
        }
        event.getEntity().swing(event.getHand(), true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)
            || !(event.getLevel() instanceof Level level)
            || level.isClientSide) {
            return;
        }

        ItemStack stack = player.getOffhandItem();
        Treatment treatment = WaxedUpCompat.treatment(stack);
        if (!WaxedUpCompat.canTreat(event.getPlacedBlock(), treatment)
            || !WaxedUpCompat.treat(level, event.getPos(), event.getPlacedBlock(), treatment)) {
            return;
        }

        WaxedUpCompat.playSprayEffect(stack, level, event.getPos(), treatment);
        consumeSprayUse(player, InteractionHand.OFF_HAND, stack);
    }

    private static void consumeSprayUse(Player player, InteractionHand hand, ItemStack stack) {
        if (player.getAbilities().instabuild) {
            return;
        }
        stack.hurtAndBreak(1, player, brokenPlayer -> brokenPlayer.broadcastBreakEvent(hand));
        if (stack.isEmpty()) {
            player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
        }
    }
}

