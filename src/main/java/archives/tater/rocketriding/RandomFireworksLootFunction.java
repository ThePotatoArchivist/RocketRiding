package archives.tater.rocketriding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RandomFireworksLootFunction extends LootItemConditionalFunction {
    public final List<FireworkExplosion.Shape> shapes;
    public final Optional<IntProvider> duration;
    public final IntProvider explosions;
    public final IntProvider colors;
    public final IntProvider fadeColors;
    public final float trailChance;
    public final float twinkleChance;

    public static final List<FireworkExplosion.Shape> ALL_TYPES = List.of(FireworkExplosion.Shape.values());
    public static final List<Integer> ALL_COLORS = Arrays.stream(DyeColor.values()).map(DyeColor::getFireworkColor).toList();

    protected RandomFireworksLootFunction(
            List<LootItemCondition> conditions,
            List<FireworkExplosion.Shape> shapes,
            Optional<IntProvider> duration,
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

    public static final MapCodec<RandomFireworksLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).and(instance.group(
            FireworkExplosion.Shape.CODEC.listOf().optionalFieldOf("shapes", ALL_TYPES).forGetter((RandomFireworksLootFunction it) -> it.shapes),
            IntProvider.POSITIVE_CODEC.optionalFieldOf("duration").forGetter((RandomFireworksLootFunction it) -> it.duration),
            IntProvider.POSITIVE_CODEC.optionalFieldOf("explosions", ConstantInt.of(1)).forGetter((RandomFireworksLootFunction it) -> it.explosions),
            IntProvider.POSITIVE_CODEC.optionalFieldOf("colors", ConstantInt.of(1)).forGetter((RandomFireworksLootFunction it) -> it.colors),
            IntProvider.POSITIVE_CODEC.optionalFieldOf("fade_colors", ConstantInt.of(0)).forGetter((RandomFireworksLootFunction it) -> it.fadeColors),
            Codec.FLOAT.optionalFieldOf("trail_chance", 0f).forGetter((RandomFireworksLootFunction it) -> it.trailChance),
            Codec.FLOAT.optionalFieldOf("twinkle_chance", 0f).forGetter((RandomFireworksLootFunction it) -> it.twinkleChance)
    )).apply(instance, RandomFireworksLootFunction::new));

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        stack.set(DataComponents.FIREWORKS, generateFirework(stack.get(DataComponents.FIREWORKS), context.getRandom()));
        return stack;
    }

    private static <T> T pick(List<T> items, RandomSource random) {
        return items.get(random.nextInt(items.size()));
    }

    private IntList generateColors(int count, RandomSource random) {
        return IntArrayList.toListWithExpectedSize(IntStream.range(0, count).map(x -> pick(ALL_COLORS, random)), count);
    }

    private FireworkExplosion generateExplosion(RandomSource random) {
        return new FireworkExplosion(
                pick(shapes, random),
                generateColors(colors.sample(random), random),
                generateColors(fadeColors.sample(random), random),
                random.nextFloat() <= trailChance,
                random.nextFloat() <= twinkleChance
        );
    }

    private Fireworks generateFirework(@Nullable Fireworks current, RandomSource random) {
        var explosionCount = explosions.sample(random);
        var explosionList = new ArrayList<FireworkExplosion>();
        for (var i = 0; i < explosionCount; i++)
            explosionList.add(generateExplosion(random));
        return new Fireworks(
                duration.map(provider -> provider.sample(random)).orElse(current == null ? 1 : current.flightDuration()),
                explosionList
        );
    }

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return RocketRiding.RANDOM_FIREWORKS;
    }
}
