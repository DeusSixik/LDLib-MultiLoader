package com.lowdragmc.lowdraglib.client.scene.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/2/8
 * @implNote WorldSceneRendererImpl
 */
public class WorldSceneRendererImpl {
    /**
     * always render it for forge blocks. forge models have their own rule.
     */
    public static boolean canRenderInLayer(BlockRenderDispatcher blockRenderDispatcher, BlockState state, BlockPos pos, BlockAndTintGetter level, RenderType renderType, RandomSource random) {
        var blockModel = blockRenderDispatcher.getBlockModel(state);
        var te = level.getBlockEntity(pos);
        ModelData modelData = blockRenderDispatcher.getBlockModel(state).getModelData(level, pos, state, te == null ? ModelData.EMPTY : te.getModelData());
        return blockModel.getRenderTypes(state, random, modelData).contains(renderType);
    }

    public static void renderBlocksForge(BlockRenderDispatcher blockRenderDispatcher, BlockState state, BlockPos pos, BlockAndTintGetter level, @Nonnull PoseStack poseStack, VertexConsumer consumer, RandomSource random, RenderType renderType) {
        var te = level.getBlockEntity(pos);
        ModelData modelData = blockRenderDispatcher.getBlockModel(state).getModelData(level, pos, state, te == null ? ModelData.EMPTY : te.getModelData());
        blockRenderDispatcher.renderBatched(state, pos, level, poseStack, consumer, false, random, modelData, renderType);
    }

}
