package io.github.chakyl.splendidslimes.util;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;

public class SlimeLootModifier extends LootModifier {

    public static final Codec<SlimeLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, SlimeLootModifier::new));

    public SlimeLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public Codec<SlimeLootModifier> codec() {
        return CODEC;
    }

    @Override
    public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.clear();
        return generatedLoot;
    }
}