package lightcontroller.serial;

import lightcontroller.Mode;

public class CommandEncoder
{
    public static String setState(boolean on)
    {
        return "sta " + (on ? "1" : "0");
    }

    public static String setMode(Mode mode)
    {
        return "mod " + mode.ordinal();
    }

    public static String setBrightness(double brightness)
    {
        return "bri " + brightness;
    }

    public static String setWhite(boolean white)
    {
        return "whi " + (white ? "1" : "0");
    }

    public static String setModeSetting(Mode mode, String setting, int value)
    {
        return "set " + mode.ordinal() + " " + setting + " " + value;
    }
}
