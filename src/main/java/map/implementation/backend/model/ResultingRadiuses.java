package map.implementation.backend.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ResultingRadiuses {

    private float firstRadius;
    private float secondRadius;
    private float thirdRadius;
    private float fourthRadius;
    private float fifthRadius;

    private Polygon firstRadiusShape;
    private Polygon secondRadiusShape;
    private Polygon thirdRadiusShape;
    private Polygon fourthRadiusShape;
    private Polygon fifthRadiusShape;

}
