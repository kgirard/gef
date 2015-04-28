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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import org.eclipse.gef4.fx.gestures.FXMouseDragGesture;
import org.eclipse.gef4.geometry.planar.Dimension;
import org.eclipse.gef4.mvc.fx.parts.FXPartUtils;
import org.eclipse.gef4.mvc.fx.policies.AbstractFXOnClickPolicy;
import org.eclipse.gef4.mvc.fx.policies.AbstractFXOnDragPolicy;
import org.eclipse.gef4.mvc.fx.viewer.FXViewer;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.mvc.policies.IPolicy;
import org.eclipse.gef4.mvc.tools.AbstractTool;
import org.eclipse.gef4.mvc.viewer.IViewer;

public class FXClickDragTool extends AbstractTool<Node> {

	public static final Class<AbstractFXOnClickPolicy> CLICK_TOOL_POLICY_KEY = AbstractFXOnClickPolicy.class;
	public static final Class<AbstractFXOnDragPolicy> DRAG_TOOL_POLICY_KEY = AbstractFXOnDragPolicy.class;

	private final Map<IViewer<Node>, FXMouseDragGesture> gestures = new HashMap<IViewer<Node>, FXMouseDragGesture>();
	private boolean dragInProgress;
	private final Map<AbstractFXOnDragPolicy, MouseEvent> pressEvents = new HashMap<AbstractFXOnDragPolicy, MouseEvent>();
	private Map<EventTarget, IVisualPart<Node, ? extends Node>> interactionTargetOverrides = new HashMap<EventTarget, IVisualPart<Node, ? extends Node>>();

	protected Set<? extends AbstractFXOnClickPolicy> getClickPolicies(
			IVisualPart<Node, ? extends Node> targetPart) {
		return new HashSet<AbstractFXOnClickPolicy>(targetPart
				.<AbstractFXOnClickPolicy> getAdapters(CLICK_TOOL_POLICY_KEY)
				.values());
	}

	protected Set<? extends AbstractFXOnDragPolicy> getDragPolicies(
			IVisualPart<Node, ? extends Node> targetPart) {
		return new HashSet<AbstractFXOnDragPolicy>(targetPart
				.<AbstractFXOnDragPolicy> getAdapters(DRAG_TOOL_POLICY_KEY)
				.values());
	}

	protected <T extends IPolicy<Node>> IVisualPart<Node, ? extends Node> getTargetPart(
			final IViewer<Node> viewer, Node target, Class<T> policy) {
		if (interactionTargetOverrides.containsKey(target)) {
			IVisualPart<Node, ? extends Node> overridingTarget = interactionTargetOverrides
					.get(target);
			if (policy != null
					&& overridingTarget.getAdapters(policy).isEmpty()) {
				return null;
			}
			return overridingTarget;
		}
		return FXPartUtils.getTargetPart(Collections.singleton(viewer), target,
				policy, true);
	}

	/**
	 * Registers the given {@link IVisualPart} as the target part for all JavaFX
	 * events which are send to the given {@link EventTarget} during the
	 * currently active or next press-drag-release gesture.
	 *
	 * @param target
	 *            The JavaFX {@link EventTarget} for which the given target
	 *            should be used.
	 * @param targetPart
	 *            The {@link IVisualPart} to use as the target for all JavaFX
	 *            events directed at the given {@link EventTarget}.
	 */
	public void overrideTargetForThisInteraction(EventTarget target,
			IVisualPart<Node, ? extends Node> targetPart) {
		interactionTargetOverrides.put(target, targetPart);
	}

	@Override
	protected void registerListeners() {
		super.registerListeners();

		for (final IViewer<Node> viewer : getDomain().getViewers().values()) {
			FXMouseDragGesture gesture = new FXMouseDragGesture() {
				@Override
				protected void drag(Node target, MouseEvent e, double dx,
						double dy) {
					if (!dragInProgress) {
						return;
					}
					IVisualPart<Node, ? extends Node> targetPart = getTargetPart(
							viewer, target, DRAG_TOOL_POLICY_KEY);
					// when no part processes the event, send it to the root
					// part
					if (targetPart == null) {
						targetPart = viewer.getRootPart();
					}
					Collection<? extends AbstractFXOnDragPolicy> policies = getDragPolicies(targetPart);
					for (AbstractFXOnDragPolicy policy : policies) {
						policy.drag(e, new Dimension(dx, dy));
					}
				}

				@Override
				protected void press(Node target, MouseEvent e) {
					// do not notify other listeners
					e.consume();

					// click first
					IVisualPart<Node, ? extends Node> clickTargetPart = getTargetPart(
							viewer, target, CLICK_TOOL_POLICY_KEY);
					// when no part processes the event, send it to the root
					// part
					if (clickTargetPart == null) {
						clickTargetPart = viewer.getRootPart();
					}

					Collection<? extends AbstractFXOnClickPolicy> clickPolicies = getClickPolicies(clickTargetPart);
					getDomain().openExecutionTransaction(FXClickDragTool.this);
					for (AbstractFXOnClickPolicy policy : clickPolicies) {
						policy.click(e);
					}
					getDomain().closeExecutionTransaction(FXClickDragTool.this);

					// drag second, but only for single clicks
					if (e.getClickCount() == 1) {
						IVisualPart<Node, ? extends Node> dragTargetPart = getTargetPart(
								viewer, target, DRAG_TOOL_POLICY_KEY);

						// if no part wants to process the drag event, send it
						// to the root part
						if (dragTargetPart == null) {
							dragTargetPart = viewer.getRootPart();
						}
						Collection<? extends AbstractFXOnDragPolicy> dragPolicies = getDragPolicies(dragTargetPart);
						getDomain().openExecutionTransaction(
								FXClickDragTool.this);
						for (AbstractFXOnDragPolicy policy : dragPolicies) {
							dragInProgress = true;
							pressEvents.put(policy, e);
							policy.press(e);
						}
					}
				}

				@Override
				protected void release(Node target, MouseEvent e, double dx,
						double dy) {
					if (!dragInProgress) {
						return;
					}
					IVisualPart<Node, ? extends Node> targetPart = getTargetPart(
							viewer, target, DRAG_TOOL_POLICY_KEY);
					// if no part wants to process the event, send it to the
					// root part
					if (targetPart == null) {
						targetPart = viewer.getRootPart();
					}
					Collection<? extends AbstractFXOnDragPolicy> policies = getDragPolicies(targetPart);
					for (AbstractFXOnDragPolicy policy : policies) {
						pressEvents.remove(policy);
						policy.release(e, new Dimension(dx, dy));
					}
					getDomain().closeExecutionTransaction(FXClickDragTool.this);
					dragInProgress = false;
					interactionTargetOverrides.clear();
				}
			};

			gesture.setScene(((FXViewer) viewer).getScene());
			gestures.put(viewer, gesture);
		}
	}

	@Override
	protected void unregisterListeners() {
		for (FXMouseDragGesture gesture : gestures.values()) {
			gesture.setScene(null);
		}
		super.unregisterListeners();
	}

}
