package pl.dorzak.transactionservice

import com.typesafe.config.ConfigFactory
import pl.dorzak.transactionservice.config.DataSourceFactory
import pl.dorzak.transactionservice.config.ServerConfig
import pl.dorzak.transactionservice.transfer.TransferModule
import pl.dorzak.transactionservice.utils.UriBuilder
import spock.lang.Specification

import java.net.http.HttpClient

/**
 * Base class for TransactionService specs.
 * It provides common utilities and sets up a test context.
 */
abstract class BaseTransactionServiceSpec extends Specification {

    protected ServerConfig serverConfig
    protected TransactionService service
    protected HttpClient httpClient

    def setup() {
        def config = ConfigFactory.load()
        serverConfig = ServerConfig.builder()
                .port(getAnyAvailablePort())
                .build()
        def dataSource = new DataSourceFactory().createDataSource(config)
        def transferModule = new TransferModule(dataSource)
        service = new TransactionService(serverConfig, transferModule)
        service.start()

        httpClient = HttpClient.newBuilder().build()
    }

    def cleanup() {
        if (service == null) {
            throw new NullPointerException('Something went wrong. Service is null which is unexpected')
        }
        service.stop()
    }

    def getServerUrBuilder() {
        return new UriBuilder('http', 'localhost', serverConfig.getPort())
    }

    def getAnyAvailablePort() {
        return new ServerSocket(0).withCloseable { socket -> socket.getLocalPort() }
    }
}
