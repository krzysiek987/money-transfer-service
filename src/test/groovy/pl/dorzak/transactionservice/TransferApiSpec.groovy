package pl.dorzak.transactionservice


import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import pl.dorzak.transactionservice.utils.HttpHeaders

import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class TransferApiSpec extends BaseTransactionServiceSpec {

    public static final String TRANSFER_API = "transfers"

    def "should return all transfers by account"() {

    }

    def "should return specific transfer by id"() {

    }

    def "should create pending transfer"() {
        given:
        def requestBodyBuilder = new JsonBuilder()
        requestBodyBuilder.amount {
            currency "USD"
            value "12.53"
        }
        requestBodyBuilder.source {
            iban "PL24685784379569696034358924"
        }
        requestBodyBuilder.destination {
            iban "PL50699066626738542673910758"
        }
        def httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(getServerUrl() + "/" + TRANSFER_API))
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyBuilder.toString()))
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == 201
        and:
        with(new JsonSlurper().parseText(response.body())) {
            status == "PENDING"
        }
        and:
        response.headers().firstValue(HttpHeaders.LOCATION).get().startsWith getServerUrl() + "/" + TRANSFER_API
    }

    def "should reject create request without amount"() {

    }

    def "transfer should fail for incorrect account"() {

    }

    def "should create scheduled transfer"() {

    }

    def "should update scheduled transfer amount"() {

    }

    def "should patch scheduled transfer amount"() {

    }

    def "should allow to delete failed transfer"() {

    }

    def "should allow to delete scheduled transfer"() {

    }

    def "should not allow to delete completed transfer"() {

    }

    def "should not allow to delete pending transfer"() {

    }
}
