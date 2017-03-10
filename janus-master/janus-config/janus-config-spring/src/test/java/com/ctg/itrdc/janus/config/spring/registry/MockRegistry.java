package com.ctg.itrdc.janus.config.spring.registry;

import java.util.ArrayList;
import java.util.List;

import com.ctg.itrdc.janus.common.URL;
import com.ctg.itrdc.janus.registry.NotifyListener;
import com.ctg.itrdc.janus.registry.Registry;

/**
 * @author <a href="mailto:gang.lvg@taobao.com">kimi</a>
 */
public class MockRegistry implements Registry {

    private URL url;

    private List<URL> registered = new ArrayList<URL>();

    private List<URL> subscribered = new ArrayList<URL>();

    public List<URL> getRegistered() {
        return registered;
    }

    public List<URL> getSubscribered() {
        return subscribered;
    }

    public MockRegistry(URL url) {
        if (url == null) {
            throw new NullPointerException();
        }
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    public boolean isAvailable() {
        return true;
    }

    public void destroy() {

    }

    public void register(URL url) {
        registered.add(url);
    }

    public void unregister(URL url) {
        registered.remove(url);
    }

    public void subscribe(URL url, NotifyListener listener) {
        subscribered.add(url);
    }

    public void unsubscribe(URL url, NotifyListener listener) {
        subscribered.remove(url);
    }

    public List<URL> lookup(URL url) {
        return null;
    }
}
