package ttv.migami.migamigos.common.container;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ttv.migami.migamigos.entity.Companion;
import ttv.migami.migamigos.init.ModContainers;

public class CompanionContainer extends AbstractContainerMenu {
    private final SimpleContainer companionInventory;
    private final Companion companion;

    public CompanionContainer(int windowId, Inventory playerInventory, Companion companion) {
        super(ModContainers.COMPANION_CONTAINER.get(), windowId);
        this.companion = companion;
        this.companionInventory = companion.getInventory();

        if (this.companionInventory != null) {
            // Armor Slots
            EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (int i = 0; i < armorSlots.length; i++) {
                EquipmentSlot slotType = armorSlots[i];
                this.addSlot(new ArmorSlot(this.companionInventory, 10 + i, 8, 18 + i * 18, companion, slotType));
            }

            // Inventory Slots
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 5; x++) {
                    int slotId = x + y * 5; // The 5 is the number of columns
                    this.addSlot(new Slot(this.companionInventory, slotId, 80 + x * 18, 18 + y * 18));
                }
            }
        }

        // Player Inventory & Hotbar
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 94 + y * 18));
            }
        }
        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 152));
        }
    }

    public Companion getCompanion() {
        return this.companion;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        Item item = stack.getItem();

        if (index < this.companionInventory.getContainerSize()) {
            if (item instanceof ArmorItem armorItem) {
                if (!this.moveItemStackTo(stack, this.companionInventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, this.companionInventory.getContainerSize(), this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            if (item instanceof ArmorItem armorItem) {
                int slotIndex = 0;

                if (armorItem.getType().equals(ArmorItem.Type.HELMET)) {
                    slotIndex = 0;
                }
                if (armorItem.getType().equals(ArmorItem.Type.CHESTPLATE)) {
                    slotIndex = 1;
                }
                if (armorItem.getType().equals(ArmorItem.Type.LEGGINGS)) {
                    slotIndex = 2;
                }
                if (armorItem.getType().equals(ArmorItem.Type.BOOTS)) {
                    slotIndex = 3;
                }

                if (!this.moveItemStackTo(stack, slotIndex, slotIndex + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, 0, this.companionInventory.getContainerSize(), false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        // Handle slot clearing and changes
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.companion.isAlive() && player.distanceTo(this.companion) < 8.0D;
    }

    private static class ArmorSlot extends Slot {
        private final Companion companion;
        private final EquipmentSlot equipmentSlot;

        public ArmorSlot(Container inventory, int index, int x, int y, Companion companion, EquipmentSlot equipmentSlot) {
            super(inventory, index, x, y);
            this.companion = companion;
            this.equipmentSlot = equipmentSlot;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            ItemStack stack = this.getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem armorItem) {
                this.companion.setItemSlot(this.equipmentSlot, stack);
            } else {
                this.companion.setItemSlot(this.equipmentSlot, ItemStack.EMPTY);
            }
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == this.equipmentSlot;
        }
    }
}