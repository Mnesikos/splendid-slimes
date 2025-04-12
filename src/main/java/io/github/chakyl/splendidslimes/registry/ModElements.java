package io.github.chakyl.splendidslimes.registry;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntityType;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import io.github.chakyl.splendidslimes.SplendidSlimes;
import io.github.chakyl.splendidslimes.block.PlortPressBlock;
import io.github.chakyl.splendidslimes.block.PlortRippitBlock;
import io.github.chakyl.splendidslimes.block.SlimeIncubatorBlock;
import io.github.chakyl.splendidslimes.block.corral.CorralBlock;
import io.github.chakyl.splendidslimes.block.corral.CorralPane;
import io.github.chakyl.splendidslimes.blockentity.PlortPressBlockEntity;
import io.github.chakyl.splendidslimes.blockentity.PlortRippitBlockEntity;
import io.github.chakyl.splendidslimes.blockentity.SlimeIncubatorBlockEntity;
import io.github.chakyl.splendidslimes.entity.SlimeEntityBase;
import io.github.chakyl.splendidslimes.entity.SplendidSlime;
import io.github.chakyl.splendidslimes.entity.Tarr;
import io.github.chakyl.splendidslimes.item.PlortItem;
import io.github.chakyl.splendidslimes.item.SlimeHeartItem;
import io.github.chakyl.splendidslimes.item.SlimeSpawnEggItem;
import io.github.chakyl.splendidslimes.recipe.PlortPressingRecipe;
import io.github.chakyl.splendidslimes.recipe.PlortRippingRecipe;
import io.github.chakyl.splendidslimes.screen.PlortPressMenu;
import io.github.chakyl.splendidslimes.util.SlimeLootModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModElements {
    private static final DeferredHelper R = DeferredHelper.create(SplendidSlimes.MODID);

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SplendidSlimes.MODID);
    public static final RegistryObject<Codec<SlimeLootModifier>> SLIME_MODIFIER = LOOT_MODIFIERS.register("slime_modifier", () -> SlimeLootModifier.CODEC);

    private static final BlockBehaviour.StatePredicate ALWAYS_FALSE = (state, world, pos) -> false;

    static RegistryObject<EntityType<SlimeEntityBase>> slimeEntity = R.entity("splendid_slime", () -> EntityType.Builder.<SlimeEntityBase>of(SplendidSlime::new, MobCategory.MONSTER).build("splendid_slime"));
    static RegistryObject<EntityType<SlimeEntityBase>> tarrEntity = R.entity("tarr", () -> EntityType.Builder.<SlimeEntityBase>of(Tarr::new, MobCategory.MONSTER).build("tarr"));

    static BlockBehaviour.Properties defaultBehavior = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK);

    public static class Blocks {
        public static final RegistryObject<Block> SLIME_INCUBATOR = R.block("slime_incubator", () -> new SlimeIncubatorBlock(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> PLORT_PRESS = R.block("plort_press", () -> new PlortPressBlock(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> PLORT_RIPPIT = R.block("plort_rippit", () -> new PlortRippitBlock(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<CorralBlock> CORRAL_BLOCK = R.block("corral_block", () -> new CorralBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.GLASS).strength(4, 3000).noOcclusion().isSuffocating(ALWAYS_FALSE).isViewBlocking(ALWAYS_FALSE)));
        public static final RegistryObject<CorralPane> CORRAL_PANE = R.block("corral_pane", () -> new CorralPane(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.GLASS_PANE).strength(4, 3000).noOcclusion().isSuffocating(ALWAYS_FALSE).isViewBlocking(ALWAYS_FALSE)));

        // Hats
        public static final RegistryObject<Block> ALL_SEEING_HAT = R.block("hat/all_seeing_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> BITWISE_HAT = R.block("hat/bitwise_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> BLAZING_HAT = R.block("hat/blazing_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> BOOMCAT_HAT = R.block("hat/boomcat_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> GOLD_HAT = R.block("hat/gold_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> ORBY_HAT = R.block("hat/orby_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> MINTY_HAT = R.block("hat/minty_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> PHANTOM_HAT = R.block("hat/phantom_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> PUDDLE_HAT = R.block("hat/puddle_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> PRISMA_HAT = R.block("hat/prisma_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> SHULKING_HAT = R.block("hat/shulking_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> SLIMY_HAT = R.block("hat/slimy_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> SWEET_HAT = R.block("hat/sweet_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> WEBBY_HAT = R.block("hat/webby_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));
        public static final RegistryObject<Block> WEEPING_HAT = R.block("hat/weeping_hat", () -> new Block(defaultBehavior.strength(4, 3000).noOcclusion()));

        private static void bootstrap() {
        }
    }

    public static class BlockEntities {
        public static final RegistryObject<BlockEntityType<SlimeIncubatorBlockEntity>> SLIME_INCUBATOR = R.blockEntity("slime_incubator",
                () -> new TickingBlockEntityType<>(SlimeIncubatorBlockEntity::new, ImmutableSet.of(ModElements.Blocks.SLIME_INCUBATOR.get()), false, true));
        public static final RegistryObject<BlockEntityType<PlortPressBlockEntity>> PLORT_PRESS = R.blockEntity("plort_press",
                () -> new TickingBlockEntityType<>(PlortPressBlockEntity::new, ImmutableSet.of(Blocks.PLORT_PRESS.get()), false, true));
        public static final RegistryObject<BlockEntityType<PlortRippitBlockEntity>> PLORT_RIPPIT = R.blockEntity("plort_rippit",
                () -> new TickingBlockEntityType<>(PlortRippitBlockEntity::new, ImmutableSet.of(Blocks.PLORT_RIPPIT.get()), false, true));

        private static void bootstrap() {
        }
    }

    public static class Items {
        public static final RegistryObject<BlockItem> SLIME_INCUBATOR = R.item("slime_incubator", () -> new BlockItem(Blocks.SLIME_INCUBATOR.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> PLORT_PRESS = R.item("plort_press", () -> new BlockItem(Blocks.PLORT_PRESS.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> PLORT_RIPPIT = R.item("plort_rippit", () -> new BlockItem(Blocks.PLORT_RIPPIT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> CORRAL_BLOCK = R.item("corral_block", () -> new BlockItem(Blocks.CORRAL_BLOCK.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> CORRAL_PANE = R.item("corral_pane", () -> new BlockItem(Blocks.CORRAL_PANE.get(), new Item.Properties()));
        public static final RegistryObject<PlortItem> PLORT = R.item("plort", () -> new PlortItem(new Item.Properties().stacksTo(64)));
        public static final RegistryObject<SlimeHeartItem> SLIME_HEART = R.item("slime_heart", () -> new SlimeHeartItem(new Item.Properties().stacksTo(64)));
        public static final RegistryObject<SlimeSpawnEggItem> SPLENDID_SLIME_SPAWN_EGG = R.item("spawn_egg_splendid_slime", () -> new SlimeSpawnEggItem(slimeEntity, 0xff7d9d, 0xff7d9d, new Item.Properties()));
        public static final RegistryObject<ForgeSpawnEggItem> TARR_SPAWN_EGG = R.item("spawn_egg_tarr", () -> new ForgeSpawnEggItem(tarrEntity, 0x2c221c, 0x921f78, new Item.Properties()));
        public static final RegistryObject<Item> TARRTAR = R.item("tarrtar", () -> new Item(new Item.Properties().stacksTo(64)));
        // Hats
        public static final RegistryObject<BlockItem> ALL_SEEING_HAT = R.item("hat/all_seeing_hat", () -> new BlockItem(Blocks.ALL_SEEING_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> BITWISE_HAT = R.item("hat/bitwise_hat", () -> new BlockItem(Blocks.BITWISE_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> BLAZING_HAT = R.item("hat/blazing_hat", () -> new BlockItem(Blocks.BLAZING_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> BOOMCAT_HAT = R.item("hat/boomcat_hat", () -> new BlockItem(Blocks.BOOMCAT_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> GOLD_HAT = R.item("hat/gold_hat", () -> new BlockItem(Blocks.GOLD_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> MINTY_HAT = R.item("hat/minty_hat", () -> new BlockItem(Blocks.MINTY_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> ORBY_HAT = R.item("hat/orby_hat", () -> new BlockItem(Blocks.ORBY_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> PHANTOM_HAT = R.item("hat/phantom_hat", () -> new BlockItem(Blocks.PHANTOM_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> PRISMA_HAT = R.item("hat/prisma_hat", () -> new BlockItem(Blocks.PRISMA_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> PUDDLE_HAT = R.item("hat/puddle_hat", () -> new BlockItem(Blocks.PUDDLE_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> SHULKING_HAT = R.item("hat/shulking_hat", () -> new BlockItem(Blocks.SHULKING_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> SLIMY_HAT = R.item("hat/slimy_hat", () -> new BlockItem(Blocks.SLIMY_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> SWEET_HAT = R.item("hat/sweet_hat", () -> new BlockItem(Blocks.SWEET_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> WEBBY_HAT = R.item("hat/webby_hat", () -> new BlockItem(Blocks.WEBBY_HAT.get(), new Item.Properties()));
        public static final RegistryObject<BlockItem> WEEPING_HAT = R.item("hat/weeping_hat", () -> new BlockItem(Blocks.WEEPING_HAT.get(), new Item.Properties()));

        private static void bootstrap() {
        }
    }

    public static class Entities {
        public static final RegistryObject<EntityType<SlimeEntityBase>> SPLENDID_SLIME = slimeEntity;
        public static final RegistryObject<EntityType<SlimeEntityBase>> TARR = tarrEntity;

        private static void bootstrap() {
        }
    }

    public static class Menus {
        public static final RegistryObject<MenuType<PlortPressMenu>> PLORT_PRESS_MENU = R.menu("plort_press_menu", () -> MenuUtil.bufType(PlortPressMenu::new));
        private static void bootstrap() {
        }
    }

    public static class Recipes {
        public static final RegistryObject<RecipeSerializer<PlortPressingRecipe>> PLORT_PRESSING_SERIALIZER = R.recipeSerializer("plort_pressing", PlortPressingRecipe.Serializer::new);
        public static final RegistryObject<RecipeSerializer<PlortRippingRecipe>> PLORT_RIPPING_SERIALIZER = R.recipeSerializer("plort_ripping", PlortRippingRecipe.Serializer::new);
        private static void bootstrap() {

        }
    }

    public static class Tabs {
        public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(SplendidSlimes.MODID, "tab"));

        public static final RegistryObject<CreativeModeTab> AB = R.tab("tab",
                () -> CreativeModeTab.builder().title(Component.translatable("itemGroup." + SplendidSlimes.MODID)).icon(() -> Items.SLIME_INCUBATOR.get().getDefaultInstance()).build());

        private static void bootstrap() {
        }
    }

    public static void bootstrap() {
        Blocks.bootstrap();
        BlockEntities.bootstrap();
        Items.bootstrap();
        Entities.bootstrap();
        Menus.bootstrap();
        Recipes.bootstrap();
        Tabs.bootstrap();
    }
}