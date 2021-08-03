package de.predic8.pgopmon;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping(value = "/health", produces = "text/plain")
    public String ready() {
        return "OK\n";
    }
}
