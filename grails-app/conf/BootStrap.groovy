import kittens.*

class BootStrap {

    def init = { servletContext ->


        def breedCount=75
        def buildingCount=250
        def personBuildingCount=20
        def kittenPerPerson=5

        def counter=0;


        log.info("creating $breedCount breeds")

        def breeds=[]

        if (Breed.count()==0) {
            breedCount.times {
                breeds << new Breed(description: "Breed $it").save(failOnError:true, flush:true)
            }
        } else {
            breeds=Breed.list()
        }


        log.info("Creating $buildingCount buildings");
        buildingCount.times { bc ->
            Building b=new Building(zipCode: "787$bc")

            personBuildingCount.times { pc ->
                def person=new Person(name: "person $pc  building $bc -- ${counter++}")
                b.addToPeople(person);

                kittenPerPerson.times { kc ->
                    def kitten=new Kitten(name: "kitten  $kc   person $pc -- ${counter++}", breed:breeds[counter%breedCount])
                    person.addToKittens(kitten)
                }
            }

            b.save(flush:true, failOnError:true);
            if (bc%20==0) {
                log.info("building $bc done.")
            }

            b.discard()
        }

        log.info("Done... transaction has to finish.")



    }
    def destroy = {
    }
}
