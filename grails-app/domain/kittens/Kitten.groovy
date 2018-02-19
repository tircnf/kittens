package kittens

class Kitten {

    static constraints = {
    }

    static belongsTo = [person: Person]

    String name
    Breed breed
}
