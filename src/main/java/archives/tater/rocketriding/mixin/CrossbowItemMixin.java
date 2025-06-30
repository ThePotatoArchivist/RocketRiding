package archives.tater.rocketriding.mixin;

import archives.tater.rocketriding.RocketRiding;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
	@ModifyExpressionValue(
			method = "createArrowEntity",
			at = @At(value = "NEW", target = "(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/Entity;DDDZ)Lnet/minecraft/entity/projectile/FireworkRocketEntity;")
	)
	private FireworkRocketEntity runEnchantmentEffect(FireworkRocketEntity original, @Local(argsOnly = true) World world, @Local(argsOnly = true, ordinal = 0) ItemStack weapon) {
        if (weapon == null || !(world instanceof ServerWorld serverWorld)) return original;

        if (weapon.isEmpty()) {
			RocketRiding.LOGGER.error("Invalid weapon firing an arrow");
			return original;
        }

        RocketRiding.onProjectileSpawned(serverWorld, weapon, original, item -> {});

        return original;
	}

	@ModifyArg(
			method = "use",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;shootAll(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;FFLnet/minecraft/entity/LivingEntity;)V"),
			index = 4
	)
	private float projectileVelocityEnchantment(float original, @Local(argsOnly = true) World world, @Local ItemStack stack) {
		return world instanceof ServerWorld serverWorld ? RocketRiding.modifyVelocity(serverWorld, stack, original) : original;
	}
}
