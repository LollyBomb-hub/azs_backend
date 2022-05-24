package map.implementation.backend.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Polygon {

    private List<List<Float>> _latlngs;

    public Pair<Float, Float> getCenter() {
        float xCenter = 0.F;
        float yCenter = 0.F;

        for (List<Float> latlng: _latlngs) {
            xCenter += latlng.get(0);
            yCenter += latlng.get(1);
        }

        xCenter /= _latlngs.size();
        yCenter /= _latlngs.size();

        return new Pair<>(xCenter, yCenter);
    }

    public Float intersectsPolarRadialLine(float angle, float xOfRadialLine, float yOfRadialLine) {
        // angle in degrees from xOfRadialLine, yOfRadialLine
        // angle by def: tg((y - yOfRadialLine) / (x - xOfRadialLine))

        double k = Math.atan(Math.toRadians(angle));

        // y(x) = k * (x - xOfRadialLine) + yOfRadialLine

        int size = _latlngs.size();

        Float closestX = null;
        Integer closestIndex = null;
        Float closestK = null;

        for (int indexOfCoordinate = 0; indexOfCoordinate < size - 1; indexOfCoordinate++) {
            List<Float> xY0 = _latlngs.get(indexOfCoordinate);
            List<Float> xY1 = _latlngs.get(indexOfCoordinate + 1);
            float x0 = xY0.get(0);
            float y0 = xY0.get(1);

            float x1 = xY1.get(0);
            float y1 = xY1.get(1);

            x0 -= xOfRadialLine;
            y0 -= yOfRadialLine;

            x1 -= xOfRadialLine;
            y1 -= yOfRadialLine;

            // Если они параллельны или совпадают, то выполняется равенство
            // (x0 - x1) * (yOfRadialLine - y3) - (y0 - y1) * (xOfRadialLine - x3) = 0
            // За значение x3 и y3 возьмём значение прямой в x1
            float x3 = x1;
            float y3 = (float) (k * (x3 - xOfRadialLine) + yOfRadialLine);

            if ((x0 - x1) * (yOfRadialLine - y3) - (y0 - y1) * (xOfRadialLine - x3) != 0) {
                // Intersection
                float xMin = Math.min(x0, x1);
                if (closestX == null) {
                    closestIndex = indexOfCoordinate;
                    closestX = xMin;
                    closestK = (x0 - x1) / (y0 - y1);
                } else if (closestX > xMin) {
                    closestIndex = indexOfCoordinate;
                    closestX = xMin;
                    closestK = (x0 - x1) / (y0 - y1);
                }
            }
        }

        // check for last points
        {
            List<Float> xY0 = _latlngs.get(size - 1);
            List<Float> xY1 = _latlngs.get(0);

            float x0 = xY0.get(0);
            float y0 = xY0.get(1);

            float x1 = xY1.get(0);
            float y1 = xY1.get(1);

            x0 -= xOfRadialLine;
            y0 -= yOfRadialLine;

            x1 -= xOfRadialLine;
            y1 -= yOfRadialLine;

            // Если они параллельны или совпадают, то выполняется равенство
            // (x0 - x1) * (yOfRadialLine - y3) - (y0 - y1) * (xOfRadialLine - x3) = 0
            // За значение x3 и y3 возьмём значение прямой в x1
            float x3 = x1;
            float y3 = (float) (k * (x3 - xOfRadialLine) + yOfRadialLine);

            if ((x0 - x1) * (yOfRadialLine - y3) - (y0 - y1) * (xOfRadialLine - x3) != 0) {
                // Intersection
                float xMin = Math.min(x0, x1);
                if (closestX == null) {
                    closestIndex = size - 1;
                    closestX = xMin;
                    closestK = (x0 - x1) / (y0 - y1);
                } else if (closestX > xMin) {
                    closestIndex = size - 1;
                    closestX = xMin;
                    closestK = (x0 - x1) / (y0 - y1);
                }
            }
        }

        if (closestIndex == null) {
            return null;
        }

        List<Float> coordinates = _latlngs.get(closestIndex);

        float x = findXOfIntersection(coordinates.get(0), coordinates.get(1), closestK, xOfRadialLine, yOfRadialLine, (float) k);

        return (float) k * (x - xOfRadialLine) + yOfRadialLine;
    }

    private float findXOfIntersection(float x0, float y0, float k0, float x1, float y1, float k1) {
        // f(x) = k0 * (x - x0) + y0
        // g(x) = k1 * (x - x1) + y1
        // f(x) = g(x)
        //
        // k0 * (x - x0) + y0 = k1 * (x - x1) + y1
        //
        // k0 * x - k0 * x0 + y0 = k1 * x - k1 * x1 + y1
        //
        // x * (k0 - k1) = k0 * x0 + y1 - k1 * x1 - y0
        // x = (k0 * x0 + y1 - k1 * x1 - y0) / (k0 - k1)

        return (k0 * x0 + y1 - k1 * x1 - y0) / (k0 - k1);
    }

    public static Polygon fromFunctionOfAngle(List<Pair<Float, Float>> function, float latitude, float longitude) {
        // returns Polygon with center at latitude, longitude
        if (function == null || function.size() == 0) {
            return null;
        }

        List<List<Float>> latlngs = new ArrayList<>();

        for (Pair<Float, Float> radiusFromAngle: function) {
            float angle = (float) Math.toRadians(radiusFromAngle.getFirst());
            float radius = radiusFromAngle.getSecond();

            float x = (float) (radius * Math.cos(angle));
            float y = (float) (radius * Math.sin(angle));

            latlngs.add(List.of(x, y));
        }

        return new Polygon(latlngs);
    }
}
