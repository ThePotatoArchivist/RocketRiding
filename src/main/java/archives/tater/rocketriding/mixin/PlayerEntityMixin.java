package archives.tater.rocketriding.mixin;

import archives.tater.rocketriding.RocketRiding;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
            method = "getProjectileType",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;getProjectiles()Ljava/util/function/Predicate;"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;getHeldProjectiles()Ljava/util/function/Predicate;")
            }
    )
    private Predicate<ItemStack> addEnchantmentProjectiles(Predicate<ItemStack> original, @Local(argsOnly = true) ItemStack stack) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return original;
        return RocketRiding.getAllowedPrimaryProjectiles(serverWorld, stack, this, original);
    }

    @ModifyExpressionValue(
            method = "getProjectileType",
            at = @At(value = "NEW", target = "(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/item/ItemStack;")
    )
    private ItemStack addEnchantmentDefaultProjectile(ItemStack original, @Local(argsOnly = true) ItemStack stack) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return original;
        return Objects.requireNonNullElse(RocketRiding.getDefaultProjectile(serverWorld, stack, this), original);
    }
}
