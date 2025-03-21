package io.github.chakyl.splendidslimes.registry;

import dev.shadowsoffire.placebo.registry.DeferredHelper;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.entity.SlimeEntityBase;
import io.github.chakyl.splendidslimes.entity.SplendidSlime;
import io.github.chakyl.splendidslimes.item.PlortItem;
import io.github.chakyl.splendidslimes.item.SpawnEggItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class ModElements {
    private static final DeferredHelper R = DeferredHelper.create(SplendidSlimes.MODID);
    static RegistryObject<EntityType<SlimeEntityBase>> slimeEntity = R.entity("splendid_slime", () -> EntityType.Builder.<SlimeEntityBase>of(SplendidSlime::new, MobCategory.MONSTER).build("splendid_slime"));

    public  static  class Entities {
        public static final RegistryObject<EntityType<SlimeEntityBase>> SPLENDID_SLIME = slimeEntity;
        private static void bootstrap() {}
    }

    public static class Items {
        public static final RegistryObject<PlortItem> PLORT = R.item("plort", () -> new PlortItem(new Item.Properties().stacksTo(64)));
        public static final RegistryObject<SpawnEggItem> SPAWN_EGG = R.item("spawn_egg_splendid_slime", () -> new SpawnEggItem(slimeEntity, 9748939, 6238757, new Item.Properties()));
        private static void bootstrap() {}
    }

    public static class Tabs {
        public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(SplendidSlimes.MODID, "tab"));

        public static final RegistryObject<CreativeModeTab> AB = R.tab("tab",
                () -> CreativeModeTab.builder().title(Component.translatable("itemGroup." + SplendidSlimes.MODID)).icon(() -> Items.PLORT.get().getDefaultInstance()).build());

        private static void bootstrap() {}
    }

    public static void bootstrap() {
        Items.bootstrap();
        Entities.bootstrap();
        Tabs.bootstrap();
    }
}