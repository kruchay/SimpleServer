package Main.Handlers.Http;

enum HTTP_ERROR {

    E400("400 Bad Request"),
    E403("403 Forbidden"),
    E404("404 Not Found"),
    E405("405 Method Not Allowed"),
    E408("408 Request Timeout"),
    E488("488 Resource Error"),
    E501("501 Not Implemented");

    private String desc;

    HTTP_ERROR (String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }

}
