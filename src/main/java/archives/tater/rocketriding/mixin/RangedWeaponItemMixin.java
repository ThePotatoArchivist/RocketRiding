package archives.tater.rocketriding.mixin;

import archives.tater.rocketriding.RocketRiding;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {
    @Unique
    private @Nullable ItemStack weaponStack = null;

    @Inject(
            method = "shootAll",
            at = @At("HEAD")
    )
    private void saveWeaponStack(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target, CallbackInfo ci) {
        weaponStack = stack;
    }

    @ModifyArg(
            method = "method_61659",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;shoot(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/ProjectileEntity;IFFFLnet/minecraft/entity/LivingEntity;)V"),
            index = 3
    )
    private float projectileVelocityEnchantment(float speed, @Local(argsOnly = true) ProjectileEntity projectileEntity) {
        if (weaponStack == null || !(projectileEntity.getWorld() instanceof ServerWorld world)) return speed;
        return RocketRiding.modifyVelocity(world, weaponStack, projectileEntity, speed);
    }

    @Inject(
            method = "method_61659",
            at = @At("TAIL")
    )
    private void runEnchantmentEffect(LivingEntity livingEntity, int i, float f, float g, float h, LivingEntity livingEntity2, ProjectileEntity projectile, CallbackInfo ci) {
        var stack = weaponStack;
        weaponStack = null;
        if (stack == null || stack.isEmpty()) {
            RocketRiding.LOGGER.error("Invalid weapon firing an arrow");
            return;
        }
        if (!(projectile.getWorld() instanceof ServerWorld world)) return;

        RocketRiding.onProjectileFired(world, stack, projectile, item -> {});
    }
}
