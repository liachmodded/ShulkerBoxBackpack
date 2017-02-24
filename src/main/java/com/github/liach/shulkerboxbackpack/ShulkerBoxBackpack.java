/*
 * The MIT License (MIT)
 *
 * Copyright (c) liachmodded <https://github.com/liachmodded>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.liach.shulkerboxbackpack;

import static org.spongepowered.api.item.ItemTypes.BLACK_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.BLUE_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.BROWN_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.CYAN_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.GRAY_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.GREEN_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.LIGHT_BLUE_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.LIME_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.MAGENTA_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.ORANGE_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.PINK_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.PURPLE_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.RED_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.SILVER_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.WHITE_SHULKER_BOX;
import static org.spongepowered.api.item.ItemTypes.YELLOW_SHULKER_BOX;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableInventoryItemData;
import org.spongepowered.api.data.manipulator.mutable.item.InventoryItemData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;

import java.util.Set;

/**
 * Main class for Shulker Box Backpack plugin.
 */
@Plugin(
    id = "shulkerboxbackpack",
    name = "Shulker Box Backpack",
    description = "Allow users to use shulker boxes as backpacks",
    url = "https://github.com/liachmodded/ShulkerBoxBackpack",
    authors = {
        "liach"
    }
)
public final class ShulkerBoxBackpack {

    private static ShulkerBoxBackpack instance;

    @Inject
    @SuppressWarnings("unused")
    private Logger logger;

    private final Set<ItemType> boxes =
        Sets.newHashSet(BLACK_SHULKER_BOX, BLUE_SHULKER_BOX,
            BROWN_SHULKER_BOX, BROWN_SHULKER_BOX,
            CYAN_SHULKER_BOX, GRAY_SHULKER_BOX, GREEN_SHULKER_BOX,
            LIGHT_BLUE_SHULKER_BOX, LIME_SHULKER_BOX,
            MAGENTA_SHULKER_BOX,
            ORANGE_SHULKER_BOX, PINK_SHULKER_BOX, PURPLE_SHULKER_BOX,
            RED_SHULKER_BOX, SILVER_SHULKER_BOX, RED_SHULKER_BOX,
            WHITE_SHULKER_BOX,
            YELLOW_SHULKER_BOX);

    /**
     * Constructor.
     */
    @Inject
    private ShulkerBoxBackpack() {
        instance = this;
    }

    /**
     * Provides access to the instance of the plugin.
     *
     * @return The plugin's instance
     */
    public static ShulkerBoxBackpack getInstance() {
        return instance;
    }

    /**
     * Event for player clicking on the shulker box.
     *
     * @param event The interaction event
     * @param humanoid The player, or a human
     * @param handInUse The active hand
     * @param view The item stack snapshot on the hand
     */
    @Listener
    public void onPlaceShulkerBox(InteractItemEvent.Secondary event, @First
        Humanoid humanoid, @First HandType handInUse, @Getter("getItemStack") ItemStackSnapshot
        view) {
        if (!this.boxes.contains(view.getType())) {
            return;
        }
        if (humanoid.get(Keys.IS_SNEAKING).orElse(true)) {
            event.setCancelled(true);
        }
        if (humanoid instanceof Player) {
            Player player = (Player) humanoid;
            InventoryItemData data = view.get(ImmutableInventoryItemData.class)
                .orElseThrow(() -> new RuntimeException("Unexpected that a shulker box has no inventory item")).asMutable();

            // Create inventory to view
            Inventory inv = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .forCarrier(data)
                .build(getInstance());
            player.openInventory(inv, Cause.source(getInstance()).build());

            // After modification
            ItemStack stack = view.createStack();
            DataTransactionResult result = stack.offer(data);
            if (!result.isSuccessful()) {
                this.logger.error("Cannot set the updated inventory content");
            }
            player.setItemInHand(handInUse, stack);
        }
    }

}
