package dev.mim1q.derelict.vendor.gimm1q.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

import static dev.mim1q.derelict.vendor.gimm1q.Gimm1qCommands.applyDumpCommand;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;


public class Gimm1qClientCommands {
    public static void init() {
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, event -> {
            event.getDispatcher().register(literal("derelict_gimm1q:dump_value_calculators_client")
                .requires(source -> source.hasPermission(2))
                .then(argument("count", IntegerArgumentType.integer())
                    .executes(context -> {
                        int count = IntegerArgumentType.getInteger(context, "count");
                        var holder = Minecraft.getInstance().player;
                        var world = Minecraft.getInstance().level;
                        if (holder == null || world == null) {
                            return 1;
                        }
                        var target = world.getNearestEntity(
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
                            it -> context.getSource().sendSuccess(() -> it, false),
                            it -> context.getSource().sendFailure(it),
                            "value_calculators_dump_client"
                        );

                        return 0;
                    })
                )
            );
        });
    }
}
