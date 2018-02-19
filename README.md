# kittens
grails - how to view 5,000,000 kittens

KITTENS -- grails 2.3.11 and java 1.7

While trying to build some code that allowed me to
paginagte a large set of items using grails, I ran
into some weird problems with ordering/filtering
these items.

Here is an example of all the failures and weird
things I ran across, along with a possible solution.


Domain Objects:

      Building                          People
      --------                          ------  
    String zipCode     has many      String name


                                     has many


       Breed                           Kittens
     -----------                      ---------
    String descrip                  String name
                                    Breed breed



There are 10,000 building.
Each building has 100 people.
Each person has 0-5 kittens.


There will be 10 Breeds of cats.


The goal is to show a search page that lists all
kittens, and allow searching/sorting/paging.


Must be able to search by buildling zipcode, person name,
kitten name and the breed.

The results better be paginated. (That's a lot of kittens).
