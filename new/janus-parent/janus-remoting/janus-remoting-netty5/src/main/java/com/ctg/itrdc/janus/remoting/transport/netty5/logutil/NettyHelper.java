package com.ctg.itrdc.janus.remoting.transport.netty5.logutil;

import com.ctg.itrdc.janus.common.logger.Logger;
import com.ctg.itrdc.janus.common.logger.LoggerFactory;
import io.netty.util.internal.logging.AbstractInternalLogger;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;


public class NettyHelper {
    public static void setNettyLoggerFactory() {
        InternalLoggerFactory factory = InternalLoggerFactory.getDefaultFactory();
        if (factory == null || !(factory instanceof JanusLoggerFactory)) {
            InternalLoggerFactory.setDefaultFactory(new JanusLoggerFactory());
        }
    }

    static class JanusLoggerFactory extends InternalLoggerFactory {
        @Override
        public InternalLogger newInstance(String name) {
            return new JanusLogger(LoggerFactory.getLogger(name), name);
        }
    }

    static class JanusLogger extends AbstractInternalLogger {

        private Logger logger;

        JanusLogger(Logger logger, String name) {
            super(name);
            this.logger = logger;
        }

        @Override
        public boolean isTraceEnabled() {
            return logger.isTraceEnabled();
        }

        @Override
        public void trace(String msg) {
            logger.trace(msg);
        }

        @Override
        public void trace(String format, Object arg) {
            if (logger.isTraceEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, arg);
                logger.trace(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void trace(String format, Object argA, Object argB) {
            if (logger.isTraceEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, argA, argB);
                logger.trace(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void trace(String format, Object... arguments) {
            if (logger.isTraceEnabled()) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
                logger.trace(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void trace(String msg, Throwable t) {
            logger.trace(msg, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public void debug(String msg) {
            logger.debug(msg);
        }

        @Override
        public void debug(String format, Object arg) {
            if (logger.isDebugEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, arg);
                logger.debug(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void debug(String format, Object argA, Object argB) {
            if (logger.isDebugEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, argA, argB);
                logger.debug(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void debug(String format, Object... arguments) {
            if (logger.isDebugEnabled()) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
                logger.debug(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void debug(String msg, Throwable t) {
            logger.debug(msg, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        @Override
        public void info(String msg) {
            logger.info(msg);
        }

        @Override
        public void info(String format, Object arg) {
            if (logger.isInfoEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, arg);
                logger.info(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void info(String format, Object argA, Object argB) {
            if (logger.isInfoEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, argA, argB);
                logger.info(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void info(String format, Object... arguments) {
            if (logger.isInfoEnabled()) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
                logger.info(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void info(String msg, Throwable t) {
            logger.info(msg, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        @Override
        public void warn(String msg) {
            logger.warn(msg);
        }

        @Override
        public void warn(String format, Object arg) {
            if (logger.isWarnEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, arg);
                logger.warn(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void warn(String format, Object... arguments) {
            if (logger.isWarnEnabled()) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
                logger.warn(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void warn(String format, Object argA, Object argB) {
            if (logger.isWarnEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, argA, argB);
                logger.warn(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void warn(String msg, Throwable t) {
            logger.warn(msg, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return logger.isErrorEnabled();
        }

        @Override
        public void error(String msg) {
            logger.error(msg);
        }

        @Override
        public void error(String format, Object arg) {
            if (logger.isErrorEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, arg);
                logger.error(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void error(String format, Object argA, Object argB) {
            if (logger.isErrorEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, argA, argB);
                logger.error(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void error(String format, Object... arguments) {
            if (logger.isErrorEnabled()) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
                logger.error(ft.getMessage(), ft.getThrowable());
            }
        }

        @Override
        public void error(String msg, Throwable t) {
            logger.error(msg, t);
        }
    }

    //iundo 测试用
    public static void printThreadInfo(Thread thread) {
        System.out.println("Thread is tid:" + thread.getId() + " tname:" + thread.getName() + " all:" + thread);
    }
}
