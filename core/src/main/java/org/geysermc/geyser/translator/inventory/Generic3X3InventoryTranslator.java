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

import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType;
import org.cloudburstmc.protocol.bedrock.packet.ContainerOpenPacket;
import org.geysermc.geyser.inventory.BedrockContainerSlot;
import org.geysermc.geyser.inventory.Generic3X3Container;
import org.geysermc.geyser.inventory.updater.ContainerInventoryUpdater;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;

/**
 * Droppers and dispensers
 */
public class Generic3X3InventoryTranslator extends AbstractBlockInventoryTranslator<Generic3X3Container> {
    public Generic3X3InventoryTranslator() {
        super(9, Blocks.DISPENSER, org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType.DISPENSER, ContainerInventoryUpdater.INSTANCE,
                Blocks.DROPPER);
    }

    @Override
    public Generic3X3Container createInventory(GeyserSession session, String name, int windowId, ContainerType containerType) {
        return new Generic3X3Container(session, name, windowId, this.size, containerType);
    }

    @Override
    public void openInventory(GeyserSession session, Generic3X3Container container) {
        ContainerOpenPacket containerOpenPacket = new ContainerOpenPacket();
        containerOpenPacket.setId((byte) container.getBedrockId());
        // Required for opening the real block - otherwise, if the container type is incorrect, it refuses to open
        containerOpenPacket.setType(container.isDropper() ? org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType.DROPPER : org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType.DISPENSER);
        containerOpenPacket.setBlockPosition(container.getHolderPosition());
        containerOpenPacket.setUniqueEntityId(container.getHolderId());
        session.sendUpstreamPacket(containerOpenPacket);
    }

    @Override
    public BedrockContainerSlot javaSlotToBedrockContainer(int javaSlot, Generic3X3Container container) {
        if (javaSlot < this.size) {
            return new BedrockContainerSlot(ContainerSlotType.LEVEL_ENTITY, javaSlot);
        }
        return super.javaSlotToBedrockContainer(javaSlot, container);
    }

    @Override
    public org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType closeContainerType(Generic3X3Container container) {
        return container.isDropper() ? org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType.DROPPER :
            org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType.DISPENSER;
    }
}
