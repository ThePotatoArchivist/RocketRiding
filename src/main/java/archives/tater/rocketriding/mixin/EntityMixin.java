package archives.tater.rocketriding.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(Entity.class)
public abstract class EntityMixin {

    @SuppressWarnings("ConstantValue")
    @ModifyReturnValue(
            method = "getVehicleAttachmentPoint",
            at = @At("RETURN")
    )
    private Vec3 playerSitHeight(Vec3 original, @Local(argsOnly = true) Entity vehicle) {
        if (!((Object) this instanceof Player) || !(vehicle instanceof FireworkRocketEntity)) return original;
        return original.add(0.0, -0.6, 0.0);
    }
}
