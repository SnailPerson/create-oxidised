package dev.mrmulle.createoxidized.neoforge.client;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import dev.mrmulle.createoxidized.CreateOxidizedCommon;
import dev.mrmulle.createoxidized.OxidationItemData;
import dev.mrmulle.createoxidized.OxidationProperties;
import dev.mrmulle.createoxidized.OxidationStage;
import dev.mrmulle.createoxidized.client.OxidizedPartialModels;
import dev.mrmulle.createoxidized.client.QuadRemapper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

@EventBusSubscriber(modid = CreateOxidizedCommon.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NeoForgeClientEvents {
    private NeoForgeClientEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        OxidizedPartialModels.init();
    }

    @SubscribeEvent
    public static void modifyBakedModels(ModelEvent.ModifyBakingResult event) {
        // wrap only known copper models, wrapping the whole registry would be very wasteful
        event.getModels().replaceAll((location, model) -> {
            if (!OxidationProperties.isSupportedModelId(
                location.id().getNamespace(),
                location.id().getPath()
            )) {
                return model;
            }
            return model instanceof OxidizedModel ? model : new OxidizedModel(model);
        });
    }

    private static final class OxidizedModel implements BakedModel {
        private static final ConcurrentHashMap<StageModelKey, BakedModel> STAGE_MODELS =
            new ConcurrentHashMap<>();

        private final BakedModel delegate;
        private final OxidationStage forcedStage;
        private final ItemOverrides overrides;

        private OxidizedModel(BakedModel delegate) {
            this(delegate, null);
        }

        private OxidizedModel(BakedModel delegate, OxidationStage forcedStage) {
            this.delegate = delegate;
            this.forcedStage = forcedStage;
            // forced stage is used for an inventory stack where there is no blockstate
            this.overrides = forcedStage == null ? new OxidizedItemOverrides(delegate) : delegate.getOverrides();
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
            List<BakedQuad> quads = delegate.getQuads(state, side, random);
            return forcedStage == null
                ? QuadRemapper.remap(quads, state)
                : QuadRemapper.remap(quads, forcedStage);
        }

        @Override
        public List<BakedQuad> getQuads(
            BlockState state,
            Direction side,
            RandomSource random,
            ModelData data,
            RenderType renderType
        ) {
            List<BakedQuad> quads = delegate.getQuads(state, side, random, data, renderType);
            return forcedStage == null
                ? QuadRemapper.remap(quads, state)
                : QuadRemapper.remap(quads, forcedStage);
        }

        @Override
        public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
            return delegate.getModelData(level, pos, state, data);
        }

        @Override
        public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
            return delegate.getRenderTypes(state, random, data);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return delegate.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return delegate.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return delegate.usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return delegate.isCustomRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return delegate.getParticleIcon();
        }

        @Override
        public ItemTransforms getTransforms() {
            return delegate.getTransforms();
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }

        private static final class OxidizedItemOverrides extends ItemOverrides {
            private final BakedModel baseModel;

            private OxidizedItemOverrides(BakedModel baseModel) {
                this.baseModel = baseModel;
            }

            @Override
            public BakedModel resolve(
                BakedModel model,
                ItemStack stack,
                ClientLevel level,
                LivingEntity entity,
                int seed
            ) {
                BakedModel resolved = baseModel.getOverrides()
                    .resolve(baseModel, stack, level, entity, seed);
                if (resolved == null) {
                    resolved = baseModel;
                }
                OxidationStage stage = OxidationItemData.read(stack)
                    .map(OxidationItemData.StoredState::stage)
                    .orElse(OxidationStage.UNAFFECTED);
                if (stage == OxidationStage.UNAFFECTED) {
                    return resolved;
                }
                return STAGE_MODELS.computeIfAbsent(
                    new StageModelKey(resolved, stage),
                    key -> new OxidizedModel(key.model(), key.stage())
                );
            }
        }

        private record StageModelKey(BakedModel model, OxidationStage stage) {
        }
    }
}

