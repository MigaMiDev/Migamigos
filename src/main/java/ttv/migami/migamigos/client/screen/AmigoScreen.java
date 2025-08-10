package ttv.migami.migamigos.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.client.util.RenderUtil;
import ttv.migami.migamigos.common.container.AmigoContainer;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.network.PacketHandler;
import ttv.migami.migamigos.network.message.C2SAmigoSwitchAttitude;
import ttv.migami.migamigos.network.message.C2SCloseAmigoInventory;
import ttv.migami.migamigos.network.message.C2SMessageToggleArmorPiece;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class AmigoScreen extends AbstractContainerScreen<AmigoContainer> {
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/amigo_inventory.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("textures/item/empty_armor_slot_helmet.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("textures/item/empty_armor_slot_chestplate.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("textures/item/empty_armor_slot_leggings.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("textures/item/empty_armor_slot_boots.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("textures/item/empty_armor_slot_shield.png");
    private final AmigoEntity amigoEntity;

    private final List<Button> buttons = new ArrayList<>();

    private final int maxHearts = 10;
    private final int healthBarX = 0;
    private final int healthBarY = -11;
    private final int levelBarY = -9;

    public AmigoScreen(AmigoContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.amigoEntity = container.getAmigo();
        this.imageWidth = 176;
        this.imageHeight = 176;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 120;
        int buttonHeight = 20;
        int startX = this.leftPos + this.imageWidth + 5;
        int startY = this.topPos;
        int buttonSpacing = 5;

        List<Pair<Supplier<Boolean>, String[]>> buttonData = List.of(
                Pair.of(amigoEntity::isAttackingAnyEnemy, new String[]{"gui.migamigos.amigo_inventory.attacking_any_entity.false", "gui.migamigos.amigo_inventory.attacking_any_entity.true"}),
                Pair.of(amigoEntity::isDefendingPlayerOnly, new String[]{"gui.migamigos.amigo_inventory.defend_player_only.false", "gui.migamigos.amigo_inventory.defend_player_only.true"}),
                Pair.of(amigoEntity::isFocusingOnMainTarget, new String[]{"gui.migamigos.amigo_inventory.focus_on_main_target.false", "gui.migamigos.amigo_inventory.focus_on_main_target.true"}),
                Pair.of(amigoEntity::canWander, new String[]{"gui.migamigos.amigo_inventory.allow_wandering.false", "gui.migamigos.amigo_inventory.allow_wandering.true"}),
                Pair.of(amigoEntity::isFollowing, new String[]{"gui.migamigos.amigo_inventory.following.false", "gui.migamigos.amigo_inventory.following.true"})
        );

        for (int i = 0; i < buttonData.size(); i++) {
            int buttonY = startY + i * (buttonHeight + buttonSpacing);
            Supplier<Boolean> getter = buttonData.get(i).getFirst();
            String[] translationKeys = buttonData.get(i).getSecond();

            int finalI = i;
            this.addRenderableWidget(
                    Button.builder(Component.translatable(getButtonTranslationKey(getter, translationKeys, false)), button -> {
                                onButtonClick(finalI);
                                button.setMessage(Component.translatable(getButtonTranslationKey(getter, translationKeys, true)));
                            })
                            .pos(startX, buttonY)
                            .size(buttonWidth, buttonHeight)
                            .build()
            );
        }

        {
            // index should be 5 here, but if you add more buttons to buttonData
            // then this will change, wich means adjusting the action id
            // on ServerPlayerHandler.amigoAttitudeSwitch
            final int index = buttonData.size();
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null) {
                if (amigoEntity.canRideWithPlayer(player) || amigoEntity.getVehicle() != null) {
                    addRideWithPlayerButton(player, index, startY, startX, buttonWidth, buttonHeight, buttonSpacing);
                }
            }
        }
    }

    private void onButtonClick(int buttonIndex) {
        PacketHandler.getPlayChannel().sendToServer(new C2SAmigoSwitchAttitude(this.amigoEntity.getUUID(), buttonIndex));
    }

    private String getButtonTranslationKey(Supplier<Boolean> getter, String[] translationKeys, boolean opposite) {
        boolean value = getter.get();
        if (opposite) {
            return translationKeys[value ? 0: 1];
        }
        return translationKeys[value ? 1 : 0];
    }

    private void addRideWithPlayerButton(LocalPlayer player, int index, int startY, int startX, int width, int height, int spacing) {
        Entity amigoVehicle = amigoEntity.getVehicle();
        Entity playerVehicle = player.getVehicle();

        // one of them should not be null, but just to be safe
        if (amigoVehicle == null && playerVehicle == null)
            return;

        MutableComponent format = Component.translatable(
            amigoVehicle == null ?
                "gui.migamigos.amigo_inventory.ride_with_player" :
                "gui.migamigos.amigo_inventory.stop_riding"
        );

        String translation = format.getString().formatted(
            amigoVehicle == null ?
                playerVehicle.getName().getString() :
                amigoVehicle.getName().getString()
        );

        int buttonY = startY + index * (height + spacing);

        this.addRenderableWidget(
            Button.builder(Component.literal(translation), button -> {
                onButtonClick(index);
                button.setMessage(Component.literal(translation));
            })
            .pos(startX, buttonY)
            .size(width, height)
            .build()
        );
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(guiGraphics);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (amigoEntity.isShowingHelmet()) {
            guiGraphics.blit(TEXTURE, this.leftPos + 4, this.topPos + 23, 246, 9, 3, 6);
        } else {
            guiGraphics.blit(TEXTURE, this.leftPos + 4, this.topPos + 23, 249, 9, 3, 6);
        }
        if (amigoEntity.isShowingChestplate()) {
            guiGraphics.blit(TEXTURE, this.leftPos + 4, this.topPos + 41, 246, 9, 3, 6);
        } else {
            guiGraphics.blit(TEXTURE, this.leftPos + 4, this.topPos + 41, 249, 9, 3, 6);
        }
        if (amigoEntity.isShowingLeggings()) {
            guiGraphics.blit(TEXTURE, this.leftPos + 4, this.topPos + 59, 246, 9, 3, 6);
        } else {
            guiGraphics.blit(TEXTURE, this.leftPos + 4, this.topPos + 59, 249, 9, 3, 6);
        }
        if (amigoEntity.isShowingBoots()) {
            guiGraphics.blit(TEXTURE, this.leftPos + 4, this.topPos + 77, 246, 9, 3, 6);
        } else {
            guiGraphics.blit(TEXTURE, this.leftPos + 4, this.topPos + 77, 249, 9, 3, 6);
        }

        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 50, j + 82, 30, (float)(i + 50) - (float)mouseX, (float)(j + 75 - 50) - (float)mouseY, this.amigoEntity);

        renderAmigoHealthBar(guiGraphics, this.leftPos + this.healthBarX, this.topPos + this.healthBarY, this.amigoEntity);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        renderAmigoHealth(guiGraphics, this.leftPos + this.healthBarX, this.topPos + this.healthBarY, this.amigoEntity);
        renderAmigoArmorBar(guiGraphics, this.leftPos + this.healthBarX, (this.topPos + this.healthBarY) - 10, this.amigoEntity);
        renderEmptyArmorIcons(guiGraphics);

        if(RenderUtil.isMouseWithin(mouseX, mouseY, this.leftPos + 4, this.topPos + 23, 3, 6) ||
                RenderUtil.isMouseWithin(mouseX, mouseY, this.leftPos + 4, this.topPos + 41, 3, 6) ||
                RenderUtil.isMouseWithin(mouseX, mouseY, this.leftPos + 4, this.topPos + 59, 3, 6) ||
                RenderUtil.isMouseWithin(mouseX, mouseY, this.leftPos + 4, this.topPos + 77, 3, 6)) {
            guiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.migamigos.hide_armor_piece").withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
        }

        int healthBarWidth = this.maxHearts * 8 + 1;
        if (RenderUtil.isMouseWithin(mouseX, mouseY, this.leftPos + this.healthBarX, this.topPos + this.healthBarY, healthBarWidth, 9)) {
            int currentHealth = (int) amigoEntity.getHealth();
            int maxHealth = (int) amigoEntity.getMaxHealth();
            Component healthTooltip = Component.literal(currentHealth + "/" + maxHealth);
            guiGraphics.renderComponentTooltip(this.font, List.of(healthTooltip), mouseX, mouseY);
        }
        if(RenderUtil.isMouseWithin(mouseX, mouseY, this.leftPos + 143, this.topPos + 5, 26, 10)) {
            guiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("cutesy.migamigos.thanks"), Component.literal("- MigaMi ♡").withStyle(ChatFormatting.BLUE)), mouseX, mouseY);
        }
        if(RenderUtil.isMouseWithin(mouseX, mouseY, this.leftPos + 36, this.topPos + 26, 29, 59)) {
            Item favoriteItem = ForgeRegistries.ITEMS.getValue(this.amigoEntity.getAmigo().getGeneral().getFavoriteItem());
            guiGraphics.renderComponentTooltip(this.font, Arrays.asList(
                    Component.translatable(this.amigoEntity.getDisplayName().getString()).withStyle(ChatFormatting.BLUE),
                    (Component.translatable("info.migamigos.favorite_item").withStyle(ChatFormatting.GRAY).append(Component.translatable(favoriteItem.getDescriptionId()).withStyle(ChatFormatting.WHITE))),
                    (Component.translatable("info.migamigos.max_health").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.amigoEntity.getAttribute(Attributes.MAX_HEALTH).getValue())).withStyle(ChatFormatting.WHITE))),
                    (Component.translatable("info.migamigos.armor").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.amigoEntity.getArmorValue())).withStyle(ChatFormatting.WHITE))),
                    (Component.translatable("info.migamigos.additional_power").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.amigoEntity.getExtraPower())).withStyle(ChatFormatting.WHITE))),
                    (Component.translatable("info.migamigos.tolerance").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.amigoEntity.getTolerance())).withStyle(ChatFormatting.WHITE))),
                    (Component.translatable("info.migamigos.speed").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.amigoEntity.getAmigo().getGeneral().getSpeed())).withStyle(ChatFormatting.WHITE)))
                    ), mouseX, mouseY);
        }
        if(RenderUtil.isMouseWithin(mouseX, mouseY, this.leftPos + 79, this.topPos + 65, 90, 25)) {
            guiGraphics.renderComponentTooltip(this.font, Arrays.asList(
                    Component.translatable("warning.migamigos.wip"),
                    Component.translatable("warning.migamigos.wip_2"),
                    Component.literal("- MigaMi ♡").withStyle(ChatFormatting.BLUE)
                    ), mouseX, mouseY);
        }
        int progressBarWidth = 80;
        int progressBarX = this.imageWidth - 80 - 11;
        if(RenderUtil.isMouseWithin(mouseX, mouseY, this.leftPos + progressBarX,  this.topPos + this.levelBarY - 2, progressBarWidth + 11, 7)) {
            guiGraphics.renderComponentTooltip(this.font, List.of(Component.translatable("gui.migamigos.amigo_inventory.level")), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);

        // Level progression bar
        int progressBarWidth = 70;
        int progressBarHeight = 5;
        int progressBarX = this.imageWidth - 80 - 11;
        int progressBarY = this.levelBarY;

        int currentExperience = this.amigoEntity.getExperience();
        int currentLevel = this.amigoEntity.getAmigoLevel();

        int experienceForNextLevel = AmigoEntity.getExperienceForNextLevel(currentLevel);
        float progress = (float) currentExperience / experienceForNextLevel;

        //guiGraphics.drawCenteredString(this.font, Component.translatable("gui.spas.mastery"), centerX, progressBarY - 12, 16777215);

        guiGraphics.blit(TEXTURE, progressBarX, progressBarY, 176,9, progressBarWidth, progressBarHeight);
        guiGraphics.blit(TEXTURE, progressBarX, progressBarY, 176, 14, (int) (progress * progressBarWidth), progressBarHeight);

        int separator = 10;
        if (currentLevel >= 100) {
            separator = 21;
        } else if (currentLevel > 10) {
            separator = 16;
        }
        //guiGraphics.drawString(this.font, String.valueOf(currentLevel), progressBarX - separator, progressBarY + (progressBarHeight / 2) - 3, 16777215, true);
        guiGraphics.drawString(this.font, String.valueOf(currentLevel + 1), progressBarX + progressBarWidth + 5, progressBarY + (progressBarHeight / 2) - 3, 16777215, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, this.leftPos + 4, this.topPos + 23, 3, 6))
        {
            if((button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                this.toggleArmorPiece(0);
                Minecraft.getInstance().player.playSound(
                        SoundEvents.ARMOR_EQUIP_GENERIC,
                        1.0F,
                        1.0F
                );
                return true;
            }
        }
        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, this.leftPos + 4, this.topPos + 41, 3, 6))
        {
            if((button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                this.toggleArmorPiece(1);
                Minecraft.getInstance().player.playSound(
                        SoundEvents.ARMOR_EQUIP_GENERIC,
                        1.0F,
                        1.0F
                );
                return true;
            }
        }
        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, this.leftPos + 4, this.topPos + 59, 3, 6))
        {
            if((button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                this.toggleArmorPiece(2);
                Minecraft.getInstance().player.playSound(
                        SoundEvents.ARMOR_EQUIP_GENERIC,
                        1.0F,
                        1.0F
                );
                return true;
            }
        }
        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, this.leftPos + 4, this.topPos + 77, 3, 6))
        {
            if((button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                this.toggleArmorPiece(3);
                Minecraft.getInstance().player.playSound(
                        SoundEvents.ARMOR_EQUIP_GENERIC,
                        1.0F,
                        1.0F
                );
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void toggleArmorPiece(int armorPiece) {
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageToggleArmorPiece(amigoEntity.getUUID(), armorPiece));
    }

    private void renderEmptyArmorIcons(GuiGraphics guiGraphics) {
        int[][] armorSlotPositions = {
                {8, 18}, // Helmet
                {8, 36}, // Chestplate
                {8, 54}, // Leggings
                {8, 72}  // Boots
        };

        ResourceLocation[] armorSlotTextures = {
                EMPTY_ARMOR_SLOT_HELMET,
                EMPTY_ARMOR_SLOT_CHESTPLATE,
                EMPTY_ARMOR_SLOT_LEGGINGS,
                EMPTY_ARMOR_SLOT_BOOTS
        };

        for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
            Slot slot = this.menu.slots.get(slotIndex);
            if (!slot.hasItem()) {
                int x = this.leftPos + armorSlotPositions[slotIndex][0];
                int y = this.topPos + armorSlotPositions[slotIndex][1];

                RenderSystem.setShaderTexture(0, armorSlotTextures[slotIndex]);
                guiGraphics.blit(armorSlotTextures[slotIndex], x, y, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    private void renderAmigoArmorBar(GuiGraphics guiGraphics, int x, int y, AmigoEntity amigoEntity) {
        int maxArmor = 20;
        int currentArmor = amigoEntity.getArmorValue();

        int fullArmorIcons = currentArmor / 2;
        boolean hasHalfArmor = currentArmor % 2 == 1;

        for (int i = 0; i < maxArmor / 2; i++) {
            int armorX = x + i * 8;

            if (i < fullArmorIcons) {
                guiGraphics.blit(GUI_ICONS_LOCATION, armorX, y, 34, 9, 9, 9); // Full armor icon
            } else if (i == fullArmorIcons && hasHalfArmor) {
                guiGraphics.blit(GUI_ICONS_LOCATION, armorX, y, 25, 9, 9, 9); // Half armor icon
            } else {
                guiGraphics.blit(GUI_ICONS_LOCATION, armorX, y, 16, 9, 9, 9); // Empty armor icon
            }
        }
    }

    private void renderAmigoHealthBar(GuiGraphics guiGraphics, int x, int y, AmigoEntity amigoEntity) {
        int maxHearts = this.maxHearts;

        for (int i = 0; i < maxHearts; i++) {
            int heartX = x + i * 8;

            guiGraphics.blit(GUI_ICONS_LOCATION, heartX, y, 16, 0, 9, 9);
        }
    }

    private void renderAmigoHealth(GuiGraphics guiGraphics, int x, int y, AmigoEntity amigoEntity) {
        int maxHearts = this.maxHearts;
        int maxHealth = (int) amigoEntity.getMaxHealth();
        int currentHealth = (int) amigoEntity.getHealth();

        float healthPercentage = (float) currentHealth / maxHealth;
        float scaledHealth = healthPercentage * maxHearts;

        int fullHearts = (int) scaledHealth;
        boolean hasHalfHeart = (scaledHealth - fullHearts) >= 0.5f;

        for (int i = 0; i < maxHearts; i++) {
            int heartX = x + i * 8;

            if (i < fullHearts) {
                guiGraphics.blit(GUI_ICONS_LOCATION, heartX, y, 52, 0, 9, 9); // Full heart
            } else if (i == fullHearts && hasHalfHeart) {
                guiGraphics.blit(GUI_ICONS_LOCATION, heartX, y, 61, 0, 9, 9); // Half heart
            }
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        PacketHandler.getPlayChannel().sendToServer(new C2SCloseAmigoInventory(this.amigoEntity.getUUID()));
        this.amigoEntity.setContainerOpen(false);
    }
}
