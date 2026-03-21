package archives.tater.rocketriding;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;

public record FireworkRocketPredicate(
        Optional<Ints> duration,
        Optional<Ints> explosions
) implements EntitySubPredicate {
    public static final MapCodec<FireworkRocketPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ints.CODEC.optionalFieldOf("duration").forGetter(FireworkRocketPredicate::duration),
            Ints.CODEC.optionalFieldOf("explosions").forGetter(FireworkRocketPredicate::explosions)
    ).apply(instance, FireworkRocketPredicate::new));

    @Override
    public boolean matches(Entity entity, ServerLevel world, @Nullable Vec3 pos) {
        if (!(entity instanceof FireworkRocketEntity fireworkRocket)) return false;
        var fireworks = fireworkRocket.getItem().get(DataComponents.FIREWORKS);
        if (fireworks == null) return false;
        return duration.map(range -> range.matches(fireworks.flightDuration())).orElse(true) &&
                explosions.map(range -> range.matches(fireworks.explosions().size())).orElse(true);
    }

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

}
