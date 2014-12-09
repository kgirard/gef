/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;

import org.eclipse.gef4.mvc.fx.parts.FXPartUtils;
import org.eclipse.gef4.mvc.fx.policies.AbstractFXScrollPolicy;
import org.eclipse.gef4.mvc.fx.viewer.FXViewer;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.mvc.tools.AbstractTool;
import org.eclipse.gef4.mvc.viewer.IViewer;

public class FXScrollTool extends AbstractTool<Node> {

	public static final Class<AbstractFXScrollPolicy> TOOL_POLICY_KEY = AbstractFXScrollPolicy.class;

	private final EventHandler<ScrollEvent> scrollListener = new EventHandler<ScrollEvent>() {
		protected Collection<? extends AbstractFXScrollPolicy> getTargetPolicies(
				ScrollEvent event) {
			EventTarget target = event.getTarget();
			if (!(target instanceof Node)) {
				return Collections.emptyList();
			}

			Node targetNode = (Node) target;
			IVisualPart<Node, ? extends Node> targetPart = FXPartUtils
					.getTargetPart(getDomain().getViewers().values(),
							targetNode, TOOL_POLICY_KEY);

			if (targetPart == null) {
				return Collections.emptyList();
			}

			Collection<? extends AbstractFXScrollPolicy> policies = getScrollPolicies(targetPart);
			return policies;
		}

		@Override
		public void handle(ScrollEvent event) {
			event.consume();

			Collection<? extends AbstractFXScrollPolicy> policies = getTargetPolicies(event);
			for (AbstractFXScrollPolicy policy : policies) {
				policy.scroll(event);
			}
		}
	};

	protected Set<? extends AbstractFXScrollPolicy> getScrollPolicies(
			IVisualPart<Node, ? extends Node> targetPart) {
		return new HashSet<>(targetPart.<AbstractFXScrollPolicy> getAdapters(
				TOOL_POLICY_KEY).values());
	}

	@Override
	protected void registerListeners() {
		super.registerListeners();

		for (IViewer<Node> viewer : getDomain().getViewers().values()) {
			Scene scene = ((FXViewer) viewer).getScene();
			scene.addEventFilter(ScrollEvent.SCROLL, scrollListener);
		}
	}

	@Override
	protected void unregisterListeners() {
		for (IViewer<Node> viewer : getDomain().getViewers().values()) {
			Scene scene = ((FXViewer) viewer).getScene();
			scene.removeEventFilter(ScrollEvent.SCROLL, scrollListener);
		}

		super.unregisterListeners();
	}

}
