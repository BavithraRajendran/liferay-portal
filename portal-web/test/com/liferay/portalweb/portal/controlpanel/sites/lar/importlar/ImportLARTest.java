/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portalweb.portal.controlpanel.sites.lar.importlar;

import com.liferay.portalweb.portal.BaseTestCase;
import com.liferay.portalweb.portal.util.RuntimeVariables;

/**
 * @author Brian Wing Shun Chan
 */
public class ImportLARTest extends BaseTestCase {
	public void testImportLAR() throws Exception {
		selenium.open("/web/guest/home/");

		for (int second = 0;; second++) {
			if (second >= 90) {
				fail("timeout");
			}

			try {
				if (selenium.isElementPresent("link=Control Panel")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.clickAt("link=Control Panel",
			RuntimeVariables.replace("Control Panel"));
		selenium.waitForPageToLoad("30000");
		selenium.clickAt("link=Sites", RuntimeVariables.replace("Sites"));
		selenium.waitForPageToLoad("30000");
		selenium.type("//input[@id='_134_name']",
			RuntimeVariables.replace("Community Name"));
		selenium.clickAt("//input[@value='Search']",
			RuntimeVariables.replace("Search"));
		selenium.waitForPageToLoad("30000");
		assertEquals(RuntimeVariables.replace("Actions"),
			selenium.getText("//td[7]/span/ul/li/strong/a/span"));
		selenium.clickAt("//td[7]/span/ul/li/strong/a/span",
			RuntimeVariables.replace("Actions"));

		for (int second = 0;; second++) {
			if (second >= 90) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible(
							"//div[@class='lfr-component lfr-menu-list']/ul/li[2]/a")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		assertEquals(RuntimeVariables.replace("Manage Pages"),
			selenium.getText(
				"//div[@class='lfr-component lfr-menu-list']/ul/li[2]/a"));
		selenium.click(RuntimeVariables.replace(
				"//div[@class='lfr-component lfr-menu-list']/ul/li[2]/a"));
		selenium.waitForPageToLoad("30000");
		selenium.clickAt("//button[3]", RuntimeVariables.replace("Import"));

		for (int second = 0;; second++) {
			if (second >= 90) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible("//input[@id='_156_importFileName']")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		selenium.type("//input[@id='_156_importFileName']",
			RuntimeVariables.replace(
				"L:\\portal\\build\\portal-web\\test\\com\\liferay\\portalweb\\portal\\controlpanel\\sites\\lar\\importlar\\dependencies\\Sites_Public_Pages.lar"));
		assertFalse(selenium.isChecked(
				"//input[@id='_156_DELETE_MISSING_LAYOUTSCheckbox']"));
		selenium.clickAt("//input[@id='_156_DELETE_MISSING_LAYOUTSCheckbox']",
			RuntimeVariables.replace("Delete Missing Pages Checkbox"));
		assertTrue(selenium.isChecked(
				"//input[@id='_156_DELETE_MISSING_LAYOUTSCheckbox']"));
		assertFalse(selenium.isChecked(
				"//input[@id='_156_PERMISSIONSCheckbox']"));
		selenium.clickAt("//input[@id='_156_PERMISSIONSCheckbox']",
			RuntimeVariables.replace("Permissions Checkbox"));
		assertTrue(selenium.isChecked("//input[@id='_156_PERMISSIONSCheckbox']"));
		assertFalse(selenium.isChecked("//input[@id='_156_CATEGORIESCheckbox']"));
		selenium.clickAt("//input[@id='_156_CATEGORIESCheckbox']",
			RuntimeVariables.replace("Categories Checkbox"));
		assertTrue(selenium.isChecked("//input[@id='_156_CATEGORIESCheckbox']"));
		assertFalse(selenium.isChecked(
				"//input[@id='_156_DELETE_PORTLET_DATACheckbox']"));
		selenium.clickAt("//input[@id='_156_DELETE_PORTLET_DATACheckbox']",
			RuntimeVariables.replace(
				"Delete portlet data before importing Checkbox"));
		assertTrue(selenium.isChecked(
				"//input[@id='_156_DELETE_PORTLET_DATACheckbox']"));
		selenium.clickAt("//input[@value='Import']",
			RuntimeVariables.replace("Import"));
		selenium.waitForPageToLoad("30000");

		for (int second = 0;; second++) {
			if (second >= 90) {
				fail("timeout");
			}

			try {
				if (selenium.isVisible("//div[@class='portlet-msg-success']")) {
					break;
				}
			}
			catch (Exception e) {
			}

			Thread.sleep(1000);
		}

		assertEquals(RuntimeVariables.replace(
				"Your request completed successfully."),
			selenium.getText("//div[@class='portlet-msg-success']"));
	}
}