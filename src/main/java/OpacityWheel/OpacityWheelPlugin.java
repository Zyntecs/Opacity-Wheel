
package com.OpacityWheel;

import com.google.inject.Provides;

import java.awt.Canvas;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;
import net.runelite.client.input.MouseWheelListener;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
		name = "Opacity Wheel",
		description = "Adjust RuneLite window opacity using configurable modifiers and scrolling"
)
public class OpacityWheelPlugin extends Plugin
{
	private static final Logger log =
			LoggerFactory.getLogger(OpacityWheelPlugin.class);

	private static final int BUTTON_4 = 4;
	private static final int BUTTON_5 = 5;

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OpacityWheelConfig config;

	@Inject
	private RuneLiteConfig runeLiteConfig;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private KeyManager keyManager;

	private Canvas canvas;

	private final boolean[] buttonsHeld = new boolean[6];

	private boolean ctrlHeld;
	private boolean shiftHeld;
	private boolean altHeld;

	// Mouse button detection
	private final MouseAdapter mouseListener = new MouseAdapter()
	{
		@Override
		public MouseEvent mousePressed(MouseEvent event)
		{
			int button = event.getButton();

			log.info("Opacity Wheel: Mouse pressed: {}", button);

			if (button == BUTTON_4 || button == BUTTON_5)
			{
				buttonsHeld[button] = true;
			}

			return event;
		}

		@Override
		public MouseEvent mouseReleased(MouseEvent event)
		{
			int button = event.getButton();

			log.info("Opacity Wheel: Mouse released: {}", button);

			if (button == BUTTON_4 || button == BUTTON_5)
			{
				buttonsHeld[button] = false;
			}

			return event;
		}
	};

	// Keyboard modifier detection
	private final KeyListener keyListener = new KeyListener()
	{
		@Override
		public void keyPressed(KeyEvent event)
		{
			updateKey(event, true);
		}

		@Override
		public void keyReleased(KeyEvent event)
		{
			updateKey(event, false);
		}

		@Override
		public void keyTyped(KeyEvent event)
		{
			// Not needed.
		}
	};

	// Scroll wheel detection
	private final MouseWheelListener wheelListener =
			new MouseWheelListener()
			{
				@Override
				public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event)
				{
					int rotation = event.getWheelRotation();

					log.info(
							"Opacity Wheel: Scroll detected. Rotation: {}, Modifier held: {}",
							rotation,
							isModifierHeld()
					);

					if (!config.enabled())
					{
						return event;
					}

					if (!isModifierHeld())
					{
						return event;
					}

					if (rotation == 0)
					{
						return event;
					}

					log.info("Opacity Wheel: Modifier detected. Adjusting opacity.");

					// Scroll up increases opacity.
					// Scroll down decreases opacity.
					changeOpacity(-rotation);

					event.consume();

					return event;
				}
			};

	// Clear modifiers when RuneLite loses focus
	private final FocusAdapter focusListener = new FocusAdapter()
	{
		@Override
		public void focusLost(FocusEvent event)
		{
			clearModifiers();
		}
	};

	@Provides
	OpacityWheelConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(OpacityWheelConfig.class);
	}

	@Override
	protected void startUp()
	{
		canvas = client.getCanvas();

		mouseManager.registerMouseListener(mouseListener);
		mouseManager.registerMouseWheelListener(wheelListener);
		keyManager.registerKeyListener(keyListener);

		canvas.addFocusListener(focusListener);

		log.info("Opacity Wheel: Plugin started successfully.");
	}

	@Override
	protected void shutDown()
	{
		mouseManager.unregisterMouseListener(mouseListener);
		mouseManager.unregisterMouseWheelListener(wheelListener);
		keyManager.unregisterKeyListener(keyListener);

		if (canvas != null)
		{
			canvas.removeFocusListener(focusListener);
		}

		clearModifiers();

		log.info("Opacity Wheel: Plugin stopped.");
	}

	// Update keyboard modifier states
	private void updateKey(KeyEvent event, boolean pressed)
	{
		switch (event.getKeyCode())
		{
			case KeyEvent.VK_CONTROL:
				ctrlHeld = pressed;
				break;

			case KeyEvent.VK_SHIFT:
				shiftHeld = pressed;
				break;

			case KeyEvent.VK_ALT:
				altHeld = pressed;
				break;

			default:
				break;
		}

		if (pressed)
		{
			log.info("Opacity Wheel: Keyboard modifier pressed: {}", event.getKeyCode());
		}
	}

	// Check whether the configured modifier is held
	private boolean isModifierHeld()
	{
		switch (config.modifier())
		{
			case MOUSE_BUTTON_4:
				return buttonsHeld[BUTTON_4];

			case MOUSE_BUTTON_5:
				return buttonsHeld[BUTTON_5];

			case CTRL:
				return ctrlHeld;

			case SHIFT:
				return shiftHeld;

			case ALT:
				return altHeld;

			default:
				return false;
		}
	}

	// Change RuneLite's actual Window Opacity setting
	private void changeOpacity(int amount)
	{
		Integer currentOpacity = configManager.getConfiguration(
				"runelite",
				"uiWindowOpacity",
				Integer.class
		);

		int opacity = currentOpacity == null ? 100 : currentOpacity;

		int updatedOpacity = Math.max(
				10,
				Math.min(100, opacity + amount)
		);

		if (updatedOpacity == opacity)
		{
			return;
		}

		log.info(
				"Opacity Wheel: Changing opacity from {}% to {}%",
				opacity,
				updatedOpacity
		);

		configManager.setConfiguration(
				"runelite",
				"uiWindowOpacity",
				updatedOpacity
		);
	}

	// Reset all held modifiers
	private void clearModifiers()
	{
		buttonsHeld[BUTTON_4] = false;
		buttonsHeld[BUTTON_5] = false;

		ctrlHeld = false;
		shiftHeld = false;
		altHeld = false;
	}
}
