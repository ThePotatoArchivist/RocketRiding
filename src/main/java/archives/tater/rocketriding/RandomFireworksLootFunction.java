package archives.tater.rocketriding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class RandomFireworksLootFunction extends ConditionalLootFunction {
    public final List<FireworkExplosionComponent.Type> shapes;
    public final IntProvider duration;
    public final IntProvider explosions;
    public final IntProvider colors;
    public final IntProvider fadeColors;
    public final float trailChance;
    public final float twinkleChance;

    public static final List<FireworkExplosionComponent.Type> ALL_TYPES = List.of(FireworkExplosionComponent.Type.values());
    public static final List<Integer> ALL_COLORS = Arrays.stream(DyeColor.values()).map(DyeColor::getFireworkColor).toList();

    protected RandomFireworksLootFunction(
            List<LootCondition> conditions,
            List<FireworkExplosionComponent.Type> shapes,
            IntProvider duration,
            IntProvider explosions,
            IntProvider colors,
            IntProvider fadeColors,
            float trailChance,
            float twinkleChance
    ) {
        super(conditions);
        this.shapes = shapes;
        this.duration = duration;
        this.explosions = explosions;
        this.colors = colors;
        this.fadeColors = fadeColors;
        this.trailChance = trailChance;
        this.twinkleChance = twinkleChance;
    }

    public static final MapCodec<RandomFireworksLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> addConditionsField(instance).and(instance.group(
            FireworkExplosionComponent.Type.CODEC.listOf().optionalFieldOf("shapes", ALL_TYPES).forGetter((RandomFireworksLootFunction it) -> it.shapes),
            IntProvider.POSITIVE_CODEC.optionalFieldOf("duration", ConstantIntProvider.create(1)).forGetter((RandomFireworksLootFunction it) -> it.duration),
            IntProvider.POSITIVE_CODEC.optionalFieldOf("explosions", ConstantIntProvider.create(1)).forGetter((RandomFireworksLootFunction it) -> it.explosions),
            IntProvider.POSITIVE_CODEC.optionalFieldOf("colors", ConstantIntProvider.create(1)).forGetter((RandomFireworksLootFunction it) -> it.colors),
            IntProvider.POSITIVE_CODEC.optionalFieldOf("fade_colors", ConstantIntProvider.create(0)).forGetter((RandomFireworksLootFunction it) -> it.fadeColors),
            Codec.FLOAT.optionalFieldOf("trail_chance", 0f).forGetter((RandomFireworksLootFunction it) -> it.trailChance),
            Codec.FLOAT.optionalFieldOf("twinkle_chance", 0f).forGetter((RandomFireworksLootFunction it) -> it.twinkleChance)
    )).apply(instance, RandomFireworksLootFunction::new));

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        stack.set(DataComponentTypes.FIREWORKS, generateFirework(context.getRandom()));
        return stack;
    }

    private static <T> T pick(List<T> items, Random random) {
        return items.get(random.nextInt(items.size()));
    }

    private IntList generateColors(int count, Random random) {
        return IntArrayList.toListWithExpectedSize(IntStream.range(0, count).map(x -> pick(ALL_COLORS, random)), count);
    }

    private FireworkExplosionComponent generateExplosion(Random random) {
        return new FireworkExplosionComponent(
                pick(shapes, random),
                generateColors(colors.get(random), random),
                generateColors(fadeColors.get(random), random),
                random.nextFloat() <= trailChance,
                random.nextFloat() <= twinkleChance
        );
    }

    private FireworksComponent generateFirework(Random random) {
        var explosionCount = explosions.get(random);
        var explosionList = new ArrayList<FireworkExplosionComponent>();
        for (var i = 0; i < explosionCount; i++)
            explosionList.add(generateExplosion(random));
        return new FireworksComponent(
                duration.get(random),
                explosionList
        );
    }

    @Override
    public LootFunctionType<? extends ConditionalLootFunction> getType() {
        return null;
    }
}
