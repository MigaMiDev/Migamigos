package ttv.migami.migamigos.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import ttv.migami.migamigos.entity.Companion;

import java.util.EnumSet;
import java.util.List;

public class CompanionFarmGoal extends Goal {
    private static final int RADIUS = 10;
    private final Companion companion;
    private final double speed;
    private BlockPos targetCropPos = null;
    private int harvestDelay = 0;
    private BlockPos chestPos = null;
    private int depositDelay = 0;
    private boolean isDepositing = false;
    private int depositTicks = 0;

    public CompanionFarmGoal(Companion companion, double speed) {
        this.companion = companion;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return findNextCrop();
    }

    @Override
    public void start() {
        if (this.targetCropPos != null) {
            moveToCrop();
        }
    }

    @Override
    public void tick() {
        if (this.isDepositing) {
            handleDepositing();
        } else {
            if (this.targetCropPos == null) return;

            double distance = this.companion.position().distanceTo(Vec3.atCenterOf(this.targetCropPos));
            moveToCrop();
            if (distance < 2.0) {
                if (this.harvestDelay > 0) {
                    this.harvestDelay--;
                } else if (!harvestAndReplant()) {
                    this.chestPos = findNearestChest();
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetCropPos != null || chestPos != null;
    }

    private boolean findNextCrop() {
        BlockPos entityPos = this.companion.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(entityPos.offset(-RADIUS, -1, -RADIUS), entityPos.offset(RADIUS, 1, RADIUS))) {
            if (this.companion.level().getBlockState(pos).getBlock() instanceof ComposterBlock) {
                for (BlockPos cropPos : BlockPos.betweenClosed(pos.offset(-RADIUS, 0, -RADIUS), pos.offset(RADIUS, 0, RADIUS))) {
                    BlockState state = this.companion.level().getBlockState(cropPos);
                    if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
                        this.targetCropPos = cropPos.immutable();
                        this.harvestDelay = 20;
                        return true;
                    }
                }
            }
        }
        this.targetCropPos = null;
        return false;
    }

    private void moveToCrop() {
        if (this.targetCropPos != null) {
            this.companion.getNavigation().moveTo(this.targetCropPos.getX() + 0.5, this.targetCropPos.getY(), this.targetCropPos.getZ() + 0.5, this.speed);
        }
    }

    /**
     * Harvest and replant crops. Returns false if inventory is full.
     */
    private boolean harvestAndReplant() {
        if (this.targetCropPos == null) return false;

        BlockState state = this.companion.level().getBlockState(this.targetCropPos);
        Block block = state.getBlock();

        if (block instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
            this.companion.level().levelEvent(2001, this.targetCropPos, Block.getId(state));

            List<ItemStack> drops = Block.getDrops(state, (ServerLevel) this.companion.level(), this.targetCropPos, null, this.companion, this.companion.getMainHandItem());
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CropBlock) {
                    this.companion.level().setBlock(this.targetCropPos, blockItem.getBlock().defaultBlockState(), 3);
                } else {
                    if (!addToInventory(drop)) {
                        return false;
                    }
                }
            }
        }

        this.targetCropPos = null;
        findNextCrop();
        return true;
    }

    /**
     * Attempts to add an ItemStack to the entity's inventory.
     * Returns true if the item was successfully added; false if the inventory is full.
     */
    private boolean addToInventory(ItemStack stack) {
        SimpleContainer inventory = this.companion.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack slotStack = inventory.getItem(i);
            if (slotStack.isEmpty()) {
                inventory.setItem(i, stack.copy());
                return true;
            } else if (ItemStack.isSameItemSameTags(slotStack, stack) && slotStack.getCount() < slotStack.getMaxStackSize()) {
                int spaceLeft = slotStack.getMaxStackSize() - slotStack.getCount();
                int toAdd = Math.min(spaceLeft, stack.getCount());
                slotStack.grow(toAdd);
                stack.shrink(toAdd);
                if (stack.isEmpty()) return true;
            }
        }

        this.isDepositing = true;
        return false;
    }

    /**
     * Finds the nearest chest to the ComposterBlock.
     */
    private BlockPos findNearestChest() {
        BlockPos entityPos = this.companion.blockPosition();
        BlockPos nearestChest = null;
        double shortestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(entityPos.offset(-RADIUS, -1, -RADIUS), entityPos.offset(RADIUS, 1, RADIUS))) {
            if (this.companion.level().getBlockState(pos).getBlock() instanceof ComposterBlock) {
                for (BlockPos chestPos : BlockPos.betweenClosed(pos.offset(-RADIUS, 0, -RADIUS), pos.offset(RADIUS, 0, RADIUS))) {
                    if (this.companion.level().getBlockEntity(chestPos) instanceof ChestBlockEntity) {
                        double distance = this.companion.position().distanceTo(Vec3.atCenterOf(chestPos));
                        if (distance < shortestDistance) {
                            shortestDistance = distance;
                            nearestChest = chestPos.immutable();
                        }
                    }
                }
            }
        }

        return nearestChest;
    }

    private void handleDepositing() {
        if (this.chestPos == null) {
            this.isDepositing = false;
            return;
        }

        double distanceToChest = this.companion.position().distanceTo(Vec3.atCenterOf(this.chestPos));

        if (distanceToChest > 2.0) {
            this.companion.getNavigation().moveTo(this.chestPos.getX() + 0.5, this.chestPos.getY() + 0.5, this.chestPos.getZ() + 0.5, 1.0);
        } else {
            if (this.depositTicks == 0) {
                this.companion.level().playSound(null, this.chestPos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f, 1.0f);


                this.depositTicks = 40;
            } else if (this.depositTicks == 1) {
                depositCollectedItems();
                this.companion.level().playSound(null, chestPos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5f, 1.0f);
                this.isDepositing = false;
            }

            this.depositTicks--;
        }
    }

    /**
     * Deposits "forge:crops" items into the nearest chest.
     */
    private void depositCollectedItems() {
        if (chestPos == null || !(companion.level().getBlockEntity(chestPos) instanceof ChestBlockEntity chest)) {
            chestPos = null;
            return;
        }

        SimpleContainer inventory = this.companion.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem().builtInRegistryHolder().is(Tags.Items.CROPS)) {
                for (int chestSlot = 0; chestSlot < chest.getContainerSize(); chestSlot++) {
                    ItemStack chestStack = chest.getItem(chestSlot);
                    if (chestStack.isEmpty()) {
                        chest.setItem(chestSlot, stack.copy());
                        inventory.setItem(i, ItemStack.EMPTY);
                        break;
                    } else if (ItemStack.isSameItemSameTags(chestStack, stack) && chestStack.getCount() < chestStack.getMaxStackSize()) {
                        int spaceLeft = chestStack.getMaxStackSize() - chestStack.getCount();
                        int toAdd = Math.min(spaceLeft, stack.getCount());
                        chestStack.grow(toAdd);
                        stack.shrink(toAdd);
                        if (stack.isEmpty()) {
                            inventory.setItem(i, ItemStack.EMPTY);
                        }
                        break;
                    }
                }
            }
        }
    }
}