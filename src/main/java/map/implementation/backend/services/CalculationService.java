package map.implementation.backend.services;

import map.implementation.backend.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CalculationService {

    private final float RADIUS_CONSTANT = 6_371_000; // In meters. WGS84
    private final float STEP = 1F;

    public ResultingRadiuses calculateRadius(Request request) {
        ResultingRadiuses result = new ResultingRadiuses();

        float massP;
        float latitude = request.getBuilding_lat();
        float longitude = request.getBuilding_lon();

        switch(request.getType()) {
            case "mass":
                // Дана исходная масса
                massP = request.getParameter();
                break;
            case "volumeH":
                // В. СУГ
                massP = (float) (request.getParameter() * 0.62);
                break;
            case "volumeL":
                // Н. СУГ
                massP = (float) (request.getParameter() * 0.34);
                break;
            default:
                throw new IllegalStateException("Invalid type of media");
        }

        float cubicRootOfMassP = (float) Math.cbrt(massP);

        // Препятствия с фронта
        List<Circle> circles = request.getCircles();
        List<Polygon> polygons = request.getPolygons();

        // Вычисляем радиусы
        float R1 = 32 * cubicRootOfMassP;
        float R2 = 45 * cubicRootOfMassP;
        float R3 = 64 * cubicRootOfMassP;
        float R4 = 120 * cubicRootOfMassP;
        float R5 = 240 * cubicRootOfMassP;

        // Готово. Теперь надо обработать препятствия

        result.setFirstRadius(R1);
        result.setSecondRadius(R2);
        result.setThirdRadius(R3);
        result.setFourthRadius(R4);
        result.setFifthRadius(R5);

        result.setFirstRadiusShape(processObstacles(latitude, longitude, 0, R1, circles, polygons));
        result.setFirstRadiusShape(processObstacles(latitude, longitude, R1, R2, circles, polygons));
        result.setFirstRadiusShape(processObstacles(latitude, longitude, R2, R3, circles, polygons));
        result.setFirstRadiusShape(processObstacles(latitude, longitude, R3, R4, circles, polygons));
        result.setFirstRadiusShape(processObstacles(latitude, longitude, R4, R5, circles, polygons));

        return result;
    }

    private float distanceInMetersBetweenGeoPoints(float latitude0, float longitude0, float latitude1, float longitude1) {

        // Converting to radians
        latitude0 = (float) Math.toRadians(latitude0);
        latitude1 = (float) Math.toRadians(latitude1);
        longitude0 = (float) Math.toRadians(longitude0);
        longitude1 = (float) Math.toRadians(longitude1);

        // Calculating distance
        float deltaPHI = (latitude0 - latitude1) / 2;
        float deltaLambda = (longitude0 - longitude1) / 2;
        double sinDeltaPHI = Math.sin(deltaPHI);
        double sinDeltaLambda = Math.sin(deltaLambda);
        return (float) (2 * RADIUS_CONSTANT * Math.asin(Math.sqrt(sinDeltaPHI + Math.cos(latitude0) * Math.cos(latitude1) * sinDeltaLambda))); // In meters
    }

    private float distanceInMetersBetweenGeoPointAndCircle(float latitude, float longitude, Circle circle) {
        return distanceInMetersBetweenGeoPoints(latitude, longitude, circle.get_x0(), circle.get_y0());
    }

    private float distanceInMetersBetweenGeoPointAndPolygon(float latitude, float longitude, Polygon polygon) {
        Pair<Float, Float> center = polygon.getCenter();
        return distanceInMetersBetweenGeoPoints(latitude, longitude, center.getFirst(), center.getSecond());
    }

    private Polygon processObstacles(float latitude, float longitude, float fromObservableRadius, float toObservableRadius, List<Circle> circleObstacles, List<Polygon> polygonObstacles) {

        // All obstacles between radiuses
        circleObstacles = circleObstacles.stream().filter(circle ->
        {
            float distance = distanceInMetersBetweenGeoPointAndCircle(latitude, longitude, circle);
            return fromObservableRadius <= distance && distance <= toObservableRadius;
        }).collect(Collectors.toList());

        polygonObstacles = polygonObstacles.stream().filter(polygon -> {
            float distance = distanceInMetersBetweenGeoPointAndPolygon(latitude, longitude, polygon);
            return fromObservableRadius <= distance && distance <= toObservableRadius;
        }).collect(Collectors.toList());

        // Ищем ближайшую точку

        List<Pair<Float, Float>> radiusFromAngle = new ArrayList<>();

        float angle = 0.F;
        while (angle < 360.) {

            // angle is responsible for radial line
            float bestR = toObservableRadius;

            for (Circle circle: circleObstacles) {
                Float betterRadius = circle.intersectsPolarRadialLine(angle, latitude, longitude);
                if (betterRadius != null && bestR > betterRadius) {
                    bestR = betterRadius;
                }
            }

            for (Polygon polygon: polygonObstacles) {
                Float betterRadius = polygon.intersectsPolarRadialLine(angle, latitude, longitude);
                if (betterRadius != null && bestR > betterRadius) {
                    bestR = betterRadius;
                }
            }

            radiusFromAngle.add(new Pair<>(angle, bestR));

            angle += STEP;
        }

        return Polygon.fromFunctionOfAngle(radiusFromAngle, latitude, longitude);
    }

}
