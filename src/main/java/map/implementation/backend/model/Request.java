package map.implementation.backend.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Request {

    private float parameter;
    private float building_lat;
    private float building_lon;
    private String type;
    private List<Circle> circles;
    private List<Polygon> polygons;

}
