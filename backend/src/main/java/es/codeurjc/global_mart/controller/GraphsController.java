package es.codeurjc.global_mart.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.codeurjc.global_mart.dto.Product.CompanyStadsDTO;
import es.codeurjc.global_mart.dto.User.HistoricalOrdersDTO;
import es.codeurjc.global_mart.service.ProductService;
import es.codeurjc.global_mart.service.UserService;

@Controller
public class GraphsController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @GetMapping("/displayGraphs")
    public String displayGraph(Model model, Authentication autentication) {

        Object principal = autentication.getPrincipal();
        if (principal instanceof OAuth2User oAuth2User) {
            model.addAttribute("username", oAuth2User.getAttribute("name"));
        }
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {

            try {

                List<CompanyStadsDTO> dataList = productService.getCompanyStadistics(userDetails.getUsername());
                model.addAttribute("dataList", dataList);
                return "companyGraphs";

            } catch (NoSuchElementException e) {
                return "error";
            }
        }

        return "error";

    }

    @GetMapping("/showUserGraphic")
    public String displayUserGraph(Model model, Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String userName = null;

        if (principal instanceof OAuth2User oAuth2User) {
            userName = oAuth2User.getAttribute("name");
        } else if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            userName = userDetails.getUsername();
        }

        try {
            if (userName != null) {
                model.addAttribute("username", userName);
                HistoricalOrdersDTO orderPrices = userService.getUserStads(userName);
                System.out.println("order prices: " + orderPrices);
                model.addAttribute("orderPrices", orderPrices.historicalOrdersPrices());
            } else {
                System.out.println("Error: No se pudo obtener el nombre de usuario.");
                return "error";
            }
        } catch (Exception e) {
            return "error";
        }

        return "userGraph";
    }

}
