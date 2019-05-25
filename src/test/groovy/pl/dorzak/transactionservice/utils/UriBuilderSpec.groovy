package pl.dorzak.transactionservice.utils

import spock.lang.Specification

class UriBuilderSpec extends Specification {

    def "should build uri properly"(final String scheme, final String host, final Integer port, final List<String> pathSegments, final Map<String, String> queryParams, String expectedResult) {
        expect:
        createUriBuilder(scheme, host, port, pathSegments, queryParams).build().toString() == expectedResult

        where:

        scheme  | host                 | port  | pathSegments      | queryParams                      || expectedResult
        'https' | 'google.com'         | null  | null              | null                             || 'https://google.com'
        'http'  | 'localhost'          | 9092  | null              | null                             || 'http://localhost:9092'
        'http'  | 'd1352sa.domain.com' | 80    | List.of('a', 'b') | null                             || 'http://d1352sa.domain.com:80/a/b'
        'http'  | '127.0.0.1'          | null  | null              | Map.of('p1', '123', 'p2', 'asd') || 'http://127.0.0.1?p1=123&p2=asd'
        'http'  | 'some.domain.com'    | 27017 | List.of('path')   | Map.of('userId', '632',)         || 'http://some.domain.com:27017/path?userId=632'

    }

    def createUriBuilder(final String scheme, final String host, final Integer port, final List<String> pathSegments, final Map<String, String> queryParams) {

    }

}
