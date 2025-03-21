package io.github.chakyl.splendidslimes.item;

import java.util.List;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.tabs.ITabFiller;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.data.SlimeBreedRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class PlortItem extends Item implements ITabFiller {
    public static final String PLORT = "plort";
    public static final String ID = "id";
    public static final String DATA = "data";

    public PlortItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> list, TooltipFlag pFlag) {
        list.add(Component.translatable("info.splendid_slimes.plort"));
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, CreativeModeTab.Output output) {
        SlimeBreedRegistry.INSTANCE.getKeys().stream().sorted().forEach(key -> {
            ItemStack s = new ItemStack(this);
            setStoredPlort(s, key);
            output.accept(s);
        });
    }

    @Override
    public Component getName(ItemStack pStack) {
        DynamicHolder<SlimeBreed> slime = getSlime(pStack);
        Component plortName;
        if (!slime.isBound()) {
            plortName = Component.literal("BROKEN").withStyle(ChatFormatting.OBFUSCATED);
        }
        else plortName = slime.get().name();
        return Component.translatable(this.getDescriptionId(pStack), plortName);
    }

    public static DynamicHolder<SlimeBreed> getSlime(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(PLORT);
        if (stack.isEmpty() || tag == null || !tag.contains(ID)) {
            return SlimeBreedRegistry.INSTANCE.emptyHolder();
        }
        return SlimeBreedRegistry.INSTANCE.holder(new ResourceLocation(tag.getString(ID)));
    }

    public static void setStoredPlort(ItemStack stack, SlimeBreed slimeBreed) {
        setStoredPlort(stack, SlimeBreedRegistry.INSTANCE.getKey(slimeBreed));
    }

    public static void setStoredPlort(ItemStack stack, ResourceLocation plort) {
        stack.removeTagKey(PLORT);
        stack.getOrCreateTagElement(PLORT).putString(ID, plort.toString());
    }

    public static int getData(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(PLORT);
        return stack.isEmpty() || tag == null ? 0 : tag.getInt(DATA);
    }

    public static void setData(ItemStack stack, int data) {
        stack.getOrCreateTagElement(PLORT).putInt(DATA, data);
    }

}