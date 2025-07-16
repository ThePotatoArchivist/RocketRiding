package archives.tater.rocketriding.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FireworkRocketEntity.class)
public interface FireworkRocketEntityAccessor {
    @Accessor
    int getLifeTime();
    @Accessor
    void setLifeTime(int lifeTime);
    @Accessor
    static TrackedData<ItemStack> getITEM() {
        throw new AssertionError();
    }
}
