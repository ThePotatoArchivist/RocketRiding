package archives.tater.rocketriding;

import archives.tater.rocketriding.mixin.FireworkRocketEntityAccessor;
import archives.tater.rocketriding.mixin.PersistentProjectileEntityInvoker;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.phys.Vec3;

public record ModifyItemEnchantmentEffect(
        List<LootItemFunction> modifiers
) implements EnchantmentEntityEffect {
    public static final MapCodec<ModifyItemEnchantmentEffect> CODEC = LootItemFunctions.ROOT_CODEC.listOf().fieldOf("modifiers").xmap(ModifyItemEnchantmentEffect::new, ModifyItemEnchantmentEffect::modifiers);

    @Override
    public void apply(ServerLevel world, int level, EnchantedItemInUse context, Entity user, Vec3 pos) {
        var stack = (switch (user) {
            case AbstractArrow projectile -> projectile.getPickupItemStackOrigin();
            case ThrowableItemProjectile projectile -> projectile.getItem();
            case FireworkRocketEntity projectile -> projectile.getItem();
            default -> ItemStack.EMPTY;
        }).copy();
        if (stack.isEmpty()) return;

        var lootContext = Enchantment.entityContext(world, level, user, pos);
        for (var modifier : modifiers)
            stack = modifier.apply(stack, lootContext);

        switch(user) {
            case AbstractArrow projectile -> ((PersistentProjectileEntityInvoker) projectile).invokeSetPickupItemStack(stack);
            case ThrowableItemProjectile projectile -> projectile.setItem(stack);
            case FireworkRocketEntity projectile -> projectile.getEntityData().set(FireworkRocketEntityAccessor.getDATA_ID_FIREWORKS_ITEM(), stack);
            default -> {}
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
