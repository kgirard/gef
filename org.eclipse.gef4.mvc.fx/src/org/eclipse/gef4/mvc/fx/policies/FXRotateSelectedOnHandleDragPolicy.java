/*******************************************************************************
 * Copyright (c) 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.policies;

import java.util.List;

import org.eclipse.gef4.geometry.euclidean.Angle;
import org.eclipse.gef4.geometry.euclidean.Vector;
import org.eclipse.gef4.geometry.planar.Dimension;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.Rectangle;
import org.eclipse.gef4.mvc.fx.parts.FXPartUtils;
import org.eclipse.gef4.mvc.models.SelectionModel;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.mvc.parts.IVisualPart;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

// TODO: Extract FXRotatePolicy and extract duplicate code with FXRotateSelectedOnRotatePolicy
public class FXRotateSelectedOnHandleDragPolicy extends AbstractFXOnDragPolicy {

	private boolean invalidGesture = false;
	private Point initialPointerLocationInScene;
	private Point pivotInScene;

	protected Angle computeRotationAngleCW(MouseEvent e,
			IVisualPart<Node, ? extends Node> part) {
		Vector vStart = new Vector(pivotInScene, initialPointerLocationInScene);
		Vector vEnd = new Vector(pivotInScene,
				new Point(e.getSceneX(), e.getSceneY()));
		Angle angle = vStart.getAngleCW(vEnd);
		return angle;
	}

	@Override
	public void drag(MouseEvent e, Dimension delta) {
		// do nothing when the user does not press control
		if (invalidGesture) {
			return;
		}
		for (IVisualPart<Node, ? extends Node> part : getTargetParts()) {
			updateOperation(e, part);
		}
	}

	protected FXRotatePolicy getRotatePolicy(
			IVisualPart<Node, ? extends Node> part) {
		return part.getAdapter(FXRotatePolicy.class);
	}

	protected List<IContentPart<Node, ? extends Node>> getTargetParts() {
		return getHost().getRoot().getViewer()
				.<SelectionModel<Node>> getAdapter(SelectionModel.class)
				.getSelected();
	}

	@Override
	public void press(MouseEvent e) {
		// do nothing when the user does not press control
		if (!e.isControlDown()) {
			invalidGesture = true;
			return;
		}

		// save pointer location for later angle calculation
		initialPointerLocationInScene = new Point(e.getSceneX(), e.getSceneY());

		// determine pivot point
		Rectangle bounds = FXPartUtils
				.getUnionedVisualBoundsInScene(getTargetParts());
		pivotInScene = bounds == null ? null : bounds.getCenter();

		// initialize for all target parts
		for (IVisualPart<Node, ? extends Node> part : getTargetParts()) {
			// transform pivot point to local coordinates
			FXRotatePolicy rotatePolicy = getRotatePolicy(part);
			if (rotatePolicy != null) {
				rotatePolicy.init();
			}
		}
	}

	@Override
	public void release(MouseEvent e, Dimension delta) {
		// do nothing when the user does not press control
		if (invalidGesture) {
			invalidGesture = false;
			return;
		}
		for (IVisualPart<Node, ? extends Node> part : getTargetParts()) {
			updateOperation(e, part);
			FXRotatePolicy rotatePolicy = getRotatePolicy(part);
			if (rotatePolicy != null) {
				getHost().getRoot().getViewer().getDomain()
						.execute(rotatePolicy.commit());
			}
		}
	}

	private void updateOperation(MouseEvent e,
			IVisualPart<Node, ? extends Node> part) {
		// determine scaling
		FXRotatePolicy rotatePolicy = getRotatePolicy(part);
		if (rotatePolicy != null) {
			rotatePolicy.performRotation(computeRotationAngleCW(e, part),
					pivotInScene);
		}
	}

}
