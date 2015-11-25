/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API & implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.zest.fx.parts;

import org.eclipse.gef4.fx.nodes.HoverOverlayImageView;
import org.eclipse.gef4.mvc.fx.parts.AbstractFXSegmentHandlePart;
import org.eclipse.gef4.zest.fx.policies.ShowHiddenNeighborsOfFirstAnchorageOnClickPolicy;

import javafx.scene.image.Image;

/**
 * The {@link ShowHiddenNeighborsHoverHandlePart} is an
 * {@link AbstractFXSegmentHandlePart} that displays an expansion image. By
 * default, the {@link ShowHiddenNeighborsOfFirstAnchorageOnClickPolicy} is
 * installed for {@link ShowHiddenNeighborsHoverHandlePart}, so that the
 * corresponding {@link NodeContentPart} can be expanded by a click on this
 * part.
 *
 * @author mwienand
 *
 */
public class ShowHiddenNeighborsHoverHandlePart extends AbstractFXSegmentHandlePart<HoverOverlayImageView> {

	/**
	 * The url to the image that is displayed when hovering this part.
	 */
	private static final String IMG_SHOW_HIDDEN_NEIGHBORS = "/expandall.gif";

	/**
	 * The url to the image that is displayed when not hovering this part.
	 */
	private static final String IMG_SHOW_HIDDEN_NEIGHBORS_DISABLED = "/expandall_disabled.gif";

	@Override
	protected HoverOverlayImageView createVisual() {
		// create blending image view for both
		HoverOverlayImageView blendImageView = new HoverOverlayImageView();
		blendImageView.baseImageProperty().set(new Image(IMG_SHOW_HIDDEN_NEIGHBORS_DISABLED));
		blendImageView.overlayImageProperty().set(new Image(IMG_SHOW_HIDDEN_NEIGHBORS));
		return blendImageView;
	}

}
