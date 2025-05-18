package io.github.chakyl.splendidslimes.util;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.splendidslimes.data.SlimeBreed;
import io.github.chakyl.splendidslimes.entity.SplendidSlime;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class SlimeUtils {
    public static MobEffectInstance copyEffect(MobEffectInstance effect) {
        return new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier());
    }

    public static void applyEffects(SplendidSlime slime, LivingEntity entity, List<MobEffectInstance> effects, boolean particles) {
        if (particles) slime.setParticleAnimationTick(0);
        for (MobEffectInstance effect : effects) {
            if (effect != null && entity != null) entity.addEffect(copyEffect(effect));
        }
    }

    public static void applyNegativeEffects(SplendidSlime splendidSlime, LivingEntity entity, boolean particles) {
        DynamicHolder<SlimeBreed> slime = splendidSlime.getSlime();
        DynamicHolder<SlimeBreed> secondarySlime = splendidSlime.getSecondarySlime();
        if (!slime.isBound()) return;
        if (!(splendidSlime.hasSameBreed(entity))) {
            List<MobEffectInstance> effects = slime.get().negativeEmitEffects();
            if (secondarySlime.isBound()) {
                List<MobEffectInstance> secondarySlimeEffects = secondarySlime.get().negativeEmitEffects();
                if (!secondarySlimeEffects.isEmpty()) {
                    applyEffects(splendidSlime, entity, secondarySlimeEffects, particles);
                }
            }
            if (!effects.isEmpty()) {
                applyEffects(splendidSlime, entity, effects, particles);
            }
        }
    }

    public static void applyPositiveEffects(SplendidSlime splendidSlime, LivingEntity entity, boolean particles) {
        DynamicHolder<SlimeBreed> slime = splendidSlime.getSlime();
        DynamicHolder<SlimeBreed> secondarySlime = splendidSlime.getSecondarySlime();
        if (!slime.isBound()) return;
        List<MobEffectInstance> effects = slime.get().positiveEmitEffects();
        if (secondarySlime.isBound()) {
            List<MobEffectInstance> secondarySlimeEffects = secondarySlime.get().positiveEmitEffects();
            if (!secondarySlimeEffects.isEmpty()) {
                applyEffects(splendidSlime, entity, secondarySlimeEffects, particles);
            }
        }
        if (!effects.isEmpty()) {
            applyEffects(splendidSlime, entity, effects, particles);
        }
    }


    private static CommandSourceStack createCommandSourceStack(@Nullable Player pPlayer, Level pLevel, BlockPos pPos, Component displayName) {
        String s = pPlayer == null ? "Splendid Slime" : pPlayer.getName().getString();
        Component component = (Component) (pPlayer == null ? displayName : pPlayer.getDisplayName());
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(pPos), Vec2.ZERO, (ServerLevel) pLevel, 2, s, component, pLevel.getServer(), pPlayer);
    }

    public static void runCommands(SplendidSlime splendidSlime, List<String> commands) {
        CommandSourceStack source = createCommandSourceStack((Player) null, splendidSlime.level(), splendidSlime.getOnPos(), splendidSlime.getDisplayName());
        source.withEntity(splendidSlime);
        source.withSuppressedOutput();
        for (String command : commands) {
            splendidSlime.getServer().getCommands().performPrefixedCommand(source, command);
        }
    }

    public static void executeSlimeCommands(SplendidSlime splendidSlime, boolean positive, boolean attack) {
        DynamicHolder<SlimeBreed> slime = splendidSlime.getSlime();
        DynamicHolder<SlimeBreed> secondarySlime = splendidSlime.getSecondarySlime();
        if (!slime.isBound()) return;
        if (attack) {
            runCommands(splendidSlime, slime.get().attackCommands());
            if (secondarySlime.isBound()) {
                runCommands(splendidSlime, secondarySlime.get().attackCommands());
            }
        } else if (positive) {
            runCommands(splendidSlime, slime.get().positiveCommands());
            if (secondarySlime.isBound()) {
                runCommands(splendidSlime, secondarySlime.get().positiveCommands());
            }
        } else {
            runCommands(splendidSlime, slime.get().negativeCommands());
            if (secondarySlime.isBound()) {
                runCommands(splendidSlime, secondarySlime.get().negativeCommands());
            }
        }
    }

    public static void handleHungryTraits(SplendidSlime splendidSlime) {
        double chance = 1 - (((splendidSlime.getEatingCooldown() + 1.0) / SplendidSlime.SLIME_HUNGRY_THRESHOLD));
        if (splendidSlime.getRandom().nextFloat() <= chance) {
            if (splendidSlime.hasTrait("explosive")) {
                splendidSlime.level().explode(splendidSlime, splendidSlime.getX(), splendidSlime.getY(), splendidSlime.getZ(), splendidSlime.hasTrait("flaming") ? 3f : 4f, splendidSlime.hasTrait("flaming"), Level.ExplosionInteraction.NONE);
            }
            if (splendidSlime.hasTrait("flaming")) {
                BlockPos blockpos = splendidSlime.getOnPos();
                if (splendidSlime.level().isEmptyBlock(blockpos)) {
                    splendidSlime.level().setBlockAndUpdate(blockpos, BaseFireBlock.getState(splendidSlime.level(), blockpos));
                }
            }
        }
    }
}
