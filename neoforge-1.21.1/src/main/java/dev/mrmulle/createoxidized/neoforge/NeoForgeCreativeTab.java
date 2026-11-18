package dev.mrmulle.createoxidized.neoforge;

import dev.mrmulle.createoxidized.CreateOxidizedCommon;
import dev.mrmulle.createoxidized.CreativeTabContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

final class NeoForgeCreativeTab {
    // only registration is loader specific, the item list is shared
    private static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateOxidizedCommon.MOD_ID);

    static {
        TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.createoxidized"))
            .icon(CreativeTabContent::icon)
            .displayItems((parameters, output) -> CreativeTabContent.populate(output))
            .build());
    }

    private NeoForgeCreativeTab() {
    }

    static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
