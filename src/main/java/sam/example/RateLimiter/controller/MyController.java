package sam.example.RateLimiter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    @GetMapping("/myEndpoint")
    public String myEndpoint() {
        return "Request allowed";
    }
}

