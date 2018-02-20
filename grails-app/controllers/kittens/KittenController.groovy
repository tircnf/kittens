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

        def searches=[
            ["A basic where query",  "where"],
            ["A basic Finder",       "finder"],
            ["Detached Criteria",    "detachedCriteria"],
            ["Criteria Query",       "criteria"],
            ["HQL Query",            "hql"],
            ["Plain old Sql",        "sql"],
        ]

        render {
            html {
                body {
                    p("Welcome to Kitten Search")
                    a(href:"http://thecatapi.com") {
                        delegate.img(src:"http://thecatapi.com/api/images/get?format=src&type=gif")
                    }

                    searches.each { searchInfo->
                        hr("")
                        p("${searchInfo[0]}")
                        a(href:"./${searchInfo[1]}.xml?max=10&offset=100") {
                            pre("curl http://localhost:8080/kittens/kitten/${searchInfo[1]}?$theTest")
                        }
                    }
                }
            }
        }
    }


    def where(KittenSearchCommand ks) {

        log.info("using where query  ${ks.properties}")

        def pagedResult=Kitten.where {

        }.list(max: ks.max, offset: ks.offset)


        def result=pagedResult.collect {
            [id: it.id, name: it.name, breed_id: it.breed.id, breed: it.breed.description, person: it.person.name, building: it.person.building.zipCode]
        }

        respond ([total: pagedResult.getTotalCount(), data: result])
    }

    def finder(KittenSearchCommand ks) {
        log.info("using finder  ${ks.properties}")

        respond ([total: 0, data: []]);
    }


    def detachedCriteria(KittenSearchCommand ks) {
        log.info("using detachedCriteria  ${ks.properties}")

        respond ([total: 0, data: []]);
    }


    def criteria(KittenSearchCommand ks) {
        log.info("using criteria  ${ks.properties}")

        respond ([total: 0, data: []]);
    }


    def hql(KittenSearchCommand ks) {
        log.info("using hql  ${ks.properties}")

        respond ([total: 0, data: []]);
    }


    def sql(KittenSearchCommand ks) {
        log.info("using sql  ${ks.properties}")

        respond ([total: 0, data: []]);
    }

}
