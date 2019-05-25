package pl.dorzak.transferservice

import com.typesafe.config.ConfigFactory
import pl.dorzak.transferservice.config.DataSourceFactory
import pl.dorzak.transferservice.config.ServerConfig
import pl.dorzak.transferservice.transfer.TransferAPI
import pl.dorzak.transferservice.utils.UriBuilder
import spock.lang.Specification

import java.net.http.HttpClient

/**
 * Base class for TransferService specs.
 * It provides common utilities and sets up a test context.
 */
abstract class BaseTransferServiceSpec extends Specification {

    protected ServerConfig serverConfig
    protected TransferService service
    protected HttpClient httpClient

    def setup() {
        def config = ConfigFactory.load()
        serverConfig = ServerConfig.builder()
                .port(getAnyAvailablePort())
                .build()
        def dataSource = new DataSourceFactory().createDataSource(config)
        def transferModule = new TransferAPI(dataSource)
        service = new TransferService(serverConfig, transferModule)
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
