package archives.tater.rocketriding;

import archives.tater.rocketriding.mixin.EnchantmentHelperInvoker;
import archives.tater.rocketriding.mixin.EnchantmentInvoker;
import archives.tater.rocketriding.mixin.FireworkRocketEntityAccessor;
import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerLootComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class RocketRiding implements ModInitializer {
	public static final String MOD_ID = "rocketriding";

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final RegistryKey<LootTable> END_CITY_INJECT = RegistryKey.of(RegistryKeys.LOOT_TABLE, id("inject/end_city_treasure"));
	public static final RegistryKey<LootTable> TRIAL_CHAMBER_INJECT = RegistryKey.of(RegistryKeys.LOOT_TABLE, id("inject/trial_chambers"));

	private static <T> ComponentType<T> registerComponent(Identifier id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
		return Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, id, (builderOperator.apply(ComponentType.builder())).build());
	}

	private static <T> ComponentType<T> registerComponent(Identifier id, Codec<T> codec) {
		return registerComponent(id, builder -> builder.codec(codec));
	}

	private static <T extends EntitySubPredicate> MapCodec<T> registerSubPredicate(Identifier id, MapCodec<T> codec) {
		return Registry.register(Registries.ENTITY_SUB_PREDICATE_TYPE, id, codec);
	}

	private static <T extends LootFunction> LootFunctionType<T> registerLootFunction(Identifier id, MapCodec<T> codec) {
		return Registry.register(Registries.LOOT_FUNCTION_TYPE, id, new LootFunctionType<>(codec));
	}

	// Currently only implemented on RangedWeaponItem
	public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> PROJECTILE_VELOCITY = registerComponent(
			id("projectile_velocity"), EnchantmentEffectEntry.createCodec(EnchantmentValueEffect.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()
	);

	public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> ROCKET_DURATION = registerComponent(
			id("firework_rocket_duration"), EnchantmentEffectEntry.createCodec(EnchantmentValueEffect.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()
	);

	public static final ComponentType<List<EnchantmentEffectEntry<EnchantmentEntityEffect>>> PROJECTILE_FIRED = registerComponent(
			id("projectile_fired"), EnchantmentEffectEntry.createCodec(EnchantmentEntityEffect.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()
	);

	public static final ComponentType<List<EnchantmentEffectEntry<RegistryEntryList<Item>>>> ALLOW_LOADING_PRIMARY = registerComponent(
			id("allow_loading_primary"), EnchantmentEffectEntry.createCodec(RegistryCodecs.entryList(RegistryKeys.ITEM), LootContextTypes.ENCHANTED_ENTITY).listOf()
	);

	public static final ComponentType<List<EnchantmentEffectEntry<ContainerLootComponent>>> DEFAULT_PROJECTILE = registerComponent(
			id("default_projectile"), EnchantmentEffectEntry.createCodec(ContainerLootComponent.CODEC, LootContextTypes.ENCHANTED_ENTITY).listOf()
	);

	public static final MapCodec<RotationPredicate> ROTATION_SUB_PREDICATE = registerSubPredicate(
			id("rotation"), RotationPredicate.CODEC
	);

	public static final LootFunctionType<RandomFireworksLootFunction> RANDOM_FIREWORKS = registerLootFunction(
			id("random_fireworks"), RandomFireworksLootFunction.CODEC
	);

	public static final TagKey<EntityType<?>> WEIGHTLESS_PROJECTILES = TagKey.of(RegistryKeys.ENTITY_TYPE, id("weightless_projectiles"));

	public static void onFireworkShot(
			ServerWorld world, ItemStack weaponStack, FireworkRocketEntity fireworkRocketEntity, java.util.function.Consumer<Item> onBreak
	) {
		modifyFireworkDuration(world, weaponStack, fireworkRocketEntity);
	}

	public static void onProjectileFired(
			ServerWorld world, ItemStack weaponStack, ProjectileEntity projectileEntity, java.util.function.Consumer<Item> onBreak
	) {
		LivingEntity livingEntity2 = projectileEntity.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null;
		EnchantmentEffectContext enchantmentEffectContext = new EnchantmentEffectContext(weaponStack, null, livingEntity2, onBreak);

		EnchantmentHelperInvoker.invokeForEachEnchantment(weaponStack, (entry, level) -> {
            EnchantmentInvoker.invokeApplyEffects(
					entry.value().getEffect(PROJECTILE_FIRED),
					EnchantmentInvoker.invokeCreateEnchantedEntityLootContext(world, level, projectileEntity, projectileEntity.getPos()),
					effect -> effect.apply(world, level, enchantmentEffectContext, projectileEntity, projectileEntity.getPos())
			);
		});
	}

	private static float modifyValue(
			ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> enchantment, ServerWorld world, ItemStack stack, Entity user, float baseValue
	) {
        var enchantments = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
		if (enchantments.isEmpty()) return baseValue;
		var workingDuration = new MutableFloat(baseValue);

		for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments.getEnchantmentEntries()) {
			((EnchantmentInvoker) (Object) entry.getKey().value()).invokeModifyValue(enchantment, world, entry.getIntValue(), stack, user, workingDuration);
		}

		return workingDuration.floatValue();
	}

	private static void modifyFireworkDuration(
			ServerWorld world, ItemStack weaponStack, FireworkRocketEntity fireworkRocketEntity
	) {
		var access = (FireworkRocketEntityAccessor) fireworkRocketEntity;
		access.setLifeTime((int) modifyValue(ROCKET_DURATION, world, weaponStack, fireworkRocketEntity, access.getLifeTime()));
	}

	public static float modifyVelocity(ServerWorld world, ItemStack stack, ProjectileEntity projectileEntity, float baseVelocity) {
		return modifyValue(PROJECTILE_VELOCITY, world, stack, projectileEntity, baseVelocity);
	}

	public static Predicate<ItemStack> getAllowedPrimaryProjectiles(ServerWorld world, ItemStack stack, Entity user, Predicate<ItemStack> original) {
		var predicates = new ArrayList<com.google.common.base.Predicate<RegistryEntry<Item>>>();

		EnchantmentHelperInvoker.invokeForEachEnchantment(stack, (entry, level) -> {
			EnchantmentInvoker.invokeApplyEffects(
					entry.value().getEffect(ALLOW_LOADING_PRIMARY),
					EnchantmentInvoker.invokeCreateEnchantedEntityLootContext(world, level, user, user.getPos()),
					effect -> predicates.add(effect::contains)
			);
		});

		if (predicates.isEmpty()) return original;

		var predicate = Predicates.or(predicates);
		return original.or(stack2 -> predicate.test(stack2.getRegistryEntry()));
	}

	@SuppressWarnings("DataFlowIssue")
    public static @Nullable ItemStack getDefaultProjectile(ServerWorld world, ItemStack stack, Entity user) {
		AtomicReference<@Nullable ItemStack> projectile = new AtomicReference<>();

		EnchantmentHelperInvoker.invokeForEachEnchantment(stack, (entry, level) -> {
			if (projectile.get() != null) return;
			EnchantmentInvoker.invokeApplyEffects(
					entry.value().getEffect(DEFAULT_PROJECTILE),
					EnchantmentInvoker.invokeCreateEnchantedEntityLootContext(world, level, user, user.getPos()),
					effect -> projectile.set(getLoot(effect.lootTable(), effect.seed(), world, user, stack))
			);
		});

		return projectile.get();
	}

	private static ItemStack getLoot(RegistryKey<LootTable> lootTableKey, long seed, ServerWorld serverWorld, Entity entity, ItemStack tool) {
		var lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(lootTableKey);
		var builder = new LootContextParameterSet.Builder(serverWorld)
				.add(LootContextParameters.THIS_ENTITY, entity)
				.add(LootContextParameters.ORIGIN, entity.getPos())
				.add(LootContextParameters.TOOL, tool);

		LootContextParameterSet lootContextParameterSet = builder.build(LootContextTypes.FISHING); // Closest?
		var loot = lootTable.generateLoot(lootContextParameterSet, seed);
		return switch (loot.size()) {
			case 0 -> ItemStack.EMPTY;
			case 1 -> loot.getFirst();
			default -> {
				LOGGER.error("Expected one loot item from loot table {}", lootTableKey.getValue());
				yield loot.getFirst();
			}
		};
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, id("owner_mount"), OwnerMountEnchantmentEntityEffect.CODEC);

		LootTableEvents.MODIFY.register((registryKey, builder, source, wrapperLookup) -> {
            if (!source.isBuiltin()) return;
            if (registryKey == LootTables.END_CITY_TREASURE_CHEST)
				builder.pool(LootPool.builder()
						.with(LootTableEntry.builder(END_CITY_INJECT))
						.build());
			if (registryKey == LootTables.TRIAL_CHAMBERS_REWARD_RARE_CHEST || registryKey == LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE_CHEST)
				builder.modifyPools(poolBuilder ->
					poolBuilder.with(
							LootTableEntry.builder(TRIAL_CHAMBER_INJECT)
									.weight(2)
					)
				);
        });
	}
}
