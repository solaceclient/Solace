package huysuh.Font;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import huysuh.Font.CFontRenderer;

import java.awt.*;
import java.io.InputStream;

public abstract class Fonts {

    public static final CFontRenderer SF = new CFontRenderer(Fonts.getFonts("sf.ttf", 22), true, true);
    public static final CFontRenderer Arial = new CFontRenderer(Fonts.getFonts("ARIAL.ttf", 20), true, true);

    private static Font getFonts(String fontName, int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("solace/fonts/" + fontName)).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(0, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading " + fontName);
            font = new Font("default", 0, size);
        }
        return font;
    }
}