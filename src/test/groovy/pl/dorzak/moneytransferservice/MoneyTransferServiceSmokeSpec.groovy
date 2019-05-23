package pl.dorzak.moneytransferservice

import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class MoneyTransferServiceSmokeSpec extends Specification {

    def "should expose api on given port"() {
        given:
        def serverConfig = ServerConfig.builder()
                .port(getAnyAvailablePort())
                .build()
        def service = new MoneyTransferService(serverConfig)
        def httpClient = HttpClient.newBuilder().build()
        def httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + serverConfig.getPort()))
                .build()

        when:
        service.start()

        then:
        httpClient.send(httpRequest, BodyHandlers.ofString()).statusCode() == 200

        cleanup:
        service.stop()

    }

    int getAnyAvailablePort() {
        return new ServerSocket(0).withCloseable { socket -> socket.getLocalPort() }
    }
}
