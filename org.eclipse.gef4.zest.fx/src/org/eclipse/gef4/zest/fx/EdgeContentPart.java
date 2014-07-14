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
package org.eclipse.gef4.zest.fx;

import java.util.Map;

import javafx.scene.Node;
import javafx.scene.shape.Polyline;

import org.eclipse.gef4.fx.anchors.AnchorKey;
import org.eclipse.gef4.fx.anchors.AnchorLink;
import org.eclipse.gef4.fx.anchors.FXChopBoxAnchor;
import org.eclipse.gef4.fx.anchors.IFXAnchor;
import org.eclipse.gef4.fx.nodes.FXChopBoxHelper;
import org.eclipse.gef4.fx.nodes.FXCurveConnection;
import org.eclipse.gef4.fx.nodes.FXGeometryNode;
import org.eclipse.gef4.fx.nodes.FXLabeledConnection;
import org.eclipse.gef4.fx.nodes.IFXDecoration;
import org.eclipse.gef4.geometry.convert.fx.JavaFX2Geometry;
import org.eclipse.gef4.geometry.planar.ICurve;
import org.eclipse.gef4.geometry.planar.IGeometry;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.Rectangle;
import org.eclipse.gef4.graph.Edge;
import org.eclipse.gef4.graph.Graph;
import org.eclipse.gef4.graph.Graph.Attr;
import org.eclipse.gef4.mvc.behaviors.AbstractHoverBehavior;
import org.eclipse.gef4.mvc.behaviors.AbstractSelectionBehavior;
import org.eclipse.gef4.mvc.fx.behaviors.FXHoverBehavior;
import org.eclipse.gef4.mvc.fx.behaviors.FXSelectionBehavior;
import org.eclipse.gef4.mvc.fx.parts.AbstractFXContentPart;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.zest.fx.layout.GraphLayoutContext;

public class EdgeContentPart extends AbstractFXContentPart {

	public static class ArrowHead extends Polyline implements IFXDecoration {
		public ArrowHead() {
			super(15.0, 0.0, 10.0, 0.0, 10.0, 3.0, 0.0, 0.0, 10.0, -3.0, 10.0,
					0.0);
		}

		@Override
		public Point getLocalEndPoint() {
			return new Point(15, 0);
		}

		@Override
		public Point getLocalStartPoint() {
			return new Point(0, 0);
		}

		@Override
		public Node getVisual() {
			return this;
		}
	}

	public static final String CSS_CLASS = "edge";
	public static final Object ATTR_CLASS = "class";
	public static final Object ATTR_ID = "id";

	private static final double GAP_LENGTH = 7d;
	private static final double DASH_LENGTH = 7d;
	private static final Double DOT_LENGTH = 1d;

	private Edge edge;
	private FXLabeledConnection visual;

	{
		visual = new FXLabeledConnection();
		new FXChopBoxHelper(visual.getConnection());
		visual.getStyleClass().add(CSS_CLASS);
		visual.getConnection().getCurveNode().getStyleClass().add("curve");
	}

	public EdgeContentPart(Edge content) {
		edge = content;
		Map<String, Object> attrs = edge.getAttrs();
		Object label = attrs.get(Attr.Key.LABEL.toString());
		if (label instanceof String) {
			visual.setLabel((String) label);
		}
		if (attrs.containsKey(ATTR_CLASS)) {
			visual.getStyleClass().add((String) attrs.get(ATTR_CLASS));
		}
		if (attrs.containsKey(ATTR_ID)) {
			visual.setId((String) attrs.get(ATTR_ID));
		}

		setAdapter(AbstractSelectionBehavior.class, new FXSelectionBehavior() {
			@Override
			protected IGeometry getHostGeometry() {
				return visual.getConnection().getCurveNode().getGeometry();
			}
		});
		setAdapter(AbstractHoverBehavior.class, new FXHoverBehavior() {
			@Override
			protected IGeometry getFeedbackGeometry() {
				return visual.getConnection().getCurveNode().getGeometry();
			}
		});
	}

	@Override
	public void attachVisualToAnchorageVisual(IVisualPart<Node> anchorage,
			Node anchorageVisual) {
		IContentPart<Node> sourcePart = anchorage.getRoot().getViewer()
				.getContentPartMap().get(edge.getSource());
		IContentPart<Node> targetPart = anchorage.getRoot().getViewer()
				.getContentPartMap().get(edge.getTarget());

		IFXAnchor anchor = ((AbstractFXContentPart) anchorage).getAnchor(this);
		AnchorKey anchorKey = new AnchorKey(visual,
				anchorage == sourcePart ? "START" : "END");
		AnchorLink anchorLink = new AnchorLink(anchor, anchorKey);

		FXCurveConnection connection = visual.getConnection();
		if (anchorage == sourcePart) {
			connection.setStartAnchorLink(anchorLink);
		} else if (anchorage == targetPart) {
			connection.setEndAnchorLink(anchorLink);
		}

		if (connection.isStartConnected() && connection.isEndConnected()) {
			AnchorLink startAl = connection.startAnchorLinkProperty().get();
			AnchorLink endAl = connection.endAnchorLinkProperty().get();
			((FXChopBoxAnchor) startAl.getAnchor()).setReferencePoint(
					startAl.getKey(), getAnchorageCenter(endAl));
			((FXChopBoxAnchor) endAl.getAnchor()).setReferencePoint(
					endAl.getKey(), startAl.getPosition());
		}

		super.attachVisualToAnchorageVisual(anchorage, anchorageVisual);
	}

	@Override
	public void detachVisualFromAnchorageVisual(IVisualPart<Node> anchorage,
			Node anchorageVisual) {
		FXCurveConnection connection = visual.getConnection();
		IFXAnchor anchor = ((AbstractFXContentPart) anchorage).getAnchor(this);
		if (anchor == connection.getStartAnchorLink().getAnchor()) {
			connection.setStartPoint(connection.getStartPoint());
		} else {
			connection.setEndPoint(connection.getEndPoint());
		}
		super.detachVisualFromAnchorageVisual(anchorage, anchorageVisual);
	}

	@Override
	public void doRefreshVisual() {
		GraphLayoutContext glc = (GraphLayoutContext) getViewer().getDomain()
				.getAdapter(ILayoutModel.class).getLayoutContext();
		if (glc == null) {
			return;
		}

		// decoration
		FXCurveConnection connection = visual.getConnection();
		if (Attr.Value.GRAPH_DIRECTED.equals(glc.getGraph().getAttrs()
				.get(Attr.Key.GRAPH_TYPE.toString()))) {
			connection.setEndDecoration(new ArrowHead());
		} else {
			connection.setEndDecoration(null);
		}

		// TODO: visibility
		FXGeometryNode<ICurve> curveNode = connection.getCurveNode();

		// dashes
		Object style = edge.getAttrs()
				.get(Graph.Attr.Key.EDGE_STYLE.toString());
		if (style == Graph.Attr.Value.LINE_DASH) {
			curveNode.getStrokeDashArray().setAll(DASH_LENGTH, GAP_LENGTH);
		} else if (style == Graph.Attr.Value.LINE_DASHDOT) {
			curveNode.getStrokeDashArray().setAll(DASH_LENGTH, GAP_LENGTH,
					DOT_LENGTH, GAP_LENGTH);
		} else if (style == Graph.Attr.Value.LINE_DASHDOTDOT) {
			curveNode.getStrokeDashArray().setAll(DASH_LENGTH, GAP_LENGTH,
					DOT_LENGTH, GAP_LENGTH, DOT_LENGTH, GAP_LENGTH);
		} else if (style == Graph.Attr.Value.LINE_DOT) {
			curveNode.getStrokeDashArray().setAll(DOT_LENGTH, GAP_LENGTH);
		} else {
			curveNode.getStrokeDashArray().clear();
		}
	}

	private Point getAnchorageCenter(AnchorLink al) {
		Node anchorage = al.getAnchor().getAnchorageNode();
		Rectangle bounds = JavaFX2Geometry.toRectangle(anchorage
				.localToScene(anchorage.getLayoutBounds()));
		return bounds.getCenter();
	}

	@Override
	public Node getVisual() {
		return visual;
	}

}
