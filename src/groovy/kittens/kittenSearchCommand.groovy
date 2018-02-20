package kittens

@grails.validation.Validateable
class KittenSearchCommand {

    Integer max=100
    Integer offset=0
    String sort=""

    // filtering (wildcard) values.
    String kittenName="";
    String personName="";
    String buildingZip="";
    String breed="";



    void setMax(Integer max) {
        if (max<1) {
            this.max=100
        } else if (max>1000) {
            this.max=1000
        } else {
            this.max=max
        }
    }

    List<String> getSortList() {
        def sortList=[]
        sort.split(",").each {field->
            field=field.trim();
            if (field) {
                sortList.add(field)
            }
        }

        sortList.add("id")

        return sortList
    }


}
