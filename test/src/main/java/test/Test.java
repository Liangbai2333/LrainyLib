package test;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import site.liangbai.lrainylib.core.annotation.CommandHandler;
import site.liangbai.lrainylib.core.annotation.Plugin;
import site.liangbai.lrainylib.core.annotation.plugin.Info;

@Plugin(
        info = @Info(name = "name", version = "1.0.0", authors = "Liangbai"),
        depend = {"one", "two"}
)
@Plugin.EventSubscriber
@CommandHandler("your cmd")
public class Test extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("OK");
    }


    public Test() {
        System.out.println(1);
    }

    public void e () {
        System.out.println();
    }
}
