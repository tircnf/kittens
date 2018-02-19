package kittens

class Building {

    static constraints = {
    }

    String zipCode

    static hasMany = [people: Person]
}
