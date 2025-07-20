package archives.tater.rocketriding;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record AllOfSubPredicate(
        List<EntitySubPredicate> predicates
) implements EntitySubPredicate {
    public static final MapCodec<AllOfSubPredicate> CODEC = EntitySubPredicate.CODEC.listOf().xmap(AllOfSubPredicate::new, AllOfSubPredicate::predicates).fieldOf("predicates");

    @Override
    public MapCodec<? extends EntitySubPredicate> getCodec() {
        return CODEC;
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        for (var predicate : predicates)
            if (!predicate.test(entity, world, pos))
                return false;
        return true;
    }
}
