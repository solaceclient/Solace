package huysuh.UI;

import com.bytespacegames.mcpauth.SessionUtils;
import huysuh.Font.Fonts;
import huysuh.Utils.RainbowUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GuiAlts extends GuiScreen implements GuiYesNoCallback {

    private GuiScreen parentScreen;
    protected String screenTitle = "Alts Manager";
    private GuiButton loginButton;
    private GuiButton refreshButton;
    private GuiButton removeButton;
    private static List<CachedAccount> cachedAccounts = new ArrayList<>();

    private static final String CACHE_FILE = "cached_accounts.txt";
    private static final int ACCOUNTS_PER_PAGE = 5;
    private int currentPage = 0;

    public static String loginText = "init";

    private int selectedAccount = -1;

    // Animation variables
    private float animationTime = 0;
    private RainbowUtil rainbowUtil = new RainbowUtil(3.0f, 0.7f, 1.0f);

    private int buttonWidth = 120;
    private int buttonHeight = 20;
    private int buttonSpacing = 70;

    private static final int BLACK = 0xFF000000;
    private static final int DARK_GRAY = 0xFF101010;
    private static final int GREEN = new Color(124, 194, 91).getRGB();
    private static final int DARK_GREEN = new Color(60, 92, 44).getRGB();
    private static final int TEXT_GREEN = new Color(124, 194, 91).getRGB();
    private static final int SELECTED_ACCOUNT_COLOR = 0x3000FF00;

    private float gridSize = 25;
    private float scrollSpeed = 0.1f;
    private float gridAlpha = 0.07f;

    private Map<String, CompletableFuture<BufferedImage>> avatarFutures = new HashMap<>();

    public GuiAlts(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        loadCachedAccounts();
        this.buttonList.clear();
        addButtons();
        loadAvatars();
    }

    private void loadAvatars() {
        for (CachedAccount account : cachedAccounts) {
            loadAvatar(account.username);
        }
    }

    private void loadAvatar(String username) {
        avatarFutures.put(username, CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL("https://minotar.net/helm/" + username + "/32");
                return ImageIO.read(url);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }));
    }

    public void addButtons() {
        this.buttonList.clear();
        int centerX = this.width / 2;
        int baseY = this.height - 60;

        this.buttonList.add(this.loginButton = new ModernButton(0, centerX - buttonWidth - buttonSpacing, baseY, buttonWidth, buttonHeight, "Login"));
        this.buttonList.add(this.removeButton = new ModernButton(2, centerX - buttonWidth / 2, baseY, buttonWidth, buttonHeight, "Remove"));
        this.buttonList.add(this.refreshButton = new ModernButton(1, centerX + buttonSpacing, baseY, buttonWidth, buttonHeight, "Refresh"));

        if (currentPage > 0) {
            this.buttonList.add(new ModernButton(100, centerX - buttonWidth * 2 - buttonSpacing * 2, baseY, buttonWidth, buttonHeight, "Previous"));
        }
        if ((currentPage + 1) * ACCOUNTS_PER_PAGE < cachedAccounts.size()) {
            this.buttonList.add(new ModernButton(101, centerX + buttonWidth + buttonSpacing * 2, baseY, buttonWidth, buttonHeight, "Next"));
        }

        updateRemoveButtonState();
    }

    private void updateRemoveButtonState() {
        removeButton.enabled = selectedAccount != -1;
    }

    @Override
    public void updateScreen() {
        animationTime += 0.01F;
        rainbowUtil.update();

        for (GuiButton button : this.buttonList) {
            if (button instanceof ModernButton) {
                ((ModernButton) button).updateRainbow();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, BLACK);

        drawAnimatedBackground(partialTicks);

        Fonts.SF.drawCenteredStringWithShadow("Alt Manager", this.width / 2, 20, rainbowUtil.getRainbow());

        Fonts.SF.drawCenteredStringWithShadow("Logged in as: " + mc.getSession().getUsername(),
                this.width / 2, 40, TEXT_GREEN);

        int panelWidth = Math.min(400, this.width - 80);
        int panelHeight = this.height - 140;
        int panelLeft = (this.width - panelWidth) / 2;
        int panelTop = 60;

        drawRect(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, DARK_GRAY);

        drawRainbowLine(panelLeft, panelLeft + panelWidth, panelTop, 2);

        int startY = panelTop + 10;
        int accountHeight = 38;
        int accountWidth = panelWidth - 20;

        for (int i = 0; i < ACCOUNTS_PER_PAGE; i++) {
            int index = currentPage * ACCOUNTS_PER_PAGE + i;
            if (index < cachedAccounts.size()) {
                CachedAccount account = cachedAccounts.get(index);
                int accountY = startY + i * (accountHeight + 5);
                boolean isHovered = mouseX >= panelLeft + 10 && mouseX <= panelLeft + 10 + accountWidth &&
                        mouseY >= accountY && mouseY <= accountY + accountHeight;

                drawRect(panelLeft + 10, accountY, panelLeft + 10 + accountWidth, accountY + accountHeight,
                        isHovered ? 0xFF151515 : DARK_GRAY);

                if (index == selectedAccount) {
                    drawRectOutline(panelLeft + 10, accountY, panelLeft + 10 + accountWidth, accountY + accountHeight,
                            rainbowUtil.getRainbow());
                }

                if (mc.getSession().getUsername().equals(account.username)) {
                    drawRect(panelLeft + 10, accountY, panelLeft + 10 + 3, accountY + accountHeight, GREEN);
                }

                CompletableFuture<BufferedImage> avatarFuture = avatarFutures.get(account.username);
                if (avatarFuture != null && avatarFuture.isDone()) {
                    try {
                        BufferedImage avatar = avatarFuture.get();
                        if (avatar != null) {
                            ResourceLocation resourceLocation = mc.getTextureManager().getDynamicTextureLocation("avatar_" + account.username, new DynamicTexture(avatar));
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                            mc.getTextureManager().bindTexture(resourceLocation);
                            Gui.drawModalRectWithCustomSizedTexture(panelLeft + 20, accountY + 3, 0, 0, 32, 32, 32, 32);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Fonts.SF.drawStringWithShadow(account.username, panelLeft + 60, accountY + 10, TEXT_GREEN);

                String lastLoginStr = "Last login: " + formatTimestamp(account.lastLoginTime);
                Fonts.SF.drawStringWithShadow(lastLoginStr, panelLeft + 60, accountY + 22, 0xFFAAAAAA);
            }
        }

        if (!this.loginText.equals("init")) {
            Fonts.SF.drawCenteredStringWithShadow(loginText, this.width / 2, this.height - 80, TEXT_GREEN);
        }

        String credits = "by huys & heart";
        Fonts.SF.drawStringWithShadow(credits,
                this.width - Fonts.SF.getStringWidth(credits) - 5,
                this.height - 12, TEXT_GREEN);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawAnimatedBackground(float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        float offset = (animationTime * scrollSpeed) % gridSize;

        GlStateManager.color(0.0F, 0.5F, 0.1F, gridAlpha);

        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        for (float y = offset; y < this.height; y += gridSize) {
            worldrenderer.pos(0, y, 0).endVertex();
            worldrenderer.pos(this.width, y, 0).endVertex();
        }
        tessellator.draw();

        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        for (float x = offset; x < this.width; x += gridSize) {
            worldrenderer.pos(x, 0, 0).endVertex();
            worldrenderer.pos(x, this.height, 0).endVertex();
        }
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawRainbowLine(int startX, int endX, int y, int thickness) {
        int segments = 20;
        int segmentWidth = (endX - startX) / segments;

        for (int i = 0; i < segments; i++) {
            int x1 = startX + (i * segmentWidth);
            int x2 = x1 + segmentWidth;

            drawRect(x1, y, x2, y + thickness, rainbowUtil.getRainbow());
        }
    }

    private void drawRectOutline(int left, int top, int right, int bottom, int color) {
        drawRect(left, top, right, top + 1, color);
        drawRect(left, bottom - 1, right, bottom, color);
        drawRect(left, top + 1, left + 1, bottom - 1, color);
        drawRect(right - 1, top + 1, right, bottom - 1, color);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int panelWidth = Math.min(400, this.width - 80);
        int panelLeft = (this.width - panelWidth) / 2;
        int panelTop = 60;
        int accountHeight = 38;
        int accountWidth = panelWidth - 20;

        for (int i = 0; i < ACCOUNTS_PER_PAGE; i++) {
            int index = currentPage * ACCOUNTS_PER_PAGE + i;
            if (index < cachedAccounts.size()) {
                int accountY = panelTop + 10 + i * (accountHeight + 5);
                if (mouseX >= panelLeft + 10 && mouseX <= panelLeft + 10 + accountWidth &&
                        mouseY >= accountY && mouseY <= accountY + accountHeight) {
                    if (mouseButton == 0) {
                        selectedAccount = index;
                        updateRemoveButtonState();
                    } else if (mouseButton == 1) {
                        // Right-click to log in
                        CachedAccount account = cachedAccounts.get(index);
                        mc.session = new Session(account.username, account.uuid, account.token, "microsoft");
                        account.lastLoginTime = System.currentTimeMillis();
                        saveCachedAccounts();
                    }
                    return;
                }
            }
        }

        // If clicked outside, deselect
        selectedAccount = -1;
        updateRemoveButtonState();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) { // Microsoft Login
            try {
                SessionUtils.tryLoginBrowser();
                // The caching will be done after successful login in the SessionUtils class
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (button.id == 1) { // Refresh
            loadCachedAccounts();
            addButtons();
        } else if (button.id == 2) { // Remove Alt
            if (selectedAccount != -1) {
                cachedAccounts.remove(selectedAccount);
                saveCachedAccounts();
                selectedAccount = -1;
                updateRemoveButtonState();
                addButtons();
            }
        } else if (button.id == 100) { // Previous page
            if (currentPage > 0) {
                currentPage--;
                selectedAccount = -1;
                updateRemoveButtonState();
                addButtons();
            }
        } else if (button.id == 101) { // Next page
            if ((currentPage + 1) * ACCOUNTS_PER_PAGE < cachedAccounts.size()) {
                currentPage++;
                selectedAccount = -1;
                updateRemoveButtonState();
                addButtons();
            }
        }
    }

    private String formatTimestamp(long timestamp) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
    }

    public static void cacheAccount(String username, String uuid, String token) {
        CachedAccount account = new CachedAccount(username, uuid, token);
        if (!cachedAccounts.contains(account)) {
            cachedAccounts.add(account);
            saveCachedAccounts();
        }
    }

    private static void loadCachedAccounts() {
        cachedAccounts.clear();
        File file = new File(CACHE_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(CACHE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 4) {
                    cachedAccounts.add(new CachedAccount(parts[0], parts[1], parts[2], Long.parseLong(parts[3])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveCachedAccounts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CACHE_FILE))) {
            for (CachedAccount account : cachedAccounts) {
                writer.write(account.username + ":" + account.uuid + ":" + account.token + ":" + account.lastLoginTime);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public class ModernButton extends GuiButton {
        private float hoverAnimation = 0;
        private boolean wasHovered = false;
        private float outlineAlpha = 0.0f;
        private RainbowUtil buttonRainbow = new RainbowUtil(10.0f, 0.8f, 1.0f);

        public ModernButton(int buttonId, int x, int y, int width, int height, String buttonText) {
            super(buttonId, x, y, width, height, buttonText);
        }

        public void updateRainbow() {
            buttonRainbow.update();
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                        mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

                // Smooth hover transition
                if (this.hovered && !wasHovered) {
                    outlineAlpha = 0.0f;
                }

                if (this.hovered) {
                    hoverAnimation = Math.min(1.0F, hoverAnimation + 0.08F);
                    outlineAlpha = Math.min(1.0F, outlineAlpha + 0.1F);
                } else {
                    hoverAnimation = Math.max(0.0F, hoverAnimation - 0.08F);
                    outlineAlpha = Math.max(0.0F, outlineAlpha - 0.06F);
                }

                wasHovered = this.hovered;

                drawRect(this.xPosition, this.yPosition, this.xPosition + this.width,
                        this.yPosition + this.height, DARK_GRAY);

                if (outlineAlpha > 0) {
                    drawRainbowOutline(this.xPosition, this.yPosition, this.width, this.height, outlineAlpha);
                }

                int textColor = this.enabled ? TEXT_GREEN : 0xFF555555;

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                Fonts.SF.drawCenteredStringWithShadow(this.displayString,
                        this.xPosition + this.width / 2,
                        this.yPosition + (this.height - 8) / 2, textColor);
            }
        }

        private void drawRainbowOutline(int x, int y, int width, int height, float alpha) {
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            int thickness = 1;
            Color rainbow1 = new Color(buttonRainbow.getRainbow());
            int segColor = new Color(rainbow1.getRed(), rainbow1.getGreen(), rainbow1.getBlue(),
                    (int)(alpha * 255)).getRGB();

            int segments = 8;
            int segLength = width / (segments / 2);

            for (int i = 0; i < segments / 2; i++) {
                int x1 = x + (i * segLength);
                int x2 = Math.min(x + width, x1 + segLength);
                drawRect(x1, y, x2, y + thickness, segColor);
            }

            for (int i = 0; i < segments / 4; i++) {
                int y1 = y + (i * (height / (segments/4)));
                int y2 = Math.min(y + height, y1 + (height / (segments/4)));
                drawRect(x + width - thickness, y1, x + width, y2, segColor);
            }

            for (int i = segments / 2 - 1; i >= 0; i--) {
                int x1 = x + (i * segLength);
                int x2 = Math.min(x + width, x1 + segLength);
                drawRect(x1, y + height - thickness, x2, y + height, segColor);
            }

            for (int i = segments / 4 - 1; i >= 0; i--) {
                int y1 = y + (i * (height / (segments/4)));
                int y2 = Math.min(y + height, y1 + (height / (segments/4)));
                drawRect(x, y1, x + thickness, y2, segColor);
            }

            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
        }
    }

    public static class CachedAccount {
        String username;
        String uuid;
        String token;
        long lastLoginTime;

        public CachedAccount(String username, String uuid, String token) {
            this(username, uuid, token, System.currentTimeMillis());
        }

        public CachedAccount(String username, String uuid, String token, long lastLoginTime) {
            this.username = username;
            this.uuid = uuid;
            this.token = token;
            this.lastLoginTime = lastLoginTime;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CachedAccount) {
                CachedAccount other = (CachedAccount) obj;
                return this.username.equals(other.username) && this.uuid.equals(other.uuid);
            }
            return false;
        }
    }
}