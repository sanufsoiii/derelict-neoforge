package dev.mim1q.derelict.vendor.gimm1q;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mim1q.derelict.vendor.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.derelict.vendor.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.derelict.vendor.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;


public class Gimm1qCommands {
    public static void init() {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event ->
            event.getDispatcher().register(literal("derelict_gimm1q:calculate")
                .requires(source -> source.hasPermission(2))
                .then(argument("type", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        builder.suggest("variable");
                        builder.suggest("expression");
                        return builder.buildFuture();
                    })
                    .then(argument("id", ResourceLocationArgument.id())
                        .suggests((context, builder) -> {
                            ValueCalculatorResourceReloader.getAllIds().forEach(it ->
                                builder.suggest(it.toString())
                            );
                            return builder.buildFuture();
                        })
                        .then(argument("name", StringArgumentType.string())
                            .suggests((context, builder) -> {
                                var id = ResourceLocationArgument.getId(context, "id");
                                var type = StringArgumentType.getString(context, "type");
                                ValueCalculatorResourceReloader.getExpressionOrVariableNames(
                                    id,
                                    type.equals("variable")
                                ).ifPresent(set -> set.forEach(builder::suggest));
                                return builder.buildFuture();
                            })
                            .then(argument("holder", EntityArgument.entity())
                                .then(argument("target", EntityArgument.entity())
                                    .executes(context -> {
                                        var id = ResourceLocationArgument.getId(context, "id");
                                        var name = StringArgumentType.getString(context, "name");
                                        var holder = EntityArgument.getEntity(context, "holder");
                                        var target = EntityArgument.getEntity(context, "target");
                                        var type = StringArgumentType.getString(context, "type");

                                        if (!(holder instanceof LivingEntity) || !(target instanceof LivingEntity)) {
                                            context.getSource().sendFailure(Component.literal("Entities must be living entities"));
                                            return 1;
                                        }

                                        var value = ValueCalculatorResourceReloader.INSTANCE.calculateExpressionOrVariable(
                                            id, name, ValueCalculatorContext.create()
                                                .with(ValueCalculatorParameter.TARGET, (LivingEntity) target)
                                                .with(ValueCalculatorParameter.HOLDER, (LivingEntity) holder),
                                            type.equals("variable")
                                        );

                                        if (value.isPresent()) {
                                            context.getSource().sendSuccess(
                                                () -> Component.literal("Value calculated for " + id + "." + name + ": " + value.get()),
                                                true
                                            );
                                        } else {
                                            context.getSource().sendFailure(
                                                Component.literal("Value calculator not found: " + id + "." + name)
                                            );
                                            return 1;
                                        }
                                        return 0;
                                    })
                                )
                            )
                        )
                    )
                )
            )
        );


        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event ->
            event.getDispatcher().register(literal("derelict_gimm1q:dump_value_calculators")
                .requires(source -> source.hasPermission(2))
                .then(argument("holder", EntityArgument.entity())
                    .then(argument("target", EntityArgument.entity())
                        .then(argument("count", IntegerArgumentType.integer(1))
                            .executes(context -> {
                                var holder = EntityArgument.getEntity(context, "holder");
                                var target = EntityArgument.getEntity(context, "target");
                                var count = IntegerArgumentType.getInteger(context, "count");

                                return applyDumpCommand(
                                    holder,
                                    target,
                                    count,
                                    it -> context.getSource().sendSuccess(() -> it, true),
                                    it -> context.getSource().sendFailure(it),
                                    "value_calculators_dump"
                                );
                            })
                        )
                    )
                )
            )
        );
    }

    public static int applyDumpCommand(
        Entity holder,
        Entity target,
        int count,
        Consumer<Component> feedbackSender,
        Consumer<Component> errorSender,
        String filename
    ) {

        var ids = ValueCalculatorResourceReloader.getAllIds();
        var builder = new StringBuilder();

        for (var id : ids) {
            try {
                builder.append("## `").append(id).append("`\n");

                var variables = ValueCalculatorResourceReloader.getExpressionOrVariableNames(id, true).orElseThrow();
                builder.append("### Variables:\n");
                builder.append("| name | average time (ms) | value |").append("\n");
                builder.append("|-|-|-|").append("\n");
                for (var variable : variables) {
                    addResultsToBuilder(builder, id, count, (LivingEntity) target, (LivingEntity) holder, variable, true);
                }
                builder.append("\n");

                builder.append("### Expressions:\n");
                builder.append("| name | average time (ms) | value |").append("\n");
                builder.append("|-|-|-|").append("\n");
                var expressions = ValueCalculatorResourceReloader.getExpressionOrVariableNames(id, false).orElseThrow();
                for (var expression : expressions) {
                    addResultsToBuilder(builder, id, count, (LivingEntity) target, (LivingEntity) holder, expression, false);
                }
                builder.append("\n---\n");
            } catch (Exception e) {
                Gimm1q.LOGGER.error("Failed to dump value calculator: {}", id, e);
            }
        }

        var relativePath = "logs/gimm1q/" + filename + ".md";

        feedbackSender.accept(Component.literal("Result dumped to file " + relativePath));

        try {
            var path = FMLPaths.GAMEDIR.get();
            var file = path.resolve(relativePath);

            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            Files.writeString(file, builder.toString());

        } catch (Exception e) {
            errorSender.accept(Component.literal("Failed to save file: " + e.getMessage()));
            return 1;
        }

        return 0;
    }

    private static void addResultsToBuilder(StringBuilder builder, ResourceLocation id, int count, LivingEntity target, LivingEntity holder, String name, boolean variable) {
        Double firstResult = null;
        float totalTime = 0f;

        for (int i = 0; i < count; i++) {
            var startTime = System.nanoTime();
            var value = ValueCalculatorResourceReloader.INSTANCE.calculateExpressionOrVariable(
                id, name, ValueCalculatorContext.create()
                    .with(ValueCalculatorParameter.TARGET, target)
                    .with(ValueCalculatorParameter.HOLDER, holder),
                variable
            );
            var time = (System.nanoTime() - startTime) / 1_000_000f;
            if (value.isPresent() && firstResult == null) {
                firstResult = value.get();
            }
            totalTime += time;
        }

        var averageTime = totalTime / count;
        builder
            .append("|").append(name)
            .append("|").append(String.format("%.8f", averageTime))
            .append("|").append(firstResult == null ? "-" : firstResult);
        builder.append("|\n");
    }
}
