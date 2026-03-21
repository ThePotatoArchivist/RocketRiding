package archives.tater.rocketriding;

import archives.tater.rocketriding.mixin.FireworkRocketEntityAccessor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import com.google.common.base.Predicates;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class RocketRiding implements ModInitializer {
	public static final String MOD_ID = "rocketriding";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ResourceKey<LootTable> END_CITY_INJECT = ResourceKey.create(Registries.LOOT_TABLE, id("inject/end_city_treasure"));
	public static final ResourceKey<LootTable> TRIAL_CHAMBER_INJECT = ResourceKey.create(Registries.LOOT_TABLE, id("inject/trial_chambers"));

	private static <T> DataComponentType<T> registerComponent(Identifier id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
		return Registry.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, id, (builderOperator.apply(DataComponentType.builder())).build());
	}

	private static <T> DataComponentType<T> registerComponent(Identifier id, Codec<T> codec) {
		return registerComponent(id, builder -> builder.persistent(codec));
	}

	private static <T extends EntitySubPredicate> MapCodec<T> registerSubPredicate(Identifier id, MapCodec<T> codec) {
		return Registry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, id, codec);
	}

	private static <T extends LootItemFunction> LootItemFunctionType<T> registerLootFunction(Identifier id, MapCodec<T> codec) {
		return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, id, new LootItemFunctionType<>(codec));
	}

	private static <T extends EnchantmentEntityEffect> MapCodec<T> registerEffect(Identifier id, MapCodec<T> codec) {
		return Registry.register(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE, id, codec);
	}

	// Currently only implemented on RangedWeaponItem
	public static final DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> PROJECTILE_VELOCITY = registerComponent(
			id("projectile_velocity"), ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf()
	);

	public static final DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> ROCKET_DURATION = registerComponent(
			id("firework_rocket_duration"), ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf()
	);

	public static final DataComponentType<List<ConditionalEffect<EnchantmentEntityEffect>>> PROJECTILE_FIRED = registerComponent(
			id("projectile_fired"), ConditionalEffect.codec(EnchantmentEntityEffect.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf()
	);

	public static final DataComponentType<List<ConditionalEffect<HolderSet<Item>>>> ALLOW_LOADING_PRIMARY = registerComponent(
			id("allow_loading_primary"), ConditionalEffect.codec(RegistryCodecs.homogeneousList(Registries.ITEM), LootContextParamSets.ENCHANTED_ENTITY).listOf()
	);

	public static final DataComponentType<List<ConditionalEffect<SeededContainerLoot>>> DEFAULT_PROJECTILE = registerComponent(
			id("default_projectile"), ConditionalEffect.codec(SeededContainerLoot.CODEC, LootContextParamSets.ENCHANTED_ENTITY).listOf()
	);

	public static final LootItemFunctionType<RandomFireworksLootFunction> RANDOM_FIREWORKS = registerLootFunction(
			id("random_fireworks"), RandomFireworksLootFunction.CODEC
	);

	public static final TagKey<EntityType<?>> WEIGHTLESS_PROJECTILES = TagKey.create(Registries.ENTITY_TYPE, id("weightless_projectiles"));

	public static void onFireworkShot(
			ServerLevel world, ItemStack weaponStack, FireworkRocketEntity fireworkRocketEntity, Consumer<Item> onBreak
	) {
		modifyFireworkDuration(world, weaponStack, fireworkRocketEntity);
	}

	public static void onProjectileFired(
			ServerLevel world, ItemStack weaponStack, Projectile projectileEntity, Consumer<Item> onBreak
	) {
		LivingEntity livingEntity2 = projectileEntity.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null;
		EnchantedItemInUse enchantmentEffectContext = new EnchantedItemInUse(weaponStack, null, livingEntity2, onBreak);

		EnchantmentHelper.runIterationOnItem(weaponStack, (entry, level) -> {
            Enchantment.applyEffects(
					entry.value().getEffects(PROJECTILE_FIRED),
					Enchantment.entityContext(world, level, projectileEntity, projectileEntity.position()),
					effect -> effect.apply(world, level, enchantmentEffectContext, projectileEntity, projectileEntity.position())
			);
		});
	}

	private static float modifyValue(
			DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> enchantment, ServerLevel world, ItemStack stack, Entity user, float baseValue
	) {
        var enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		if (enchantments.isEmpty()) return baseValue;
		var workingDuration = new MutableFloat(baseValue);

		for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
			entry.getKey().value().modifyEntityFilteredValue(enchantment, world, entry.getIntValue(), stack, user, workingDuration);
		}

		return workingDuration.floatValue();
	}

	private static void modifyFireworkDuration(
			ServerLevel world, ItemStack weaponStack, FireworkRocketEntity fireworkRocketEntity
	) {
		var access = (FireworkRocketEntityAccessor) fireworkRocketEntity;
		access.setLifetime((int) modifyValue(ROCKET_DURATION, world, weaponStack, fireworkRocketEntity, access.getLifetime()));
	}

	public static float modifyVelocity(ServerLevel world, ItemStack stack, Projectile projectileEntity, float baseVelocity) {
		return modifyValue(PROJECTILE_VELOCITY, world, stack, projectileEntity, baseVelocity);
	}

	public static Predicate<ItemStack> getAllowedPrimaryProjectiles(ServerLevel world, ItemStack stack, Entity user, Predicate<ItemStack> original) {
		var predicates = new ArrayList<com.google.common.base.Predicate<Holder<Item>>>();

		EnchantmentHelper.runIterationOnItem(stack, (entry, level) -> {
			Enchantment.applyEffects(
					entry.value().getEffects(ALLOW_LOADING_PRIMARY),
					Enchantment.entityContext(world, level, user, user.position()),
					effect -> predicates.add(effect::contains)
			);
		});

		if (predicates.isEmpty()) return original;

		var predicate = Predicates.or(predicates);
		return original.or(stack2 -> predicate.test(stack2.getItemHolder()));
	}

	@SuppressWarnings("DataFlowIssue")
    public static @Nullable ItemStack getDefaultProjectile(ServerLevel world, ItemStack stack, Entity user) {
		AtomicReference<@Nullable ItemStack> projectile = new AtomicReference<>();

		EnchantmentHelper.runIterationOnItem(stack, (entry, level) -> {
			if (projectile.get() != null) return;
			Enchantment.applyEffects(
					entry.value().getEffects(DEFAULT_PROJECTILE),
					Enchantment.entityContext(world, level, user, user.position()),
					effect -> projectile.set(getLoot(effect.lootTable(), effect.seed(), world, user, stack))
			);
		});

		return projectile.get();
	}

	private static ItemStack getLoot(ResourceKey<LootTable> lootTableKey, long seed, ServerLevel serverWorld, Entity entity, ItemStack tool) {
		var lootTable = serverWorld.getServer().reloadableRegistries().getLootTable(lootTableKey);
		var parameters = new LootParams.Builder(serverWorld)
				.withParameter(LootContextParams.THIS_ENTITY, entity)
				.withParameter(LootContextParams.ORIGIN, entity.position())
				.withParameter(LootContextParams.TOOL, tool)
				.create(LootContextParamSets.FISHING);
		var loot = lootTable.getRandomItems(parameters, seed);
		return switch (loot.size()) {
			case 0 -> ItemStack.EMPTY;
			case 1 -> loot.getFirst();
			default -> {
				LOGGER.error("Expected one loot item from loot table {}", lootTableKey.identifier());
				yield loot.getFirst();
			}
		};
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		registerEffect(id("owner_mount"), OwnerMountEnchantmentEffect.CODEC);
		registerEffect(id("modify_item"), ModifyItemEnchantmentEffect.CODEC);

		registerSubPredicate(id("rotation"), RotationPredicate.CODEC);
		registerSubPredicate(id("firework_rocket"), FireworkRocketPredicate.CODEC);
		registerSubPredicate(id("all_of"), AllOfSubPredicate.CODEC);
		registerSubPredicate(id("owner"), OwnerPredicate.CODEC);

		LootTableEvents.MODIFY.register((registryKey, builder, source, wrapperLookup) -> {
            if (!source.isBuiltin()) return;
            if (registryKey == BuiltInLootTables.END_CITY_TREASURE)
				builder.pool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(END_CITY_INJECT))
						.build());
			if (registryKey == BuiltInLootTables.TRIAL_CHAMBERS_REWARD_RARE || registryKey == BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE)
				builder.modifyPools(poolBuilder ->
					poolBuilder.add(
							NestedLootTable.lootTableReference(TRIAL_CHAMBER_INJECT)
									.setWeight(2)
					)
				);
        });
	}
}
