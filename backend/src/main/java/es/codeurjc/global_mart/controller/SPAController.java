package es.codeurjc.global_mart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SPAController {
    @GetMapping({"/new", "/new/", "/new/**/{path:[^\\.]*}", "/{path:new[^\\.]*}"})
    public String redirectAngularRoutes() {
        return "forward:/new/index.html";
    }
}
