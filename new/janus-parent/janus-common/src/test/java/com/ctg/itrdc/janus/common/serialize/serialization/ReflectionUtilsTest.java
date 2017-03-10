package com.ctg.itrdc.janus.common.serialize.serialization;

import com.ctg.itrdc.janus.common.serialize.support.kryo.ReflectionUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;



/**
 * @author lishen
 */
public class ReflectionUtilsTest {

    @Test
    public void test() {
        assertTrue(ReflectionUtils.checkZeroArgConstructor(String.class));
        assertTrue(ReflectionUtils.checkZeroArgConstructor(Bar.class));
        assertFalse(ReflectionUtils.checkZeroArgConstructor(Foo.class));
    }

    static class Foo {
        public Foo(int i) {

        }
    }

    static class Bar {
        private Bar() {

        }
    }
}
