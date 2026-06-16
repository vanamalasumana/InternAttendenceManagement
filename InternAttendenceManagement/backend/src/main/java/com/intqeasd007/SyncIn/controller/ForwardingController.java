package com.intqeasd007.SyncIn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForwardingController {

    // Capture everything that isn't an explicit API endpoint or asset file 
    @RequestMapping(value = "{path:^(?!api|public)[^\\.]*}")
    public String redirectInternalSPA() {
        // Forward back into Angular's index pipeline loop securely
        return "forward:/index.html";
    }
}