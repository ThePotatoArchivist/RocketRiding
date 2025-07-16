package archives.tater.rocketriding.mixin;

import archives.tater.rocketriding.RocketRiding;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(HostileEntity.class)
public abstract class HostileEntityMixin extends PathAwareEntity {

    protected HostileEntityMixin(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
            method = "getProjectileType",
            at = {
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
