package kittens

class KittenController {

    static responseFormats=['json','xml']

    def sessionFactory

    // search command, sorting and filtering properties.
    //kittenName
    //personName
    //buildingZip
    //breed


    def index() {
        def tests=[]

        tests << "max=10&offset=5"
        tests << "max=10&offset=5&kittenName=2"
        tests << "max=10&offset=5&kittenName=2&breed=3"
        tests << "max=10&offset=5&kittenName=2&breed=3&personName=1"
        tests << "max=10&offset=5&kittenName=2&breed=3&personName=1&sort=name"
        tests << "max=10&offset=5&kittenName=2&breed=3&personName=1&sort=name,personName"



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
                        tests.each {t->
                            a(href:"./${searchInfo[1]}.xml?$t") {
                                pre("curl http://localhost:8080/kittens/kitten/${searchInfo[1]}?$t")
                            }
                        }
                    }
                }
            }
        }
    }


    def where(KittenSearchCommand ks) {

        sessionFactory.settings.sqlStatementLogger.logToStdout=true

        try {

        log.info("using where query  ${ks.properties}")

        def pagedResult=Kitten.where {
            if (ks.kittenName) {
                name =~ "%${ks.kittenName}%"
            }

            person {
                if (ks.personName) {
                    name =~ "%${ks.personName}%"
                }

                building {
                    if (ks.buildingZip) {
                        zipCode =~ "%${ks.buildingZip}%"
                    }
                }
            }

            breed {
                if (ks.breed) {
                    description =~ "%${ks.breed}%"
                }
            }

            ks.sortList.each {sort ->
                if ("personName".equals(sort)) {
                    // adding this causes an extra query to run...
                    //  a count of patients.. no idea why.
                    //  and the order by doesn't work.
                    //
                    //person {
                    //    order("whatisthisthing")
                    //}
                } else {
                    order(sort)
                }
            }

        }.list(max: ks.max, offset: ks.offset)


        def result=pagedResult.collect {
            [id: it.id, name: it.name, breed: it.breed.description, person: it.person.name, building: it.person.building.zipCode]
        }

        respond ([total: pagedResult.getTotalCount(), data: result])

        } finally {
            sessionFactory.settings.sqlStatementLogger.logToStdout=false

        }
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
