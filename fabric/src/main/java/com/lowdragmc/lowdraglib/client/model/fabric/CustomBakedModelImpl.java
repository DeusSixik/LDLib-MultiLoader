package com.lowdragmc.lowdraglib.client.model.fabric;

import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import com.lowdragmc.lowdraglib.client.bakedpipeline.Submap;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.model.custommodel.Connections;
import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;
import com.lowdragmc.lowdraglib.client.model.custommodel.LDLMetadataSection;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.*;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static com.lowdragmc.lowdraglib.client.model.fabric.LDLRendererModel.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomBakedModelImpl extends CustomBakedModel implements WrapperBakedModel {

    protected final ConcurrentMap<Integer, ConcurrentMap<Connections, Mesh>> sideCache = new ConcurrentHashMap<>();

    public CustomBakedModelImpl(BakedModel parent) {
        super(parent);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        final RenderMaterial defaultMaterial = this.useAmbientOcclusion() ? MATERIAL_STANDARD : MATERIAL_NO_AO;
        final QuadEmitter emitter = context.getEmitter();
        final RandomSource rand = randomSupplier.get();

        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            final Direction cullFace = ModelHelper.faceFromIndex(i);
            if (!context.hasTransform() && context.isFaceCulled(cullFace)) {
                // Skip entire quad list if possible.
                continue;
            }

            var connections = Connections.checkConnections(level, pos, state, cullFace);

            ConcurrentMap<Connections, Mesh> map;
            synchronized (sideCache) {
                map = sideCache.computeIfAbsent(i, key -> new ConcurrentHashMap<>());
            }
            Mesh mesh = map.computeIfAbsent(connections, key -> {
                assert RENDERER != null;
                MeshBuilder builder = RENDERER.meshBuilder();
                QuadEmitter meshEmitter = builder.getEmitter();
                WrappingRenderContext ctx = new WrappingRenderContext(context, meshEmitter);

                parent.emitBlockQuads(level, state, pos, randomSupplier, ctx);

                return buildCtmQuads(key, builder.build(), 0.0f);
            });

            mesh.outputTo(emitter);
        }
    }

    @SuppressWarnings("deprecation")
    public static Mesh buildCtmQuads(Connections connections, Mesh base, float offset) {
        MeshBuilder newMesh = RENDERER.meshBuilder();
        final QuadEmitter copy = newMesh.getEmitter();

        TextureAtlas blockAtlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        final SpriteFinder finder = SpriteFinder.get(blockAtlas);

        base.forEach(quadView -> {
            TextureAtlasSprite quadSprite = finder.find(quadView);
            var section = LDLMetadataSection.getMetadata(quadSprite);
            TextureAtlasSprite connection = section.connection == null ? null : ModelFactory.getBlockSprite(section.connection);

            if (connection == null) {
                copy.copyFrom(quadView);
                copy.emit();
                return;
            }

            Quad quad = makeQuad(quadView, quadSprite, section, offset).derotate();
            Quad[] quads = quad.subdivide(4);
            int[] ctm = connections.getSubmapIndices();

            for (int j = 0; j < quads.length; j++) {
                Quad q = quads[j];
                if (q != null) {
                    int ctmid = q.getUvs().normalize().getQuadrant();
                    Quad newQ = q.grow().transformUVs(ctm[ctmid] > 15 ? quadSprite : connection, Submap.uvs[ctm[ctmid]]);

                    emitQuad(newQ, copy);
                }
            }
        });
        return newMesh.build();
    }

    protected static Quad makeQuad(QuadView quadView, TextureAtlasSprite sprite, LDLMetadataSection section, float vertexOffset) {
        Quad.Builder builder = new Quad.Builder(sprite);
        builder.setQuadTint(quadView.colorIndex());
        builder.setQuadOrientation(quadView.nominalFace());
        builder.setApplyDiffuseLighting(!quadView.material().disableDiffuse());

        Direction quadDir = quadView.nominalFace();
        float xOffset = 0, yOffset = 0, zOffset = 0;
        if (quadDir != null && vertexOffset != 0) {
            xOffset = vertexOffset * quadDir.getStepX();
            yOffset = vertexOffset * quadDir.getStepY();
            zOffset = vertexOffset * quadDir.getStepZ();
        }

        for (int i = 0; i < 4; i++) {
            builder.positions[i] = new float[] {
                    quadView.x(i) + xOffset,
                    quadView.y(i) + yOffset,
                    quadView.z(i) + zOffset,
            };
            int packedColor = quadView.color(i);
            builder.colors[i] = new int[] {
                    packedColor & 0xFF,
                    (packedColor << 8) & 0xFF,
                    (packedColor << 16) & 0xFF,
                    (packedColor << 24) & 0xFF
            };
            builder.uvs[i] = new float[] {
                    quadView.u(i),
                    quadView.v(i),
            };
            int lightMap = quadView.lightmap(i);
            builder.uvs2[i] = new int[] {
                    lightMap & 0xFFFF,
                    (lightMap >> 16) & 0xFFFF
            };
        }

        Quad q = builder.build();
        if (section.emissive) {
            q = q.setLight(15, 15);
        }
        return q;
    }

    protected static void emitQuad(Quad quad, QuadEmitter emitter) {
        emitter.nominalFace(quad.getBuilder().getQuadOrientation());
        emitter.colorIndex(quad.getBuilder().getQuadTint());
        emitter.spriteBake(quad.getUvs().getSprite(), 0);

        RenderMaterial material;
        if (quad.getBlocklight() > 0 || quad.getSkylight() > 0) {
            material = MATERIAL_EMISSIVE;
        } else {
            material = quad.getBuilder().isApplyDiffuseLighting() ? MATERIAL_STANDARD : MATERIAL_NO_AO;
        }
        emitter.material(material);

        List<VertexFormatElement> elements = DefaultVertexFormat.BLOCK.getElements();

        for (int v = 0; v < 4; v++) {
            for (int i = 0; i < elements.size(); i++) {
                VertexFormatElement ele = elements.get(i);
                switch (ele.getUsage()) {
                    case POSITION -> {
                        Vector3f p = quad.getVertPos()[v];
                        emitter.pos(v, p.x(), p.y(), p.z());
                    }
                    case COLOR -> {
                        int[] c = quad.getBuilder().colors[v];
                        int color = FastColor.ARGB32.color(c[0], c[1], c[2], c[3]);
                        emitter.color(v, color);
                    }
                    case UV -> {
                        if (ele.getIndex() == 2) {
                            emitter.lightmap(v, LightTexture.pack(quad.getBlocklight(), quad.getSkylight()));
                        } else if (ele.getIndex() == 0) {
                            Vec2 uv = quad.getVertUv()[v];
                            emitter.uv(v, uv.x, uv.y);
                        }
                    }
                }
            }
            emitter.emit();
        }
    }

    @Override
    public @Nullable BakedModel getWrappedModel() {
        return this.parent;
    }
}
