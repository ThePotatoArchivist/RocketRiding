package archives.tater.rocketriding;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public class OwnerMountEnchantmentEffect implements EnchantmentEntityEffect {
    private OwnerMountEnchantmentEffect() {}

    public static final OwnerMountEnchantmentEffect INSTANCE = new OwnerMountEnchantmentEffect();

    public static MapCodec<OwnerMountEnchantmentEffect> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public void apply(ServerLevel world, int level, EnchantedItemInUse context, Entity user, Vec3 pos) {
        if (!(user instanceof TraceableEntity ownable)) return;
        var owner = ownable.getOwner();
        if (owner == null) return;
        owner.startRiding(user);
    }

    @Override
    public MapCodec<? extends OwnerMountEnchantmentEffect> codec() {
        return CODEC;
    }
}
