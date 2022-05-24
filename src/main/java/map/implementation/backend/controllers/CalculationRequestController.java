package map.implementation.backend.controllers;

import lombok.extern.slf4j.Slf4j;
import map.implementation.backend.model.Request;
import map.implementation.backend.model.ResultingRadiuses;
import map.implementation.backend.services.CalculationService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class CalculationRequestController {

    private CalculationService calculationService;

    public CalculationRequestController(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @PostMapping("/calculate")
    public ResultingRadiuses processRequest(@RequestBody(required = false) Request request) {
        if (request != null) {
            log.info(request.toString());
            return calculationService.calculateRadius(request);
        }
        return null;
    }

}
