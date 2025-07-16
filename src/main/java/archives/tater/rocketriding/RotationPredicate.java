package archives.tater.rocketriding;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.predicate.NumberRange.DoubleRange;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record RotationPredicate(
        Optional<DoubleRange> pitch,
        Optional<DoubleRange> yaw
) implements EntitySubPredicate {
    public static final MapCodec<RotationPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DoubleRange.CODEC.optionalFieldOf("pitch").forGetter(RotationPredicate::pitch),
            DoubleRange.CODEC.optionalFieldOf("yaw").forGetter(RotationPredicate::yaw)
    ).apply(instance, RotationPredicate::new));

    @Override
    public MapCodec<? extends EntitySubPredicate> getCodec() {
        return CODEC;
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (!(entity instanceof ProjectileEntity projectile)) return false;
        return pitch().map(range -> range.test(projectile.getPitch())).orElse(true) &&
                yaw().map(range -> range.test(entity.getYaw())).orElse(true);
    }
}
