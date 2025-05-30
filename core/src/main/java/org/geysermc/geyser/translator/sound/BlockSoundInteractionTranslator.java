/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
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
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.translator.sound;

import org.cloudburstmc.math.vector.Vector3f;
import org.geysermc.geyser.inventory.GeyserItemStack;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;

import java.util.Map;

/**
 * Sound interaction handler for when a block is right-clicked.
 */
public interface BlockSoundInteractionTranslator extends SoundInteractionTranslator<BlockState> {
    /**
     * Handles the block interaction when a player
     * right-clicks a block.
     *
     * @param session the session interacting with the block
     * @param position the position of the block
     * @param state the state of the block
     */
    static void handleBlockInteraction(GeyserSession session, Vector3f position, BlockState state) {
        // If we need to get the hand identifier, only get it once and save it to a variable
        String handIdentifier = null;

        for (Map.Entry<SoundTranslator, SoundInteractionTranslator<?>> interactionEntry : Registries.SOUND_TRANSLATORS.get().entrySet()) {
            if (!(interactionEntry.getValue() instanceof BlockSoundInteractionTranslator)) {
                continue;
            }
            if (interactionEntry.getKey().blocks().length != 0) {
                boolean contains = false;
                for (String blockIdentifier : interactionEntry.getKey().blocks()) {
                    if (state.toString().contains(blockIdentifier)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) continue;
            }
            GeyserItemStack itemInHand = session.getPlayerInventory().getItemInHand();
            if (interactionEntry.getKey().items().length != 0) {
                if (itemInHand.isEmpty()) {
                    continue;
                }
                if (handIdentifier == null) {
                    handIdentifier = itemInHand.asItem().javaIdentifier();
                }
                boolean contains = false;
                for (String itemIdentifier : interactionEntry.getKey().items()) {
                    if (handIdentifier.contains(itemIdentifier)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) continue;
            }
            if (session.isSneaking() && !interactionEntry.getKey().ignoreSneakingWhileHolding()) {
                if (!itemInHand.isEmpty()) {
                    continue;
                }
            }
            ((BlockSoundInteractionTranslator) interactionEntry.getValue()).translate(session, position, state);
        }
    }

    /**
     * Determines if the adventure gamemode would prevent this item from actually succeeding
     */
    static boolean canInteract(GeyserSession session, GeyserItemStack itemInHand, String blockIdentifier) {
        if (session.getGameMode() != GameMode.ADVENTURE) {
            // There are no restrictions on the item
            return true;
        }

        var canPlaceOn = itemInHand.getComponent(DataComponentTypes.CAN_PLACE_ON);
        if (canPlaceOn == null || canPlaceOn.getPredicates().isEmpty()) {
            // Component doesn't exist - no restrictions apply.
            return true;
        }

        for (var blockPredicate : canPlaceOn.getPredicates()) {
            // I don't want to deal with this right now. TODO
        }

        // The block in world is not present in the CanPlaceOn tag on the item
        return false;
    }
}
