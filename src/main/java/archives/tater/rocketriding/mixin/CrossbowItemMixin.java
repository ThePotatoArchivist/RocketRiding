package archives.tater.rocketriding.mixin;

import archives.tater.rocketriding.RocketRiding;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
	@ModifyExpressionValue(
			method = "createProjectile",
			at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;DDDZ)Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;")
	)
	private FireworkRocketEntity runEnchantmentEffect(FireworkRocketEntity original, @Local(argsOnly = true) Level world, @Local(argsOnly = true, ordinal = 0) ItemStack weapon) {
        if (weapon == null || !(world instanceof ServerLevel serverWorld)) return original;

        if (weapon.isEmpty()) {
			RocketRiding.LOGGER.error("Invalid weapon firing an arrow");
			return original;
        }

        RocketRiding.onFireworkShot(serverWorld, weapon, original, item -> {});

        return original;
	}

	@ModifyExpressionValue(
			method = "shootProjectile",
			at = @At(value = "INVOKE", target = "Ljava/lang/Math;sqrt(D)D")
	)
	private double checkGravity(double original, @Local(argsOnly = true) Projectile projectile) {
		return projectile.getType().is(RocketRiding.WEIGHTLESS_PROJECTILES) ? 0.0 : original;
	}
}
