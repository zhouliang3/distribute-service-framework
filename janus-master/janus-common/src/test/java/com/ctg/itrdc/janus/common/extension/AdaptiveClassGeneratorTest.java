/**
 * 
 */
package com.ctg.itrdc.janus.common.extension;

import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Test;

import com.ctg.itrdc.janus.common.extensionloader.ext1.SimpleExt;
import com.ctg.itrdc.janus.common.extensionloader.ext10_exception.Ext10;
import com.ctg.itrdc.janus.common.extensionloader.ext9_noadaptive.Ext9;

/**
 * @author Administrator
 */
public class AdaptiveClassGeneratorTest {

	@Test
	public void testCreateAdaptiveExtensionClassCode() {

		String code = AdaptiveClassGenerator.createAdaptiveExtensionClassCode(
				SimpleExt.class, "impl1");
		System.out.println(code);
	}

	@Test
	public void testNoAdaptive() {
		try {
			AdaptiveClassGenerator.createAdaptiveExtensionClassCode(Ext9.class,
					"impl1");
			fail();
		} catch (IllegalStateException expected) {
			assertThat(
					expected.getMessage(),
					allOf(containsString("No adaptive method on extension"),
							containsString(Ext9.class.getName())));
		}
	}

	@Test
	public void testHasException() {
		String code = AdaptiveClassGenerator.createAdaptiveExtensionClassCode(
				Ext10.class, "impl1");
		assertThat(
				code,
				allOf(containsString("ArrayIndexOutOfBoundsException"),
						containsString(Ext10.class.getName())));
	}
}
