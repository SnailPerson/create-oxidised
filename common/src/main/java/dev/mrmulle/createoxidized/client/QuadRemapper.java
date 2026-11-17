package dev.mrmulle.createoxidized.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import dev.mrmulle.createoxidized.OxidationProperties;
import dev.mrmulle.createoxidized.OxidationStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class QuadRemapper {
    private static final int U_OFFSET = 4;
    private static final int V_OFFSET = 5;
    private static final Set<String> OXIDIZABLE_TEXTURES = loadTextureManifest();
    private static final List<ResourceLocation> OXIDIZABLE_LOCATIONS = OXIDIZABLE_TEXTURES.stream()
        .map(ResourceLocation::tryParse)
        .toList();
    private static final ConcurrentHashMap<CacheKey, BakedQuad> CACHE = new ConcurrentHashMap<>();

    private QuadRemapper() {
    }

    public static List<BakedQuad> remap(List<BakedQuad> quads, BlockState state) {
        if (state == null || !OxidationProperties.hasProperties(state)) {
            return quads;
        }
        OxidationStage stage = state.getValue(OxidationProperties.STAGE);
        return remap(quads, stage);
    }

    public static List<BakedQuad> remap(List<BakedQuad> quads, OxidationStage stage) {
        if (stage == OxidationStage.UNAFFECTED || quads.isEmpty()) {
            return quads;
        }

        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            ResourceLocation declaredSource = quad.getSprite().contents().name();
            String sourceKey = declaredSource.getNamespace() + ":" + declaredSource.getPath();
            if (!OXIDIZABLE_TEXTURES.contains(sourceKey)) {
                result.add(quad);
                continue;
            }
            ResourceLocation source = actualSource(quad);
            if (source == null) {
                result.add(quad);
                continue;
            }
            result.add(CACHE.computeIfAbsent(new CacheKey(quad, stage), ignored -> remapQuad(quad, source, stage)));
        }
        return result;
    }

    private static ResourceLocation actualSource(BakedQuad quad) {
        TextureAtlasSprite declared = quad.getSprite();
        if (containsAllUvs(declared, quad.getVertices())) {
            return declared.contents().name();
        }
        for (ResourceLocation location : OXIDIZABLE_LOCATIONS) {
            TextureAtlasSprite candidate = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(location);
            if (candidate.contents().name().equals(location)
                && containsAllUvs(candidate, quad.getVertices())) {
                return location;
            }
        }
        return null;
    }

    private static boolean containsAllUvs(TextureAtlasSprite sprite, int[] vertices) {
        int stride = vertices.length / 4;
        float epsilon = 0.00001F;
        float minU = Math.min(sprite.getU0(), sprite.getU1()) - epsilon;
        float maxU = Math.max(sprite.getU0(), sprite.getU1()) + epsilon;
        float minV = Math.min(sprite.getV0(), sprite.getV1()) - epsilon;
        float maxV = Math.max(sprite.getV0(), sprite.getV1()) + epsilon;
        for (int vertex = 0; vertex < 4; vertex++) {
            int base = vertex * stride;
            float u = Float.intBitsToFloat(vertices[base + U_OFFSET]);
            float v = Float.intBitsToFloat(vertices[base + V_OFFSET]);
            if (u < minU || u > maxU || v < minV || v > maxV) {
                return false;
            }
        }
        return true;
    }

    private static BakedQuad remapQuad(BakedQuad quad, ResourceLocation source, OxidationStage stage) {
        String path = source.getPath();
        if (path.startsWith("block/")) {
            path = path.substring("block/".length());
        }
        ResourceLocation targetLocation = ResourceLocation.tryParse(
            "createoxidized:block/" + stage.getSerializedName() + "/" + path
        );
        TextureAtlasSprite target = Minecraft.getInstance()
            .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
            .apply(targetLocation);
        if (!target.contents().name().equals(targetLocation)) {
            return quad;
        }
        TextureAtlasSprite sourceSprite = Minecraft.getInstance()
            .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
            .apply(source);
        int[] vertices = quad.getVertices().clone();
        int stride = vertices.length / 4;

        for (int vertex = 0; vertex < 4; vertex++) {
            int base = vertex * stride;
            float oldU = Float.intBitsToFloat(vertices[base + U_OFFSET]);
            float oldV = Float.intBitsToFloat(vertices[base + V_OFFSET]);
            float localU = Math.max(0.0F, Math.min(16.0F, sourceSprite.getUOffset(oldU)));
            float localV = Math.max(0.0F, Math.min(16.0F, sourceSprite.getVOffset(oldV)));
            vertices[base + U_OFFSET] = Float.floatToRawIntBits(target.getU(localU));
            vertices[base + V_OFFSET] = Float.floatToRawIntBits(target.getV(localV));
        }

        return new BakedQuad(
            vertices,
            quad.getTintIndex(),
            quad.getDirection(),
            target,
            quad.isShade()
        );
    }

    private static Set<String> loadTextureManifest() {
        InputStream stream = QuadRemapper.class.getResourceAsStream(
            "/assets/createoxidized/oxidizable_textures.txt"
        );
        if (stream == null) {
            return Collections.emptySet();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .collect(Collectors.toUnmodifiableSet());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read oxidation texture manifest", exception);
        }
    }

    private record CacheKey(BakedQuad quad, OxidationStage stage) {
    }
}

