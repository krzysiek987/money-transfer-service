package pl.dorzak.transferservice

import com.typesafe.config.ConfigFactory
import org.jdbi.v3.core.Jdbi
import pl.dorzak.transferservice.config.JdbiFactory
import pl.dorzak.transferservice.config.ServerConfig
import pl.dorzak.transferservice.transfer.ScheduledTransfersProcessor
import pl.dorzak.transferservice.transfer.TransferAPIImpl
import pl.dorzak.transferservice.utils.UriBuilder
import spock.lang.Specification

import java.net.http.HttpClient

/**
 * Base class for TransferService integration tests.
 * It provides common utilities and sets up a test context.
 */
abstract class BaseITSpec extends Specification {

    protected ServerConfig serverConfig
    protected TransferService service
    protected HttpClient httpClient
    protected TransferAPIImpl transferAPI
    protected Jdbi jdbi
    protected ScheduledTransfersProcessor scheduledTransfersProcessor

    def setup() {
        def config = ConfigFactory.load()
        serverConfig = ServerConfig.builder()
                .port(getAnyAvailablePort())
                .host('localhost')
                .build()
        jdbi = new JdbiFactory().create(config)
        transferAPI = new TransferAPIImpl(jdbi)

        scheduledTransfersProcessor = new ScheduledTransfersProcessor(transferAPI)
        service = new TransferService(serverConfig, transferAPI, scheduledTransfersProcessor)
        httpClient = HttpClient.newBuilder().build()
    }

    def cleanup() {
        if (scheduledTransfersProcessor != null) {
            scheduledTransfersProcessor.stop()
        }
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
