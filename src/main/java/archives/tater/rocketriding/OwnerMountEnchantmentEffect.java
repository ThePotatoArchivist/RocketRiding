package archives.tater.rocketriding;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public record OwnerMountEnchantmentEffect(Optional<EntityPredicate> ownerCondition) implements EnchantmentEntityEffect {
    public static MapCodec<OwnerMountEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityPredicate.CODEC.optionalFieldOf("owner_condition").forGetter(OwnerMountEnchantmentEffect::ownerCondition)
    ).apply(instance, OwnerMountEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        if (!(user instanceof ProjectileEntity projectileEntity)) return;
        var owner = projectileEntity.getOwner();
        if (owner == null) return;
        if (ownerCondition.isEmpty() || ownerCondition.get().test(world, owner.getPos(), owner))
            owner.startRiding(user);
    }

    private LootContext createOwnerContext(ServerWorld world, Entity owner) {
        return new LootContext.Builder(new LootContextParameterSet.Builder(world)
                .add(LootContextParameters.THIS_ENTITY, owner)
                .build(LootContextTypes.BARTER)).build(Optional.empty());
    }

    @Override
    public MapCodec<? extends OwnerMountEnchantmentEffect> getCodec() {
        return CODEC;
    }
}
