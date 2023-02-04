package emanondev.minigames;

import org.bukkit.Color;
import org.bukkit.DyeColor;

public class UtilColor {

    public static String getColorHexa(DyeColor color) {
        return getColorHexa(color.getColor());
    }

    public static String getColorHexa(Color color) {
        return "#" + (color.getRed() < 16 ? "0" : "") + Integer.toHexString(color.getRed()) +
                (color.getGreen() < 16 ? "0" : "") + Integer.toHexString(color.getGreen()) +
                (color.getBlue() < 16 ? "0" : "") + Integer.toHexString(color.getBlue());
    }
}
