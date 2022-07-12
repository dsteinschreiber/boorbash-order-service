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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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


    // Best practice once optimized (good for big query) :

    @GetMapping("getMenu")
    public Menu getMenu(@RequestParam("restaurantId") int restaurantId) throws SQLException {
        LOGGER.debug("Searching restaurant ID: " + restaurantId);
        Connection con = this.dataSource.getConnection();

        PreparedStatement stmt = con.prepareStatement(
                "select dish_name, division, dish_description, price, dish_pic_url " +
                        "from menu_entry where restaurant_id=? order by division"
        );

        stmt.setInt(1, restaurantId);
        ResultSet rs = stmt.executeQuery();

        Collection<MenuDivision> menuDivisions = new ArrayList<>();
        Collection<MenuItem> menuItems = new ArrayList<>();

        String lastDivision = "";

        while (rs.next()) {
            String dishName = rs.getString(1);
            String division = rs.getString(2);
            String dishDescription = rs.getString(3);
            BigDecimal price = rs.getBigDecimal(4);
            String dishPicUrl = rs.getString(5);

            if (lastDivision.equals("")) {
                lastDivision = division;
            }

            if (!lastDivision.equals(division)) {
                menuDivisions.add(MenuDivision.of(menuItems, lastDivision));
                menuItems = new ArrayList<>();
                lastDivision = division;
            }

            menuItems.add(MenuItem.of(dishName, dishDescription, price, dishPicUrl));

        }

        menuDivisions.add(MenuDivision.of(menuItems, lastDivision));

        stmt.close();
        con.close();

        return Menu.of(menuDivisions);
    }


    // Long query (too many calls to database, don't ever do this!) :

    @GetMapping("getMenuFirstRun")
    public Menu getMenuFirstRun(@RequestParam("restaurantId") int restaurantId) throws SQLException {
        LOGGER.debug("Searching restaurant ID: " + restaurantId);
        Connection con = this.dataSource.getConnection();


        PreparedStatement stmt1 = con.prepareStatement(
                "select distinct division from menu_entry where restaurant_id=?"

        );

        stmt1.setInt(1, restaurantId);
        ResultSet rs1 = stmt1.executeQuery();

        Collection<MenuDivision> menuDivisions = new ArrayList<>();

        while (rs1.next()) {
            String division = rs1.getString(1);

            PreparedStatement stmt2 = con.prepareStatement(
                    "select dish_name, dish_description, price, dish_pic_url from menu_entry where division=?"
            );

            stmt2.setString(1, division);
            ResultSet rs2 = stmt2.executeQuery();

            Collection<MenuItem> menuItems = new ArrayList<>();

            while (rs2.next()) {
                String dishName = rs2.getString(1);
                String dishDescription = rs2.getString(2);
                BigDecimal dishPrice = rs2.getBigDecimal(3);
                String disPicUrl = rs2.getString(4);

                menuItems.add(MenuItem.of(dishName, dishDescription, dishPrice, disPicUrl));
            }

            menuDivisions.add(MenuDivision.of(menuItems, division));

            stmt2.close();
        }

        stmt1.close();
        con.close();

        return Menu.of(menuDivisions);
    }


    // Using java.util.Map to build menu (have this be your first move, and usually last) :

    @GetMapping("getMenuThirdRun")
    public Menu getMenuThirdRun(@RequestParam("restaurantId") int restaurantId) throws SQLException {
        LOGGER.debug("Searching restaurant ID: " + restaurantId);
        Connection con = this.dataSource.getConnection();

        PreparedStatement stmt = con.prepareStatement(
                "select dish_name, division, dish_description, price, dish_pic_url " +
                        "from menu_entry where restaurant_id=?"
        );

        stmt.setInt(1, restaurantId);
        ResultSet rs = stmt.executeQuery();

        Map<String, Collection<MenuItem>> menuMap = new HashMap<>();

        while (rs.next()) {
            String dishName = rs.getString(1);
            String division = rs.getString(2);
            String dishDescription = rs.getString(3);
            BigDecimal price = rs.getBigDecimal(4);
            String dishPicUrl = rs.getString(5);

            // using merge:

            menuMap.merge(division,
                    new ArrayList<>(List.of(MenuItem.of(dishName, dishDescription, price, dishPicUrl))),
                    (oldValue, value) -> {
                        oldValue.add(MenuItem.of(dishName, dishDescription, price, dishPicUrl));
                        return oldValue;
                    }
            );


            // using get (once) and put:

//            Collection<MenuItem> menuItems = menuMap.get(division);
//
//            if (menuItems != null) {
//                menuItems.add(MenuItem.of(dishName, dishDescription, price, dishPicUrl));
//            } else {
//                menuMap.put(division,
//                        new ArrayList<>(List.of(MenuItem.of(dishName, dishDescription, price, dishPicUrl))));
//            }


            // using get (twice) and put:

//            if (menuMap.containsKey(division)) {
//                menuMap.get(division).add(MenuItem.of(dishName, dishDescription, price, dishPicUrl));
//            } else {
//                menuMap.put(division,
//                        new ArrayList<>(List.of(MenuItem.of(dishName, dishDescription, price, dishPicUrl))));
//            }
        }

        Collection<MenuDivision> menuDivisions = new ArrayList<>();

        for (String divisionName : menuMap.keySet()) {
            menuDivisions.add(MenuDivision.of(menuMap.get(divisionName), divisionName));
        }

        stmt.close();
        con.close();

        return Menu.of(menuDivisions);
    }

}
