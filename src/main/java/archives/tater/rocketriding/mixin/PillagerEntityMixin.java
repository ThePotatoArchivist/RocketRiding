package archives.tater.rocketriding.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PillagerEntity.class)
public abstract class PillagerEntityMixin extends IllagerEntity {
    protected PillagerEntityMixin(EntityType<? extends IllagerEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "addBonusForWave",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PillagerEntity;equipStack(Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER)
    )
    private void setGuranteedDrop(ServerWorld world, int wave, boolean unused, CallbackInfo ci, @Local ItemStack stack) {
        if (stack.getEnchantments().getEnchantments().stream().anyMatch(entry -> entry.isIn(EnchantmentTags.TREASURE)))
            setEquipmentDropChance(EquipmentSlot.MAINHAND, 1f);
    }
}
