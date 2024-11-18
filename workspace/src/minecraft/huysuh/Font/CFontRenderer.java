package huysuh.Font;

import huysuh.Font.CFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CFontRenderer extends CFont {
    public static Random random = new Random();
    protected CFont.CharData[] boldChars = new CFont.CharData[256];
    protected CFont.CharData[] italicChars = new CFont.CharData[256];
    protected CFont.CharData[] boldItalicChars = new CFont.CharData[256];
    protected DynamicTexture texBold;

    protected DynamicTexture texItalic;

    protected DynamicTexture texItalicBold;
    private final int[] colorCode = new int[32];

    public CFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        super(font, antiAlias, fractionalMetrics);
        setupMinecraftColorcodes();
        setupBoldItalicIDs();
    }

    public float drawString(String text, float x, float y, int color) {
        return drawString(text, x, y, color, false);
    }

    public float drawString(String text, double x, double y, int color) {
        return drawString(text, x, y, color, false);
    }

    public float drawStringWithShadow(String text, float x, float y, int color) {
        float shadowWidth = drawString(text, x + 0.5D, y + 0.5D, color, true);
        return Math.max(shadowWidth, drawString(text, x, y, color, false));
    }

    public float drawStringWithShadow(String text, double x, double y, int color) {
        float shadowWidth = drawString(text, x + 0.5D, y + 0.5D, color, true);
        return Math.max(shadowWidth, drawString(text, x, y, color, false));
    }

    public float drawCenteredString(String text, float x, float y, int color) {
        return drawString(text, x - (getStringWidth(text) / 2), y, color);
    }

    public float drawCenteredString(String text, double x, double y, int color) {
        return drawString(text, x - (getStringWidth(text) / 2), y, color);
    }

    public float drawCenteredStringWithShadow(String text, float x, float y, int color) {
        return drawStringWithShadow(text, x - (getStringWidth(text) / 2), y, color);
    }

    public float drawCenteredStringWithShadow(String text, double x, double y, int color) {
        return drawString(text, x - (getStringWidth(text) / 2), y, color);
    }

    public void drawHeightCenteredString(String text, float x, float y, int color) {
        drawString(text, (int) x, (int) (y - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 2f), color);
    }

    public void drawHeightCenteredStringWithShadow(String text, float x, float y, int color) {
        drawStringWithShadow(text, (int) x, (int) (y - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 2f), color);
    }

    public float drawString(String text, double x, double y, int color, boolean shadow) {
        GlStateManager.pushMatrix();
        x--;
        if (text == null)
            return 0.0F;
        if (color == 553648127)
            color = 16777215;
        if ((color & 0xFC000000) == 0)
            color |= 0xFF000000;
        if (shadow)
            color = (color & 0xFCFCFC) >> 2 | color & (new Color(14, 14, 14, 150)).getRGB();
        CFont.CharData[] currentData = this.charData;
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        boolean randomCase = false;
        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;
        boolean underline = false;
        x *= 2.0D;
        y = (y - 3.0D) * 2.0D;
        GL11.glPushMatrix();
        GlStateManager.scale(0.5D, 0.5D, 0.5D);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, alpha);
        int size = text.length();
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(this.tex.getGlTextureId());
        GL11.glBindTexture(3553, this.tex.getGlTextureId());
        for (int i = 0; i < size; i++) {
            char character = text.charAt(i);
            if (String.valueOf(character).equals("§") && i < size) {
                int colorIndex = 21;
                try {
                    colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                    randomCase = false;
                    underline = false;
                    strikethrough = false;
                    GlStateManager.bindTexture(this.tex.getGlTextureId());
                    currentData = this.charData;
                    if (colorIndex < 0)
                        colorIndex = 15;
                    if (shadow)
                        colorIndex += 16;
                    int colorcode = this.colorCode[colorIndex];
                    GlStateManager.color((colorcode >> 16 & 0xFF) / 255.0F, (colorcode >> 8 & 0xFF) / 255.0F, (colorcode & 0xFF) / 255.0F, alpha);
                } else if (colorIndex == 16) {
                    randomCase = true;
                } else if (colorIndex == 17) {
                    bold = true;
                    if (italic) {
                        GlStateManager.bindTexture(this.texItalicBold.getGlTextureId());
                        currentData = this.boldItalicChars;
                    } else {
                        GlStateManager.bindTexture(this.texBold.getGlTextureId());
                        currentData = this.boldChars;
                    }
                } else if (colorIndex == 18) {
                    strikethrough = true;
                } else if (colorIndex == 19) {
                    underline = true;
                } else if (colorIndex == 20) {
                    italic = true;
                    if (bold) {
                        GlStateManager.bindTexture(this.texItalicBold.getGlTextureId());
                        currentData = this.boldItalicChars;
                    } else {
                        GlStateManager.bindTexture(this.texItalic.getGlTextureId());
                        currentData = this.italicChars;
                    }
                } else {
                    bold = false;
                    italic = false;
                    randomCase = false;
                    underline = false;
                    strikethrough = false;
                    currentData = this.charData;
                    GlStateManager.color((color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, alpha);
                    GlStateManager.bindTexture(this.tex.getGlTextureId());
                }
                i++;
            } else if (character < currentData.length) {
                if (randomCase)
                    character = character;
                GL11.glBegin(4);
                drawChar(currentData, character, (float) x, (float) y);
                GL11.glEnd();
                if (strikethrough)
                    drawLine(x, y + ((currentData[character]).height / 2), x + (currentData[character]).width - 8.0D, y + ((currentData[character]).height / 2), 1.0F);
                if (underline)
                    drawLine(x, y + (currentData[character]).height - 2.0D, x + (currentData[character]).width - 8.0D, y + (currentData[character]).height - 2.0D, 1.0F);
                x += ((currentData[character]).width - 8 + this.charOffset);
            }
        }
        GL11.glHint(3155, 4352);
        GL11.glPopMatrix();
        GlStateManager.popMatrix();
        return (float) x / 2.0F;
    }

    public int getStringWidth(String text) {
        if (text == null)
            return 0;
        text = EnumChatFormatting.getTextWithoutFormattingCodes(text);
        int width = 0;
        CFont.CharData[] currentData = this.charData;
        int size = text.length();
        for (int i = 0; i < size; i++) {
            char character = text.charAt(i);
            if (character < currentData.length)
                width += (currentData[character]).width - 8 + this.charOffset;
        }
        return width / 2;
    }

    public void setFont(Font font) {
        super.setFont(font);
        setupBoldItalicIDs();
    }

    public void setAntiAlias(boolean antiAlias) {
        super.setAntiAlias(antiAlias);
        setupBoldItalicIDs();
    }

    public void setFractionalMetrics(boolean fractionalMetrics) {
        super.setFractionalMetrics(fractionalMetrics);
        setupBoldItalicIDs();
    }

    private void setupBoldItalicIDs() {
        this.texBold = setupTexture(this.font.deriveFont(1), this.antiAlias, this.fractionalMetrics, this.boldChars);
        this.texItalic = setupTexture(this.font.deriveFont(2), this.antiAlias, this.fractionalMetrics, this.italicChars);
        this.texItalicBold = setupTexture(this.font.deriveFont(3), this.antiAlias, this.fractionalMetrics, this.boldItalicChars);
    }

    public String trimStringToWidth(String text, int width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        float f = 0.0F;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < (float) width; k += j) {
            char c0 = text.charAt(k);
            float f1 = this.getStringWidthCust(Character.toString(c0));

            if (flag) {
                flag = false;

                if (c0 != 'l' && c0 != 'L') {
                    if (c0 == 'r' || c0 == 'R') {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (f1 < 0.0F) {
                flag = true;
            } else {
                f += f1;

                if (flag1) {
                    ++f;
                }
            }

            if (f > (float) width) {
                break;
            }

            if (reverse) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    public int getStringWidthCust(String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        CFont.CharData[] currentData = this.charData;
        boolean bold = false;
        boolean italic = false;
        int size = text.length();
        for (int i = 0; i < size; ++i) {
            char character = text.charAt(i);
            if (String.valueOf(character).equals("\u00a7") && i < size) {
                int colorIndex = "0123456789abcdefklmnor".indexOf(character);
                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                } else if (colorIndex == 17) {
                    bold = true;
                    currentData = italic ? this.boldItalicChars : this.boldChars;
                } else if (colorIndex == 20) {
                    italic = true;
                    currentData = bold ? this.boldItalicChars : this.italicChars;
                } else if (colorIndex == 21) {
                    bold = false;
                    italic = false;
                    currentData = this.charData;
                }
                ++i;
                continue;
            }
            if (character >= currentData.length || character < '\u0000') continue;
            width += currentData[character].width - 8 + this.charOffset;
        }
        return (width - this.charOffset) / 2;
    }

    private void drawLine(double x, double y, double x1, double y1, float width) {
        GL11.glDisable(3553);
        GL11.glLineWidth(width);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x1, y1);
        GL11.glEnd();
        GL11.glEnable(3553);
    }

    public List<String> formatString(String string, double width) {
        ArrayList<String> finalWords = new ArrayList<>();
        String currentWord = "";
        char lastColorCode = Character.MAX_VALUE;
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (String.valueOf(c).equals("§") && i < chars.length - 1)
                lastColorCode = chars[i + 1];
            StringBuilder stringBuilder = new StringBuilder();
            if (getStringWidth(stringBuilder.append(currentWord).append(c).toString()) < width) {
                currentWord = currentWord + c;
            } else {
                finalWords.add(currentWord);
                currentWord = String.valueOf(lastColorCode) + c;
            }
        }
        if (currentWord.length() > 0)
            finalWords.add(currentWord);
        return finalWords;
    }

    private void setupMinecraftColorcodes() {
        for (int index = 0; index < 32; index++) {
            int noClue = (index >> 3 & 0x1) * 85;
            int red = (index >> 2 & 0x1) * 170 + noClue;
            int green = (index >> 1 & 0x1) * 170 + noClue;
            int blue = (index >> 0 & 0x1) * 170 + noClue;
            if (index == 6)
                red += 85;
            if (index >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            this.colorCode[index] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
        }
    }
}