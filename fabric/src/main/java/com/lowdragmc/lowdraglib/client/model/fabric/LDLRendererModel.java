package com.lowdragmc.lowdraglib.client.model.fabric;

import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import lombok.Setter;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/05/28
 * @implNote LDLModel, use vanilla way to improve model rendering
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LDLRendererModel implements UnbakedModel {
    public static final LDLRendererModel INSTANCE = new LDLRendererModel();

    public static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();
    public static final RenderMaterial MATERIAL_STANDARD;
    public static final RenderMaterial MATERIAL_NO_AO;
    public static final RenderMaterial MATERIAL_EMISSIVE;

    static {
        if (RENDERER != null) {
            MATERIAL_STANDARD = RENDERER.materialFinder().find();

            MATERIAL_NO_AO = RENDERER.materialFinder()
                    .ambientOcclusion(TriState.FALSE).find();

            MATERIAL_EMISSIVE = RENDERER.materialFinder().copyFrom(MATERIAL_NO_AO)
                    .emissive(true).find();
        } else {
            MATERIAL_NO_AO = null;
            MATERIAL_STANDARD = null;
            MATERIAL_EMISSIVE = null;
        }
    }

    private LDLRendererModel() {}

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> models) {

    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, ResourceLocation location) {
        return new RendererBakedModel();
    }

    public static final class RendererBakedModel implements BakedModel, FabricBakedModel {
        @Setter
        private IRenderer renderer = IRenderer.EMPTY;

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
            return Collections.emptyList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return renderer.useAO();
        }

        @Override
        public boolean isGui3d() {
            return renderer.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return renderer.useBlockLight(ItemStack.EMPTY);
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return renderer.getParticleTexture();
        }

        @Override
        public ItemTransforms getTransforms() {
            return ItemTransforms.NO_TRANSFORMS;
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }


        @Override
        public boolean isVanillaAdapter() {
            return false;
        }

        @Override
        public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
            if (!(state.getBlock() instanceof IBlockRendererProvider rendererProvider)) {
                return;
            }
            IRenderer renderer = rendererProvider.getRenderer(state);
            if (renderer == null) {
                return;
            }
            RenderMaterial defaultMaterial = renderer.useAO(state) ? MATERIAL_STANDARD : MATERIAL_NO_AO;
            QuadEmitter emitter = context.getEmitter();
            RandomSource random = randomSupplier.get();

            for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
                final Direction cullFace = ModelHelper.faceFromIndex(i);

                if (!context.hasTransform() && context.isFaceCulled(cullFace)) {
                    // Skip entire quad list if possible.
                    continue;
                }

                var quads = renderer.renderModel(blockView, pos, state, cullFace, random);
                if (renderer.reBakeCustomQuads()) {
                    quads = CustomBakedModel.reBakeCustomQuads(quads, blockView, pos, state,
                            cullFace, renderer.reBakeCustomQuadsOffset());
                }
                final int count = quads.size();

                for (int j = 0; j < count; j++) {
                    emitter.fromVanilla(quads.get(j), defaultMaterial, cullFace);
                    emitter.emit();
                }
            }
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
            /** use mixin {@link com.lowdragmc.lowdraglib.core.mixins.ItemRendererMixin}*/
        }

    }
}
