package archives.tater.rocketriding.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pillager.class)
public abstract class PillagerEntityMixin extends AbstractIllager {
    protected PillagerEntityMixin(EntityType<? extends AbstractIllager> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(
            method = "applyRaidBuffs",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/illager/Pillager;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER)
    )
    private void setGuranteedDrop(ServerLevel world, int wave, boolean unused, CallbackInfo ci, @Local ItemStack stack) {
        if (stack.getEnchantments().keySet().stream().anyMatch(entry -> entry.is(EnchantmentTags.TREASURE)))
            setDropChance(EquipmentSlot.MAINHAND, 1f);
    }
}
