import kittens.*

class BootStrap {

    def init = { servletContext ->
        log.info("creating 10 breeds")

        def breeds=[]

        if (Breed.count()==0) {
            10.times {
                breeds << new Breed(description: "Breed $it").save(failOnError:true, flush:true)
            }
        } else {
            breeds=Breed.list()
        }
    }
    def destroy = {
    }
}
