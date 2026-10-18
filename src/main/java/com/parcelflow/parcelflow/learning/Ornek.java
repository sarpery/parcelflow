package com.parcelflow.parcelflow.learning;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Ornek {

    record Car(String type, String make, String model, int engineCapacity){

    }

    public static void main(String[] args) {
        List<Car> cars = List.of( (new Car("sedan","BMW","530",1998)),
                new Car("sedan","Audi","A5", 1998),
                new Car("sedan","Mercedes","E-Class", 2500),
                new Car("hatchback","Skoda","Octavia", 1600),
                new Car("sedan","Audi","A5", 1998),
                new Car("sedan","Toyota","TypeR", 1450)
        );

         //cars.stream().filter(car -> car.type.equals("sedan")).forEach(System.out::println);
        List list = cars.stream().filter(car -> car.type.equals("sedan")).flatMap(car -> Stream.of(car.make, car.model)).toList();
        System.out.println(list);
    }
}
