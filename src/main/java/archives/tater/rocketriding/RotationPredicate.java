package archives.tater.rocketriding;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public record RotationPredicate(
        Optional<Doubles> pitch,
        Optional<Doubles> yaw
) implements EntitySubPredicate {
    public static final MapCodec<RotationPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Doubles.CODEC.optionalFieldOf("pitch").forGetter(RotationPredicate::pitch),
            Doubles.CODEC.optionalFieldOf("yaw").forGetter(RotationPredicate::yaw)
    ).apply(instance, RotationPredicate::new));

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel world, @Nullable Vec3 pos) {
        if (!(entity instanceof Projectile projectile)) return false;
        return pitch().map(range -> range.matches(projectile.getXRot())).orElse(true) &&
                yaw().map(range -> range.matches(entity.getYRot())).orElse(true);
    }
}
