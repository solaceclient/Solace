package huysuh.UI;

import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class GuiAccountDetails extends GuiScreen {

    private GuiScreen parentScreen;
    private GuiAlts.CachedAccount account;
    private CompletableFuture<BufferedImage> bodyFuture;

    private static final int BACKGROUND_COLOR = 0xFF000000;
    private static final int PANEL_COLOR = 0xFF111111;
    private static final int ACCENT_COLOR = 0xFF00FFFF;
    private static final int TEXT_COLOR = new Color(124, 194, 91).getRGB();

    public GuiAccountDetails(GuiScreen parentScreen, GuiAlts.CachedAccount account) {
        this.parentScreen = parentScreen;
        this.account = account;
    }

    public void initGui() {
        int panelWidth = Math.min(300, this.width - 40);
        int panelLeft = (this.width - panelWidth) / 2;
        int panelBottom = this.height - 40;

        this.buttonList.add(new GuiButton(0, panelLeft + 10, panelBottom + 5, 135, 20, "Login"));
        this.buttonList.add(new GuiButton(1, panelLeft + panelWidth - 145, panelBottom + 5, 135, 20, "Back"));
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, BACKGROUND_COLOR);

        int panelWidth = Math.min(300, this.width - 40);
        int panelHeight = this.height - 80;
        int panelLeft = (this.width - panelWidth) / 2;
        int panelTop = 40;
        drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, PANEL_COLOR);

        this.drawCenteredString(this.fontRendererObj, "Account Details", this.width / 2, 20, ACCENT_COLOR);

        this.drawCenteredString(this.fontRendererObj, "Username: " + account.username, this.width / 2, panelTop + 20, TEXT_COLOR);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastLoginStr = sdf.format(new Date(account.lastLoginTime));
        this.drawCenteredString(this.fontRendererObj, "Last Login: " + lastLoginStr, this.width / 2, panelTop + 40, TEXT_COLOR);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) { // Login
            mc.session = new Session(account.username, account.uuid, account.token, "microsoft");
            account.lastLoginTime = System.currentTimeMillis();
            GuiAlts.saveCachedAccounts();
            mc.displayGuiScreen(parentScreen);
            if (mc.currentScreen == null) {
                mc.setIngameFocus();
            }
        } else if (button.id == 1) { // Back
            mc.displayGuiScreen(parentScreen);
        }
    }
}
