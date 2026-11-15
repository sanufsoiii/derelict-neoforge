package dev.mim1q.derelict.vendor.gimm1q.client.tooltip;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Registry for resolving custom item tooltips.
 * These allow you to resolve tooltips only when they start being displayed instead of every frame.
 * Useful in case the tooltip data requires resource-heavy calculations.
 */
public interface TooltipResolverRegistry {
    /**
     * Register the Tooltip Resolver for given items
     *
     * @param resolver the tooltip resolver
     * @param items    the items that will use the resolver for their tooltip
     */
    void register(TooltipResolver resolver, Item... items);

    /**
     * Retrieve the resolver used for a given item
     *
     * @param item an item that may or may not have a registered Tooltip Resolver
     * @return the Tooltip Resolver assigned to the Item, or null if there isn't one
     */
    @Nullable
    TooltipResolver getResolver(Item item);

    /**
     * @return the singleton instance of this registry
     */
    static TooltipResolverRegistry getInstance() {
        return TooltipResolverRegistryImpl.INSTANCE;
    }

    @FunctionalInterface
    interface TooltipResolver {
        /**
         * @param context the context containing some data that might be useful when resolving the tooltip
         * @param helper  an interface for performing actions on the tooltip
         * @see TooltipResolverContext
         * @see TooltipHelper
         */
        void resolve(TooltipResolverContext context, TooltipHelper helper);
    }

    /**
     * A set of useful actions that can be performed on an item tooltip
     */
    @ApiStatus.NonExtendable
    interface TooltipHelper {
        /**
         * Add a line (or multiple lines if it exceeds the max defined width) to the tooltip
         *
         * @param text  the text to add
         * @param extra if the line should only be shown when [Left Alt] is pressed
         * @return this
         */
        TooltipHelper addLine(Component text, boolean extra);

//        /**
//         * Lets you hide default sections of the tooltip
//         *
//         * @param sections the sections to hide
//         * @return this
//         */
//        TooltipHelper hideSections(ItemStack.TooltipSection... sections);

        /**
         * The default style to apply to text if it doesn't have a custom one
         *
         * @param style the default style
         * @return this
         */
        TooltipHelper defaultStyle(Style style);

        /**
         * Sets the max width of the tooltip
         *
         * @param width the max width of a tooltip line, in characters
         * @return this
         */
        TooltipHelper maxLineWidth(int width);

        /**
         * Add a line to the tooltip
         *
         * @param text the text to add
         * @return this
         * @see TooltipHelper#addLine(Component, boolean)
         */
        default TooltipHelper addLine(Component text) {
            return addLine(text, false);
        }

        /**
         * Add a line to the tooltip that will only be shown when [Left Alt] is pressed
         *
         * @param text the text to add
         * @return this
         * @see TooltipHelper#addLine(Component, boolean)
         */
        default TooltipHelper addExtraLine(Component text) {
            return addLine(text, true);
        }

        /**
         * Splits a line of text into multiple lines if it exceeds the max defined width
         *
         * @param text       the text to split
         * @param characters the max number of characters per line
         * @return the list of lines that were created by splitting the original text
         */
        static List<Component> splitTextIfExceeds(Component text, int characters) {
            if (text.getString().length() <= characters) {
                return List.of(text);
            }

            var result = new ArrayList<Component>();
            var style = text.getStyle();
            var content = text.getString();

            while (content.length() > characters) {
                var lastSpace = 0;
                for (var i = 0; i < characters; ++i) {
                    if (content.charAt(i) == ' ') {
                        lastSpace = i;
                    }
                }

                result.add(Component.literal(content.substring(0, lastSpace)).setStyle(style));
                content = content.substring(lastSpace + 1);
            }
            result.add(Component.literal(content).setStyle(style));
            return result;
        }
    }

    record TooltipResolverContext(
        ItemStack item,
        Player player,
        Item.TooltipContext tooltipContext
    ) {
    }
}
