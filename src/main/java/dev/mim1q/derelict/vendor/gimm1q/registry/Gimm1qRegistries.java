package dev.mim1q.derelict.vendor.gimm1q.registry;

import dev.mim1q.derelict.vendor.gimm1q.Gimm1q;
import dev.mim1q.derelict.vendor.gimm1q.valuecalculators.variables.VariableSource;
import dev.mim1q.derelict.vendor.gimm1q.valuecalculators.variables.VariableSourceType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class Gimm1qRegistries {
    public static final ResourceKey<Registry<VariableSourceType<? extends VariableSource>>> VARIABLE_SOURCE_TYPE_KEY =
        ResourceKey.createRegistryKey(Gimm1q.id("value_calculator_variable_type"));

    public static final Registry<VariableSourceType<? extends VariableSource>> VARIABLE_SOURCE_TYPE =
        FabricRegistryBuilder
            .createSimple(VARIABLE_SOURCE_TYPE_KEY)
            .buildAndRegister();
}
