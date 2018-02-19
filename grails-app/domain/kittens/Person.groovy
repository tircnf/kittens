package kittens

class Person {

    static constraints = {
    }

    String name

    static belongsTo = [building: Building]
    static hasMany   = [kittens: Kitten]

}
