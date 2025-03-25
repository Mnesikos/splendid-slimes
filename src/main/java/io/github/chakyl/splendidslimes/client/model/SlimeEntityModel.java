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
	public static final ModelLayerLocation SLIME_LOCATION = new ModelLayerLocation(new ResourceLocation(SplendidSlimes.MODID, "textures/entity/gold_slime.png"), "main");
	private final ModelPart root;

	public SlimeEntityModel(ModelPart root) {
		this.root = root;
	}

	public static LayerDefinition createOuterBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public static LayerDefinition createInnerBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(32, 0).addBox(-3.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(32, 4).addBox(1.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(32, 8).addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, int hexCode, int secondaryHexCode) {
		int red = (hexCode >> 16) & 0xFF;
		int green = (hexCode >> 8) & 0xFF;
		int blue = hexCode & 0xFF;
		float normalizedAlpha = 255 / 255.0f;
		float normalizedRed = red / 255.0f;
		float normalizedGreen = green / 255.0f;
		float normalizedBlue = blue / 255.0f;
		if (secondaryHexCode != -1) {
			int red2 = (secondaryHexCode >> 16) & 0xFF;
			int green2 = (secondaryHexCode >> 8) & 0xFF;
			int blue2 = secondaryHexCode & 0xFF;
			normalizedRed = ((float) (red + red2) / 2) / 255.0f;
			normalizedGreen = ((float) (green + green2) / 2)  / 255.0f;
			normalizedBlue = ((float) (blue + blue2) / 2)  / 255.0f;
		}
		super.renderToBuffer(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, normalizedRed, normalizedGreen, normalizedBlue, normalizedAlpha);
	}

	public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
	}

	public ModelPart root() {
		return this.root;
	}
}
