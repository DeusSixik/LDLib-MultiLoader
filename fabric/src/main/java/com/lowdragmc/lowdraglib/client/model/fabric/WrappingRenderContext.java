package com.lowdragmc.lowdraglib.client.model.fabric;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

public class WrappingRenderContext implements RenderContext {

    public final RenderContext parent;
    public final QuadEmitter quadEmitter;

    public WrappingRenderContext(RenderContext parent, QuadEmitter quadEmitter) {
        this.parent = parent;
        this.quadEmitter = quadEmitter;
    }

    @Override
    public QuadEmitter getEmitter() {
        return this.quadEmitter;
    }

    @Override
    public void pushTransform(QuadTransform transform) {
        parent.pushTransform(transform);
    }

    @Override
    public void popTransform() {
        parent.popTransform();
    }

    @SuppressWarnings("removal")
    @Override
    public BakedModelConsumer bakedModelConsumer() {
        return parent.bakedModelConsumer();
    }
}
