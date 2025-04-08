package io.github.chakyl.splendidslimes.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.entity.SlimeEntityBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class SlimeHatLayer<T extends Entity> extends RenderLayer<T, SlimeEntityModel<T>> {
    public SlimeHatLayer(RenderLayerParent<T, SlimeEntityModel<T>> renderer) {

        super(renderer);
    }

    protected void scale(SlimeEntityBase slimeEntityBase, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float slimeSize = (float) slimeEntityBase.getSize();
        slimeSize += 0.25F;
        float lerpy = Mth.lerp(partialTickTime, slimeEntityBase.oSquish, slimeEntityBase.squish) / (slimeSize * 0.5F + 1.0F);
        float cutter = 1.0F / (lerpy + 1.0F);
        poseStack.translate(0, 0, 0);
        poseStack.scale(cutter * slimeSize, 1.0F / cutter * slimeSize, cutter * slimeSize);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

        if (pLivingEntity.isAlive() && !pLivingEntity.isInvisible()) {

            DynamicHolder<SlimeBreed> slime = ((SlimeEntityBase) pLivingEntity).getHatSlime();
            if (!slime.isBound()) return;
            SlimeBreed breed = slime.get();
            ItemStack stack = breed.hat();
            if (stack.isEmpty()) {
                return;
            }
            if (!stack.isEmpty()) {
                pPoseStack.pushPose();
                float scaleFactor = breed.hatScale();
                pPoseStack.mulPose(new Quaternionf().rotateX(Mth.PI));
                pPoseStack.scale(scaleFactor, scaleFactor, scaleFactor);
//                SplendidSlimes.LOGGER.info(breed.toString());
                pPoseStack.translate(breed.hatXOffset(), breed.hatYOffset(), breed.hatZOffset());
                Minecraft.getInstance()
                        .getItemRenderer()
                        .renderStatic(stack, ItemDisplayContext.GROUND, pPackedLight, OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, pLivingEntity.level(), (int) pLivingEntity.blockPosition()
                                .asLong());
                pPoseStack.popPose();
            }

        }
    }

}
