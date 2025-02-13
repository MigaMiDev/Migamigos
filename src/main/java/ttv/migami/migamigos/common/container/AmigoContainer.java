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
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.init.ModContainers;

public class AmigoContainer extends AbstractContainerMenu {
    private final SimpleContainer amigoInventory;
    private final AmigoEntity amigoEntity;

    public AmigoContainer(int windowId, Inventory playerInventory, AmigoEntity amigoEntity) {
        super(ModContainers.AMIGO_CONTAINER.get(), windowId);
        this.amigoEntity = amigoEntity;
        this.amigoInventory = amigoEntity.getInventory();

        if (this.amigoInventory != null) {
            // Armor Slots
            EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (int i = 0; i < armorSlots.length; i++) {
                EquipmentSlot slotType = armorSlots[i];
                this.addSlot(new ArmorSlot(this.amigoInventory, 10 + i, 8, 18 + i * 18, amigoEntity, slotType));
            }

            // Inventory Slots
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 5; x++) {
                    int slotId = x + y * 5; // The 5 is the number of columns
                    this.addSlot(new Slot(this.amigoInventory, slotId, 80 + x * 18, 18 + y * 18));
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

    public AmigoEntity getAmigo() {
        return this.amigoEntity;
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

        if (index < this.amigoInventory.getContainerSize()) {
            if (item instanceof ArmorItem armorItem) {
                if (!this.moveItemStackTo(stack, this.amigoInventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, this.amigoInventory.getContainerSize(), this.slots.size(), false)) {
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
                if (!this.moveItemStackTo(stack, 0, this.amigoInventory.getContainerSize(), false)) {
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
        return this.amigoEntity.isAlive() && player.distanceTo(this.amigoEntity) < 8.0D;
    }

    private static class ArmorSlot extends Slot {
        private final AmigoEntity amigoEntity;
        private final EquipmentSlot equipmentSlot;

        public ArmorSlot(Container inventory, int index, int x, int y, AmigoEntity amigoEntity, EquipmentSlot equipmentSlot) {
            super(inventory, index, x, y);
            this.amigoEntity = amigoEntity;
            this.equipmentSlot = equipmentSlot;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            ItemStack stack = this.getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem armorItem) {
                this.amigoEntity.setItemSlot(this.equipmentSlot, stack);
            } else {
                this.amigoEntity.setItemSlot(this.equipmentSlot, ItemStack.EMPTY);
            }
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == this.equipmentSlot;
        }
    }
}