package archives.tater.rocketriding;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record OwnerPredicate(
        EntityPredicate predicate
) implements EntitySubPredicate {
    public static final MapCodec<OwnerPredicate> CODEC = EntityPredicate.CODEC.xmap(OwnerPredicate::new, OwnerPredicate::predicate).fieldOf("predicate");

    @Override
    public MapCodec<? extends OwnerPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel world, @Nullable Vec3 pos) {
        if (!(entity instanceof TraceableEntity ownable)) return false;
        return predicate.matches(world, pos, ownable.getOwner());
    }
}
