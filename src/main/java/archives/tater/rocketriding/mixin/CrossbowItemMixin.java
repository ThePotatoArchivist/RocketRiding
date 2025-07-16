package archives.tater.rocketriding.mixin;

import archives.tater.rocketriding.RocketRiding;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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

        RocketRiding.onFireworkShot(serverWorld, weapon, original, item -> {});

        return original;
	}

	@ModifyExpressionValue(
			method = "shoot",
			at = @At(value = "INVOKE", target = "Ljava/lang/Math;sqrt(D)D")
	)
	private double checkGravity(double original, @Local(argsOnly = true) ProjectileEntity projectile) {
		return projectile.getType().isIn(RocketRiding.WEIGHTLESS_PROJECTILES) ? 0.0 : original;
	}
}
