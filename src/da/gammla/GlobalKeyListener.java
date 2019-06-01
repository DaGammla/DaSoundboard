package da.gammla;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalKeyListener implements NativeKeyListener {

    boolean[] pressed = new boolean[214748364];

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeEvent) {

    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        if (pressed[nativeEvent.getKeyCode()] != true) {
            Main.gui.buttonPressed(nativeEvent.getKeyCode());
            pressed[nativeEvent.getKeyCode()] = true;
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
        pressed[nativeEvent.getKeyCode()] = false;
    }
}
