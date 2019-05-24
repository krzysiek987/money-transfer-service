package pl.dorzak.transactionservice


import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class TransactionServiceSmokeSpec extends BaseTransactionServiceSpec {

    def "should expose api on given port"() {
        given:
        def httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(getServerUrl() + "/" + "health"))
                .build()
        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == 200
    }

}
