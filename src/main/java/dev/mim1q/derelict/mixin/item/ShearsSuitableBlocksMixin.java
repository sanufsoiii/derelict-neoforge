package dev.mim1q.derelict.mixin.item;

import dev.mim1q.derelict.tag.ModBlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.component.Tool;

@Mixin(ShearsItem.class)
public class ShearsSuitableBlocksMixin {
    @Inject(
        method = "createToolProperties",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void derelict$isSuitableFor(CallbackInfoReturnable<Tool> cir) {
        var result = cir.getReturnValue();
        var newList = new ArrayList<>(result.rules());
        newList.add(Tool.Rule.minesAndDrops(ModBlockTags.INSTANCE.getCOBWEBS(), 15));
        cir.setReturnValue(new Tool(newList, result.defaultMiningSpeed(), result.damagePerBlock()));
    }
}
