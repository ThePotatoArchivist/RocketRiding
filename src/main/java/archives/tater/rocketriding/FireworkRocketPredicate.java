package archives.tater.rocketriding;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.predicate.NumberRange.IntRange;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record FireworkRocketPredicate(
        Optional<IntRange> duration,
        Optional<IntRange> explosions
) implements EntitySubPredicate {
    public static final MapCodec<FireworkRocketPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IntRange.CODEC.optionalFieldOf("duration").forGetter(FireworkRocketPredicate::duration),
            IntRange.CODEC.optionalFieldOf("explosions").forGetter(FireworkRocketPredicate::explosions)
    ).apply(instance, FireworkRocketPredicate::new));

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (!(entity instanceof FireworkRocketEntity fireworkRocket)) return false;
        var fireworks = fireworkRocket.getStack().get(DataComponentTypes.FIREWORKS);
        if (fireworks == null) return false;
        return duration.map(range -> range.test(fireworks.flightDuration())).orElse(true) &&
                explosions.map(range -> range.test(fireworks.explosions().size())).orElse(true);
    }

    @Override
    public MapCodec<? extends EntitySubPredicate> getCodec() {
        return CODEC;
    }

}
