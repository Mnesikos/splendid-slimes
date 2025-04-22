package io.github.chakyl.splendidslimes.item.ItemProjectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;

public class ItemProjectileEntity extends AbstractHurtingProjectile implements IEntityAdditionalSpawnData {
    protected ItemStack stack = ItemStack.EMPTY;

    public ItemProjectileEntity(EntityType<? extends AbstractHurtingProjectile> type, Level world) {
        super(type, world);
    }

    public ItemStack getItem() {
        return stack;
    }

    public void setItem(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        stack = ItemStack.of(nbt.getCompound("Item"));
        super.readAdditionalSaveData(nbt);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        nbt.put("Item", stack.serializeNBT());
        super.addAdditionalSaveData(nbt);
    }

    public void tick() {
        setDeltaMovement(getDeltaMovement().add(0, -0.05, 0).scale(1));
        super.tick();
    }

    @Override
    protected float getInertia() {
        return 1;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected void onHitEntity(EntityHitResult ray) {
        super.onHitEntity(ray);

        Entity target = ray.getEntity();
        float knockback = 1;
        Entity owner = this.getOwner();

        if (!target.isAlive())
            return;
        if (target instanceof WitherBoss && ((WitherBoss) target).isPowered())
            return;


        boolean targetIsEnderman = target.getType() == EntityType.ENDERMAN;

        boolean onServer = !level().isClientSide;

        if (targetIsEnderman)
            return;

        if (!(target instanceof LivingEntity)) {
            recoverItem();
            kill();
            return;
        }

        LivingEntity livingentity = (LivingEntity) target;

        if (onServer && knockback > 0) {
            Vec3 appliedMotion = this.getDeltaMovement()
                    .multiply(1.0D, 0.0D, 1.0D)
                    .normalize()
                    .scale(knockback * 0.6);
            if (appliedMotion.lengthSqr() > 0.0D)
                livingentity.push(appliedMotion.x, 0.1D, appliedMotion.z);
        }

        if (livingentity != owner && livingentity instanceof Player && owner instanceof ServerPlayer
                && !this.isSilent()) {
            ((ServerPlayer) owner).connection
                    .send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
        }
        recoverItem();
        kill();
    }

    private void recoverItem() {
        if (!stack.isEmpty())
            spawnAtLocation(ItemHandlerHelper.copyStackWithSize(stack, 1));
    }


    @Override
    protected void onHitBlock(BlockHitResult ray) {
        recoverItem();
        super.onHitBlock(ray);
        kill();
    }
    @SuppressWarnings("unchecked")
    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        EntityType.Builder<ItemProjectileEntity> entityBuilder = (EntityType.Builder<ItemProjectileEntity>) builder;
        return entityBuilder.sized(.25f, .25f);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        CompoundTag compound = new CompoundTag();
        addAdditionalSaveData(compound);
        buffer.writeNbt(compound);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        readAdditionalSaveData(additionalData.readNbt());
    }

}