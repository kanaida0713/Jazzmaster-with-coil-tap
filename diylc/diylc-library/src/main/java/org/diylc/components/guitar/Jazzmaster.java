/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.components.guitar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.core.ComponentState;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Jazzmaster Pickup", category = "Guitar", author = "Branislav Stojkovic Main code Curran Sheldon Jazzmaster addition",
    description = "Jazzmaster-style pickup with full winding and optional coil tap",
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "PKP",
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Guitar Wiring Diagram", enableCache = true)
public class Jazzmaster extends AbstractSingleOrHumbuckerPickup {

  private static final long serialVersionUID = 1L;

  private static Color BODY_COLOR = Color.decode("#D8C989");;
//  private static Color POINT_COLOR = Color.darkGray;

  // Jazzmaster dimensions
  private static Size JAZZMASTER_WIDTH = new Size(38d, SizeUnit.mm);
  private static Size JAZZMASTER_LENGTH = new Size(91d, SizeUnit.mm);
  private static Size JAZZMASTER_EDGE_RADIUS = new Size(6d, SizeUnit.mm);

  private static Size POINT_MARGIN = new Size(3.5d, SizeUnit.mm);
  private static Size POLE_SIZE = new Size(4d, SizeUnit.mm);
  private static Size POLE_SPACING = new Size(10.7d, SizeUnit.mm);
    
  private Color color = BODY_COLOR;
  private Color poleColor = METAL_COLOR;
  private WiringType wiringType = WiringType.TAPPED;
  
  @Override
  protected OrientationHV getControlPointDirection() {   
    return OrientationHV.VERTICAL;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    Composite oldComposite = applyAlpha(g2d, componentState);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : color);
    g2d.fill(body[0]);
    if (body[1] != null)
      g2d.fill(body[1]);
//    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : POINT_COLOR);
//    g2d.fill(body[2]);
    drawingObserver.stopTracking();
    g2d.setComposite(oldComposite);    

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : color.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);
    if (body[1] != null)
      g2d.draw(body[1]);

    markContactPoints(g2d, drawingObserver);

    if (!outlineMode) {
      g2d.setColor(getPoleColor());
      g2d.fill(body[3]);
      g2d.setColor(darkerOrLighter(getPoleColor()));
      g2d.draw(body[3]);    
    }

//    Color finalLabelColor;
//    if (outlineMode) {
//      Theme theme =
//          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
//      finalLabelColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
//              : theme.getOutlineColor();
//    } else {
//      finalLabelColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
//              : LABEL_COLOR;
//    }
//    g2d.setColor(finalLabelColor);
//    g2d.setFont(project.getFont());
//    Rectangle bounds = body[0].getBounds();
//    drawCenteredText(g2d, value, bounds.x + bounds.width / 2, bounds.y + bounds.height / 2, HorizontalAlignment.CENTER,
//        VerticalAlignment.CENTER);
    drawMainLabel(g2d, project, outlineMode, componentState);
    
    drawTerminalLabels(g2d, finalBorderColor, project);
    // terminal labels
//    Point[] points = getControlPoints();
//    g2d.setColor(finalBorderColor);
//
//    g2d.setFont(project.getFont().deriveFont(TERMINAL_FONT_SIZE * 1f));
//    int dx = 0;
//    int dy = 0;
//    switch (orientation) {
//      case DEFAULT:        
//        dx = (int) (TERMINAL_FONT_SIZE * 0.8);
//        dy = 0;  
//        break;
//      case _90:
//        dx = 0;
//        dy = (int) (TERMINAL_FONT_SIZE * 0.8);
//        break;
//      case _180:
//        dx = -(int) (TERMINAL_FONT_SIZE * 0.8);
//        dy = 0;       
//        break;
//      case _270:
//        dx = 0;
//        dy = -(int) (TERMINAL_FONT_SIZE * 0.8);
//        break;     
//    }
//    
//    if (getPolarity() == Polarity.North || getPolarity() == Polarity.South) {
//      drawCenteredText(g2d, getPolarity().name().substring(0, 1), (points[1].x + points[2].x) / 2 + dx, (points[1].y + points[2].y) / 2 + dy, HorizontalAlignment.CENTER,
//          VerticalAlignment.CENTER);
//    } else {
//      drawCenteredText(g2d, "N", (points[0].x + points[1].x) / 2 + dx, (points[0].y + points[1].y) / 2 + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
//      drawCenteredText(g2d, "S", (points[2].x + points[3].x) / 2 + dx, (points[2].y + points[3].y) / 2 + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
//    }
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[4];

      Point2D[] points = getControlPoints();
      double x = points[0].getX();
      double y = points[0].getY();
      int width = (int) JAZZMASTER_WIDTH.convertToPixels();
      int length = (int) JAZZMASTER_LENGTH.convertToPixels();
      int edgeRadius = (int) JAZZMASTER_EDGE_RADIUS.convertToPixels();
      int pointMargin = (int) POINT_MARGIN.convertToPixels();

      body[0] =
          new Area(new RoundRectangle2D.Double(x /*+ pointMargin*/ - length, y - pointMargin, length, width, edgeRadius,
              edgeRadius));


      int poleSize = (int) POLE_SIZE.convertToPixels();
      int poleSpacing = (int) POLE_SPACING.convertToPixels();
      int poleMargin = (length - poleSpacing * 5) / 2;
      Area poleArea = new Area();
      for (int i = 0; i < 6; i++) {
        Ellipse2D pole =
            new Ellipse2D.Double(x /*+ pointMargin*/ - length + poleMargin + i * poleSpacing - poleSize / 2, y
                - pointMargin - poleSize / 2 + width / 2, poleSize, poleSize);
        poleArea.add(new Area(pole));
      }
      body[3] = poleArea;

      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
        double theta = 0;
        switch (orientation) {
          case _90:
            theta = Math.PI / 2;
            break;
          case _180:
            theta = Math.PI;
            break;
          case _270:
            theta = Math.PI * 3 / 2;
            break;
        }
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        for (Shape shape : body) {
          Area area = (Area) shape;
          if (shape != null)
            area.transform(rotation);
        }
      }
    }
    return body;
  }
  
  @Override
  protected int getMainLabelYOffset() {
    return (int) (JAZZMASTER_WIDTH.convertToPixels() / 2 - 20);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(Math.PI / 4, width / 2, height / 2);

    int baseWidth = 13 * width / 32;
    int baseLength = 27 * width / 32;
    int radius = 6 * width / 32;

    g2d.setColor(BODY_COLOR);
   // Polygon base =
   //     new Polygon(new int[] {width / 2, (width + baseWidth) / 2, (width + baseWidth) / 2, width / 2,
   //         (width - baseWidth) / 2, (width - baseWidth) / 2}, new int[] {-2, (height - baseLength) / 2,
   //         (height + baseLength) / 2, height + 1, (height + baseLength) / 2, (height - baseLength) / 2}, 6);
   // Area baseArea = new Area(base);
   // baseArea.intersect(new Area(new Rectangle(0, -1, width, height + 1)));
   // g2d.fill(baseArea);
   // g2d.setColor(BODY_COLOR.darker());
   // g2d.draw(baseArea);

    g2d.setColor(BODY_COLOR);
    g2d.fillRoundRect((width - baseWidth) / 2, (height - baseLength) / 2, baseWidth, baseLength, radius, radius);
    g2d.setColor(BODY_COLOR.darker());
    g2d.drawRoundRect((width - baseWidth) / 2, (height - baseLength) / 2, baseWidth, baseLength, radius, radius);

    g2d.setColor(METAL_COLOR);
    int poleSize = 2;
    int poleSpacing = 17 * width / 32;
    for (int i = 0; i < 6; i++) {
      g2d.fillOval((width - poleSize) / 2, (height - poleSpacing) / 2 + (i * poleSpacing / 5), poleSize, poleSize);
    }
  }

  @EditableProperty(name = "Wiring")
public WiringType getWiringType() {
  return wiringType;
}

public void setWiringType(WiringType wiringType) {
  this.wiringType = wiringType;
}

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }
  
  @EditableProperty(name = "Pole Color")
  public Color getPoleColor() {
    if (poleColor == null)
      poleColor = METAL_COLOR;
    return poleColor;
  }

  public void setPoleColor(Color poleColor) {
    this.poleColor = poleColor;
  }  

  
  @Override
public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
  if (index == 0)
    return VisibilityPolicy.NEVER;

  if (index == 3 && wiringType == WiringType.STANDARD)
    return VisibilityPolicy.NEVER;

  return VisibilityPolicy.ALWAYS;
}

  @Override
public boolean isControlPointSticky(int index) {
  if (index == 0)
    return false;

  if (index == 3 && wiringType == WiringType.STANDARD)
    return false;

  return true;
}

 @Override
public String getControlPointNodeName(int index) {
  if (wiringType == WiringType.TAPPED) {
    switch (index) {
      case 1:
        return "Start";
      case 2:
        return "Tap";
      case 3:
        return "Finish";
      default:
        return null;
    }
  }

  switch (index) {
    case 1:
      return "Start";
    case 2:
      return "Finish";
    default:
      return null;
  }
}

 @Override
public String getInternalLinkName(int index1, int index2) {
  if (wiringType == WiringType.TAPPED) {
    if (index1 == 1 && index2 == 2)
      return "Start->Tap";
    if (index1 == 2 && index2 == 1)
      return "Start<-Tap";
    if (index1 == 2 && index2 == 3)
      return "Tap->Finish";
    if (index1 == 3 && index2 == 2)
      return "Tap<-Finish";
  } else {
    if (index1 == 1 && index2 == 2)
      return "Start->Finish";
    if (index1 == 2 && index2 == 1)
      return "Start<-Finish";
  }

  return null;
}

  @Override
  public boolean isHumbucker() {
    return false;
  }

  public enum WiringType {
  STANDARD("Standard - 2 leads"),
  TAPPED("Tapped - 3 leads");

  private final String label;

  WiringType(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }
}
}
