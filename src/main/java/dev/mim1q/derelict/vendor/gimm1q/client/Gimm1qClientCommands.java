package dev.mim1q.derelict.vendor.gimm1q.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import static dev.mim1q.derelict.vendor.gimm1q.Gimm1qCommands.applyDumpCommand;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public class Gimm1qClientCommands {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("gimm1q:dump_value_calculators_client")
                .requires(source -> source.hasPermission(2))
                .then(argument("count", IntegerArgumentType.integer())
                    .executes(context -> {
                        int count = IntegerArgumentType.getInteger(context, "count");
                        var holder = context.getSource().getPlayer();
                        var target = context.getSource().getWorld().getNearestEntity(
                            LivingEntity.class,
                            TargetingConditions.DEFAULT,
                            holder,
                            holder.getX(),
                            holder.getY(),
                            holder.getZ(),
                            holder.getBoundingBox().inflate(1024.0)
                        );

                        if (target == null) {
                            return 1;
                        }

                        applyDumpCommand(
                            holder,
                            target,
                            count,
                            it -> context.getSource().sendFeedback(it),
                            it -> context.getSource().sendError(it),
                            "value_calculators_dump_client"
                        );

                        return 0;
                    })
                )
            );
        });
    }
}
