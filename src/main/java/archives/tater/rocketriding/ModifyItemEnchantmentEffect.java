package archives.tater.rocketriding;

import archives.tater.rocketriding.mixin.EnchantmentInvoker;
import archives.tater.rocketriding.mixin.FireworkRocketEntityAccessor;
import archives.tater.rocketriding.mixin.PersistentProjectileEntityInvoker;
import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public record ModifyItemEnchantmentEffect(
        List<LootFunction> modifiers
) implements EnchantmentEntityEffect {
    public static final MapCodec<ModifyItemEnchantmentEffect> CODEC = LootFunctionTypes.CODEC.listOf().fieldOf("modifiers").xmap(ModifyItemEnchantmentEffect::new, ModifyItemEnchantmentEffect::modifiers);

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        var stack = (switch (user) {
            case PersistentProjectileEntity projectile -> projectile.getItemStack();
            case ThrownItemEntity projectile -> projectile.getStack();
            case FireworkRocketEntity projectile -> projectile.getStack();
            default -> ItemStack.EMPTY;
        }).copy();
        if (stack.isEmpty()) return;

        var lootContext = EnchantmentInvoker.invokeCreateEnchantedEntityLootContext(world, level, user, pos);
        for (var modifier : modifiers)
            stack = modifier.apply(stack, lootContext);

        switch(user) {
            case PersistentProjectileEntity projectile -> ((PersistentProjectileEntityInvoker) projectile).invokeSetStack(stack);
            case ThrownItemEntity projectile -> projectile.setItem(stack);
            case FireworkRocketEntity projectile -> projectile.getDataTracker().set(FireworkRocketEntityAccessor.getITEM(), stack);
            default -> {}
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
