package kittens

class Kitten {

    static constraints = {
    }

    static belongsTo = [person: Person]

    String name
    Breed breed

    String toString() {
        return "name: $name     breed: $breed"
    }
}
