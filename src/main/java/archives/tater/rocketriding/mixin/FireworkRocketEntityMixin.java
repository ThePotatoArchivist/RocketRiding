package archives.tater.rocketriding.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin extends ProjectileEntity {
    public FireworkRocketEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/FireworkRocketEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;", ordinal = 1)
    )
    private Vec3d passengerControl(Vec3d original) {
        if (!isAlive() || !(getFirstPassenger() instanceof PlayerEntity player)) {
            return original;
        }

        return player.getRotationVector().multiply(original.length());
    }

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/FireworkRocketEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V")
    )
    private void kineticDamage(FireworkRocketEntity instance, MovementType movementType, Vec3d vec3d, Operation<Void> original) {
        original.call(instance, movementType, vec3d);
        if (!horizontalCollision) return;
        var damage = 10 * (float) (vec3d.horizontalLength() - getVelocity().horizontalLength()) - 3;
        for (var passenger : getPassengerList()) {
            passenger.damage(getDamageSources().flyIntoWall(), damage);
        }
    }
}
