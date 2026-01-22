package com.jmonkeyvibe.game.input;

import com.jme3.input.InputManager;
import com.jme3.input.JoystickAxis;
import com.jme3.input.JoystickButton;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.input.Joystick;
import com.jme3.math.Vector2f;

/**
 * Manages gamepad/controller input for the game.
 * Supports Xbox-style controllers with:
 * - Left analog stick for movement
 * - Right analog stick for aiming
 * - Triggers and buttons for actions
 */
public class GamepadManager implements RawInputListener {

    // Deadzone threshold for analog sticks
    private static final float DEADZONE = 0.15f;

    // Current analog stick values
    private float leftStickX = 0f;
    private float leftStickY = 0f;
    private float rightStickX = 0f;
    private float rightStickY = 0f;

    // Button states
    private boolean aButtonPressed = false;
    private boolean bButtonPressed = false;
    private boolean xButtonPressed = false;
    private boolean yButtonPressed = false;
    private boolean leftBumperPressed = false;
    private boolean rightBumperPressed = false;
    private boolean startButtonPressed = false;
    private boolean backButtonPressed = false;
    private boolean dpadUpPressed = false;
    private boolean dpadDownPressed = false;
    private boolean dpadLeftPressed = false;
    private boolean dpadRightPressed = false;

    // Trigger values (0.0 to 1.0)
    private float leftTrigger = 0f;
    private float rightTrigger = 0f;

    // Listener for gamepad events
    private GamepadListener listener;

    // Whether a controller is connected
    private boolean controllerConnected = false;

    // Button state change flags for one-shot detection
    private boolean aButtonJustPressed = false;
    private boolean bButtonJustPressed = false;
    private boolean xButtonJustPressed = false;
    private boolean yButtonJustPressed = false;
    private boolean startButtonJustPressed = false;
    private boolean backButtonJustPressed = false;
    private boolean dpadUpJustPressed = false;
    private boolean dpadDownJustPressed = false;
    private boolean dpadLeftJustPressed = false;
    private boolean dpadRightJustPressed = false;
    private boolean rightTriggerJustPressed = false;

    private boolean rightTriggerWasPressed = false;

    public GamepadManager() {
    }

    /**
     * Initialize the gamepad manager with the input manager
     */
    public void initialize(InputManager inputManager) {
        // Add raw input listener to receive joystick events
        inputManager.addRawInputListener(this);

        // Check for connected joysticks
        Joystick[] joysticks = inputManager.getJoysticks();
        if (joysticks != null && joysticks.length > 0) {
            controllerConnected = true;
            System.out.println("Gamepad connected: " + joysticks[0].getName());

            // Log available axes and buttons for debugging
            for (Joystick joystick : joysticks) {
                System.out.println("  Joystick: " + joystick.getName());
                for (JoystickAxis axis : joystick.getAxes()) {
                    System.out.println("    Axis: " + axis.getName() + " (ID: " + axis.getAxisId() + ")");
                }
                for (JoystickButton button : joystick.getButtons()) {
                    System.out.println("    Button: " + button.getName() + " (ID: " + button.getButtonId() + ")");
                }
            }
        } else {
            System.out.println("No gamepad detected. Keyboard/mouse controls active.");
        }
    }

    /**
     * Set the listener for gamepad events
     */
    public void setListener(GamepadListener listener) {
        this.listener = listener;
    }

    /**
     * Get the left analog stick position
     * @return Vector2f with x (-1 to 1, left to right) and y (-1 to 1, down to up)
     */
    public Vector2f getLeftStick() {
        return new Vector2f(applyDeadzone(leftStickX), applyDeadzone(leftStickY));
    }

    /**
     * Get the right analog stick position
     * @return Vector2f with x (-1 to 1, left to right) and y (-1 to 1, down to up)
     */
    public Vector2f getRightStick() {
        return new Vector2f(applyDeadzone(rightStickX), applyDeadzone(rightStickY));
    }

    /**
     * Check if the right trigger is pressed (for firing)
     */
    public boolean isRightTriggerPressed() {
        return rightTrigger > 0.5f;
    }

    /**
     * Check if the right trigger was just pressed this frame
     */
    public boolean isRightTriggerJustPressed() {
        boolean result = rightTriggerJustPressed;
        rightTriggerJustPressed = false;
        return result;
    }

    /**
     * Check if the A button is pressed (interact)
     */
    public boolean isAButtonPressed() {
        return aButtonPressed;
    }

    /**
     * Check if the A button was just pressed
     */
    public boolean isAButtonJustPressed() {
        boolean result = aButtonJustPressed;
        aButtonJustPressed = false;
        return result;
    }

    /**
     * Check if the B button was just pressed
     */
    public boolean isBButtonJustPressed() {
        boolean result = bButtonJustPressed;
        bButtonJustPressed = false;
        return result;
    }

    /**
     * Check if the X button was just pressed
     */
    public boolean isXButtonJustPressed() {
        boolean result = xButtonJustPressed;
        xButtonJustPressed = false;
        return result;
    }

    /**
     * Check if the Y button was just pressed
     */
    public boolean isYButtonJustPressed() {
        boolean result = yButtonJustPressed;
        yButtonJustPressed = false;
        return result;
    }

    /**
     * Check if the start button was just pressed
     */
    public boolean isStartButtonJustPressed() {
        boolean result = startButtonJustPressed;
        startButtonJustPressed = false;
        return result;
    }

    /**
     * Check if the back/select button was just pressed
     */
    public boolean isBackButtonJustPressed() {
        boolean result = backButtonJustPressed;
        backButtonJustPressed = false;
        return result;
    }

    /**
     * Check if D-pad up was just pressed
     */
    public boolean isDpadUpJustPressed() {
        boolean result = dpadUpJustPressed;
        dpadUpJustPressed = false;
        return result;
    }

    /**
     * Check if D-pad down was just pressed
     */
    public boolean isDpadDownJustPressed() {
        boolean result = dpadDownJustPressed;
        dpadDownJustPressed = false;
        return result;
    }

    /**
     * Check if D-pad left was just pressed
     */
    public boolean isDpadLeftJustPressed() {
        boolean result = dpadLeftJustPressed;
        dpadLeftJustPressed = false;
        return result;
    }

    /**
     * Check if D-pad right was just pressed
     */
    public boolean isDpadRightJustPressed() {
        boolean result = dpadRightJustPressed;
        dpadRightJustPressed = false;
        return result;
    }

    /**
     * Check if a controller is connected
     */
    public boolean isControllerConnected() {
        return controllerConnected;
    }

    /**
     * Apply deadzone to analog stick value
     */
    private float applyDeadzone(float value) {
        if (Math.abs(value) < DEADZONE) {
            return 0f;
        }
        // Rescale to 0-1 range after deadzone
        float sign = Math.signum(value);
        return sign * (Math.abs(value) - DEADZONE) / (1f - DEADZONE);
    }

    // RawInputListener implementation

    @Override
    public void onJoyAxisEvent(JoyAxisEvent evt) {
        JoystickAxis axis = evt.getAxis();
        float value = evt.getValue();
        String axisName = axis.getName().toLowerCase();
        int axisId = axis.getAxisId();

        // Handle different axis types
        // Standard Xbox controller mapping:
        // Axis 0: Left Stick X
        // Axis 1: Left Stick Y
        // Axis 2: Right Stick X (or Left Trigger on some controllers)
        // Axis 3: Right Stick Y (or Right Trigger on some controllers)
        // Axis 4: Left Trigger
        // Axis 5: Right Trigger

        if (axisName.contains("x") && axisName.contains("left") || axisId == 0) {
            leftStickX = value;
        } else if (axisName.contains("y") && axisName.contains("left") || axisId == 1) {
            leftStickY = -value; // Invert Y axis
        } else if (axisName.contains("x") && axisName.contains("right") || axisId == 2) {
            rightStickX = value;
        } else if (axisName.contains("y") && axisName.contains("right") || axisId == 3) {
            rightStickY = -value; // Invert Y axis
        } else if (axisName.contains("trigger") && axisName.contains("left") || axisId == 4) {
            leftTrigger = (value + 1f) / 2f; // Convert from -1..1 to 0..1
        } else if (axisName.contains("trigger") && axisName.contains("right") || axisId == 5) {
            float newTrigger = (value + 1f) / 2f; // Convert from -1..1 to 0..1
            boolean nowPressed = newTrigger > 0.5f;
            if (nowPressed && !rightTriggerWasPressed) {
                rightTriggerJustPressed = true;
            }
            rightTriggerWasPressed = nowPressed;
            rightTrigger = newTrigger;
        } else if (axisName.contains("z") || axisName.contains("rz")) {
            // Alternative axis naming for triggers or right stick
            if (axisName.contains("rz")) {
                rightStickY = -value;
            } else {
                rightStickX = value;
            }
        } else if (axisName.contains("pov")) {
            // D-pad as axis (hat switch)
            handlePovAxis(value);
        }

        // Notify listener
        if (listener != null) {
            listener.onAxisChanged(axisId, value);
        }
    }

    @Override
    public void onJoyButtonEvent(JoyButtonEvent evt) {
        JoystickButton button = evt.getButton();
        boolean pressed = evt.isPressed();
        String buttonName = button.getName().toLowerCase();
        int buttonId = button.getButtonId();

        // Standard Xbox controller button mapping:
        // Button 0: A
        // Button 1: B
        // Button 2: X
        // Button 3: Y
        // Button 4: Left Bumper
        // Button 5: Right Bumper
        // Button 6: Back/Select
        // Button 7: Start
        // Button 8: Left Stick Click
        // Button 9: Right Stick Click
        // Button 10-13: D-pad (if not on axis)

        if (buttonName.contains("a") || buttonId == 0) {
            if (pressed && !aButtonPressed) aButtonJustPressed = true;
            aButtonPressed = pressed;
        } else if (buttonName.contains("b") || buttonId == 1) {
            if (pressed && !bButtonPressed) bButtonJustPressed = true;
            bButtonPressed = pressed;
        } else if (buttonName.contains("x") || buttonId == 2) {
            if (pressed && !xButtonPressed) xButtonJustPressed = true;
            xButtonPressed = pressed;
        } else if (buttonName.contains("y") || buttonId == 3) {
            if (pressed && !yButtonPressed) yButtonJustPressed = true;
            yButtonPressed = pressed;
        } else if (buttonName.contains("left") && buttonName.contains("bumper") || buttonId == 4) {
            leftBumperPressed = pressed;
        } else if (buttonName.contains("right") && buttonName.contains("bumper") || buttonId == 5) {
            rightBumperPressed = pressed;
            // Also use right bumper as fire alternative
            if (pressed && !rightTriggerWasPressed) {
                rightTriggerJustPressed = true;
            }
            rightTriggerWasPressed = pressed;
        } else if (buttonName.contains("back") || buttonName.contains("select") || buttonId == 6) {
            if (pressed && !backButtonPressed) backButtonJustPressed = true;
            backButtonPressed = pressed;
        } else if (buttonName.contains("start") || buttonId == 7) {
            if (pressed && !startButtonPressed) startButtonJustPressed = true;
            startButtonPressed = pressed;
        } else if (buttonName.contains("up") || buttonId == 10) {
            if (pressed && !dpadUpPressed) dpadUpJustPressed = true;
            dpadUpPressed = pressed;
        } else if (buttonName.contains("down") || buttonId == 11) {
            if (pressed && !dpadDownPressed) dpadDownJustPressed = true;
            dpadDownPressed = pressed;
        } else if (buttonName.contains("left") || buttonId == 12) {
            if (pressed && !dpadLeftPressed) dpadLeftJustPressed = true;
            dpadLeftPressed = pressed;
        } else if (buttonName.contains("right") || buttonId == 13) {
            if (pressed && !dpadRightPressed) dpadRightJustPressed = true;
            dpadRightPressed = pressed;
        }

        // Notify listener
        if (listener != null) {
            listener.onButtonChanged(buttonId, pressed);
        }
    }

    /**
     * Handle POV/hat switch axis for D-pad
     */
    private void handlePovAxis(float value) {
        // POV values: 0.25=up, 0.5=right, 0.75=down, 1.0=left, 0=center
        // Some controllers use different values
        boolean wasUp = dpadUpPressed;
        boolean wasDown = dpadDownPressed;
        boolean wasLeft = dpadLeftPressed;
        boolean wasRight = dpadRightPressed;

        dpadUpPressed = false;
        dpadDownPressed = false;
        dpadLeftPressed = false;
        dpadRightPressed = false;

        if (value >= 0.125f && value <= 0.375f) {
            dpadUpPressed = true;
            if (!wasUp) dpadUpJustPressed = true;
        } else if (value >= 0.375f && value <= 0.625f) {
            dpadRightPressed = true;
            if (!wasRight) dpadRightJustPressed = true;
        } else if (value >= 0.625f && value <= 0.875f) {
            dpadDownPressed = true;
            if (!wasDown) dpadDownJustPressed = true;
        } else if (value >= 0.875f || (value > 0 && value <= 0.125f)) {
            dpadLeftPressed = true;
            if (!wasLeft) dpadLeftJustPressed = true;
        }
    }

    @Override
    public void beginInput() {}

    @Override
    public void endInput() {}

    @Override
    public void onKeyEvent(KeyInputEvent evt) {}

    @Override
    public void onMouseMotionEvent(MouseMotionEvent evt) {}

    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt) {}

    @Override
    public void onTouchEvent(TouchEvent evt) {}

    /**
     * Interface for listening to gamepad events
     */
    public interface GamepadListener {
        void onAxisChanged(int axisId, float value);
        void onButtonChanged(int buttonId, boolean pressed);
    }
}
