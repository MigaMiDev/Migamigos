package ttv.migami.migamigos.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import ttv.migami.migamigos.Reference;
import ttv.migami.migamigos.common.container.CompanionContainer;
import ttv.migami.migamigos.entity.Companion;
import ttv.migami.migamigos.network.PacketHandler;
import ttv.migami.migamigos.network.message.C2SCloseCompanionInventory;
import ttv.migami.migamigos.network.message.C2SCompanionSwitchAttitude;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CompanionScreen extends AbstractContainerScreen<CompanionContainer> {
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/companion_inventory.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("textures/item/empty_armor_slot_helmet.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("textures/item/empty_armor_slot_chestplate.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("textures/item/empty_armor_slot_leggings.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("textures/item/empty_armor_slot_boots.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("textures/item/empty_armor_slot_shield.png");
    private final Companion companion;

    private final List<Button> buttons = new ArrayList<>();

    private final int maxHearts = 10;
    private final int healthBarX = 0;
    private final int healthBarY = -11;

    public CompanionScreen(CompanionContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.companion = container.getCompanion();
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
                Pair.of(companion::isAttackingAnyEnemy, new String[]{"gui.migamigos.companion_inventory.attacking_any_entity.false", "gui.migamigos.companion_inventory.attacking_any_entity.true"}),
                Pair.of(companion::isDefendingPlayerOnly, new String[]{"gui.migamigos.companion_inventory.defend_player_only.false", "gui.migamigos.companion_inventory.defend_player_only.true"}),
                Pair.of(companion::isFocusingOnMainTarget, new String[]{"gui.migamigos.companion_inventory.focus_on_main_target.false", "gui.migamigos.companion_inventory.focus_on_main_target.true"}),
                Pair.of(companion::canWander, new String[]{"gui.migamigos.companion_inventory.allow_wandering.false", "gui.migamigos.companion_inventory.allow_wandering.true"}),
                Pair.of(companion::isFollowing, new String[]{"gui.migamigos.companion_inventory.following.false", "gui.migamigos.companion_inventory.following.true"})
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
    }

    private void onButtonClick(int buttonIndex) {
        PacketHandler.getPlayChannel().sendToServer(new C2SCompanionSwitchAttitude(this.companion.getUUID(), buttonIndex));
    }

    private String getButtonTranslationKey(Supplier<Boolean> getter, String[] translationKeys, boolean opposite) {
        boolean value = getter.get();
        if (opposite) {
            return translationKeys[value ? 0: 1];
        }
        return translationKeys[value ? 1 : 0];
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(guiGraphics);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 50, j + 82, 30, (float)(i + 50) - (float)mouseX, (float)(j + 75 - 50) - (float)mouseY, this.companion);

        renderCompanionHealthBar(guiGraphics, this.leftPos + this.healthBarX, this.topPos + this.healthBarY, this.companion);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        renderCompanionHealth(guiGraphics, this.leftPos + this.healthBarX, this.topPos + this.healthBarY, this.companion);
        renderEmptyArmorIcons(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
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

    private void renderCompanionHealthBar(GuiGraphics guiGraphics, int x, int y, Companion companion) {
        int maxHearts = this.maxHearts;

        for (int i = 0; i < maxHearts; i++) {
            int heartX = x + i * 8;

            guiGraphics.blit(GUI_ICONS_LOCATION, heartX, y, 16, 0, 9, 9);
        }
    }

    private void renderCompanionHealth(GuiGraphics guiGraphics, int x, int y, Companion companion) {
        int maxHearts = this.maxHearts;
        int maxHealth = (int) companion.getMaxHealth();
        int currentHealth = (int) companion.getHealth();

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
        PacketHandler.getPlayChannel().sendToServer(new C2SCloseCompanionInventory(this.companion.getUUID()));
        this.companion.setContainerOpen(false);
    }
}
