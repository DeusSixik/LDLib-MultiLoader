package com.lowdragmc.lowdraglib.client.scene.fabric;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author KilaBash
 * @date 2023/3/5
 * @implNote WorldSceneRendererImpl
 */
public class WorldSceneRendererImpl {

    public static boolean canRenderInLayer(BlockRenderDispatcher blockRenderDispatcher, BlockState state, BlockPos pos, BlockAndTintGetter level, RenderType renderType, RandomSource random) {
        return ItemBlockRenderTypes.getChunkRenderType(state) == renderType;
    }

}
