package com.github.zottaa.binarytree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserFactory {
    public ArrayList<String> getTypeNameList() {
        return new ArrayList<>(Arrays.asList("Point", "Fraction"));
    }

    public UserType getBuilderByName(String name) {
        switch (name) {
            case "Point":
                return new Point();
            case "Fraction":
                return new Fraction();
            default:
                return null;
        }
    }
}
