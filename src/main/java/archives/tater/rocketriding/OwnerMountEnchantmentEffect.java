package archives.tater.rocketriding;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class OwnerMountEnchantmentEffect implements EnchantmentEntityEffect {
    private OwnerMountEnchantmentEffect() {}

    public static final OwnerMountEnchantmentEffect INSTANCE = new OwnerMountEnchantmentEffect();

    public static MapCodec<OwnerMountEnchantmentEffect> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        if (!(user instanceof Ownable ownable)) return;
        var owner = ownable.getOwner();
        if (owner == null) return;
        owner.startRiding(user);
    }

    @Override
    public MapCodec<? extends OwnerMountEnchantmentEffect> getCodec() {
        return CODEC;
    }
}
