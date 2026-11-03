
package com.OpacityWheel;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("opacitywheel")
public interface OpacityWheelConfig extends Config
{
	enum Modifier
	{
		MOUSE_BUTTON_4,
		MOUSE_BUTTON_5,
		CTRL,
		SHIFT,
		ALT
	}

	@ConfigItem(
			keyName = "enabled",
			name = "Enable plugin",
			description = "Enable or disable opacity controls"
	)
	default boolean enabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "modifier",
			name = "Modifier hotkey",
			description = "Choose which button or keyboard key to hold while scrolling"
	)
	default Modifier modifier()
	{
		return Modifier.MOUSE_BUTTON_5;
	}
}
