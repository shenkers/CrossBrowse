/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Soma
 */
public class CurvedOverlayPath {

    Logger logger = LogManager.getLogger();

    Path p;

    private DoubleProperty yScale;
    private DoubleProperty xScale;

    private List<Pair<DoubleProperty, DoubleProperty>> relativeXCoords;
    private List<Pair<DoubleProperty, DoubleProperty>> relativeYCoords;

    List<Pair<DoubleBinding, DoubleBinding>> componentXCoords;
    List<Pair<DoubleBinding, DoubleBinding>> componentYCoords;

    private List<BooleanProperty> genomeFlipped;

    List<PathElement> pathElements;

    /**
     * @return the yScale
     */
    public DoubleProperty getYScale() {
        return yScale;
    }

    /**
     * @param yScale the yScale to set
     */
    public void setYScale(DoubleProperty yScale) {
        this.yScale = yScale;
    }

    /**
     * @return the xScale
     */
    public DoubleProperty getXScale() {
        return xScale;
    }

    /**
     * @return the relativeXCoords
     */
    public List<Pair<DoubleProperty, DoubleProperty>> getRelativeXCoords() {
        return relativeXCoords;
    }

    /**
     * @return the relativeYCoords
     */
    public List<Pair<DoubleProperty, DoubleProperty>> getRelativeYCoords() {
        return relativeYCoords;
    }

    /**
     * @return the genomeFlipped
     */
    public List<BooleanProperty> getGenomeFlipped() {
        return genomeFlipped;
    }

    enum PathCase {

        first, middle, l_even, l_odd, r_even, r_odd, last;

    };

    class ComplementBinding extends DoubleBinding {

        boolean isFirst;
        BooleanProperty complemented;
        ObservableDoubleValue lower;
        ObservableDoubleValue upper;

        public ComplementBinding(boolean isFirst, BooleanProperty complemented, ObservableDoubleValue lower, ObservableDoubleValue upper) {
            super.bind(complemented, lower, upper);
            this.isFirst = isFirst;
            this.complemented = complemented;
            this.lower = lower;
            this.upper = upper;
        }

        @Override
        protected double computeValue() {
            if (isFirst) {
                if (complemented.get()) {
                    return 1 - upper.doubleValue();
                } else {
                    return lower.doubleValue();
                }
            } else {
                if (complemented.get()) {
                    return 1 - lower.doubleValue();
                } else {
                    return upper.doubleValue();
                }
            }
        }

    }

    public CurvedOverlayPath(int nGenomes) {

        yScale = new SimpleDoubleProperty(1);
        xScale = new SimpleDoubleProperty(1);

        relativeXCoords = new ArrayList<>();
        relativeYCoords = new ArrayList<>();
        componentXCoords = new ArrayList<>();
        componentYCoords = new ArrayList<>();
        genomeFlipped = new ArrayList<>();

        for (int i = 0; i < nGenomes; i++) {
            relativeXCoords.add(new Pair<>(new SimpleDoubleProperty(), new SimpleDoubleProperty()));
            relativeYCoords.add(new Pair<>(new SimpleDoubleProperty(), new SimpleDoubleProperty()));
            BooleanProperty flipped = new SimpleBooleanProperty(false);
            genomeFlipped.add(flipped);
            DoubleProperty lower = getRelativeXCoords().get(i).getKey();
            DoubleProperty upper = getRelativeXCoords().get(i).getValue();
            componentXCoords.add(new Pair<>(new ComplementBinding(true, flipped, lower, upper).multiply(getXScale()), new ComplementBinding(false, flipped, lower, upper).multiply(getXScale())));
            componentYCoords.add(new Pair<>(getRelativeYCoords().get(i).getKey().multiply(getYScale()), getRelativeYCoords().get(i).getValue().multiply(getYScale())));
        }

        pathElements = new ArrayList<>();

//            pathElements.add(new MoveTo());
        for (int i = 0; i < nGenomes * 4; i++) {
            Function<Integer, PathCase> f = (j -> {
                if (j == 0) {
                    return PathCase.first;
                } else if (j + 1 == nGenomes * 4) {
                    return PathCase.last;
                } else if (j + 1 == nGenomes * 2) {
                    return PathCase.middle;
                } else if (j % 2 == 1) {
                    if (j < nGenomes * 2) {
                        return PathCase.l_odd;
                    } else {
                        return PathCase.r_odd;
                    }
                } else {
                    if (j < nGenomes * 2) {
                        return PathCase.l_even;
                    } else {
                        return PathCase.r_even;
                    }
                }
            });

//            logger.info("i {}", i);

            // get the index of the genome
            int g = i < nGenomes * 2 ? i / 2 : ((4 * nGenomes - i - 1) / 2);
//            logger.info("g {}", g);

            Pair<DoubleBinding, DoubleBinding> xCoord = componentXCoords.get(g);
            Pair<DoubleBinding, DoubleBinding> yCoord = componentYCoords.get(g);

            PathCase pc = f.apply(i);
            switch (pc) {
                case first: {
                    MoveTo moveTo = new MoveTo();
                    LineTo lineTo = new LineTo();

                    moveTo.xProperty().bind(xCoord.getKey());
                    moveTo.yProperty().bind(yCoord.getKey());

                    lineTo.xProperty().bind(xCoord.getKey());
                    lineTo.yProperty().bind(yCoord.getValue());

                    pathElements.add(moveTo);
                    pathElements.add(lineTo);
                    break;
                }
                case middle: {
                    LineTo lineTo = new LineTo();
                    lineTo.xProperty().bind(xCoord.getValue());
                    lineTo.yProperty().bind(yCoord.getValue());
                    pathElements.add(lineTo);
                    break;
                }
                case last: {
                    LineTo lineTo = new LineTo();
                    lineTo.xProperty().bind(xCoord.getKey());
                    lineTo.yProperty().bind(yCoord.getKey());
                    pathElements.add(lineTo);
                    break;
                }
                case l_odd: {
                    CubicCurveTo curveTo = new CubicCurveTo();

                    Pair<DoubleBinding, DoubleBinding> xCoordNext = componentXCoords.get(g + 1);
                    Pair<DoubleBinding, DoubleBinding> yCoordNext = componentYCoords.get(g + 1);

                    DoubleBinding controlY = yCoord.getValue().add(yCoordNext.getKey()).divide(2.);

                    curveTo.controlX1Property().bind(xCoord.getKey());
                    curveTo.controlY1Property().bind(controlY);

                    curveTo.controlX2Property().bind(xCoordNext.getKey());
                    curveTo.controlY2Property().bind(controlY);

                    curveTo.xProperty().bind(xCoordNext.getKey());
                    curveTo.yProperty().bind(yCoordNext.getKey());

                    pathElements.add(curveTo);
                    break;
                }
                case l_even: {
                    LineTo lineTo = new LineTo();
                    lineTo.xProperty().bind(xCoord.getKey());
                    lineTo.yProperty().bind(yCoord.getValue());
                    pathElements.add(lineTo);
                    break;
                }
                case r_odd: {
                    CubicCurveTo curveTo = new CubicCurveTo();

                    Pair<DoubleBinding, DoubleBinding> xCoordNext = componentXCoords.get(g - 1);
                    Pair<DoubleBinding, DoubleBinding> yCoordNext = componentYCoords.get(g - 1);

                    DoubleBinding controlY = yCoord.getKey().add(yCoordNext.getValue()).divide(2.);

                    curveTo.controlX1Property().bind(xCoord.getValue());
                    curveTo.controlY1Property().bind(controlY);

                    curveTo.controlX2Property().bind(xCoordNext.getValue());
                    curveTo.controlY2Property().bind(controlY);

                    curveTo.xProperty().bind(xCoordNext.getValue());
                    curveTo.yProperty().bind(yCoordNext.getValue());

                    pathElements.add(curveTo);
                    break;
                }
                case r_even: {
                    LineTo lineTo = new LineTo();
                    lineTo.xProperty().bind(xCoord.getValue());
                    lineTo.yProperty().bind(yCoord.getKey());
                    pathElements.add(lineTo);
                    break;
                }
                default: {
                    throw new RuntimeException("should not get here");
                }
            }
        }

        p = new Path(pathElements);

    }

    public Path getPath() {
        return p;
    }

}
