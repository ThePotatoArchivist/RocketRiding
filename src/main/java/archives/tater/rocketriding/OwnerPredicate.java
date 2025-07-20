package archives.tater.rocketriding;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record OwnerPredicate(
        EntityPredicate predicate
) implements EntitySubPredicate {
    public static final MapCodec<OwnerPredicate> CODEC = EntityPredicate.CODEC.xmap(OwnerPredicate::new, OwnerPredicate::predicate).fieldOf("predicate");

    @Override
    public MapCodec<? extends OwnerPredicate> getCodec() {
        return CODEC;
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (!(entity instanceof Ownable ownable)) return false;
        return predicate.test(world, pos, ownable.getOwner());
    }
}
