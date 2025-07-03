package archives.tater.rocketriding.mixin;

import archives.tater.rocketriding.RocketRiding;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {
    @ModifyArg(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;shoot(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/ProjectileEntity;IFFFLnet/minecraft/entity/LivingEntity;)V"),
            index = 3
    )
    private float projectileVelocityEnchantment(float speed, @Local(argsOnly = true) ServerWorld world, @Local(argsOnly = true) ItemStack stack, @Local ProjectileEntity projectileEntity) {
        return RocketRiding.modifyVelocity(world, stack, projectileEntity, speed);
    }
}
