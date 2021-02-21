package test;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import site.liangbai.lrainylib.annotation.command.CommandHandler;
import site.liangbai.lrainylib.annotation.Plugin;
import site.liangbai.lrainylib.annotation.plugin.Info;

@Plugin(
        info = @Info(name = "Test", version = "1.0.0", authors = "Liangbai"),
        depend = {"one", "two"}
)
@Plugin.EventSubscriber
@CommandHandler("your cmd")
public class Test extends JavaPlugin implements Listener {
    private static final Test plugin = null;

    @Override
    public void onEnable() {
        getLogger().info("OK");
    }
}
