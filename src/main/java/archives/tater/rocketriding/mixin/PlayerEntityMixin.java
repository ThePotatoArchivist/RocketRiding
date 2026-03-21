package archives.tater.rocketriding.mixin;

import archives.tater.rocketriding.RocketRiding;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
            method = "getProjectile",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getAllSupportedProjectiles()Ljava/util/function/Predicate;"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getSupportedHeldProjectiles()Ljava/util/function/Predicate;")
            }
    )
    private Predicate<ItemStack> addEnchantmentProjectiles(Predicate<ItemStack> original, @Local(argsOnly = true) ItemStack stack) {
        if (!(level() instanceof ServerLevel serverWorld)) return original;
        return RocketRiding.getAllowedPrimaryProjectiles(serverWorld, stack, this, original);
    }

    @ModifyExpressionValue(
            method = "getProjectile",
            at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack addEnchantmentDefaultProjectile(ItemStack original, @Local(argsOnly = true) ItemStack stack) {
        if (!(level() instanceof ServerLevel serverWorld)) return original;
        return Objects.requireNonNullElse(RocketRiding.getDefaultProjectile(serverWorld, stack, this), original);
    }
}
