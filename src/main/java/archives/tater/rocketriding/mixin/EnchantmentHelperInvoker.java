package archives.tater.rocketriding.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EnchantmentHelper.class)
public interface EnchantmentHelperInvoker {
    @Invoker
    static void invokeForEachEnchantment(ItemStack stack, EnchantmentHelper.Consumer consumer) {
        throw new AssertionError();
    }
}
