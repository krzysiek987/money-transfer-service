package pl.dorzak.transactionservice

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import pl.dorzak.transactionservice.utils.HttpHeaders
import pl.dorzak.transactionservice.utils.HttpStatus

import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.ZoneId
import java.time.ZonedDateTime

class TransferApiSpec extends BaseTransactionServiceSpec {

    private static final String TRANSFER_API = "transfers"
    private static final String API_VERSION = "1.0"

    def "should return all transfers by account"() {
        given:
        def accountId = UUID.randomUUID()
        def otherAccountId = UUID.randomUUID()
        createTransferForAccount accountId
        createTransferForAccount accountId
        createTransferForAccount accountId
        createTransferForAccount otherAccountId

        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().queryParam('account_id', accountId).build())
                .GET()
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.OK
        and:
        (new JsonSlurper().parseText(response.body()) as List).size() == 3
    }

    def "should return specific transfer by id"() {
        given:
        def accountId = UUID.randomUUID()
        def transferId = createTransferForAccount accountId

        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().pathSegment(transferId).build())
                .GET()
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.OK
        and:
        with(new JsonSlurper().parseText(response.body())) {
            status == 'PENDING'
            title == 'something'
        }
    }

    def "should create pending transfer"() {
        given:
        def requestBodyBuilder = new JsonBuilder() {}
        requestBodyBuilder {
            sourceAccountId UUID.randomUUID()
            destinationAccountId UUID.randomUUID()
            amount '12.53'
            currency 'USD'
            title 'Dummy'
        }
        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().build())
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyBuilder.toString()))
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.CREATED
        and:
        with(new JsonSlurper().parseText(response.body())) {
            status == "PENDING"
        }
        and:
        response.headers().firstValue(HttpHeaders.LOCATION).get().startsWith getServerUrl() + "/" + TRANSFER_API
    }

    def "should reject create request without amount"() {
        given:
        def requestBodyBuilder = new JsonBuilder() {}
        requestBodyBuilder {
            sourceAccountId UUID.randomUUID()
            destinationAccountId UUID.randomUUID()
            currency 'USD'
            title 'Dummy'
        }
        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().build())
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyBuilder.toString()))
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.BAD_REQUEST
    }

    def "transfer should fail for incorrect account"() {
        given:
        def requestBodyBuilder = new JsonBuilder() {}
        requestBodyBuilder {
            sourceAccountId UUID.randomUUID()
            destinationAccountId UUID.randomUUID()
            amount '12.53'
            currency 'USD'
            title 'Dummy'
        }
        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().build())
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyBuilder.toString()))
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.BAD_REQUEST
    }

    def "should create scheduled transfer"() {
        given:
        def requestBodyBuilder = new JsonBuilder() {}
        requestBodyBuilder {
            sourceAccountId UUID.randomUUID()
            destinationAccountId UUID.randomUUID()
            amount '12.53'
            currency 'USD'
            title 'Dummy'
            scheduleAt ZonedDateTime.of(2019, 5, 24, 23, 0, 0, 0, ZoneId.of("Europe/Warsaw"))
        }
        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().build())
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyBuilder.toString()))
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.CREATED
        and:
        with(new JsonSlurper().parseText(response.body())) {
            status == "SCHEDULED"
        }
    }

    def "should update scheduled transfer amount"() {
        given:
        UUID transferId = createTransferForAccount UUID.randomUUID(), 'SCHEDULED'

        def requestBodyBuilder = new JsonBuilder() {}
        requestBodyBuilder {
            sourceAccountId accountId
            destinationAccountId UUID.randomUUID()
            amount '999.53'
            currency 'USD'
            title 'Dummy'
            scheduleAt ZonedDateTime.of(2019, 5, 24, 23, 0, 0, 0, ZoneId.of("Europe/Warsaw"))
        }

        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().pathSegment(transferId).build())
                .PUT(HttpRequest.BodyPublishers.ofString(requestBodyBuilder.toString()))
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.OK
        and:
        with(new JsonSlurper().parseText(response.body())) {
            amount == '999.53'
        }
    }

    def "should patch scheduled transfer amount"() {
        given:
        UUID transferId = createTransferForAccount UUID.randomUUID(), 'SCHEDULED'

        def requestBodyBuilder = new JsonBuilder() {}
        requestBodyBuilder {
            amount '999.53'
        }

        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().pathSegment(transferId).build())
                .PUT(HttpRequest.BodyPublishers.ofString(requestBodyBuilder.toString()))
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.OK
        and:
        with(new JsonSlurper().parseText(response.body())) {
            amount == '999.53'
        }
    }

    def "should allow to delete failed transfer"() {
        given:
        UUID transferId = createTransferForAccount UUID.randomUUID(), 'FAILED'

        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().pathSegment(transferId).build())
                .DELETE()
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.NO_CONTENT
    }

    def "should allow to delete scheduled transfer"() {
        given:
        UUID transferId = createTransferForAccount UUID.randomUUID(), 'SCHEDULED'

        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().pathSegment(transferId).build())
                .DELETE()
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.NO_CONTENT
    }

    def "should not allow to delete completed transfer"() {
        given:
        UUID transferId = createTransferForAccount UUID.randomUUID(), 'COMPLETED'

        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().pathSegment(transferId).build())
                .DELETE()
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.FORBIDDEN
    }

    def "should not allow to delete pending transfer"() {
        given:
        UUID transferId = createTransferForAccount UUID.randomUUID(), 'COMPLETED'

        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().pathSegment(transferId).build())
                .DELETE()
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.FORBIDDEN
    }

    private def getTransferApiUriBuilder() {
        return getServerUrBuilder().pathSegment(API_VERSION).pathSegment(TRANSFER_API)
    }


    private def createTransferForAccount(final UUID sourceAccountId, final String status = 'PENDING') {
        return UUID.randomUUID()
    }
}
