package pl.dorzak.transferservice


import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class ITSmokeSpec extends BaseITSpec {

    def "should expose api on given port"() {
        given:
        def httpRequest = HttpRequest.newBuilder()
                .uri(getServerUrBuilder().pathSegment("health").build())
                .build()
        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == 200
    }

}
