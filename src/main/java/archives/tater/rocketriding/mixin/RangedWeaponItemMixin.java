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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

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

    @Inject(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V")
    )
    private void runEnchantmentEffect(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target, CallbackInfo ci, @Local ProjectileEntity projectileEntity) {
        if (stack == null || !(world instanceof ServerWorld serverWorld)) return;

        if (stack.isEmpty()) {
            RocketRiding.LOGGER.error("Invalid weapon firing an arrow");
            return;
        }

        RocketRiding.onProjectileFired(serverWorld, stack, projectileEntity, item -> {});
    }
}
