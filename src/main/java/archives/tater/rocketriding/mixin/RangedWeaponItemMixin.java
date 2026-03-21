package archives.tater.rocketriding.mixin;

import archives.tater.rocketriding.RocketRiding;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

@Mixin(ProjectileWeaponItem.class)
public class RangedWeaponItemMixin {
    @Unique
    private @Nullable ItemStack weaponStack = null;

    @Inject(
            method = "shoot",
            at = @At("HEAD")
    )
    private void saveWeaponStack(ServerLevel world, LivingEntity shooter, InteractionHand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target, CallbackInfo ci) {
        weaponStack = stack;
    }

    @ModifyArg(
            method = "method_61659",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;shootProjectile(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/projectile/Projectile;IFFFLnet/minecraft/world/entity/LivingEntity;)V"),
            index = 3
    )
    private float projectileVelocityEnchantment(float speed, @Local(argsOnly = true) Projectile projectileEntity) {
        if (weaponStack == null || !(projectileEntity.level() instanceof ServerLevel world)) return speed;
        return RocketRiding.modifyVelocity(world, weaponStack, projectileEntity, speed);
    }

    @Inject(
            method = "method_61659",
            at = @At("TAIL")
    )
    private void runEnchantmentEffect(LivingEntity livingEntity, int i, float f, float g, float h, LivingEntity livingEntity2, Projectile projectile, CallbackInfo ci) {
        var stack = weaponStack;
        weaponStack = null;
        if (stack == null || stack.isEmpty()) {
            RocketRiding.LOGGER.error("Invalid weapon firing an arrow");
            return;
        }
        if (!(projectile.level() instanceof ServerLevel world)) return;

        RocketRiding.onProjectileFired(world, stack, projectile, item -> {});
    }
}
