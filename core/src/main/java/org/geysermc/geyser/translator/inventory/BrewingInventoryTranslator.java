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

package org.geysermc.geyser.translator.inventory;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType;
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.ItemStackRequestSlotData;
import org.cloudburstmc.protocol.bedrock.packet.ContainerSetDataPacket;
import org.geysermc.geyser.inventory.BedrockContainerSlot;
import org.geysermc.geyser.inventory.Container;
import org.geysermc.geyser.inventory.updater.ContainerInventoryUpdater;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.session.GeyserSession;

public class BrewingInventoryTranslator extends AbstractBlockInventoryTranslator<Container> {
    public BrewingInventoryTranslator() {
        super(5, Blocks.BREWING_STAND.defaultBlockState()
                .withValue(Properties.HAS_BOTTLE_0, false)
                .withValue(Properties.HAS_BOTTLE_1, false)
                .withValue(Properties.HAS_BOTTLE_2, false), ContainerType.BREWING_STAND, ContainerInventoryUpdater.INSTANCE);
    }

    @Override
    public void openInventory(GeyserSession session, Container container) {
        super.openInventory(session, container);
        ContainerSetDataPacket dataPacket = new ContainerSetDataPacket();
        dataPacket.setWindowId((byte) container.getBedrockId());
        dataPacket.setProperty(ContainerSetDataPacket.BREWING_STAND_FUEL_TOTAL);
        dataPacket.setValue(20);
        session.sendUpstreamPacket(dataPacket);
    }

    @Override
    public void updateProperty(GeyserSession session, Container container, int key, int value) {
        ContainerSetDataPacket dataPacket = new ContainerSetDataPacket();
        dataPacket.setWindowId((byte) container.getBedrockId());
        switch (key) {
            case 0:
                dataPacket.setProperty(ContainerSetDataPacket.BREWING_STAND_BREW_TIME);
                break;
            case 1:
                dataPacket.setProperty(ContainerSetDataPacket.BREWING_STAND_FUEL_AMOUNT);
                break;
            default:
                return;
        }
        dataPacket.setValue(value);
        session.sendUpstreamPacket(dataPacket);
    }

    @Override
    public int bedrockSlotToJava(ItemStackRequestSlotData slotInfoData) {
        if (slotInfoData.getContainerName().getContainer() == ContainerSlotType.BREWING_INPUT) {
            // Ingredient
            return 3;
        }
        if (slotInfoData.getContainerName().getContainer() == ContainerSlotType.BREWING_RESULT) {
            // Potions
            return slotInfoData.getSlot() - 1;
        }
        return super.bedrockSlotToJava(slotInfoData);
    }

    @Override
    public int javaSlotToBedrock(int slot) {
        return switch (slot) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 0;
            case 4 -> 4;
            default -> super.javaSlotToBedrock(slot);
        };
    }

    @Override
    public BedrockContainerSlot javaSlotToBedrockContainer(int slot, Container container) {
        return switch (slot) {
            case 0, 1, 2 -> new BedrockContainerSlot(ContainerSlotType.BREWING_RESULT, javaSlotToBedrock(slot));
            case 3 -> new BedrockContainerSlot(ContainerSlotType.BREWING_INPUT, 0);
            case 4 -> new BedrockContainerSlot(ContainerSlotType.BREWING_FUEL, 4);
            default -> super.javaSlotToBedrockContainer(slot, container);
        };
    }

    @Override
    public @Nullable ContainerType closeContainerType(Container container) {
        return ContainerType.BREWING_STAND;
    }
}
