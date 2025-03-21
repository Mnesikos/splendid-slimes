package io.github.chakyl.splendidslimes.events;

import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.client.model.SlimeEntityModel;
import io.github.chakyl.splendidslimes.client.renderer.SlimeEntityRenderer;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.registry.ModElements;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
//
//@Mod.EventBusSubscriber(modid = SplendidSlimes.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
//public class ClientModEvents {
//
////    @SubscribeEvent
////    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event){
////        event.registerEntityRenderer(ModElements.GOLD_SLIME.get(), context -> new SlimeEntityRenderer(context, 0xFFfcba52));
////        event.registerEntityRenderer(ModElements.ANCIENT_SLIME.get(), context -> new SlimeEntityRenderer(context, 0xFF049ab7));
////    }
//    @SubscribeEvent
//    public static void onClientSetup(FMLClientSetupEvent event) {
//
//        SplendidSlimes.LOGGER.info("yeet");
//        event.enqueueWork(() -> {
//            EntityRenderers.register(ModElements.GOLD_SLIME.get(), context -> new SlimeEntityRenderer(context, 0xFFfcba52));
//            EntityRenderers.register(ModElements.ANCIENT_SLIME.get(), context -> new SlimeEntityRenderer(context, 0xFF049ab7));
//            SplendidSlimes.LOGGER.info("yeet 2");
//            for (SlimeBreed.SplendidSlimeCodex variant : SlimeBreed.getLoadedTiers()){
//                SplendidSlimes.LOGGER.info("yeet 3");
//                SplendidSlimes.LOGGER.info("Hex: " + variant.getColor());
//                SplendidSlimes.LOGGER.info(SlimeBreed.getSlimeForVariant(variant.getBreed()).get().toString());
//                EntityRenderers.register(SlimeBreed.getSlimeForVariant(variant.getBreed()).get(), pContext -> new SlimeEntityRenderer(pContext, variant.getColor()));
//            }
//
//        });
//
//    }
//
//    @SubscribeEvent
//    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
//        event.registerLayerDefinition(SlimeEntityModel.SLIME_LOCATION, SlimeEntityModel::createOuterBodyLayer);
//    }
//}

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.client.model.PlortModel;
import io.github.chakyl.splendidslimes.item.PlortItem;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import java.util.Set;
import net.minecraft.client.Minecraft;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT, modid = SplendidSlimes.MODID)
public class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event){
            event.registerEntityRenderer(ModElements.Entities.SPLENDID_SLIME.get(), SlimeEntityRenderer::new);
        }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        SplendidSlimes.LOGGER.info("yeet");
        event.enqueueWork(() -> {
            EntityRenderers.register(ModElements.Entities.SPLENDID_SLIME.get(), SlimeEntityRenderer::new);
        });

    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SlimeEntityModel.SLIME_LOCATION, SlimeEntityModel::createOuterBodyLayer);
    }

    @SubscribeEvent
    public static void addPlortModel(ModelEvent.RegisterAdditional e) {
        Set<ResourceLocation> locs = Minecraft.getInstance().getResourceManager().listResources("models", loc -> SplendidSlimes.MODID.equals(loc.getNamespace()) && loc.getPath().contains("/plort/") && loc.getPath().endsWith(".json"))
                .keySet();
        for (ResourceLocation s : locs) {
            String path = s.getPath().substring("models/".length(), s.getPath().length() - ".json".length());
            e.register(new ResourceLocation(SplendidSlimes.MODID, path));
        }
    }

    @SubscribeEvent
    public static void replacePlortModel(ModelEvent.ModifyBakingResult e) {
        ModelResourceLocation key = new ModelResourceLocation(SplendidSlimes.loc("plort"), "inventory");
        BakedModel oldModel = e.getModels().get(key);
        if (oldModel != null) {
            e.getModels().put(key, new PlortModel(oldModel, e.getModelBakery()));
        }
    }
    @SubscribeEvent
    public static void colors(RegisterColorHandlersEvent.Item e) {
        e.register((stack, tint) -> {
            DynamicHolder<SlimeBreed> slime = PlortItem.getSlime(stack);
            int color = 0xFFFFFF;
            if (slime.isBound()) {
                color = slime.get().getColor();
            }
            return color;
        }, ModElements.Items.SPAWN_EGG.get());
    }

}