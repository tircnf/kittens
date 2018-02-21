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
        sessionFactory.settings.sqlStatementLogger.logToStdout=true
        def tests=[]

        tests << "max=10&offset=5"
        tests << "max=10&offset=5&kittenName=2"
        tests << "max=10&offset=5&kittenName=2&breed=3"
        tests << "max=10&offset=5&kittenName=2&breed=3&personName=1"
        tests << "max=10&offset=5&kittenName=2&breed=3&personName=1&sort=name"
        tests << "max=10&offset=5&kittenName=2&breed=3&personName=1&sort=name,personName"
        tests << "max=10&offset=5&kittenName=2&breed=3&personName=1&sort=breed,name,personName"
        tests << "max=10&offset=5&kittenName=2&breed=3&personName=1&sort=buildingZip,breed,name,personName"



        def searches=[
            [
                "A basic Finder",
                "finder",
                "Finders are tough.  I don't know how to write a finder that filters on associations. So it fails on test 3 when we try and filter on breed"
            ],
            [
                "A basic where query",
                "where",
                "fails once you try to sort on properties not in the kitten object.  So this fails on test 6."
            ],
            [
                "Detached Criteria",
                "detachedCriteria",
                "Very similar to where queries, these work until you try and sort on a property not on the kitten object."
            ],
            [
                "Criteria Query",
                "criteria",
                "This guy works and passes all tests.  The syntax is a bit ugly, having to build a nested closure for sorting, but that sure beats not working at all."
            ],
            [
                "HQL Query",
                "hql",
                """This one works as well as the Criteria query.  The sorting code looks better, but the combining of sql statment into where clauses is hard.
                And the fact that you can't send in a map with extra parameters to the sql command kind of sucks."""
                ],
            [
                "Plain old Sql",
                "sql",
                "should work like HQL.  But you don't get the database independence."
            ],
        ]

        render {
            html {
                body {
                    div {
                        a(style: "float:right;", href:"http://thecatapi.com") {
                            delegate.img(height: "200px", src:"http://thecatapi.com/api/images/get?format=src&type=gif")
                        }


                        p("Welcome to Kitten Search")
                                            searches.each { searchInfo->
                            hr("")
                            p("${searchInfo[0]}")
                            p("${searchInfo[2]}")
                            ol {
                                tests.each {t->
                                    li {
                                        a(href:"./${searchInfo[1]}.xml?$t") {
                                            pre("curl http://localhost:8080/kittens/kitten/${searchInfo[1]}?$t")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    def finder(KittenSearchCommand ks) {
        log.info("using finder  ${ks.properties}")

        def total=Kitten.count();
        def list=Kitten.list(max:ks.max, offset: ks.offset).collect {

            new KittenDTO([id: it.id, name: it.name, breed: it.breed.description, person: it.person.name, building: it.person.building.zipCode])

        }

        // these dynamic finders are not really meant to be built programatically.
        // for instance, to find by breed would need two queries.

        //  def b=Breed.findAllByDescriptionLike("%1%")
        //  def list=Kittens.findAllByBreedIn(b, [max: ks.max, offset: ks.offset])
        //  this is definitely the wrong tool for the job.

        respond ([total: total, data: list]);
    }



    def where(KittenSearchCommand ks) {

        // this guy works great until you want to add a dynamic sort to a property that isn't on Kitten.
        // it his compile time checks for property name.  (if the lhs of a comparison isn't a property, you get
        // a compile time error).
        //

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
                    //    order("name")
                    //}
                    throw new RuntimeException("Can't sort by person")
                } else {
                    order(sort)   // this doesn't cause an error, but doesn't do anything for non kitten properties.
                }
            }

        }.list(max: ks.max, offset: ks.offset)


        def result=pagedResult.collect {
            new KittenDTO([id: it.id, name: it.name, breed: it.breed.description, person: it.person.name, building: it.person.building.zipCode])
        }

        respond ([total: pagedResult.getTotalCount(), data: result])

    }


    def detachedCriteria(KittenSearchCommand ks) {
        log.info("using detachedCriteria  ${ks.properties}")

        // this is almost the identical implementation of the where query, but you don't get
        // compile time checks.  and have to use the hibernate syntax.
        //
        //  instead of    name = "foo"
        //  you use       eq ("name","foo")
        //

        def crit=new grails.gorm.DetachedCriteria(Kitten).build {

            createAlias("person","_p_")
            if (ks.kittenName) {
                ilike "name", "%${ks.kittenName}%"
            }

            person {
                if (ks.personName) {
                    ilike "name", "%${ks.personName}%"
                }

                building {
                    if (ks.buildingZip) {
                        ilike "zipCode", "%${ks.buildingZip}%"
                    }
                }
            }

            breed {
                if (ks.breed) {
                    ilike "description" , "%${ks.breed}%"
                }
            }

            ks.sortList.each {sort ->
                if ("personName".equals(sort)) {

                    // adding the alias, and trying to use it
                    // throws a "can't find property _p_ on kitten"
                    // the alias above doesn't work.
                    //order("_p_.name")
                    //order("person_alias1.name")

                    person {
                    //    Order(order("foo")) // causes error, buy might be able to trick it somehow.
                        order("name") // does nothing.
                    }
                    throw new RuntimeException("Can't sort by person")
                } else {
                    order(sort)   // this doesn't cause an error, but doesn't do anything for non kitten properties.
                }
            }
        }

        def pagedResult=crit.list(max:ks.max, offset: ks.offset)

        def result=pagedResult.collect {
            new KittenDTO([id: it.id, name: it.name, breed: it.breed.description, person: it.person.name, building: it.person.building.zipCode])
        }

        respond ([total: pagedResult.totalCount, data: result]);
    }


    def criteria(KittenSearchCommand ks) {
        log.info("using criteria  ${ks.properties}")

        def crit=Kitten.createCriteria()
        def pagedResult=crit.list(max:ks.max, offset: ks.offset) {

            if (ks.kittenName) {
                ilike "name", "%${ks.kittenName}%"
            }

            person {
                if (ks.personName) {
                    ilike "name", "%${ks.personName}%"
                }

                building {
                    if (ks.buildingZip) {
                        ilike "zipCode", "%${ks.buildingZip}%"
                    }
                }
            }

            breed {
                if (ks.breed) {
                    ilike "description" , "%${ks.breed}%"
                }
            }


            ks.sortList.each {sort ->
                if ("personName".equals(sort)) {
                    // little bit hacky here.  have to expand all the nodes
                    person {
                        order("name")
                    }
                } else if ("breed".equals(sort)) {
                    //order("b.description")
                    breed {
                        order("description")
                    }
                } else if ("buildingZip".equals(sort)) {
                    person {
                        building {
                            order("zipCode")
                        }
                    }
                } else {
                    order(sort)   // this doesn't cause an error, but doesn't do anything for non kitten properties.
                }
            }
        }


        def result=pagedResult.collect {
            new KittenDTO([id: it.id, name: it.name, breed: it.breed.description, person: it.person.name, building: it.person.building.zipCode])
        }

        respond ([total: pagedResult.totalCount, data: result]);
    }

    def hql(KittenSearchCommand ks) {
        log.info("using hql  ${ks.properties}")

        def select="new kittens.KittenDTO(id, name, breed.description, person.name, person.building.zipCode)"

        def where =["1=1"]
        def whereMap=[:]
        def orderBy=[]

        ks.sortList.each {sort ->
            if ("personName".equals(sort)) {
                orderBy << 'person.name'
            } else if ("breed".equals(sort)) {
                orderBy << 'breed.description'
            } else if ("buildingZip".equals(sort)) {
                orderBy << 'person.building.zipCode'
            } else {
                orderBy << sort
            }
        }
        if (ks.kittenName) {
            whereMap.kittenName=ks.kittenName
            where << "name like concat('%',:kittenName,'%')"
        }

        if (ks.personName) {
            whereMap.personName=ks.personName
            where << "person.name like concat('%',:personName,'%')"
        }
        if (ks.buildingZip) {
            whereMap.buildingZip=ks.buildingZip
            where << "person.building.zipCode like concat('%',:buildingZip,'%')"
        }
        if (ks.breed) {
            whereMap.breed=ks.breed
            where << "breed.description like concat('%',:breed,'%')"
        }


        def hql="select $select from Kitten where ${where.join(" and ")} order by ${orderBy.join(",")}"

        def countHql="select count(*) from Kitten where ${where.join(" and ")}"

        // unluckily, executeQuery with named params can't take a map with extra data files in it.
        // i was hoping to send in ks.properties
        // but executeQuery complain of unmapped param "sort".
        // so the if statements above have to add the item to the map if they add the variable to the query.
        // this is ugly hard to read. :(

        def total=Kitten.executeQuery(countHql,whereMap)
        def result=Kitten.executeQuery(hql,whereMap,[max:ks.max, offset: ks.offset])

        respond ([total: total, data: result]);
    }


    def sql(KittenSearchCommand ks) {
        log.info("using sql  ${ks.properties}")

        respond ([total: 0, data: []]);
    }

}

class KittenDTO {
    Integer id
    String name
    String breed
    String person
    String building

    // this constructor is required if you still want to be able to
    // construct using parameter map.
    KittenDTO() {
    }


    KittenDTO(id, name, breed, person, building) {
        this.id=id
        this.name=name
        this.breed=breed
        this.person=person
        this.building=building
    }

}
