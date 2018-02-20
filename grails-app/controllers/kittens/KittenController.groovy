package kittens

class KittenController {

    static responseFormats=['json','xml']

    // search command, sorting and filtering properties.
    //kittenName
    //personName
    //buildingZip
    //breed


    def test1="max=10&offset=100"
    def test2="max=10&offset=100&kittenName=2"


    def theTest=test1


    def index() {
        render {
            html {
                body {
                    p("Welcome to Kitten Search")
                    a(href:"http://thecatapi.com") {
                        delegate.img(src:"http://thecatapi.com/api/images/get?format=src&type=gif")
                    }
                    p("Try a basic where query")
                    a(href:"./where?max=10&offset=100") {
                        pre("curl http://localhost:8080/kittens/kitten/where?$theTest")
                    }
                }
            }
        }
    }


    def where(KittenSearchCommand ks) {

        log.info("index called");
        log.info("ks = ${ks.properties}");

        def result=Kitten.where {

        }.list(max: ks.max, offset: ks.offset).collect {
            [id: it.id, name: it.name, breed_id: it.breed.id, breed: it.breed.description, person: it.person.name, building: it.person.building.zipCode]
        }

        respond ([total: result.totalCount, data: result])
    }
}
