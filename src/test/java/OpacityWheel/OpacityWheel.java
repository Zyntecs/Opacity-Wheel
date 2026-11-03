package OpacityWheel;
import com.OpacityWheel.OpacityWheelPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class OpacityWheel
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(OpacityWheelPlugin.class);
		RuneLite.main(args);
	}
}