package dev.mrmulle.createoxidized.neoforge;

import dev.mrmulle.createoxidized.CreateOxidizedCommon;
import dev.mrmulle.createoxidized.OxidationManager;
import dev.mrmulle.createoxidized.OxidationItemData;
import dev.mrmulle.createoxidized.OxidationManager.ScrapeResult;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(CreateOxidizedCommon.MOD_ID)
public final class CreateOxidizedNeoForge {
    public CreateOxidizedNeoForge(IEventBus modBus, ModContainer container) {
        OxidationItemData.install(new NeoForgeItemDataAdapter());
        NeoForgeCreativeTab.register(modBus);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, NeoForgeConfig.SPEC);
        CreateOxidizedCommon.installConfig(
            () -> NeoForgeConfig.OXIDATION_ENABLED.get(),
            () -> NeoForgeConfig.OXIDATION_SPEED_FACTOR.get(),
            () -> NeoForgeConfig.CLUTTER_REDUCER_3000.get()
        );
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }

        ItemStack stack = event.getItemStack();
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
}

