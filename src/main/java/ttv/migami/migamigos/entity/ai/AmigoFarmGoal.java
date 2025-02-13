package ttv.migami.migamigos.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import ttv.migami.migamigos.entity.AmigoEntity;

import java.util.EnumSet;
import java.util.List;

public class AmigoFarmGoal extends Goal {
    private static final int RADIUS = 10;
    private final AmigoEntity amigoEntity;
    private final double speed;
    private BlockPos targetCropPos = null;
    private int harvestDelay = 0;
    private BlockPos chestPos = null;
    private int depositDelay = 0;
    private boolean isDepositing = false;
    private int depositTicks = 0;
    private boolean fullNoChest = false;

    private final static int FARM_TIMER = 38;

    public AmigoFarmGoal(AmigoEntity amigoEntity, double speed) {
        this.amigoEntity = amigoEntity;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        //return findNextCrop() && !this.amigo.isFollowing();
        if (this.targetCropPos != null) {
            return true;
        }
        if (this.amigoEntity.isFollowing()) {
            return false;
        }
        if (this.amigoEntity.isEating() || this.amigoEntity.isEmoting()) {
            this.amigoEntity.getNavigation().stop();
            return false;
        }

        return findNextCrop();
    }

    @Override
    public void start() {
        this.amigoEntity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_HOE));
        this.amigoEntity.setIsFarming(true);
        if (this.targetCropPos != null) {
            moveToCrop();
        }
    }

    @Override
    public void stop() {
        if (this.amigoEntity.getMainHandItem().is(Items.IRON_HOE)) {
            this.amigoEntity.setItemSlot(EquipmentSlot.MAINHAND, this.amigoEntity.getDefaultItem().getDefaultInstance());
        }
        this.amigoEntity.setIsFarming(false);
        this.amigoEntity.setIsHarvesting(false);
    }

    @Override
    public void tick() {
        this.chestPos = findNearestChest();

        if (this.chestPos != null) {
            this.fullNoChest = false;
        }
        else if (this.fullNoChest) {
            this.stop();
        }

        double distance = this.amigoEntity.position().distanceTo(Vec3.atCenterOf(this.targetCropPos));

        if (distance > 7.0) {
            this.stop();
        }

        if (this.isDepositing) {
            this.amigoEntity.setIsHarvesting(false);
            handleDepositing();
        } else {
            if (this.targetCropPos == null) {
                this.stop();
            }

            moveToCrop();
            if (distance < 2) {
                if (this.harvestDelay > 0) {
                    if(this.amigoEntity.getSpeed() < 0.3) {
                        this.amigoEntity.setIsHarvesting(true);
                        if (this.harvestDelay % 10 == 0) {
                            this.amigoEntity.level().playSound(null, this.amigoEntity, SoundEvents.HOE_TILL, SoundSource.PLAYERS, 1.0F, this.amigoEntity.getRandom().nextFloat() * 0.1F + 0.9F);
                        }
                        this.harvestDelay--;
                    }
                } else {
                    harvestAndReplant();
                }
            } else {
                this.amigoEntity.setIsHarvesting(false);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetCropPos != null;
    }

    private boolean findNextCrop() {
        BlockPos entityPos = this.amigoEntity.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(entityPos.offset(-RADIUS, -1, -RADIUS), entityPos.offset(RADIUS, 1, RADIUS))) {
            if (this.amigoEntity.level().getBlockState(pos).getBlock() instanceof ComposterBlock) {
                for (BlockPos cropPos : BlockPos.betweenClosed(pos.offset(-RADIUS, 0, -RADIUS), pos.offset(RADIUS, 0, RADIUS))) {
                    BlockState state = this.amigoEntity.level().getBlockState(cropPos);
                    if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
                        this.targetCropPos = cropPos.immutable();
                        return true;
                    }
                }
            }
        }
        this.amigoEntity.setIsHarvesting(false);
        this.targetCropPos = null;
        return false;
    }

    private void moveToCrop() {
        if (this.targetCropPos != null && !this.amigoEntity.isEating() && !this.amigoEntity.isEmoting()) {
            this.amigoEntity.setIsHarvesting(false);
            this.amigoEntity.getNavigation().moveTo(this.targetCropPos.getX() + 0.5, this.targetCropPos.getY(), this.targetCropPos.getZ() + 0.5, this.speed);
        }
    }

    /**
     * Harvest and replant crops. Returns false if inventory is full.
     */
    private boolean harvestAndReplant() {
        if (this.targetCropPos == null || this.fullNoChest) return false;

        BlockState state = this.amigoEntity.level().getBlockState(this.targetCropPos);
        Block block = state.getBlock();

        if (block instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
            this.amigoEntity.level().levelEvent(2001, this.targetCropPos, Block.getId(state));

            List<ItemStack> drops = Block.getDrops(state, (ServerLevel) this.amigoEntity.level(), this.targetCropPos, null, this.amigoEntity, this.amigoEntity.getMainHandItem());
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CropBlock) {
                    this.harvestDelay = FARM_TIMER;
                    this.amigoEntity.level().setBlock(this.targetCropPos, blockItem.getBlock().defaultBlockState(), 3);
                } else {
                    if (!addToInventory(drop)) {
                        if (this.chestPos == null) {
                            this.fullNoChest = true;
                        }
                        return false;
                    }
                }
            }
        }

        this.amigoEntity.setIsHarvesting(false);
        this.targetCropPos = null;
        findNextCrop();
        return true;
    }

    /**
     * Attempts to add an ItemStack to the entity's inventory.
     * Returns true if the item was successfully added; false if the inventory is full.
     */
    private boolean addToInventory(ItemStack stack) {
        SimpleContainer inventory = this.amigoEntity.getInventory();

        for (int i = 0; i < inventory.getContainerSize() - 4; i++) {
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
        BlockPos entityPos = this.amigoEntity.blockPosition();
        BlockPos nearestChest = null;
        double shortestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(entityPos.offset(-RADIUS, -1, -RADIUS), entityPos.offset(RADIUS, 1, RADIUS))) {
            if (this.amigoEntity.level().getBlockState(pos).getBlock() instanceof ComposterBlock) {
                for (BlockPos chestPos : BlockPos.betweenClosed(pos.offset(-RADIUS, 0, -RADIUS), pos.offset(RADIUS, 0, RADIUS))) {
                    if (this.amigoEntity.level().getBlockEntity(chestPos) instanceof ChestBlockEntity) {
                        double distance = this.amigoEntity.position().distanceTo(Vec3.atCenterOf(chestPos));
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

        double distanceToChest = this.amigoEntity.position().distanceTo(Vec3.atCenterOf(this.chestPos));

        if (distanceToChest > 2.0 && !this.amigoEntity.isEating() && !this.amigoEntity.isEmoting()) {
            this.amigoEntity.getNavigation().moveTo(this.chestPos.getX() + 0.5, this.chestPos.getY() + 0.5, this.chestPos.getZ() + 0.5, 1.0);
        } else {
            if (this.depositTicks == 0) {
                this.amigoEntity.level().playSound(null, this.chestPos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f, 1.0f);


                this.depositTicks = 40;
            } else if (this.depositTicks == 1) {
                depositCollectedItems();
                this.amigoEntity.level().playSound(null, chestPos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5f, 1.0f);
                this.isDepositing = false;
            }

            this.depositTicks--;
        }
    }

    /**
     * Deposits "forge:crops" items into the nearest chest.
     */
    private void depositCollectedItems() {
        if (chestPos == null || !(amigoEntity.level().getBlockEntity(chestPos) instanceof ChestBlockEntity chest)) {
            chestPos = null;
            return;
        }

        SimpleContainer inventory = this.amigoEntity.getInventory();

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