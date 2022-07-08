package com.example.boorbashorderservice;

import com.boorbash.interfaces.menu.Menu;
import com.boorbash.interfaces.menu.MenuDivision;
import com.boorbash.interfaces.menu.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@RestController
public class OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private DataSource dataSource;

    private static final Menu SAMPLE_RESULT = Menu.of(
            List.of(
                    MenuDivision.of(
                            List.of(
                                    MenuItem.of(
                                        "Mushroom Burger",
                                           "A friggin Mushroom Burger.",
                                           new BigDecimal("15.99"),
                                           ""
                                    ),
                                    MenuItem.of(
                                            "Loaded Fries",
                                            "Fries with a whole bunch of other stuff.",
                                            new BigDecimal("12.99"),
                                            ""
                                    )),
                            "Main Dishes"
                    ),
                    MenuDivision.of(
                            List.of(
                                    MenuItem.of(
                                            "Fries",
                                            "Well... Fries.",
                                            new BigDecimal("4.99"),
                                            ""
                                    )
                            ),
                            "Sides"
                    )
            )
    );

    @GetMapping("getMenu")
    public Menu getMenu(@RequestParam("restaurantId") int restaurantId) throws SQLException {
        LOGGER.debug("Searching restaurant ID: " + restaurantId);
//        Connection con = this.dataSource.getConnection();
//        PreparedStatement stmt = con.prepareStatement(
//                "select division from menu_entry where restaurant_id=?"
//        );
//        stmt.setString(1, restaurantId);

        return SAMPLE_RESULT;


    }
}
