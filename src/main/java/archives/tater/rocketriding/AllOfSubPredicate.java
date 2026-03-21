package archives.tater.rocketriding;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record AllOfSubPredicate(
        List<EntitySubPredicate> predicates
) implements EntitySubPredicate {
    public static final MapCodec<AllOfSubPredicate> CODEC = EntitySubPredicate.CODEC.listOf().xmap(AllOfSubPredicate::new, AllOfSubPredicate::predicates).fieldOf("predicates");

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel world, @Nullable Vec3 pos) {
        for (var predicate : predicates)
            if (!predicate.matches(entity, world, pos))
                return false;
        return true;
    }
}
