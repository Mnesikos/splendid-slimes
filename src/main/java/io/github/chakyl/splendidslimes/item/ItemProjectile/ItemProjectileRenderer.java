package io.github.chakyl.splendidslimes.item.ItemProjectile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemProjectileRenderer extends EntityRenderer<ItemProjectileEntity> {

    public ItemProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ItemProjectileEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer,
                       int light) {
        ItemStack item = entity.getItem();
        if (item.isEmpty())
            return;
        ms.pushPose();
        ms.translate(0, entity.getBoundingBox()
                .getYsize() / 2 - 1 / 8f, 0);

        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(item, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, ms, buffer, entity.level(),
                        0);
        ms.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ItemProjectileEntity entity) {
        return null;
    }

}