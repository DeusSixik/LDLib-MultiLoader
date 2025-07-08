package com.lowdragmc.lowdraglib.client.model.forge;

import com.lowdragmc.lowdraglib.client.model.custommodel.Connections;
import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel.RendererBakedModel.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomBakedModelImpl extends CustomBakedModel {

    public CustomBakedModelImpl(BakedModel parent) {
        super(parent);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                             ModelData modelData, @Nullable RenderType renderType) {
        BlockAndTintGetter level = modelData.get(WORLD);
        BlockPos pos = modelData.get(POS);
        ModelData parentModelData = modelData.get(MODEL_DATA);
        if (parentModelData == null) parentModelData = ModelData.EMPTY;

        if (level != null && pos != null && state != null) {
            return getCustomQuads(level, pos, state, side, rand, parentModelData, renderType);
        } else {
            // return the parent's quads (instead of nothing), like CustomBakedModel does in the default getQuads method
            return parent.getQuads(state, side, rand, parentModelData, renderType);
        }
    }

    public @NotNull List<BakedQuad> getCustomQuads(BlockAndTintGetter level, BlockPos pos, @NotNull BlockState state,
                                                   @Nullable Direction side, RandomSource rand,
                                                   ModelData parentModelData, @Nullable RenderType renderType) {
        var connections = Connections.checkConnections(level, pos, state, side);
        // Don't cache the quads if we're rendering for a specific render type or if the parent model has set any model data
        // as that might change the model and caching anything will likely result in broken models.
        if (renderType != null || !parentModelData.getProperties().isEmpty()) {
            return buildCustomQuads(connections, parent.getQuads(state, side, rand, parentModelData, renderType), 0.0f);
        }

        return super.getCustomQuads(level, pos, state, side, rand);
    }


    @Override
    public @NotNull ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        return modelData.derive()
                .with(WORLD, level)
                .with(POS, pos)
                .with(MODEL_DATA, parent.getModelData(level, pos, state, modelData))
                .build();
    }
}
