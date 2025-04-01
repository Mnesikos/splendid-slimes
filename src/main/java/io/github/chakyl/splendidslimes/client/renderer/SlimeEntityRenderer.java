package io.github.chakyl.splendidslimes.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.client.model.SlimeEntityModel;
import io.github.chakyl.splendidslimes.client.model.SlimeEntityOuterLayer;
import io.github.chakyl.splendidslimes.entity.SlimeEntityBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SlimeEntityRenderer extends MobRenderer<SlimeEntityBase, SlimeEntityModel<SlimeEntityBase>> {
    private static final ResourceLocation SLIME_LOCATION = new ResourceLocation(SplendidSlimes.MODID, "textures/entity/template_slime_entity.png");

    public SlimeEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeEntityModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
        this.addLayer(new SlimeEntityOuterLayer<>(this, context.getModelSet()));
    }

    public void render(SlimeEntityBase slimeEntityBase, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        this.shadowRadius = 0.25F * (float) slimeEntityBase.getSize();
        super.render(slimeEntityBase, entityYaw, partialTicks, poseStack, multiBufferSource, packedLight);
    }

    protected void scale(SlimeEntityBase slimeEntityBase, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float slimeSize = (float) slimeEntityBase.getSize();
        float lerpy = Mth.lerp(partialTickTime, slimeEntityBase.oSquish, slimeEntityBase.squish) / (slimeSize * 0.5F + 1.0F);
        float cutter = 1.0F / (lerpy + 1.0F);
        poseStack.scale(cutter * slimeSize, 1.0F / cutter * slimeSize, cutter * slimeSize);
    }

    public ResourceLocation getTextureLocation(SlimeEntityBase slimeEntityBase) {
        return SLIME_LOCATION;
    }
}

