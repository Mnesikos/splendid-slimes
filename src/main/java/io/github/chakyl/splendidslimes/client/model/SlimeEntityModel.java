package io.github.chakyl.splendidslimes.client.model;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;


public class SlimeEntityModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;

    public SlimeEntityModel(ModelPart root) {
        this.root = root.getChild("root");
    }

    public static LayerDefinition createInnerBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(2, 2).addBox(-2.5F, -6.0F, -5.1F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(2, 2).addBox(1.5F, -5.7F, -5.1F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16).addBox(-2.0F, -3.7F, -5.1F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(3, 16).addBox(-3.0F, -7.0F, -4.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {}

    public ModelPart root() {
        return this.root;
    }
}
