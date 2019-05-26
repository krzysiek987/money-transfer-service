package pl.dorzak.transferservice

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import pl.dorzak.transferservice.transfer.TransferDao
import pl.dorzak.transferservice.transfer.model.TransferStatus
import pl.dorzak.transferservice.utils.HttpHeaders
import pl.dorzak.transferservice.utils.HttpStatus

import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TransferApiITSpec extends BaseITSpec {

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
                .uri(getTransferApiUriBuilder().queryParam('sourceAccountId', accountId).build())
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
            status == 'SUCCESS'
        }
    }

    def "should create success transfer"() {
        given:
        def requestBodyBuilder = new JsonBuilder() {}
        requestBodyBuilder {
            sourceAccountId UUID.fromString('d6896820-7f3e-11e9-b475-0800200c9a66')
            destinationAccountId UUID.fromString('f18e5e50-7f3e-11e9-b475-0800200c9a66')
            value '12.53'
            currency 'PLN'
            title 'Dummy'
        }
        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().build())
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyBuilder.toString()))
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())
        def responseBody = new JsonSlurper().parseText(response.body())

        then:
        response.statusCode() == HttpStatus.CREATED
        and:
        with(responseBody) {
            status == "SUCCESS"
            title == 'Dummy'
        }
        and:
        response.headers().firstValue(HttpHeaders.LOCATION).get() == 'http://localhost:' + serverConfig.getPort() + '/1.0/transfers/' + responseBody.id
    }


    def "should reject create request without value"() {
        given:
        def requestBodyBuilder = new JsonBuilder() {}
        requestBodyBuilder {
            sourceAccountId UUID.fromString('d6896820-7f3e-11e9-b475-0800200c9a66')
            destinationAccountId UUID.fromString('f18e5e50-7f3e-11e9-b475-0800200c9a66')
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

    def "transfer should fail when negative value"() {
        given:
        def requestBodyBuilder = new JsonBuilder() {}
        requestBodyBuilder {
            sourceAccountId UUID.fromString('d6896820-7f3e-11e9-b475-0800200c9a66')
            destinationAccountId UUID.fromString('f18e5e50-7f3e-11e9-b475-0800200c9a66')
            value '-12.53'
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
            sourceAccountId UUID.fromString('d6896820-7f3e-11e9-b475-0800200c9a66')
            destinationAccountId UUID.fromString('f18e5e50-7f3e-11e9-b475-0800200c9a66')
            value '12.53'
            currency 'PLN'
            title 'Dummy'
            scheduledAt ZonedDateTime.now().plusSeconds(10).format(DateTimeFormatter.ISO_DATE_TIME)
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
            status == 'SCHEDULED'
            currency == 'PLN'
        }
    }

    def "should update scheduled transfer value and title"() {
        given:
        def accountId = UUID.randomUUID()
        UUID transferId = createTransferForAccount accountId, TransferStatus.SCHEDULED, ZonedDateTime.now().plusSeconds(5)

        def requestBodyBuilder = new JsonBuilder() {}
        requestBodyBuilder {
            status 'SCHEDULED'
            value '999.53'
            title 'Dummy'
            scheduledAt ZonedDateTime.now().plusSeconds(10).format(DateTimeFormatter.ISO_DATE_TIME)
        }

        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().pathSegment(transferId).build())
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_JSON)
                .PUT(HttpRequest.BodyPublishers.ofString(requestBodyBuilder.toString()))
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())
        println response.body()

        then:
        response.statusCode() == HttpStatus.OK
        and:
        with(new JsonSlurper().parseText(response.body())) {
            value == BigDecimal.valueOf(999.53)
        }
    }

    def "should accept delete request and return 204 HTTP status"() {
        given:
        UUID transferId = createTransferForAccount UUID.randomUUID(), TransferStatus.SCHEDULED

        def httpRequest = HttpRequest.newBuilder()
                .uri(getTransferApiUriBuilder().pathSegment(transferId).build())
                .DELETE()
                .build()

        when:
        def response = httpClient.send(httpRequest, BodyHandlers.ofString())

        then:
        response.statusCode() == HttpStatus.NO_CONTENT
    }

    def "should reject delete for SUCCESS transaction"() {
        given:
        UUID transferId = createTransferForAccount UUID.randomUUID(), TransferStatus.SUCCESS

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


    private def createTransferForAccount(final UUID sourceAccountId, final TransferStatus status = TransferStatus.SUCCESS, ZonedDateTime scheduledAt = null) {
        return jdbi.withExtension(TransferDao.class, {
            it
                    .insertTransfer(status, sourceAccountId, UUID.randomUUID(), BigDecimal.TEN, Currency.getInstance('GBP'), null, Instant.now(), scheduledAt)
        })
    }
}
