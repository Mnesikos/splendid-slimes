package io.github.chakyl.splendidslimes.registry;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntityType;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.block.PlortRippitBlock;
import io.github.chakyl.splendidslimes.block.SlimeIncubatorBlock;
import io.github.chakyl.splendidslimes.blockentity.PlortRippitBlockEntity;
import io.github.chakyl.splendidslimes.blockentity.SlimeIncubatorBlockEntity;
import io.github.chakyl.splendidslimes.entity.SlimeEntityBase;
import io.github.chakyl.splendidslimes.entity.SplendidSlime;
import io.github.chakyl.splendidslimes.item.PlortItem;
import io.github.chakyl.splendidslimes.item.SlimeHeartItem;
import io.github.chakyl.splendidslimes.item.SpawnEggItem;
import io.github.chakyl.splendidslimes.util.SlimeLootModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModElements {
    private static final DeferredHelper R = DeferredHelper.create(SplendidSlimes.MODID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SplendidSlimes.MODID);
    public static final RegistryObject<Codec<SlimeLootModifier>> SLIME_MODIFIER = LOOT_MODIFIERS.register("slime_modifier", () -> SlimeLootModifier.CODEC);

    static RegistryObject<EntityType<SlimeEntityBase>> slimeEntity = R.entity("splendid_slime", () -> EntityType.Builder.<SlimeEntityBase>of(SplendidSlime::new, MobCategory.MONSTER).build("splendid_slime"));
        public static class Blocks {
        public static final RegistryObject<Block> SLIME_INCUBATOR = R.block("slime_incubator", () -> new SlimeIncubatorBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> PLORT_RIPPIT = R.block("plort_rippit", () -> new PlortRippitBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK).strength(4, 3000).noOcclusion()));

        private static void bootstrap() {}
    }

    public static class BlockEntities {
        public static final RegistryObject<BlockEntityType<SlimeIncubatorBlockEntity>> SLIME_INCUBATOR = R.blockEntity("slime_incubator",
                () -> new TickingBlockEntityType<>(SlimeIncubatorBlockEntity::new, ImmutableSet.of(ModElements.Blocks.SLIME_INCUBATOR.get()), false, true));

        public static final RegistryObject<BlockEntityType<PlortRippitBlockEntity>> PLORT_RIPPIT = R.blockEntity("plort_rippit",
                () -> new TickingBlockEntityType<>(PlortRippitBlockEntity::new, ImmutableSet.of(Blocks.PLORT_RIPPIT.get()), false, true));

        private static void bootstrap() {}
    }

    public  static  class Entities {
        public static final RegistryObject<EntityType<SlimeEntityBase>> SPLENDID_SLIME = slimeEntity;
        private static void bootstrap() {}
    }

    public static class Items {
        public static final RegistryObject<BlockItem> SLIME_INCUBATOR = R.item("slime_incubator", () -> new BlockItem(Blocks.SLIME_INCUBATOR.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> PLORT_RIPPIT = R.item("plort_rippit", () -> new BlockItem(Blocks.PLORT_RIPPIT.get(), new Item.Properties()));
        public static final RegistryObject<PlortItem> PLORT = R.item("plort", () -> new PlortItem(new Item.Properties().stacksTo(64)));
        public static final RegistryObject<SlimeHeartItem> SLIME_HEART = R.item("slime_heart", () -> new SlimeHeartItem(new Item.Properties().stacksTo(64)));
        public static final RegistryObject<SpawnEggItem> SPAWN_EGG = R.item("spawn_egg_splendid_slime", () -> new SpawnEggItem(slimeEntity, 9748939, 6238757, new Item.Properties()));
        private static void bootstrap() {}
    }

    public static class Tabs {
        public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(SplendidSlimes.MODID, "tab"));

        public static final RegistryObject<CreativeModeTab> AB = R.tab("tab",
                () -> CreativeModeTab.builder().title(Component.translatable("itemGroup." + SplendidSlimes.MODID)).icon(() -> Items.SLIME_INCUBATOR.get().getDefaultInstance()).build());

        private static void bootstrap() {}
    }

    public static void bootstrap() {
        Blocks.bootstrap();
        BlockEntities.bootstrap();
        Items.bootstrap();
        Entities.bootstrap();
        Tabs.bootstrap();
    }
}