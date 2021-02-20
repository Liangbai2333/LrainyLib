package test;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import site.liangbai.lrainylib.annotation.Plugin;

@Plugin.EventSubscriber
public class Test2 implements Listener {
    @Plugin.Instance
    private static Test test;

    @EventHandler
    public void onLoad(PluginEnableEvent event) {
        System.out.println(event.getPlugin().getName());
    }
}
