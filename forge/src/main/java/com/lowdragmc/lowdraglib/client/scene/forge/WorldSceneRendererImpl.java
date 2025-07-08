package com.lowdragmc.lowdraglib.client.scene.forge;

import com.lowdragmc.lowdraglib.utils.DummyWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelDataManager;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/2/8
 * @implNote WorldSceneRendererImpl
 */
@SuppressWarnings("unused")
public class WorldSceneRendererImpl {

    @SuppressWarnings("UnstableApiUsage")
    public static ModelData getModelData(BakedModel model, BlockState state, BlockPos pos, BlockAndTintGetter level) {

        ModelDataManager manager = level.getModelDataManager();
        if (manager == null && level instanceof DummyWorld dummyLevel) {
            manager = dummyLevel.getLevel().getModelDataManager();
        }
        ModelData modelData = manager.getAt(pos);
        return model.getModelData(level, pos, state, modelData != null ? modelData : ModelData.EMPTY);
    }

    public static boolean canRenderInLayer(BlockRenderDispatcher blockRenderDispatcher,
                                           BlockState state, BlockPos pos, BlockAndTintGetter level,
                                           RenderType renderType, RandomSource random) {
        var blockModel = blockRenderDispatcher.getBlockModel(state);
        ModelData modelData = getModelData(blockModel, state, pos, level);

        return blockModel.getRenderTypes(state, random, modelData).contains(renderType);
    }

    public static void renderBlocksForge(BlockRenderDispatcher blockRenderDispatcher,
                                         BlockState state, BlockPos pos, BlockAndTintGetter level,
                                         @Nonnull PoseStack poseStack, VertexConsumer consumer, RandomSource random, RenderType renderType) {
        if (state.getRenderShape() != RenderShape.MODEL) {
            return;
        }
        var blockModel = blockRenderDispatcher.getBlockModel(state);
        ModelData modelData = getModelData(blockModel, state, pos, level);

        blockRenderDispatcher.getModelRenderer().tesselateBlock(level, blockModel, state, pos,
                poseStack, consumer, true, random, state.getSeed(pos),
                OverlayTexture.NO_OVERLAY, modelData, renderType);
    }

}
