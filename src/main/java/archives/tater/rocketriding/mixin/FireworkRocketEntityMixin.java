package archives.tater.rocketriding.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin extends Projectile {
    public FireworkRocketEntityMixin(EntityType<? extends Projectile> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;", ordinal = 1)
    )
    private Vec3 passengerControl(Vec3 original) {
        if (!isAlive() || !(getFirstPassenger() instanceof Player player)) {
            return original;
        }

        return player.getLookAngle().scale(original.length());
    }

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V")
    )
    private void kineticDamage(FireworkRocketEntity instance, MoverType movementType, Vec3 vec3d, Operation<Void> original) {
        original.call(instance, movementType, vec3d);
        if (!(level() instanceof ServerLevel serverWorld) || !horizontalCollision) return;
        var damage = 10 * (float) (vec3d.horizontalDistance() - getDeltaMovement().horizontalDistance()) - 3;
        for (var passenger : getPassengers()) {
            passenger.hurtServer(serverWorld, damageSources().flyIntoWall(), damage);
        }
    }
}
