package io.github.chakyl.splendidslimes.item;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.tabs.ITabFiller;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.data.SlimeBreedRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

import static io.github.chakyl.splendidslimes.util.SlimeData.getSlimeData;

public class SlimeHeartItem extends Item implements ITabFiller {
    public static final String SLIME_HEART = "slime";
    public static final String ID = "id";
    public static final String DATA = "data";

    public SlimeHeartItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> list, TooltipFlag pFlag) {
        list.add(Component.translatable("info.splendid_slimes.slime_heart"));
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
        DynamicHolder<SlimeBreed> slime = getSlimeData(pStack, SLIME_HEART);
        Component slimeName;
        if (!slime.isBound()) {
            slimeName = Component.translatable("splendid_slimes.item.default_heart");
        }
        else slimeName = slime.get().name();
        return Component.translatable(this.getDescriptionId(pStack), slimeName);
    }

    public static void setStoredPlort(ItemStack stack, SlimeBreed slimeBreed) {
        setStoredPlort(stack, SlimeBreedRegistry.INSTANCE.getKey(slimeBreed));
    }

    public static void setStoredPlort(ItemStack stack, ResourceLocation plort) {
        stack.removeTagKey(SLIME_HEART);
        stack.getOrCreateTagElement(SLIME_HEART).putString(ID, plort.toString());
    }

    public static int getData(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(SLIME_HEART);
        return stack.isEmpty() || tag == null ? 0 : tag.getInt(DATA);
    }

    public static void setData(ItemStack stack, int data) {
        stack.getOrCreateTagElement(SLIME_HEART).putInt(DATA, data);
    }

}