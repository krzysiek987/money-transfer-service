package pl.dorzak.transferservice.transfer

import com.typesafe.config.ConfigFactory
import org.jdbi.v3.core.Handle
import pl.dorzak.transferservice.config.JdbiFactory
import pl.dorzak.transferservice.transfer.model.Transfer
import pl.dorzak.transferservice.transfer.model.TransferStatus
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class TransferDaoSpec extends Specification {

    @Shared
    @AutoCleanup
    private Handle handle
    @Shared
    private TransferDao dao

    def setupSpec() {
        def jdbi = new JdbiFactory().create(ConfigFactory.load())
        handle = jdbi.open()
        handle.attach(TransferDao.class)

        dao = handle.attach(TransferDao)

        dao.insertTransferWithId(UUID.fromString("31a17d04-741f-4b3d-af78-e5be263743a1"), TransferStatus.SUCCESS,
                UUID.fromString("64daa53d-10c9-4f1b-92c4-63c543823da6"),
                UUID.fromString("436ad3d0-f9f8-480c-8350-27314179bd54"), BigDecimal.valueOf(15.01),
                Currency.getInstance("PLN"), null, Instant.now(), null)
        dao.insertTransferWithId(UUID.fromString("400a020a-b8f4-467c-8337-763ec892aeee"), TransferStatus.SUCCESS,
                UUID.fromString("64daa53d-10c9-4f1b-92c4-63c543823da6"),
                UUID.fromString("436ad3d0-f9f8-480c-8350-27314179bd54"), BigDecimal.valueOf(100.0),
                Currency.getInstance("PLN"), null, Instant.now(), null)
        dao.insertTransferWithId(UUID.fromString("beeaedce-12b8-4739-8904-bb5c53acc32c"), TransferStatus.SCHEDULED,
                UUID.fromString("64daa53d-10c9-4f1b-92c4-63c543823da6"),
                UUID.fromString("cbd807b1-98b6-4f68-9168-110c60988d8a"), BigDecimal.valueOf(2.50),
                Currency.getInstance("PLN"), null, Instant.now(), null)
        dao.insertTransferWithId(UUID.fromString("1d0f8741-aeb8-4dd6-9faf-f7289636a042"), TransferStatus.SUCCESS,
                UUID.fromString("6aa1dabd-d3f7-4209-a76e-13eb58fe3a82"),
                UUID.fromString("84e10d96-d746-488e-854a-1a834ce5cdc2"), BigDecimal.valueOf(1.53),
                Currency.getInstance("PLN"), null, Instant.now(), null)
    }

    def "findBySourceAccountId should return filtered transfers"() {
        given:
        def accountId = UUID.fromString('64daa53d-10c9-4f1b-92c4-63c543823da6')
        def expectedTransferIds = ['31a17d04-741f-4b3d-af78-e5be263743a1', '400a020a-b8f4-467c-8337-763ec892aeee', 'beeaedce-12b8-4739-8904-bb5c53acc32c']
        when:

        def transfers = dao.findTransfersBySourceAccountId(accountId)
        then:
        transfers.size() == 3
        and:
        transfers.collect { it.id.toString() } == expectedTransferIds

    }

    def "findById should return transfer with proper value"() {
        given:
        def transferId = UUID.fromString('400a020a-b8f4-467c-8337-763ec892aeee')
        when:
        def transfer = dao.findById(transferId)
        then:
        transfer.isPresent()
        and:
        transfer.get().value == BigDecimal.valueOf(100.0)
    }

    def "should insert transfer and generate it's id"() {
        given:
        def transfer = Transfer.builder()
                .status(TransferStatus.SUCCESS)
                .sourceAccountId(UUID.fromString('b8cca210-eaa4-4a7b-91d6-36195a2f2106'))
                .destinationAccountId(UUID.fromString('42aa7c50-05da-4a13-952c-ebc8cb12dbac'))
                .value(BigDecimal.valueOf(1.10))
                .currency(Currency.getInstance('USD'))
                .createdAt(Instant.ofEpochMilli(1558808555000L))
                .build()

        when:
        def newTransferId = dao.insertTransfer(transfer.status, transfer.sourceAccountId, transfer.destinationAccountId,
                transfer.value, transfer.currency, transfer.title, transfer.createdAt, transfer.scheduledAt)
        transfer.id = newTransferId

        then:
        newTransferId != null
        and:
        dao.findById(newTransferId).get() == transfer

        cleanup:
        dao.deleteById(newTransferId)
    }

    def "should update transfer"() {
        given:
        def transferId = UUID.fromString('beeaedce-12b8-4739-8904-bb5c53acc32c')
        def existingTransfer = dao.findById(transferId).get()
        def newScheduledAt = ZonedDateTime.now().plusDays(1)
        def transfer = existingTransfer.toBuilder()
                .scheduledAt(newScheduledAt.truncatedTo(ChronoUnit.MILLIS))
                .value(BigDecimal.valueOf(10.0))
                .build()

        when:
        dao.updateTransfer(transfer.title, transfer.value, transfer.scheduledAt, transfer.id)

        then:
        dao.findById(transferId).get() == transfer

        cleanup:
        dao.updateTransfer(existingTransfer.title, existingTransfer.value, existingTransfer.scheduledAt, existingTransfer.id)
    }

    def "deleteById "() {
        given:
        def transferId = UUID.fromString('1d0f8741-aeb8-4dd6-9faf-f7289636a042')
        def existingTransfer = dao.findById(transferId)

        expect:
        existingTransfer.isPresent()

        when:
        dao.deleteById(transferId)

        then:
        !dao.findById(transferId).isPresent()

        cleanup:
        def toRestore = existingTransfer.get()
        dao.insertTransferWithId(toRestore.id, toRestore.status, toRestore.sourceAccountId, toRestore.destinationAccountId,
                toRestore.value, toRestore.currency, toRestore.title, toRestore.createdAt, toRestore.scheduledAt)
    }

}
