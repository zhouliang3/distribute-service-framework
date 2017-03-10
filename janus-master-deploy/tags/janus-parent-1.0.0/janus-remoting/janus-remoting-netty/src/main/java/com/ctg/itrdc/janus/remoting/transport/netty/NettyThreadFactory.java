package com.ctg.itrdc.janus.remoting.transport.netty;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NettyThreadFactory implements ThreadFactory {

    private final String pattern;
    private final String name;
    private final boolean daemon;

    public NettyThreadFactory(String pattern, String name, boolean daemon) {
        this.pattern = pattern;
        this.name = name;
        this.daemon = daemon;
    }

    
    public static final String DEFAULT_PATTERN = "Janus Thread ##counter# - #name#";
    private static final Pattern INVALID_PATTERN = Pattern.compile(".*#\\w+#.*");

    private static AtomicLong threadCounter = new AtomicLong();

    public Thread newThread(Runnable runnable) {
        String threadName = resolveThreadName(pattern, name);
        Thread answer = new Thread(runnable, threadName);
        answer.setDaemon(daemon);

        return answer;
    }

    public String getName() {
        return name;
    }
    
    public static String before(String text, String before) {
        if (!text.contains(before)) {
            return null;
        }
        return text.substring(0, text.indexOf(before));
    }
    
    private static long nextThreadCounter() {
        return threadCounter.getAndIncrement();
    }
    
    public static String resolveThreadName(String pattern, String name) {
        if (pattern == null) {
            pattern = DEFAULT_PATTERN;
        }

        String longName = name;
        String shortName = name.contains("?") ? before(name, "?") : name;
        shortName = Matcher.quoteReplacement(shortName);
        longName = Matcher.quoteReplacement(longName);

        String answer = pattern.replaceFirst("#counter#", "" + nextThreadCounter());
        answer = answer.replaceFirst("#longName#", longName);
        answer = answer.replaceFirst("#name#", shortName);

        if (INVALID_PATTERN.matcher(answer).matches()) {
            throw new IllegalArgumentException("Pattern is invalid: " + pattern);
        }

        return answer;
    }

    public String toString() {
        return "com.ctg.itrdc.janus.remoting.transport.netty.NettyThreadFactory[" + name + "]";
    }
}