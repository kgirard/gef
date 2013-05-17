/*******************************************************************************
 * Copyright (c) 2013 itemis AG and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 * 
 *******************************************************************************/
package org.eclipse.gef4.swt.canvas.ex;

import org.eclipse.gef4.swt.canvas.Group;
import org.eclipse.gef4.swt.canvas.gc.GraphicsContext;
import org.eclipse.gef4.swt.canvas.gc.RgbaColor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Canvas;

public class ShowTextExample implements IExample {

	public static void main(String[] args) {
		new Example(new ShowTextExample());
	}

	private Canvas c;

	@Override
	public void addUi(Group c) {
		this.c = c;
	}

	@Override
	public int getHeight() {
		return 480;
	}

	@Override
	public String getTitle() {
		return "Show Text";
	}

	@Override
	public int getWidth() {
		return 640;
	}

	@Override
	public void render(GraphicsContext g) {
		g.clearRect(0, 0, c.getSize().x, c.getSize().y);
		g.setFont(new FontData("Times", 12, SWT.NORMAL));

		g.setFill(new Color(g.getGcByReference().getDevice(), 200, 0, 0));
		g.setStroke(new RgbaColor(20, 40, 250));

		g.fillText(
				"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor",
				20, 20);
		g.strokeText(
				"incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud",
				20, 40);
		g.fillText(
				"exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure",
				20, 60);
		g.strokeText(
				"dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
				20, 80);
		g.fillText(
				"Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt",
				20, 100);
		g.strokeText("mollit anim id est laborum.", 20, 120);
	}

}
