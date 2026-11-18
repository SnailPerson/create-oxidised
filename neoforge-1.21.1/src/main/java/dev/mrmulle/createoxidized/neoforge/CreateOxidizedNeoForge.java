package dev.mrmulle.createoxidized.neoforge;

import dev.mrmulle.createoxidized.CreateOxidizedCommon;
import dev.mrmulle.createoxidized.OxidationManager;
import dev.mrmulle.createoxidized.OxidationItemData;
import dev.mrmulle.createoxidized.OxidationManager.ScrapeResult;
import dev.mrmulle.createoxidized.WaxedUpCompat;
import dev.mrmulle.createoxidized.WaxedUpCompat.Treatment;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(CreateOxidizedCommon.MOD_ID)
public final class CreateOxidizedNeoForge {
    public CreateOxidizedNeoForge(IEventBus modBus, ModContainer container) {
        // loader entrypoint, all the actual oxidation rules live in common
        OxidationItemData.install(new NeoForgeItemDataAdapter());
        NeoForgeCreativeTab.register(modBus);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, NeoForgeConfig.SPEC);
        CreateOxidizedCommon.installConfig(
            () -> NeoForgeConfig.OXIDATION_ENABLED.get(),
            () -> NeoForgeConfig.OXIDATION_SPEED_FACTOR.get(),
            () -> NeoForgeConfig.CLUTTER_REDUCER_3000.get()
        );
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(this::onBlockPlaced);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        BlockState state = event.getLevel().getBlockState(event.getPos());
        // sprays go first since they arent axes or honeycomb and have their own durability
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
            stack.hurtAndBreak(1, event.getEntity(), LivingEntity.getSlotForHand(event.getHand()));
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

        // catches Create placement arrows which skip the normal BlockItem mixin
        OxidationItemData.restoreFromHeldItem(
            player,
            level,
            event.getPos(),
            event.getPlacedBlock()
        );

        // waxed up also supports spraying a block while its being placed
        ItemStack stack = player.getOffhandItem();
        Treatment treatment = WaxedUpCompat.treatment(stack);
        BlockState placedState = level.getBlockState(event.getPos());
        if (!WaxedUpCompat.canTreat(placedState, treatment)
            || !WaxedUpCompat.treat(level, event.getPos(), placedState, treatment)) {
            return;
        }

        WaxedUpCompat.playSprayEffect(stack, level, event.getPos(), treatment);
        consumeSprayUse(player, InteractionHand.OFF_HAND, stack);
    }

    private static void consumeSprayUse(Player player, InteractionHand hand, ItemStack stack) {
        if (player.getAbilities().instabuild) {
            return;
        }
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        if (stack.isEmpty()) {
            player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
        }
    }
}

