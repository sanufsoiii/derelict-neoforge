package dev.mim1q.derelict.vendor.gimm1q.registry;

import dev.mim1q.derelict.vendor.gimm1q.Gimm1q;
import dev.mim1q.derelict.vendor.gimm1q.valuecalculators.variables.VariableSource;
import dev.mim1q.derelict.vendor.gimm1q.valuecalculators.variables.VariableSourceType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class Gimm1qRegistries {
    public static final ResourceKey<Registry<VariableSourceType<? extends VariableSource>>> VARIABLE_SOURCE_TYPE_KEY =
        ResourceKey.createRegistryKey(Gimm1q.registeredId("value_calculator_variable_type"));

    /** Added to the registry list in {@code NewRegistryEvent} (see {@link Gimm1q#init}). */
    public static final Registry<VariableSourceType<? extends VariableSource>> VARIABLE_SOURCE_TYPE =
        new RegistryBuilder<>(VARIABLE_SOURCE_TYPE_KEY).create();
}
