class BootStrap {

    def init = { servletContext ->
        log.info("Bootstrap running")
    }
    def destroy = {
    }
}
