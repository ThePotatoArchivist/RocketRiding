package archives.tater.rocketriding.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FireworkRocketEntity.class)
public interface FireworkRocketEntityAccessor {
    @Accessor
    int getLifetime();
    @Accessor
    void setLifetime(int lifeTime);
    @Accessor
    static EntityDataAccessor<ItemStack> getDATA_ID_FIREWORKS_ITEM() {
        throw new AssertionError();
    }
}
