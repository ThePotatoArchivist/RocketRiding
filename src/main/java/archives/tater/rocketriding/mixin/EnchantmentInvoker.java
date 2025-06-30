package archives.tater.rocketriding.mixin;

import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Enchantment.class)
public interface EnchantmentInvoker {
    @Invoker
    void invokeModifyValue(ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> type, ServerWorld world, int level, ItemStack stack, MutableFloat value);
}
