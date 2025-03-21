package io.github.chakyl.splendidslimes.item;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.tabs.ITabFiller;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.data.SlimeBreedRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class SpawnEggItem  extends ForgeSpawnEggItem implements ITabFiller {
    public static final String SLIME = "EntityTag";
    public static final String ID = "type";
    public static final String DATA = "data";
    public static int color;
    public SpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties props) {
        super(type, backgroundColor, highlightColor, props);
    }
    @Override
    public void fillItemCategory(CreativeModeTab tab, CreativeModeTab.Output output) {
        SlimeBreedRegistry.INSTANCE.getKeys().stream().sorted().forEach(key -> {
            ItemStack s = new ItemStack(this);
            setStoredSlime(s, key);
            output.accept(s);
        });
    }
    @Override

    public Component getName(ItemStack pStack) {
        DynamicHolder<SlimeBreed> slime = getSlime(pStack);
        Component slimeName;
        if (!slime.isBound()) {
            slimeName = Component.literal("BROKEN").withStyle(ChatFormatting.OBFUSCATED);
        }
        else slimeName = slime.get().name();
        color = slime.get().getColor();
        return Component.translatable(this.getDescriptionId(pStack), slimeName);
    }

    public static DynamicHolder<SlimeBreed> getSlime(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(SLIME);
        if (stack.isEmpty() || tag == null || !tag.contains(ID)) {
            return SlimeBreedRegistry.INSTANCE.emptyHolder();
        }
        return SlimeBreedRegistry.INSTANCE.holder(new ResourceLocation(tag.getString(ID)));
    }

    public static void setStoredSlime(ItemStack stack, ResourceLocation slime) {
        stack.removeTagKey(SLIME);
        stack.getOrCreateTagElement(SLIME).putString(ID, slime.toString());
    }
    @Override
    public int getColor(int tintIndex) {
        return color;
    }
    @Nonnull
    @Override
    public EntityType<?> getType(CompoundTag compound) {
        if (compound != null && compound.contains("EntityTag", 10)) {
            CompoundTag entityTag = compound.getCompound("EntityTag");

            if (entityTag.contains("type", 8)) {
                return EntityType.byString(entityTag.getString("type")).orElse(getDefaultType());
            }
        }
        return getDefaultType();
    }
}
