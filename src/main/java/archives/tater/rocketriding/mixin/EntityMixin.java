package archives.tater.rocketriding.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(Entity.class)
public abstract class EntityMixin {

    @SuppressWarnings("ConstantValue")
    @ModifyReturnValue(
            method = "getVehicleAttachmentPos",
            at = @At("RETURN")
    )
    private Vec3d playerSitHeight(Vec3d original, @Local(argsOnly = true, ordinal = 1) Entity vehicle) {
        if (!((Object) this instanceof PlayerEntity) || !(vehicle instanceof FireworkRocketEntity)) return original;
        return original.add(0.0, -0.6, 0.0);
    }
}
