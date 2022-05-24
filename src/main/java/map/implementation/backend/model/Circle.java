package map.implementation.backend.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Circle {

    private float _x0;
    private float _y0;
    private float _r;

    public Float intersectsPolarRadialLine(float angle, float x1, float y1) {
        // If exists solution for equation
        // r^2 + 2 * r * r_0 * cos (angle - theta) + (r_0) ^ 2 = a^2;
        // Where r_0, theta - polar coordinates of circle center
        // a - its radius
        // r, angle - polar space metrics

        float dx = _x0 - x1;
        float dy = _y0 - y1;

        float theta = (float) Math.atan(dx / dy);
        float r_0 = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

        float relativeB = (float) (2 * r_0 * Math.cos(Math.toRadians(angle) - theta));

        float discriminant = (float) (Math.pow(relativeB, 2) - 4 * (r_0 * r_0 - _r * _r));

        if (discriminant < 0)
            return null;

        float r1 = (float) ((-relativeB + Math.sqrt(discriminant)) / 2);
        float r2 = (float) ((-relativeB - Math.sqrt(discriminant)) / 2);

        return Math.abs(r1) < Math.abs(r2) ? r1 : r2;
    }
}
