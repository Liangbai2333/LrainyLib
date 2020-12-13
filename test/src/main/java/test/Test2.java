package test;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import site.liangbai.lrainylib.core.annotation.Plugin;
import site.liangbai.lrainylib.core.annotation.Service;

@Plugin.EventSubscriber
public class Test2 implements Listener {
    @Service.ServiceProviderInstance(classFullName = "test.Test")
    private static Object e;
    @Plugin.Instance
    private static Test test;

    @EventHandler
    public void onLoad(PluginEnableEvent event) {
        System.out.println(event.getPlugin().getName());
    }
}
